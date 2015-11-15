package swapify.com.swapify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
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
        flightInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), FlightPassengerActivity.class);
                i.putExtra("FlightNo", flightInfoArrayList.get(position).getFlightNo());

                startActivity(i);
            }
        });

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
                    flightInfoArrayList.addAll(filterFlightInfo(flightInfo, ParseUser.getCurrentUser().getObjectId()));
                    flightListAdapter.notifyDataSetChanged(); // update adapter
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }

    private List<FlightInfo> filterFlightInfo(List<FlightInfo> flightInfoList, String userId) {
        List<FlightInfo> filteredFlightInfo = new ArrayList<>();
        for (int i = 0; i < flightInfoList.size(); i++) {
            FlightInfo flightInfo = flightInfoList.get(i);
            Log.d("flightno", flightInfo.getFlightNo());
            Log.d("userId", userId);
            List<List<String>> passengerInfo = flightInfo.getSeats();
            Boolean userFound = false;
            for (int j = 0; j < passengerInfo.size(); j++) {
                if (passengerInfo.get(j).get(0).equals(userId)) {
                    userFound = true;
                    Log.d("userFound", userId);
                    break;
                }
            }
            if (userFound) {
                filteredFlightInfo.add(flightInfoList.get(i));
            }
        }
        Log.d("size filtered", Integer.toString(filteredFlightInfo.size()));
        return filteredFlightInfo;
    }
}
