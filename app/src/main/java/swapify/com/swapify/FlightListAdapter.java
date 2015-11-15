package swapify.com.swapify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Thomas on 2015-11-15.
 */
public class FlightListAdapter extends ArrayAdapter<FlightInfo> {
    public FlightListAdapter(Context context, List<FlightInfo> flights) {
        super(context, 0, flights);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.flight_listitem, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.airlineLogoImg = (ImageView)convertView.findViewById(R.id.airlineLogo);
            holder.flightNo = (TextView)convertView.findViewById(R.id.flightNumber);
            convertView.setTag(holder);
        }
        final FlightInfo flightInfo = getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();

        Picasso.with(getContext()).load(flightInfo.getLogo()).into(holder.airlineLogoImg);
        holder.flightNo.setText(flightInfo.getFlightNo());
        return convertView;
    }

    final class ViewHolder {
        public ImageView airlineLogoImg;
        public TextView flightNo;
    }
}
