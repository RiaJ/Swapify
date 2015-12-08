package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {
    private static final int MAX_MESSAGES = 50;

    private static final String TAG = "ChatActivity";

    public static final String USER_ID_KEY = "userId";

    private EditText messageField;
    private Button sendButton;

    private ListView chatListView;
    private List<List<String>> messageArrayList;
    private ChatListAdapter chatListAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    private boolean mFirstLoad;

    // Create a handler which can run code periodically
    private Handler handler = new Handler();

    private static String userOne = "";
    private static String userTwo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer_list_factory);

        ParseObject.registerSubclass(Chat.class);

        View navDrawerView = getLayoutInflater().inflate(
                R.layout.activity_navigation_drawer_list_factory, null);
        FrameLayout mainContentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
        View chatView = getLayoutInflater().inflate(R.layout.activity_chat, null);
        mainContentFrame.addView(chatView);
        ListView navDrawerList = (ListView) findViewById(R.id.nav_drawer);
        NavigationDrawerListFactory navDrawerListFactory =
                new NavigationDrawerListFactory(navDrawerList, navDrawerView.getContext(), this);

        setupMessagePosting();
        handler.postDelayed(runnable, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // Defines a runnable which is run every 100ms
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            handler.postDelayed(this, 100);
        }
    };

    private void refreshMessages() {
        receiveMessage();
    }

    // Setup message field and posting
    private void setupMessagePosting() {
        userTwo = getIntent().getStringExtra("userTwo");
        userOne = ParseUser.getCurrentUser().getObjectId();

        messageField = (EditText) findViewById(R.id.messageField);
        sendButton = (Button) findViewById(R.id.sendButton);
        chatListView = (ListView) findViewById(R.id.chatListView);
        messageArrayList = new ArrayList<List<String>>();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        chatListView.setTranscriptMode(1);
        mFirstLoad = true;
        chatListAdapter = new ChatListAdapter(ChatActivity.this, userOne, messageArrayList);
        chatListView.setAdapter(chatListAdapter);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String body = messageField.getText().toString();
                // Use Message model to create new messages now

                //check if chat exits
                ParseQuery query = new ParseQuery("Chat");
                query.whereEqualTo("combinedUserId", combineUserIds(userOne, userTwo));
                query.findInBackground(new FindCallback<Chat>() {
                    public void done(List<Chat> chats, ParseException e) {
                        if (e == null) {
                            Chat chat;
                            List<List<String>> messagesSoFar;
                            if (chats.isEmpty()) {
                                chat = new Chat();
                                chat.setUserOneId(userOne);
                                chat.setUserTwoId(userTwo);
                                chat.setCombinedUserId(combineUserIds(userOne, userTwo));
                                messagesSoFar = new ArrayList<List<String>>();
                            } else {
                                chat = chats.get(0);
                                messagesSoFar = chat.getMessages();
                                if (messagesSoFar == null)
                                    messagesSoFar = new ArrayList<List<String>>();
                            }
                            List<String> newMessage = new ArrayList<String>(2);
                            newMessage.add(userOne);
                            newMessage.add(body);
                            messagesSoFar.add(newMessage);

                            chat.setMessages(messagesSoFar);
                            chat.saveInBackground();
                        } else {
                            Log.d("message", "Error: " + e.getMessage());
                        }
                    }
                });
                messageField.setText("");
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void receiveMessage() {
        // Construct query to execute
        ParseQuery query = new ParseQuery("Chat");
        query.whereEqualTo("combinedUserId", combineUserIds(userOne, userTwo));

        query.findInBackground(new FindCallback<Chat>() {
            public void done(List<Chat> chats, ParseException e) {
                if (e == null) {
                    Chat currentChat = new Chat();
                    List<List<String>> messagesSoFar;
                    if (chats.isEmpty()) {
                        messagesSoFar = new ArrayList<List<String>>();
                    } else {
                        currentChat = chats.get(0);
                        messagesSoFar = currentChat.getMessages();
                        if (messagesSoFar == null)
                            messagesSoFar = new ArrayList<List<String>>();
                    }

                    messageArrayList.clear();
                    messageArrayList.addAll(messagesSoFar);
                    chatListAdapter.notifyDataSetChanged();
                    //TODO:scroll to bottom magic
                } else {
                    Log.d("chat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private String combineUserIds(String userOneId, String userTwoId) {
        if (userOneId.compareTo(userTwoId) < 0) {
            return userOneId + userTwoId;
        } else {
            return userTwoId + userOneId;
        }
    }
}
