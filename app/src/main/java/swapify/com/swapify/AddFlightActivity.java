package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AddFlightActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        findViewById(R.id.save_flight_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}
