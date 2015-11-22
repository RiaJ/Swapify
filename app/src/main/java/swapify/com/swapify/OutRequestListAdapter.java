package swapify.com.swapify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class OutRequestListAdapter extends ArrayAdapter<SwapRequest> {
    public OutRequestListAdapter(Context context, List<SwapRequest> swapRequests) {
        super(context, 0, swapRequests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.request_listitem, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.request_username);
            holder.flightNo = (TextView) convertView.findViewById(R.id.request_flight_no);
            holder.seatNo = (TextView) convertView.findViewById(R.id.request_seat);
            convertView.setTag(holder);
        }
        SwapRequest request = getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        String userId = request.getUserTwoId();
        final String seat = request.getUserTwoSeat();
        final String flight = request.getUserTwoFlight();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    holder.name.setText(objects.get(0).getUsername());
                    holder.flightNo.setText(flight);
                    holder.seatNo.setText(seat);
                    // The query was successful.
                } else {
                    // Something went wrong.
                }
            }
        });
        return convertView;
    }

    final class ViewHolder {
        public TextView name;
        public TextView flightNo;
        public TextView seatNo;
    }
}
