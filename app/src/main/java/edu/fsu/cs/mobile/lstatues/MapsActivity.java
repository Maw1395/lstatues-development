package edu.fsu.cs.mobile.lstatues;


import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.util.concurrent.Service;

//this is our "main" activity
// extends ActionBarActivity implements
//    ConnectionCallbacks, OnConnectionFailedListener {
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    public GoogleApiClient GAC;
    private GoogleMap mMap;
    public FragmentManager FM = getSupportFragmentManager();
    FragmentTransaction FT;
    double latitude = 0;
    double longitude = 0;
    double clatitude = 0;
    double clongitude = 0;
    boolean anotherlocation = false;
    boolean ServiceIsOn;
    Intent GPSintent;
    private static Context mContext;
    ContentValues values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // setContentView(R.layout.main_activity);
        mContext = getApplicationContext();
        MyContentProvider db = new MyContentProvider();
        values = new ContentValues();
        values.put("name", "J. Stanley Marshall Plaza");
        values.put("description", "J. Stanley Marshall served as President of Florida State University from 1967-1976.");
        values.put("longitude", 30.443694305537228);
        values.put("latitude", -84.29701179346921);
        getContentResolver().insert(db.CONTENT_URI, values);
        values = new ContentValues();
        values.put("name", "Sportsmanship");
        values.put("description", "Sportsmanship stands 15’ tall and is assembled from two hundred individually cast bronze pieces.");
        values.put("longitude", 30.437375357840484);
        values.put("latitude", -84.3053388599219);
        getContentResolver().insert(db.CONTENT_URI, values);
        values = new ContentValues();
        values.put("name", "Unconquered Statue");
        values.put("description", "Unconquered rises above the Williams Family Plaza at the north end of Langford Green outside the south entrance to Doak S. Campbell Stadium.");
        values.put("longitude", 30.436916322272662);
        values.put("latitude", -84.30301050539123);
        getContentResolver().insert(db.CONTENT_URI, values);
        values = new ContentValues();
        values.put("name", "Integration Statue");
        values.put("description", "The Integration Sculpture celebrates students who pioneered integration at FSU.");
        values.put("longitude", 30.443826569432975);
        values.put("latitude", -84.29803133010864);
        getContentResolver().insert(db.CONTENT_URI, values);

        setContentView(R.layout.activity_maps);
        //setup buttons
        Button B2 = (Button) findViewById(R.id.button2);
        Button B1 = (Button) findViewById(R.id.button3);
        Log.d("main", "buttons setup");
        SupportMapFragment mapFragment = (SupportMapFragment) FM.findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //FM.beginTransaction().add(R.id.frame, mapFragment).commit();

        GPSTracker GPS = new GPSTracker(getApplicationContext());
        clatitude = GPS.getLatitude();
        clongitude = GPS.getLongitude();
        Toast.makeText(getApplicationContext(), clongitude + " : " + clatitude, Toast.LENGTH_SHORT).show();
        Log.d("main", clongitude + " : " + clatitude);


        anotherlocation = (getIntent() != null || getIntent().getStringExtra("directionsflag") != null);


        //TODO change false to boolean expression that checks if the service is on.
        ServiceIsOn = false;


        GPSintent = new Intent(this, GPSTracker.class);


        GAC = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)//TODO: implement OnConnectionFailedListener
                .build();


        //place the map fragment
        //FT = FM.beginTransaction();
        //FT.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        //FT.show(mapFragment);
        //mainFT.commit();

    }


    public void STSEButton(View view){
        Log.d("main", "start button clicked");
        if (!ServiceIsOn) {
            Log.d("main", "starting service");
            //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
            startService(GPSintent);
            ServiceIsOn = true;
        }
        else
            Toast.makeText(getApplicationContext(), "Service is already on", Toast.LENGTH_LONG).show();
    }

    public void STButton(View view){
        Log.d("main", "stop button clicked");
        if (ServiceIsOn) {
            Log.d("main", "stopping service");
            //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
            stopService(GPSintent);
            ServiceIsOn = false;
        }
        else
            Toast.makeText(getApplicationContext(), "Service is already off", Toast.LENGTH_LONG).show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Toast.makeText(getApplicationContext(), "recenter", Toast.LENGTH_SHORT).show();
        Log.w("main", "map ready");

        // Add a marker in Sydney and move the camera
        LatLng currentlocation = new LatLng(clatitude, clongitude);
        mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
        if (anotherlocation) {
            LatLng targetlocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
            //TODO: may (or may not(probably not)) center camera between points
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));

    }

    @Override
    public void onConnectionFailed(final ConnectionResult result){
        //Toast.makeText(getApplicationContext(), "started", Toast.LENGTH_SHORT).show();
        if (!ServiceIsOn && result.hasResolution()) {
            try {
                ServiceIsOn = true;
                result.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                ServiceIsOn = false;
                GAC.connect();
            }
        }
    }


}
