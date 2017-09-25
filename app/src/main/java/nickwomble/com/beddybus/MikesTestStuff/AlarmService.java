package nickwomble.com.beddybus.MikesTestStuff;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import nickwomble.com.beddybus.alarmSystem.LocationAlarmManager;

/**
 * Created by MikeM on 9/23/2017.
 * Wrapper to the LocationAlarmManager to run it as a service
 */

public class AlarmService extends Service {

    public static final String ALARM_SERVICE_DESTINATION = "destination";
    public static final String ALARM_SERVICE_INTERVAL = "interval";
    public static final String ALARM_SERVICE_BUFFER = "buffer";
    public static final String ALARM_SERVICE_BROADCAST = "broadcast";
    public static final String ALARM_SERVICE_BROADCAST_STATUS = "broadcast_status";
    public static final String ALARM_SERVICE_BROADCAST_TIME = "time_update";
    public static final int DEFAULT_INTERVAL = 1000 * 5; // in milliseconds - 5 seconds;
    public static final int DEFAULT_BUFFER = 60 * 5; // in seconds - 5 minutes;

    public enum Alarm_Service_Broadcast_Status { IN_ALARM_RANGE, NO_PERMISSIONS, UPDATE_TIME}

    // Main point of the service is to check location on interval
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest locationRequest;
    private LocationAlarmManager locationAlarmManager;
    private Location currentDestination;
    private Location currentLocation;
    private int checkInterval;
    private int bufferTime;
    private boolean serviceIsActive;

    public AlarmService()
    {
        // Default interval
        checkInterval = DEFAULT_INTERVAL;
        // Default Buffer Time
        bufferTime = DEFAULT_BUFFER;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEFAULT_INTERVAL);
        locationRequest.setFastestInterval(DEFAULT_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                Log.d("AlarmService", "locationCallback:" + currentLocation.toString());
                locationAlarmManager.set(currentLocation, currentDestination);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("AlarmService", "onStartCommand: entered function");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bufferTime = intent.getIntExtra(AlarmService.ALARM_SERVICE_BUFFER, DEFAULT_BUFFER);

        locationAlarmManager = new LocationAlarmManager(this, checkInterval, bufferTime);

        createLocationRequest();

        if (intent != null) {
            updateDestinationInfo(intent);
        }

        _startService();

        return START_STICKY;
    }

    protected void _startService() {

        Log.d("AlarmService", "================= ALARM SERVICE STARTED =================");

        serviceIsActive = true;

        // Should already be enabled..
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createLocationCallback();

        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private void updateDestinationInfo(Intent intent)
    {
        Location destination = (Location) intent.getParcelableExtra(ALARM_SERVICE_DESTINATION);
        Log.d("AlarmService", "destination:" + destination.toString());
        currentDestination = destination;
    }

    @Override
    public void onDestroy() {

        if (serviceIsActive) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            serviceIsActive = false;
        }

        locationAlarmManager.cancel();
        Log.d("AlarmService", "================= ALARM SERVICE STOPPED =================");
        super.onDestroy();
    }

    public void performInRangeCheck()
    {
        try {
            Log.d("AlarmService", "onAlarm: updating location.");

            Intent localIntent = new Intent(ALARM_SERVICE_BROADCAST).putExtra(ALARM_SERVICE_BROADCAST_STATUS, Alarm_Service_Broadcast_Status.UPDATE_TIME);
            localIntent.putExtra(ALARM_SERVICE_BROADCAST_TIME, locationAlarmManager.toString());
            // Broadcast the alarm is to be set off
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            if( locationAlarmManager.inRangeForAlarm() ) {
                cancelAlarm(); // cancel anything scheduled
                Log.d("AlarmService", "onAlarm: inRangeForAlarm.");
                localIntent = new Intent(ALARM_SERVICE_BROADCAST).putExtra(ALARM_SERVICE_BROADCAST_STATUS, Alarm_Service_Broadcast_Status.IN_ALARM_RANGE);
                // Broadcast the alarm is to be set off
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            } else
            {
                Log.d("AlarmService", "onAlarm: not inRangeForAlarm.");
            }

        } catch (SecurityException error) {
            Intent localIntent = new Intent(ALARM_SERVICE_BROADCAST).putExtra(ALARM_SERVICE_BROADCAST_STATUS, Alarm_Service_Broadcast_Status.NO_PERMISSIONS);
            // Broadcast the alarm is to be set off
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } catch (IndexOutOfBoundsException AE){}
    }

    public void cancelAlarm(){
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
