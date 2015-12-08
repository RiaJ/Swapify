package swapify.com.swapify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class NavigationDrawerListFactory {
    private final static String LIST_ITEM_HOME = "Home";
    private final static String LIST_ITEM_ADD = "Add Flight";
    private final static String LIST_ITEM_FLIGHTS = "Current Flights";
    private final static String LIST_ITEM_REQUESTS = "Current Requests";

    private ImageView profilePic;
    private TextView profileName;

    public NavigationDrawerListFactory(ListView navDrawerList, final Context context,
                                       final Activity activity) {

        profilePic = (ImageView) activity.findViewById(R.id.profile_pic);
        profileName = (TextView) activity.findViewById(R.id.profile_name);
        setProfile();

        String[] listViewItems = {LIST_ITEM_HOME, LIST_ITEM_ADD,
                LIST_ITEM_FLIGHTS, LIST_ITEM_REQUESTS};
        ArrayAdapter<String> navDrawerAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, listViewItems);
        navDrawerList.setAdapter(navDrawerAdapter);

        navDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemText = (String) parent.getItemAtPosition(position);
                Intent intent;
                switch (itemText) {
                    case LIST_ITEM_HOME:
                        intent = new Intent(view.getContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        view.getContext().startActivity(intent);
                        break;
                    case LIST_ITEM_ADD:
                        intent = new Intent(view.getContext(), AddFlightActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        view.getContext().startActivity(intent);
                        break;
                    case LIST_ITEM_FLIGHTS:
                        intent = new Intent(view.getContext(), FlightActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        view.getContext().startActivity(intent);
                        break;
                    case LIST_ITEM_REQUESTS:
                        intent = new Intent(view.getContext(), RequestActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        view.getContext().startActivity(intent);
                        break;
                }
            }
        });
    }

    private void setProfile() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    String fullName = objects.get(0).getUsername();
                    Integer firstName = fullName.indexOf(" ");
                    profileName.setText(fullName.substring(0, firstName));
                    new FlightPassengerAdapter.AsyncUploadImage(profilePic).
                            execute(objects.get(0).getString("profileImg"));
                    // The query was successful.
                } else {
                    // Something went wrong.
                }
            }
        });
    }
}

