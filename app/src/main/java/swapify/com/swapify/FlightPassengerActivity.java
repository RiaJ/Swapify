package swapify.com.swapify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

        setupFlightInfo();

        passengerInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), ChatActivity.class);
                i.putExtra("userTwo", passengerInfoList.get(position).get(0));

                startActivity(i);
            }
        });
    }

    private void setupFlightInfo() {
        passengerInfoListView = (ListView) findViewById(R.id.passengersListView);
        passengerInfoList = new ArrayList<>();

        passengerInfoListView.setTranscriptMode(1);
        flightPassengerListAdapter = new FlightPassengerAdapter(FlightPassengerActivity.this, passengerInfoList);
        passengerInfoListView.setAdapter(flightPassengerListAdapter);

        final String currentFlightNo = getIntent().getStringExtra("FlightNo");

        ParseQuery query = new ParseQuery("FlightInfo");
        query.whereEqualTo("flightNo", currentFlightNo);
        query.findInBackground(new FindCallback<FlightInfo>() {
            public void done(List<FlightInfo> flightInfos, ParseException e) {
                if (e == null) {
                    FlightInfo currentFlightInfo = flightInfos.get(0);
                    String myId = ParseUser.getCurrentUser().getObjectId();
                    List<List<String>> seats = currentFlightInfo.getSeats();
                    List<List<String>> filteredSeats = filterSeats(seats, myId);
                    // add current flight no
                    for (List<String> seat : filteredSeats) {
                        seat.add(currentFlightNo);
                    }
                    // add my own seat number
                    String mySeat = "";
                    for (List<String> seat : seats) {
                        if (seat.get(0).equals(myId)) {
                            mySeat = seat.get(1);
                            break;
                        }
                    }
                    for (List<String> seat : filteredSeats) {
                        seat.add(mySeat);
                    }
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
