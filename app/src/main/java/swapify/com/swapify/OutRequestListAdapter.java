package swapify.com.swapify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.DeleteCallback;
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
                    inflate(R.layout.outrequest_listitem, parent, false);
            final ViewHolder holder1 = new ViewHolder();
            holder1.name = (TextView) convertView.findViewById(R.id.request_username);
            holder1.flightNo = (TextView) convertView.findViewById(R.id.request_flight_no);
            holder1.seatNo = (TextView) convertView.findViewById(R.id.request_seat);
            convertView.setTag(holder1);
            final SwapRequest request = getItem(position);
            Log.d("out adapter", request.getUserTwoId());
            final ViewHolder holder2 = (ViewHolder)convertView.getTag();
            String userId = request.getUserTwoId();
            final String seat = request.getUserTwoSeat();
            final String flight = request.getUserTwoFlight();

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", userId);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        holder2.name.setText(objects.get(0).getUsername());
                        holder2.flightNo.setText(flight);
                        holder2.seatNo.setText(seat);
                        // The query was successful.
                    } else {
                        // Something went wrong.
                    }
                }
            });
        }
        return convertView;
    }

    final class ViewHolder {
        public TextView name;
        public TextView flightNo;
        public TextView seatNo;
    }
}
