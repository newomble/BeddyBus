package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MikeM on 9/24/2017.
 *
 * for matt's pleasure xd
 */

public class VibratorManager {

    Vibrator vibrator;
    Timer timer;
    boolean cancelthisstupidthing;

    public static final int VIBRATION_DURATION = 1000;
    public static final int VIBRATION_INTERVAL = 2000;

    public VibratorManager(Activity activity)
    {
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void startPeriodicVibration()
    {
        cancelthisstupidthing = false;

        timer = new Timer();

        timer.scheduleAtFixedRate(

                new TimerTask() {

                    public void run() {
                        if(!cancelthisstupidthing) {
                            vibrator.vibrate(VIBRATION_DURATION);
                        }

                    }
                }, 100, VIBRATION_INTERVAL);
    }

    public void stopPeriodicVibration()
    {
        Log.d("VibratorManager", "stoping periodic vibration.");
        vibrator.cancel();
        vibrator.vibrate(100);
        cancelthisstupidthing = true;

        if(timer != null) {
            timer.cancel();
        }
    }
}
