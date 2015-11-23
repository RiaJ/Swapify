package swapify.com.swapify;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

public class AddFlightActivity extends FragmentActivity{
    private static Integer CONNECTION_TIMEOUT = 15000;
    private static Integer DATARETRIEVAL_TIMEOUT = 15000;

    private static String Flight_Stats_Base_URI = "https://api.flightstats.com/flex/";

    private EditText dateEditView;
    public static Calendar mCalendar;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    DatePickerFragment dateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        ParseObject.registerSubclass(FlightInfo.class);

        dateEditView = (EditText) findViewById(R.id.date_chooser_text_edit);
        mCalendar = Calendar.getInstance();

        dateEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        updateDateButtonText();

        findViewById(R.id.save_flight_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText carrierText = (EditText) findViewById(R.id.edit_carrier);
                final String carrier = carrierText.getText().toString();
                EditText flightNoText = (EditText) findViewById(R.id.edit_flight_no);
                final String flightNo = flightNoText.getText().toString();
                EditText seatNoText = (EditText) findViewById(R.id.edit_seat_no);
                final String seatNo = seatNoText.getText().toString();

                TempFlightInfo tempFlightInfo = new TempFlightInfo();
                tempFlightInfo.airline = carrier.toUpperCase();
                tempFlightInfo.flightNum = flightNo;
                tempFlightInfo.seatNum = seatNo.toUpperCase();

                if (carrier.matches("")) {
                    Toast.makeText(AddFlightActivity.this, "Please enter an air carrier", Toast.LENGTH_SHORT).show();
                    return;
                } else if (flightNo.matches("")) {
                    Toast.makeText(AddFlightActivity.this, "Please enter a flight number", Toast.LENGTH_SHORT).show();
                    return;
                } else if (seatNo.matches("")) {
                    Toast.makeText(AddFlightActivity.this, "Please enter a seat number", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.d("non empty", "execute");
                    checkAgainstFlightSchedule(tempFlightInfo);
                }
            }
        });
    }

    private void checkAgainstFlightSchedule(TempFlightInfo flightInfo) {
        String year = Integer.toString(mCalendar.get(Calendar.YEAR));
        String month = Integer.toString(mCalendar.get(Calendar.MONTH) + 1);
        String day = Integer.toString(mCalendar.get(Calendar.DAY_OF_MONTH));
        Log.d("Year", year);
        Log.d("Month", month);
        Log.d("Day", day);

        String APIRequestString = Flight_Stats_Base_URI
                + "schedules/rest/v1/json/flight/"
                + flightInfo.airline + "/"
                + flightInfo.flightNum
                + "/departing/"
                + year + "/"
                + month + "/"
                + day
                + "?appId=" + Constants.Flight_Stats_App_Id
                + "&appKey=" + Constants.Flight_Stats_API_Key;

        FlightStatsService flightStatsService = new FlightStatsService(flightInfo);
        Log.d("API Request", APIRequestString);
        flightStatsService.execute(APIRequestString);
    }

    public void updateDateButtonText() {
        LocalDate localDateToday = new DateTime(Calendar.getInstance()).toLocalDate();
        LocalDate calendarSetDate = new DateTime(mCalendar).toLocalDate();

        if (calendarSetDate.compareTo(localDateToday) < 0) {
            mCalendar = Calendar.getInstance();
            Toast.makeText(AddFlightActivity.this, "You can't select a date in the past", Toast.LENGTH_SHORT).show();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateForButton = dateFormat.format(mCalendar.getTime());
        dateEditView.setText(dateForButton);
    }

    public void showDatePickerDialog(View v) {
        dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private class FlightStatsService extends AsyncTask<String, Integer, JSONObject>{
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

                DialogFragment confirmFlightSelectionDialog =
                        new ConfirmFlightSelectionDialog(flightData);
                confirmFlightSelectionDialog.show(getSupportFragmentManager(), "chooser");
            } catch (JSONException e) {
                Log.d("JSON Exception", e.getMessage());
                handleFlightNotExists();
            }
        }

        private void handleFlightNotExists() {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddFlightActivity.this);

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

    @SuppressLint("ValidFragment")
    public static class ConfirmFlightSelectionDialog extends DialogFragment {
        public Map<String, String> flightData = new HashMap<>();

        public ConfirmFlightSelectionDialog(Map<String, String> flightInfo) {
            flightData = flightInfo;
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);

            dialog.setTitle("Confirm this flight");
            dialog.setContentView(R.layout.flight_confirmation_popup_dialog);

            TextView flight_info = (TextView) dialog.findViewById(R.id.flight_info);
            flight_info.setText(flightData.get("flightNumber"));
            TextView equipment = (TextView) dialog.findViewById(R.id.equipment);
            equipment.setText(flightData.get("equipment"));

            TextView dep_iata = (TextView) dialog.findViewById(R.id.dep_iata);
            dep_iata.setText(flightData.get("dep_iata"));
            TextView dep_city = (TextView) dialog.findViewById(R.id.dep_city);
            dep_city.setText(flightData.get("dep_city"));
            TextView dep_time = (TextView) dialog.findViewById(R.id.dep_time);
            dep_time.setText(flightData.get("dep_time"));

            TextView arr_iata = (TextView) dialog.findViewById(R.id.arr_iata);
            arr_iata.setText(flightData.get("arr_iata"));
            TextView arr_city = (TextView) dialog.findViewById(R.id.arr_city);
            arr_city.setText(flightData.get("arr_city"));
            TextView arr_time = (TextView) dialog.findViewById(R.id.arr_time);
            arr_time.setText(flightData.get("arr_time"));

            Button confirm_button = (Button) dialog.findViewById(R.id.confirm_button);
            confirm_button.setText("Yes, this is my flight");
            confirm_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFlightInfo(flightData);
                }
            });

            return dialog;
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
                        } else {
                            flightInfo = flightInfos.get(0);
                            seatsSoFar = flightInfo.getSeats();
                            if (seatsSoFar == null) {
                                seatsSoFar = new ArrayList<List<String>>();
                            } else {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.choose_activity)
                    .setPositiveButton(R.string.go_current, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(getContext(), FlightActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(R.string.add_more, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(getContext(), AddFlightActivity.class);
                            startActivity(i);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            ((AddFlightActivity) getActivity()).updateDateButtonText();
        }
    }

    public class TempFlightInfo {
        private String flightNum;
        private String airline;
        private String seatNum;
    }
}