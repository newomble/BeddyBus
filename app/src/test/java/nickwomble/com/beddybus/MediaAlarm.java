package nickwomble.com.beddybus;


import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import nickwomble.com.beddybus.alarmSystem.MediaManager;

/**
 * Created by Womble on 9/8/2017.
 * bad planning - dont need
 */

public class MediaAlarm implements AlarmManager.OnAlarmListener{
    private MediaManager mediaManager = null;
    private int bufferTime = 1000 * 60 * 10;//amount of time before the destination the alarm should go off
    private boolean isActive = false;

    public MediaAlarm(){}

    public boolean setAlarm(int timeFromNow, Activity context)
    {
        long calcTime = calcAlarmTime(timeFromNow);
        Log.d("Calc Time" + "", "setAlarm: "+	new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(calcTime) ));
        mediaManager = new MediaManager(context);
        if(calcTime <= System.currentTimeMillis()){
            mediaManager.activate();
            return false;
        }
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long time = System.currentTimeMillis()+calcTime;
        am.setExact(AlarmManager.RTC_WAKEUP, time,"media_alarm",this,new Handler());
        return true;
    }

    public void cancelAlarm(Context context)
    {
        mediaManager.deactivate();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(this);
        isActive = false;
    }

    public void updateAlarm(int timeFromNow,Activity context){
        if( mediaManager != null) {
            cancelAlarm(context);
        }
        setAlarm(timeFromNow,context);
    }

    private long calcAlarmTime(int secondsLeft){
        return System.currentTimeMillis() +  (secondsLeft) - bufferTime;
    }

    public boolean isActive(){
        return isActive;
    }

    @Override
    public void onAlarm() {
        mediaManager.activate();
        isActive = true;
        Log.d("Media Alarm", "onAlarm: Media Playing");
    }
}
