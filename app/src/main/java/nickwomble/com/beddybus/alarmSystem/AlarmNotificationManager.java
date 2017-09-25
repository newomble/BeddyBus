package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import nickwomble.com.beddybus.R;
import nickwomble.com.beddybus.fragments.SetAlarm;

/**
 * Created by MikeM on 9/24/2017.
 */

public class AlarmNotificationManager {

    Activity activity;

    public AlarmNotificationManager(Activity activity)
    {
        this.activity = activity;
    }

    public void sendAlarmNotification()
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Beddy Bus")
                        .setContentText("You are now close to your destination!");


        Intent resultIntent = new Intent(activity, SetAlarm.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(001, mBuilder.build());
    }
}
