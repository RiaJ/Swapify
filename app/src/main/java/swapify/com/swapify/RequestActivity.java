package swapify.com.swapify;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends FragmentActivity {
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

        ParseObject.registerSubclass(FlightInfo.class);
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
                    for (SwapRequest req : outRequestArrayList) {
                        Log.d("out", req.getUserTwoId());
                    }
                    outRequestListAdapter.notifyDataSetChanged();
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });

        inRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment inReqDialog = new InReqDialogFragment();
                SwapRequest request = inRequestArrayList.get(position);
                ArrayList<String> args = new ArrayList<String>();
                args.add(request.getUserOneFlight());
                args.add(request.getUserOneId());
                args.add(request.getUserTwoId());
                args.add(request.getUserOneSeat());
                args.add(request.getUserTwoSeat());
                args.add(Integer.toString(position));
                Bundle req = new Bundle();
                req.putStringArrayList("reqArgs", args);
                inReqDialog.setArguments(req);
                inReqDialog.show(getSupportFragmentManager(), "inReq");
            }
        });

        outRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment outReqDialog = new OutReqDialogFragment();
                Bundle req = new Bundle();
                req.putInt("outPos", position);
                outReqDialog.setArguments(req);
                outReqDialog.show(getSupportFragmentManager(), "outReq");
            }
        });
    }

    private void deleteReq(int position, boolean in) {
        SwapRequest request;
        if (in) {
            request = inRequestArrayList.get(position);
        } else {
            request = outRequestArrayList.get(position);
        }
        request.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Intent i = new Intent(getApplicationContext(), RequestActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    public class InReqDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ArrayList<String> args = getArguments().getStringArrayList("reqArgs");
            builder.setMessage(R.string.accept_or_decline)
                    .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ParseQuery query = new ParseQuery("FlightInfo");
                            query.whereEqualTo("flightNo", args.get(0));
                            query.findInBackground(new FindCallback<FlightInfo>() {
                                public void done(List<FlightInfo> flightInfos, ParseException e) {
                                    if (e == null) {
                                        FlightInfo currentFlightInfo = flightInfos.get(0);
                                        String userOneId = args.get(1);
                                        String userTwoId = args.get(2);
                                        String userOneSeat = args.get(3);
                                        String userTwoSeat = args.get(4);
                                        List<List<String>> seats = currentFlightInfo.getSeats();
                                        for (List<String> seat : seats) {
                                            if (seat.get(0).equals(userOneId)) {
                                                seat.set(1, userTwoSeat);
                                            } else if (seat.get(0).equals(userTwoId)) {
                                                seat.set(1, userOneSeat);
                                            }
                                        }
                                        currentFlightInfo.setSeats(seats);
                                        currentFlightInfo.saveInBackground();
                                        deleteReq(Integer.valueOf(args.get(5)), true);
                                    } else {
                                        Log.d("message", "Error: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteReq(Integer.valueOf(args.get(5)), true);
                        }
                    });
            final AlertDialog inReqDialog = builder.create();
            inReqDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    Button posButton = inReqDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negButton = inReqDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                    LinearLayout.LayoutParams posParams =
                            (LinearLayout.LayoutParams) posButton.getLayoutParams();
                    posParams.weight = 1;

                    LinearLayout.LayoutParams negParams =
                            (LinearLayout.LayoutParams) negButton.getLayoutParams();
                    negParams.weight = 1;

                    posButton.setLayoutParams(posParams);
                    negButton.setLayoutParams(negParams);
                }
            });
            return inReqDialog;
        }
    }

    public class OutReqDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Integer pos = getArguments().getInt("reqPos");
            builder.setMessage(R.string.cancel_or_not)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteReq(Integer.valueOf(pos), false);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog outReqDialog = builder.create();
            outReqDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    Button posButton = outReqDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negButton = outReqDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                    LinearLayout.LayoutParams posParams =
                            (LinearLayout.LayoutParams) posButton.getLayoutParams();
                    posParams.weight = 1;

                    LinearLayout.LayoutParams negParams =
                            (LinearLayout.LayoutParams) negButton.getLayoutParams();
                    negParams.weight = 1;

                    posButton.setLayoutParams(posParams);
                    negButton.setLayoutParams(negParams);
                }
            });
            return outReqDialog;
        }
    }
}

