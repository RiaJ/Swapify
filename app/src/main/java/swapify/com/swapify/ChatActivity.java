package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2015-11-13.
 */
public class ChatActivity extends Activity{
    private static final int MAX_MESSAGES = 50;

    private static final String TAG = "ChatActivity";

    private static String userId;

    public static final String USER_ID_KEY = "userId";

    private EditText messageField;
    private Button sendButton;

    private ListView chatListView;
    private List<Message> messageArrayList;
    private ChatListAdapter chatListAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    private boolean mFirstLoad;

    // Create a handler which can run code periodically
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ParseObject.registerSubclass(Message.class);

        //TODO:set userId here...

        handler.postDelayed(runnable, 100);
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
        messageField = (EditText) findViewById(R.id.etMessage);
        sendButton = (Button) findViewById(R.id.btSend);
        chatListView = (ListView) findViewById(R.id.lvChat);
        messageArrayList = new ArrayList<Message>();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        chatListView.setTranscriptMode(1);
        mFirstLoad = true;
        chatListAdapter = new ChatListAdapter(ChatActivity.this, userId, messageArrayList);
        chatListView.setAdapter(chatListAdapter);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String body = messageField.getText().toString();
                // Use Message model to create new messages now
                Message message = new Message();
                message.setUserId(userId);
                message.setBody(body);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        receiveMessage();
                    }
                });
                messageField.setText("");
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void receiveMessage() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_MESSAGES);
        query.orderByAscending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    messageArrayList.clear();
                    messageArrayList.addAll(messages);
                    chatListAdapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (mFirstLoad) {
                        chatListView.setSelection(chatListAdapter.getCount() - 1);
                        mFirstLoad = false;
                    }
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }
}
