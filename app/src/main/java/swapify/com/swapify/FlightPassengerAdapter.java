package swapify.com.swapify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FlightPassengerAdapter extends ArrayAdapter<List<String>> {
    public FlightPassengerAdapter(Context context, List<List<String>> passengers) {
        super(context, 0, passengers);
        ParseObject.registerSubclass(SwapRequest.class);
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
                    new AsyncUploadImage(holder.profileImg).
                            execute(objects.get(0).getString("profileImg"));

                    holder.swapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final String userOneId = ParseUser.getCurrentUser().getObjectId();
                            String key = "";
                            if (userOneId.compareTo(userId) < 0) {
                                key = userOneId + userId + flight;
                            } else {
                                key = userId + userOneId + flight;
                            }
                            final String reqKey = key;
                            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            ParseQuery query = new ParseQuery("SwapRequest");
                            query.whereEqualTo("requestKey", key);
                            query.findInBackground(new FindCallback<SwapRequest>() {
                                public void done(List<SwapRequest> swapRequests, ParseException e) {
                                    if (e == null) {
                                        if (swapRequests.isEmpty()) {
                                            final SwapRequest request = new SwapRequest();
                                            request.setRequestKey(reqKey);
                                            request.setUserTwoId(userId);
                                            request.setUserTwoSeat(seat);
                                            request.setUserTwoFlight(flight);
                                            request.setUserOneId(userOneId);
                                            request.setUserOneFlight(flight);
                                            request.setUserOneSeat(passengerInfo.get(3));
                                            request.saveInBackground();
                                            builder.setMessage(R.string.save_request);
                                        } else {
                                            builder.setMessage(R.string.already_exist);
                                        }
                                        builder.setPositiveButton(R.string.go_requests, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        Intent i = new Intent(v.getContext(), RequestActivity.class);
                                                        v.getContext().startActivity(i);
                                                    }
                                                })
                                                .setNegativeButton(R.string.make_req, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                })
                                                .show();
                                    } else {
                                        Log.d("message", "Error: " + e.getMessage());
                                    }
                                }
                            });
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

    public static class AsyncUploadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView iv;
        private HttpURLConnection connection;
        private InputStream is;
        private Bitmap bitmap;

        public AsyncUploadImage(ImageView mImageView) {
            iv = mImageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setUseCaches(true);
                connection.connect();
                is = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (null != result) {
                iv.setImageBitmap(result);
            } else {
                iv.setBackgroundResource(R.mipmap.ic_launcher);
            }
        }
    }
}
