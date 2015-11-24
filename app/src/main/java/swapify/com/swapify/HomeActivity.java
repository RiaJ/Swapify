package swapify.com.swapify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends Activity {
    //Parse credentials
    public static final String SWAPIFY_APPLICATION_ID = "M0VgBIPclT0xQP1vVT2lDspJZCIl6pxbmuQ3Jzhq";
    public static final String SWAPIFY_CLIENT_KEY = "HSqUBvUPlZUrpqFgbM90fpoEDkKzcSJ0OStQIny8";

    private static final String TAG = "HomeActivity";

    private static String userId;

    private static String facebookId;

    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_home);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, SWAPIFY_APPLICATION_ID, SWAPIFY_CLIENT_KEY);
        ParseFacebookUtils.initialize(this);

        // facebook login
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        findViewById(R.id.add_flight_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), AddFlightActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.current_flights_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), FlightActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.swap_requests_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), RequestActivity.class);
                startActivity(i);
            }
        });
    }

    private void onLoginButtonClicked() {
        if (AccessToken.getCurrentAccessToken() != null) {
            findViewById(R.id.add_flight_and_current_flights).setVisibility(View.GONE);
            return;
        }

        List<String> permissions = Arrays.asList("public_profile");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    userId = ParseUser.getCurrentUser().getObjectId();
                    /* make the API call */

                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    // Application code
                                    response.getError();
                                    Log.e("JSON:", object.toString());
                                    try {
                                        facebookId = object.getString("id");
                                        String url = "https://graph.facebook.com/"
                                                + facebookId
                                                + "/picture?width=200&height=200";
                                        user.put("profileImg", url);
                                        user.setUsername(object.getString("name"));
                                        user.saveInBackground();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email");
                    request.setParameters(parameters);
                    request.executeAsync();

                    new GraphRequest(AccessToken.getCurrentAccessToken(), userId, null, HttpMethod.GET, new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {

                        }
                    }).executeAsync();

                    findViewById(R.id.add_flight_and_current_flights).setVisibility(View.VISIBLE);
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                } else {
                    userId = ParseUser.getCurrentUser().getObjectId();
                    findViewById(R.id.add_flight_and_current_flights).setVisibility(View.VISIBLE);
                    Log.d("MyApp", "User logged in through Facebook!");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
