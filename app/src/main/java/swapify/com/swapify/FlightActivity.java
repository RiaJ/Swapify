package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2015-11-15.
 */
public class FlightActivity extends Activity {
    private ListView flightInfoListView;
    private List<FlightInfo> flightInfoArrayList;
    private FlightListAdapter flightListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listflights);

        ParseObject.registerSubclass(FlightInfo.class);
        ParseObject.registerSubclass(Message.class);

        //TODO:set userId here...
        setupFlightInfo();
    }

    private void setupFlightInfo() {
        flightInfoListView = (ListView) findViewById(R.id.flightsListView);
        flightInfoArrayList = new ArrayList<FlightInfo>();

        flightInfoListView.setTranscriptMode(1);
        flightListAdapter = new FlightListAdapter(FlightActivity.this, flightInfoArrayList);
        flightInfoListView.setAdapter(flightListAdapter);

        retrieveFlightInfo();
    }

    private void retrieveFlightInfo() {
        // Construct query to execute
        ParseQuery<FlightInfo> query = ParseQuery.getQuery(FlightInfo.class);
        query.orderByAscending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<FlightInfo>() {
            public void done(List<FlightInfo> flightInfo, ParseException e) {
                if (e == null) {
                    flightInfoArrayList.clear();
                    flightInfoArrayList.addAll(flightInfo);
                    flightListAdapter.notifyDataSetChanged(); // update adapter
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }
}