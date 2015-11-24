package swapify.com.swapify;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import eu.livotov.labs.android.camview.ScannerLiveView;

/**
 * Created by Thomas on 2015-11-24.
 */
public class ScanQRCodeActivity extends Activity{
    private static Integer CONNECTION_TIMEOUT = 15000;
    private static Integer DATARETRIEVAL_TIMEOUT = 15000;

    private static String Flight_Stats_Base_URI = "https://api.flightstats.com/flex/";

    private ScannerLiveView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_flight_qr_code);

        findViewById(R.id.scan_qr_code_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.stopScanner();
                Intent i = new Intent(ScanQRCodeActivity.this, AddFlightActivity.class);
                startActivity(i);
            }
        });


        camera = (ScannerLiveView) findViewById(R.id.camview);
        camera.setPlaySound(false);
        camera.startScanner();
        camera.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scannerLiveView) {

            }

            @Override
            public void onScannerStopped(ScannerLiveView scannerLiveView) {

            }

            @Override
            public void onScannerError(Throwable throwable) {

            }

            @Override
            public void onCodeScanned(String s) {
                Log.d("QR Code", s);
                Toast.makeText(ScanQRCodeActivity.this, s, Toast.LENGTH_SHORT).show();
                parseQRCodeResponse(s);
            }
        });
    }

    private void parseQRCodeResponse(String s) {
        String[] results = s.split(" ");

        if (results != null && results[0].contains("/")) {
            try {
                String eTicketInfo = s.split(results[0])[1].trim();
                String airportStartInfo = eTicketInfo.substring(8, eTicketInfo.length());

                String carrierStartInfo = airportStartInfo.substring(6, airportStartInfo.length());
                String flightInfo = carrierStartInfo.substring(0, 7);

                String[] flightInfoArray = flightInfo.replaceAll("\\s","").split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                String carrier = flightInfoArray[0];
                String flightNo = flightInfoArray[1];

                String dateInfoStart = carrierStartInfo.split(flightInfo)[1];

                String dayOfYearString = dateInfoStart.substring(1, 4);
                Integer dayOfYearInt = Integer.parseInt(dayOfYearString);

                String[] seatStartInfoArray = dateInfoStart.split(dayOfYearString);

                String seatNo;
                if (seatStartInfoArray[1].charAt(0) == 'Y' ||
                        seatStartInfoArray[1].charAt(0) == 'F' ||
                        seatStartInfoArray[1].charAt(0) == 'J') {
                    seatNo = Integer.toString(Integer.parseInt(seatStartInfoArray[1].substring(1, 4))) + seatStartInfoArray[1].substring(4, 5);
                } else {
                    String[] seatSpaceArray = seatStartInfoArray[1].split(" ");
                    seatNo = seatSpaceArray[1];
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_YEAR, dayOfYearInt);

                Log.d("Carrier", carrier);
                Log.d("flightNo", flightNo);
                Log.d("seatNo", seatNo);
                Log.d("Calendar", calendar.toString());

                addFlight(carrier, flightNo, seatNo, calendar);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(ScanQRCodeActivity.this, "It seems we couldn't read that QR code. Try again or enter manually!", Toast.LENGTH_SHORT).show();
                Log.d("Malformed QR Code", "QR code is incomplete or incorrect");
            }
        }
    }

    private void addFlight(String carrier, String flightNo, String seatNo, Calendar c) {
        String year = Integer.toString(c.get(Calendar.YEAR));
        String month = Integer.toString(c.get(Calendar.MONTH) + 1);
        String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

//        String year = Integer.toString(2015);
//        String month = Integer.toString(11);
//        String day = Integer.toString(24);

        String APIRequestString = Flight_Stats_Base_URI
                + "schedules/rest/v1/json/flight/"
                + carrier + "/"
                + flightNo
                + "/departing/"
                + year + "/"
                + month + "/"
                + day
                + "?appId=" + Constants.Flight_Stats_App_Id
                + "&appKey=" + Constants.Flight_Stats_API_Key;

        TempFlightInfo tempFlightInfo = new TempFlightInfo();
        tempFlightInfo.airline = carrier.toUpperCase();
        tempFlightInfo.flightNum = flightNo;
        tempFlightInfo.seatNum = seatNo.toUpperCase();

        FlightStatsService flightStatsService = new FlightStatsService(tempFlightInfo);
        Log.d("API Request", APIRequestString);
        flightStatsService.execute(APIRequestString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.startScanner();
    }

    @Override
    protected void onPause() {
        camera.stopScanner();
        super.onPause();
    }


    private class FlightStatsService extends AsyncTask<String, Integer, JSONObject> {
        private TempFlightInfo flightInfo;

        //override constructor
        public FlightStatsService(TempFlightInfo tempFlightInfo) {
            flightInfo = tempFlightInfo;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            disableConnectionReuseIfNecessary();

            HttpURLConnection urlConnection = null;
            try {
                // create connection
                URL urlToRequest = new URL(params[0]);
                urlConnection = (HttpURLConnection)
                        urlToRequest.openConnection();
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

                // handle issues
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // handle unauthorized (if service requires user login)
                    Log.d("HTTP UNAUTHORIZED", "handle unauthorized (if service requires user login)");
                } else if (statusCode != HttpURLConnection.HTTP_OK) {
                    // handle any other errors, like 404, 500,..
                    Log.d("HTTP NOT OK", "handle any other errors, like 404, 500,..");
                }

                // create JSON object from content
                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                return new JSONObject(getResponseText(in));

            } catch (MalformedURLException e) {
                // URL is invalid
                Log.d("URL INVALID", "URL is invalid");
            } catch (SocketTimeoutException e) {
                // data retrieval or connection timed out
                Log.d("SOCKET TIMEOUT", "data retrieval or connection timed out");
            } catch (IOException e) {
                // could not read response body
                // (could not create input stream)
                Log.d("IO EXCEPTION", "could not read response body");
            } catch (JSONException e) {
                // response body is no valid JSON string
                Log.d("JSON Exception", "response body is no valid JSON string");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try{
                Log.d("JSON", jsonObject.toString());
                JSONObject scheduledFlights = jsonObject.getJSONArray("scheduledFlights").getJSONObject(0);
                HashMap<String, String> flightData = new HashMap<>();
                DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

                DateTime departureTime = dateStringFormat.parseDateTime(scheduledFlights.getString("departureTime"));
                DateTime arrivalTime = dateStringFormat.parseDateTime(scheduledFlights.getString("arrivalTime"));

                JSONArray airports = jsonObject.getJSONObject("appendix").getJSONArray("airports");

                JSONObject departureAirport = airports.getJSONObject(0);
                JSONObject arrivalAirport = airports.getJSONObject(1);

                flightData.put("flightNumber", scheduledFlights.getString("carrierFsCode") + scheduledFlights.getString("flightNumber"));
                flightData.put("equipment", jsonObject.getJSONObject("appendix").getJSONArray("equipments").getJSONObject(0).getString("name"));

                flightData.put("dep_iata", departureAirport.getString("iata"));
                flightData.put("dep_city", departureAirport.getString("city"));
                flightData.put("dep_time", departureTime.getHourOfDay() + ":" + departureTime.getMinuteOfHour());
                flightData.put("arr_iata", arrivalAirport.getString("iata"));
                flightData.put("arr_city", arrivalAirport.getString("city"));
                flightData.put("arr_time", arrivalTime.getHourOfDay() + ":" + arrivalTime.getMinuteOfHour());
                flightData.put("seat_number", flightInfo.seatNum);

                saveFlightInfo(flightData);
            } catch (JSONException e) {
                Log.d("JSON Exception", e.getMessage());
                handleFlightNotExists();
            }
        }

        private void handleFlightNotExists() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCodeActivity.this);

            builder.setMessage(R.string.flight_add_error_message).setTitle(R.string.flight_add_error);
            builder.setNegativeButton(R.string.flight_add_error_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dismiss alertview
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void disableConnectionReuseIfNecessary() {
            // see HttpURLConnection API doc
            if (Integer.parseInt(Build.VERSION.SDK)
                    < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }
        }

        private String getResponseText(InputStream inStream) {
            // very nice trick from
            // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
            return new Scanner(inStream).useDelimiter("\\A").next();
        }
    }

    private void saveFlightInfo(Map<String, String> flightInfo) {
        final Map<String, String> flightData = flightInfo;

        ParseQuery query = new ParseQuery("FlightInfo");
        query.whereEqualTo("flightNo", flightData.get("flightNumber"));
        Log.d("flightNum", flightData.get("flightNumber"));
        query.findInBackground(new FindCallback<FlightInfo>() {
            public void done(List<FlightInfo> flightInfos, ParseException e) {
                if (e == null) {
                    FlightInfo flightInfo;
                    List<List<String>> seatsSoFar;
                    String userId = ParseUser.getCurrentUser().getObjectId();
                    boolean userExists = false;
                    if (flightInfos.isEmpty()) {
                        flightInfo = new FlightInfo();
                        flightInfo.setFlightNo(flightData.get("flightNumber"));
                        flightInfo.setEquipment(flightData.get("equipment"));
                        seatsSoFar = new ArrayList<List<String>>();
                        List<String> newSeat = new ArrayList<String>(2);
                        newSeat.add(userId);
                        newSeat.add(flightData.get("seat_number"));
                        seatsSoFar.add(newSeat);
                    } else {
                        flightInfo = flightInfos.get(0);
                        seatsSoFar = flightInfo.getSeats();
                        for (int i = 0; i < seatsSoFar.size(); i ++) {
                            List<String> seatItem = seatsSoFar.get(i);
                            if (seatItem.get(0).equals(userId)) {
                                userExists = true;
                                break;
                            }
                        }
                        if (!userExists) {
                            List<String> newSeat = new ArrayList<String>(2);
                            newSeat.add(userId);
                            newSeat.add(flightData.get("seat_number"));
                            seatsSoFar.add(newSeat);
                        }
                    }
                    if (userExists) {
                        handleUserExists();
                    } else {
                        flightInfo.setSeats(seatsSoFar);
                        flightInfo.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    handleFlightCreation();
                                }
                            }
                        });
                    }
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void handleUserExists() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCodeActivity.this);
        builder.setTitle(R.string.user_exists_error)
                .setMessage(R.string.user_exists_error_message)
                .setNegativeButton(R.string.user_exists_error_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //user cancelled flight add
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleFlightCreation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCodeActivity.this);
        builder.setMessage(R.string.choose_activity)
                .setPositiveButton(R.string.go_current, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(ScanQRCodeActivity.this, FlightActivity.class);
                        startActivity(i);
                    }
                })
                .setNegativeButton(R.string.add_more, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(ScanQRCodeActivity.this, AddFlightActivity.class);
                        startActivity(i);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class TempFlightInfo {
        private String flightNum;
        private String airline;
        private String seatNum;
    }
}
