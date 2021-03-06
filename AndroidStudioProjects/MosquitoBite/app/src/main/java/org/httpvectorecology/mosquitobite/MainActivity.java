package org.httpvectorecology.mosquitobite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.location.Location;

import java.util.Random;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;



public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {
    Button btnClosePopup;
    final Random randomGenerator = new Random();
    final ArrayList funFactsList = new ArrayList() {{
        add("North Dakota is the state with the fewest types (species) of mosquitoes with 24 species; Florida has the most with 76");
        add("Mosquitoes spit into you when they bite, and that’s what makes you itch");
        add("Eliminating places for mosquitoes to grow is an effective and free way to control them");
        add("There is a spider in Africa that specializes in eating mosquitoes");
        add("All mosquitoes grow in water");
        add("Eggs of some desert mosquitoes can survive several years before hatching");
        add("There is a tropical mosquito that is fed by ants");
        add("Some mosquitoes have been living close to people since the beginning of cities, about 3000 years ago");
        add("There are some mosquitoes that prefer birds, and some that prefer frogs");
        add("Salt marsh mosquitoes have been found flying 15 miles from where they emerged");
        add("The most common lethal pathogen of dogs in the United States in transmitted by mosquitoes, dog heartworm");
        add("Due to its role as the vector of malaria and viruses, mosquitoes kill more people every year than any other animal");
        add("The biggest mosquito in the world doesn’t drink blood, and eats other mosquitoes!");
        add("Certain mosquitoes become more abundant in times of drought, and can drive epidemics of deadly brain swelling");
        add("Most types of mosquitoes (species) don’t bite people");
        add("Some mosquitoes have spread around the world, and the same species can be found on all continents except Antarctica");
        add("Male mosquitoes are attracted to the buzz of female mosquitoes, and female mosquitoes judge males by their buzz");
        add("Some people are more attractive to mosquitoes than others, but we don’t know why yet.  We’re working on it");
        add("Fossil mosquitoes have been found from the time of the dinosaurs, but no dino DNA has been preserved");
        add("Mosquito populations can quickly evolve resistance to pesticides, but resistance to repellents is rare");
    }};

    // Testing Google Analytics


    //
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mFormattedDate;
    ProgressBar pb;
    List<MyTask> tasks;
    private Button btnNewShowLocation;
    View v;
    private double longitude;
    private double latitude;

    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);
        btnNewShowLocation = (Button) findViewById(R.id.btnNewShowLocation);
        tasks = new ArrayList<>();
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }
            /*
    *For Google Analytics...
    */
        Tracker t = ((GoogleAnalyticsMB) getApplication()).getTracker(GoogleAnalyticsMB.TrackerName.APP_TRACKER);
        t.setScreenName("MainActivity");
        t.send(new HitBuilders.ScreenViewBuilder().build());

        btnNewShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    postDataToServer("http://vectorecology.org/mosq_app/index.php");
                } else {
                    Toast.makeText(MainActivity.this, "Network isn't available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FATEST_INTERVAL)
                .setSmallestDisplacement(DISPLACEMENT);
    }

    private void getData() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time =&gt; " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mFormattedDate = df.format(c.getTime());
        System.out.println("Longitude (getData)" + longitude + " and Latitude: " + latitude);
    }

    private PopupWindow popupWin;

    private void customPopupWindow(View v) {
        try {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.screen_popup,
                    (ViewGroup) findViewById(R.id.popup_element));
            TextView newArray = (TextView) layout.findViewById(R.id.txtViewArrayContent);
            String funFactString = (String) funFactsList.get(randomGenerator.nextInt(funFactsList.size()));
            newArray.setText(funFactString);
            popupWin = new PopupWindow(layout, 750, 650, true);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.transparent);
            popupWin.setBackgroundDrawable(new BitmapDrawable(getResources(),
                    bitmap));
            popupWin.setOutsideTouchable(true);
            popupWin.setFocusable(true);
            popupWin.showAtLocation(layout, Gravity.CENTER, 0, 0);
            btnClosePopup = (Button) layout.findViewById(R.id.btn_close_popup);
            btnClosePopup.setOnClickListener(cancel_button_click_listener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            popupWin.dismiss();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.add(Menu.NONE, Menu.NONE, 100, "About");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void postDataToServer(String uri) {

        RequestPackage p = new RequestPackage();
        p.setMethod("POST");
        p.setUri(uri);
        p.setParam("longitude", String.valueOf(longitude));
        p.setParam("latitude", String.valueOf(latitude));
        p.setParam("date", String.valueOf(mFormattedDate));
        MyTask task = new MyTask();
        task.execute(p);
        System.out.println("Longitude (post data to server)" + longitude + " and Latitude: " + latitude);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        getData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed:  ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        getData();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        System.out.println("Longitude (on Connected): " + longitude + " and Latitude: " + latitude);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        } else {
            Toast.makeText(getApplicationContext(), "Searching for your location... \nMake sure WiFi or GPS is turned On", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private class MyTask extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            if (tasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            tasks.add(this);
        }

        @Override
        protected String doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            customPopupWindow(v);
            tasks.remove(this);
            if (tasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }
}