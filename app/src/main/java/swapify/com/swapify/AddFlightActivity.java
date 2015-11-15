package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.SaveCallback;

public class AddFlightActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        findViewById(R.id.save_flight_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText flightNoText = (EditText) findViewById(R.id.edit_flight_no);
                String flightNo = flightNoText.getText().toString();
                EditText planeTypeText = (EditText) findViewById(R.id.edit_plane_type);
                String planeType = planeTypeText.getText().toString();
                EditText seatNoText = (EditText) findViewById(R.id.edit_seat_no);
                String seatNo = seatNoText.getText().toString();

                FlightInfo flightInfo = new FlightInfo();
                flightInfo.setFlightNo(flightNo);
                flightInfo.setPlaneType(planeType);
                flightInfo.setSeatNo(seatNo);
                flightInfo.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        
                    }
                });
            }
        });
    }
}
