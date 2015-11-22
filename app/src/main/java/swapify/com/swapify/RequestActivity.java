package swapify.com.swapify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends Activity {
    private ListView inRequestListView;
    private ListView outRequestListView;
    private List<SwapRequest> inRequestArrayList;
    private List<SwapRequest> outRequestArrayList;
    private InRequestListAdapter inRequestListAdapter;
    private OutRequestListAdapter outRequestListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listrequests);

        ParseObject.registerSubclass(SwapRequest.class);

        setupSwapRequest();
    }

    private void setupSwapRequest() {
        inRequestListView = (ListView) findViewById(R.id.inRequestListView);
        outRequestListView = (ListView) findViewById(R.id.outRequestListView);
        inRequestArrayList = new ArrayList<SwapRequest>();
        outRequestArrayList = new ArrayList<SwapRequest>();

        inRequestListView.setTranscriptMode(1);
        outRequestListView.setTranscriptMode(1);
        inRequestListAdapter = new InRequestListAdapter(RequestActivity.this, inRequestArrayList);
        outRequestListAdapter = new OutRequestListAdapter(RequestActivity.this, outRequestArrayList);
        inRequestListView.setAdapter(inRequestListAdapter);
        outRequestListView.setAdapter(outRequestListAdapter);

        retrieveSwapRequest();
    }

    private void retrieveSwapRequest() {
        // incoming requests
        ParseQuery queryIn = new ParseQuery("SwapRequest");
        queryIn.whereEqualTo("userTwoId", ParseUser.getCurrentUser().getObjectId());
        queryIn.findInBackground(new FindCallback<SwapRequest>() {
            public void done(List<SwapRequest> swapRequests, ParseException e) {
                if (e == null) {
                    inRequestArrayList.clear();
                    inRequestArrayList.addAll(swapRequests);
                    inRequestListAdapter.notifyDataSetChanged();
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });

        // outgoing requests
        ParseQuery queryOut = new ParseQuery("SwapRequest");
        queryOut.whereEqualTo("userOneId", ParseUser.getCurrentUser().getObjectId());
        queryOut.findInBackground(new FindCallback<SwapRequest>() {
            public void done(List<SwapRequest> swapRequests, ParseException e) {
                if (e == null) {
                    outRequestArrayList.clear();
                    outRequestArrayList.addAll(swapRequests);
                    outRequestListAdapter.notifyDataSetChanged();
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }
}

