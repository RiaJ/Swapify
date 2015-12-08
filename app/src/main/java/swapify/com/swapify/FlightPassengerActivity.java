package swapify.com.swapify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FlightPassengerActivity extends Activity {
    private ListView passengerInfoListView;
    private List<List<String>> passengerInfoList;
    private FlightPassengerAdapter flightPassengerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer_list_factory);
        ParseObject.registerSubclass(FlightInfo.class);

        View navDrawerView = getLayoutInflater().inflate(
                R.layout.activity_navigation_drawer_list_factory, null);
        FrameLayout mainContentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
        View passengerView = getLayoutInflater().inflate(R.layout.activity_listpassengers, null);
        mainContentFrame.addView(passengerView);
        ListView navDrawerList = (ListView) findViewById(R.id.nav_drawer);
        NavigationDrawerListFactory navDrawerListFactory =
                new NavigationDrawerListFactory(navDrawerList, navDrawerView.getContext(), this);

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

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void setupFlightInfo() {
        passengerInfoListView = (ListView) findViewById(R.id.passengersListView);
        passengerInfoList = new ArrayList<>();

        final TextView flightNoTitle = (TextView) findViewById(R.id.flight_display);

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

                    flightNoTitle.setText(currentFlightInfo.getFlightNo());

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

                    // set my seat field
                    TextView my_seat_tv = (TextView) findViewById(R.id.my_seat);
                    my_seat_tv.setText(getString(R.string.my_seat_fmt, mySeat));
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
        return passengersNotIncludingSelf;
    }
}
