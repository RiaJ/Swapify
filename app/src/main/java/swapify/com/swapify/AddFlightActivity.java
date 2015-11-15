package swapify.com.swapify;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AddFlightActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        ParseObject.registerSubclass(FlightInfo.class);

        findViewById(R.id.save_flight_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText flightNoText = (EditText) findViewById(R.id.edit_flight_no);
                String flightNo = flightNoText.getText().toString();
                EditText equipmentText = (EditText) findViewById(R.id.edit_equipment);
                String equipment = equipmentText.getText().toString();
                EditText seatNoText = (EditText) findViewById(R.id.edit_seat_no);
                String seatNo = seatNoText.getText().toString();

                FlightInfo flightInfo = new FlightInfo();
                flightInfo.setFlightNo(flightNo);
                flightInfo.setEquipment(equipment);
                List<List<String>> seatsSoFar = flightInfo.getSeats();
                String userId = ParseUser.getCurrentUser().getObjectId();
                List<String> newSeat = new ArrayList<String>(2);
                newSeat.add(userId);
                newSeat.add(seatNo);
                seatsSoFar.add(newSeat);
                flightInfo.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            DialogFragment chooseActivityFragment =
                                    new ChooseActivityDialogFragment();
                            chooseActivityFragment.show(getSupportFragmentManager(), "chooser");
                        }
                    }
                });
            }
        });
    }

    public static class ChooseActivityDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.choose_activity)
                    .setPositiveButton(R.string.go_current, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(R.string.add_more, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(getContext(), AddFlightActivity.class);
                            startActivity(i);
                        }
                    });
            return builder.create();
        }
    }
}
