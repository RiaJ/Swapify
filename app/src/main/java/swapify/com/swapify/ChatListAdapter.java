package swapify.com.swapify;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2015-11-13.
 */
public class ChatListAdapter extends ArrayAdapter<List<String>> {
    private String mUserId;

    public ChatListAdapter(Context context, String userId, List<List<String>> messages) {
        super(context, 0, messages);
        this.mUserId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.chat_listitem, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.imageLeft = (ImageView)convertView.findViewById(R.id.ivProfileLeft);
            holder.imageRight = (ImageView)convertView.findViewById(R.id.ivProfileRight);
            holder.body = (TextView)convertView.findViewById(R.id.tvBody);
            convertView.setTag(holder);
        }
        List<String> message = getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = message.get(0).equals(mUserId);
        // Show-hide image based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        if (isMe) {
            holder.imageRight.setVisibility(View.VISIBLE);
            holder.imageLeft.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            holder.imageLeft.setVisibility(View.VISIBLE);
            holder.imageRight.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
        final ImageView profileView = isMe ? holder.imageRight : holder.imageLeft;

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", message.get(0));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    Picasso.with(getContext()).load(objects.get(0).
                            getString("profileImg")).into(profileView);
                    // The query was successful.
                } else {
                    // Something went wrong.
                }
            }
        });
        holder.body.setText(message.get(1));
        return convertView;
    }

    // Create a gravatar image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }

    final class ViewHolder {
        public ImageView imageLeft;
        public ImageView imageRight;
        public TextView body;
    }
}
