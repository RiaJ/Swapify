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
        passengerInfoList = new ArrayList<List<String>>();

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
                    passengerInfoList.clear();
                    passengerInfoList.addAll(currentFlightInfo.getSeats());
                    flightPassengerListAdapter.notifyDataSetChanged();
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }
}
