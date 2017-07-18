package edu.fsu.cs.mobile.lstatues;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;
    MyContentProvider db = new MyContentProvider();

    final static private long ONE_SECOND = 1000;
    final static private long TWENTY_SECONDS = ONE_SECOND * 20;

    protected AlarmManager am;
    // Flag for GPS status
    boolean isGPSEnabled = false;
    // Flag for network status
    boolean isNetworkEnabled = false;
    // Flag for GPS status
    boolean canGetLocation = false;
    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude
    double radius = 100; //assigned radius
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;
    boolean on;

    public GPSTracker(Context context) {
        Log.w("main", "GPS created");

        this.mContext = context;
        getLocation();
    }

    public GPSTracker() {
        this.mContext = GPSTracker.this;
        //startService(new Intent(this, GPSTracker.class));
        getLocation();
    }

    //use: initial startup
    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.w("main", "in service onStart");
        am = (AlarmManager)(mContext.getSystemService(mContext.ALARM_SERVICE));

        //GooglePlaces Gplaces = new GooglePlaces(location, radius);
        location = getLocation();//latitude&longitude set
        Log.d("main", "ran");
        if(location != null) {
            Log.d("main", location.getLongitude() + " : " + location.getLatitude());

            try (Cursor cursor = getContentResolver().query(db.CONTENT_URI, null, null, null, null)) {
                while (cursor.moveToNext()) {
                    if ((cursor.getDouble(cursor.getColumnIndex("longitude")) - location.getLongitude()) < .001 & (cursor.getDouble(cursor.getColumnIndex("latitude")) - location.getLatitude()) < .001) {
                        NotificationManager notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder notify = new NotificationCompat.Builder(getApplicationContext()).setContentTitle("Downloading Song").setSmallIcon(R.drawable.cdalbumsmall).setAutoCancel(false).setOngoing(true);
                        notification_manager.notify(2, notify.build());
                        notification_manager.cancelAll();
                        notify.setContentTitle("This Statue :").setContentText(cursor.getString(cursor.getColumnIndex("name"))).setOngoing(false);
                        Intent n_intent = new Intent(getApplicationContext(), Statue.class);
                        n_intent.putExtra("name", cursor.getString(cursor.getColumnIndex("name")));
                        n_intent.putExtra("lat", cursor.getDouble(cursor.getColumnIndex("latitude")));
                        n_intent.putExtra("lon", cursor.getDouble(cursor.getColumnIndex("longitude")));
                        n_intent.putExtra("des", cursor.getString(cursor.getColumnIndex("description")));
                        PendingIntent c_intent = PendingIntent.getActivity(mContext, 0, n_intent, 0);
                        notify.setContentIntent(c_intent);
                        notification_manager.notify(1, notify.build());
                    }
                }
                cursor.close();
            }
        }


            //try {
            //    Place place = closestPlace(placesList);
            //    Notify(place);
            //} catch (Exception e) {
            //}
            //TODO need to setup approach detection
        Intent GPSintent = new Intent(this, GPSTracker.class);
        PendingIntent pi = PendingIntent.getService(mContext, 0, GPSintent, 0);
        am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                TWENTY_SECONDS, pi );
        stopSelf();
        return Service.START_STICKY;
    }

    //TODO: send pending intent to notifications that Launches info activity.
    public void Notify(Place P) {

    }


    //get the closest place from the place list.
    public Place closestPlace(PlacesList pl) {

        double pre_distance;
        List<Place> list = pl.results;
        Place place = list.get(0);
        double distance = findDistance(list.get(0).geometry.location, location);

        for (Place p : list) {
            pre_distance = distance;
            distance = findDistance(p.geometry.location, location);
            if (distance < pre_distance) place = p;
            else distance = pre_distance;
        }
        return place;


    }

    //
    public double findDistance(Place.Location location1, Location location2) {
        double difference = 0;
        double lat1 = location1.lat;
        double long1 = location1.lng;
        double lat2 = location2.getLatitude();
        double long2 = location2.getLongitude();
        //TODO: Use meth to find distance

        //
        return difference;
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            showSettingsAlert();

            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                this.canGetLocation = true;
                try {
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                } catch (SecurityException e) {

                }

                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    try {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    } catch (SecurityException e) {

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);

        }
    }


    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        //alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
    }


    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}