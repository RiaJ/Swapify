package swapify.com.swapify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2015-11-15.
 */
public class FlightPassengerAdapter extends ArrayAdapter<List<String>> {
    public FlightPassengerAdapter(Context context, List<List<String>> passengers) {
        super(context, 0, passengers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.passenger_listitem, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.profileImg = (ImageView)convertView.findViewById(R.id.profileImg);
            holder.passengerName = (TextView)convertView.findViewById(R.id.passengerName);
            holder.seatNo = (TextView)convertView.findViewById(R.id.seatNumber);
            holder.swapButton = (Button)convertView.findViewById(R.id.swap_button);
            convertView.setTag(holder);
        }
        final List<String> passengerInfo = getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final String userId = passengerInfo.get(0);
        final String seat = passengerInfo.get(1);
        final String flight = passengerInfo.get(2);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    holder.passengerName.setText(objects.get(0).getUsername());
                    holder.seatNo.setText(seat);

                    holder.swapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SwapRequest request = new SwapRequest();
                            request.setUserTwoId(userId);
                            request.setUserTwoSeat(seat);
                            request.setUserTwoFlight(flight);
                            request.setUserOneId(ParseUser.getCurrentUser().getObjectId());
                            request.setUserOneFlight(flight);
                            request.setUserOneSeat(passengerInfo.get(3));
                            request.saveInBackground();
                            holder.swapButton.setEnabled(false);
                        }
                    });
                    // The query was successful.
                } else {
                    // Something went wrong.
                }
            }
        });
        return convertView;
    }

    final class ViewHolder {
        public ImageView profileImg;
        public TextView passengerName;
        public TextView seatNo;
        public Button swapButton;
    }
}
