package nickwomble.com.beddybus.MikesTestStuff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import nickwomble.com.beddybus.fragments.SetAlarm;
import nickwomble.com.beddybus.SettingsActivity;

/**
 * Created by MikeM on 9/23/2017.
 * Lets the SetAlarm class receive broadcasts from our service
 */

public class AlarmServiceReceiver extends BroadcastReceiver {

    SetAlarm setAlarm;

    public AlarmServiceReceiver(SetAlarm setAlarm)
    {
        this.setAlarm = setAlarm;

        IntentFilter broadcastStatusFilter = new IntentFilter(AlarmService.ALARM_SERVICE_BROADCAST);

        LocalBroadcastManager.getInstance(setAlarm.getActivity()).registerReceiver(this, broadcastStatusFilter);
    }

    public void startAlarmService(Location destination)
    {
        Intent startAlarmService = new Intent(setAlarm.getActivity(), AlarmService.class);
        startAlarmService.putExtra(AlarmService.ALARM_SERVICE_DESTINATION, destination);

        SharedPreferences sharedPref = setAlarm.getActivity().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);

        int bufferTime = sharedPref.getInt(SettingsActivity.SHARED_PREF_BUFFER, AlarmService.DEFAULT_BUFFER); // read from shared preferences
        Log.d("AlarmServiceReceiver", "startAlarmService: sending buffer time of:"+bufferTime);
        startAlarmService.putExtra(AlarmService.ALARM_SERVICE_BUFFER, bufferTime);

        setAlarm.getActivity().startService(startAlarmService);
    }

    // Sends out the request for the service to stop
    public void initiateStopAlarmService()
    {
        Intent stopAlarmService = new Intent(setAlarm.getActivity(), AlarmService.class);
        setAlarm.getActivity().stopService(stopAlarmService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("AlarmServiceReceiver", "onReceive: received from AlarmService.");
        AlarmService.Alarm_Service_Broadcast_Status intended_command = (AlarmService.Alarm_Service_Broadcast_Status) intent.getSerializableExtra(AlarmService.ALARM_SERVICE_BROADCAST_STATUS);
        Log.d("AlarmServiceReceiver", "onReceive: received: "+intended_command.toString());
        parseBroadcastStatus(intended_command, intent);
    }

    private void parseBroadcastStatus( AlarmService.Alarm_Service_Broadcast_Status intended_command, Intent intent)
    {
        switch (intended_command)
        {
            case IN_ALARM_RANGE:
                setAlarm.triggerAlarm();
                break;

            case NO_PERMISSIONS:
                setAlarm.triggerNoPermissionsToast();
                break;

            case UPDATE_TIME:
                updateTime(intent);
                break;

            default:
                Log.e(this.getClass().getCanonicalName(), this.getClass().getEnclosingMethod().getName()+":: Unrecognized command " + intended_command);
                break;
        }
    }

    private void updateTime(Intent intent)
    {
        String updatedTime = intent.getStringExtra(AlarmService.ALARM_SERVICE_BROADCAST_TIME);
        Log.d("AlarmServiceReceiver", "updateTime: received: "+updatedTime);
        setAlarm.setTimeView(updatedTime);
    }
}
