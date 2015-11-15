package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2015-11-15.
 */
public class FlightPassengerActivity extends Activity {
    private ListView passengerInfoListView;
    private List<List<String>> passengerInfoList;
    private FlightPassengerAdapter flightPassengerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listpassengers);

        ParseObject.registerSubclass(FlightInfo.class);
        ParseObject.registerSubclass(Message.class);

        //TODO:set userId here...
        setupFlightInfo();
    }

    private void setupFlightInfo() {
        passengerInfoListView = (ListView) findViewById(R.id.passengersListView);
        passengerInfoList = new ArrayList<>();

        passengerInfoListView.setTranscriptMode(1);
        flightPassengerListAdapter = new FlightPassengerAdapter(FlightPassengerActivity.this, passengerInfoList);
        passengerInfoListView.setAdapter(flightPassengerListAdapter);

        String currentFlightNo = getIntent().getStringExtra("FlightNo");

        ParseQuery query = new ParseQuery("FlightInfo");
        query.whereEqualTo("flightNo", currentFlightNo);
        query.findInBackground(new FindCallback<FlightInfo>() {
            public void done(List<FlightInfo> flightInfos, ParseException e) {
                if (e == null) {
                    FlightInfo currentFlightInfo = flightInfos.get(0);
                    List<List<String>> filteredSeats = filterSeats(currentFlightInfo.getSeats(), ParseUser.getCurrentUser().getObjectId());
                    passengerInfoList.clear();
                    passengerInfoList.addAll(filteredSeats);
                    flightPassengerListAdapter.notifyDataSetChanged();
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }

    //remove entries where current user exists
    private List<List<String>> filterSeats(List<List<String>> seats, String userId) {
        List<List<String>> passengersNotIncludingSelf = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            if (!seats.get(i).get(0).equals(userId)) {
                passengersNotIncludingSelf.add(seats.get(i));
            }
        }
        return  passengersNotIncludingSelf;
    }
}
