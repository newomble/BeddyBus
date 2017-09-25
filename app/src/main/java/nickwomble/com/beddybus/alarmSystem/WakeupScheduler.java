package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Observer;

/**
 * Created by Womble on 9/9/2017.
 *
 */

public class WakeupScheduler implements AlarmManager.OnAlarmListener {
    private final Activity activity;
    private final int wakeupInterval = 1000 * 10;// in milliseconds - 5minutes
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationAlarmManager locationAlarmManager;

    private Location destination = null;

    /**
     * Deprecated for the Alarm Service which performs pretty much as this does but as a Service
     * @param fusedLocationProviderClient
     * @param _activity
     */
    @Deprecated
    public WakeupScheduler(FusedLocationProviderClient fusedLocationProviderClient, Activity _activity) {
        mFusedLocationClient = fusedLocationProviderClient;
        activity = _activity;
        //locationAlarmManager = new LocationAlarmManager(activity,wakeupInterval);
    }
    public boolean startWakeups(){
        if(destination == null){
            return false;
        }
        init();
        return true;
    }
    public boolean startWakeups(Location dest){
        destination = dest;
        init();
        return true;
    }
    private void init(){
        checkLocation();
        setAlarm();
    }

    private void setAlarm(){
        AlarmManager am =(AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + wakeupInterval,"location_check",this,new Handler());
    }

    private void checkLocation() throws SecurityException{
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            locationAlarmManager.set(location,destination);
                        }
                    }
                });
    }

    public void cancel(){
        AlarmManager am =(AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        am.cancel(this);
        locationAlarmManager.cancel();
    }

    @Override
    public void onAlarm() {
        Log.d("Wakeup Alarm", "onAlarm: Alarm wake up.");

        try {
            checkLocation();
        } catch (SecurityException error){
            Toast.makeText(activity,"No Location Permissions",Toast.LENGTH_LONG).show();
        } catch (IndexOutOfBoundsException AE){

        }
        if(! locationAlarmManager.inRangeForAlarm() ) {
            setAlarm();
        }
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

}
