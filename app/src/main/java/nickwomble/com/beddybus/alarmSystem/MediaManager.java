package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.media.MediaPlayer;

import nickwomble.com.beddybus.R;

/**
 * Created by Womble on 9/8/2017.
 * Handles audio
 */

public class MediaManager {
    private final int DEFAULT_AUDIO = R.raw.this_girl;
    private Activity myActivity = null;
    private MediaPlayer mp;

    public MediaManager(Activity activity){
        this.init(activity, DEFAULT_AUDIO);
    }
    public MediaManager(Activity activity, int resource){
        this.init(activity,resource);
    }
    private void init(Activity activity, int resource){
        myActivity = activity;
        mp =  MediaPlayer.create(activity, resource);
    }

    /**
     * Plays audio file on loop
     */
    public void activate(){
        mp.setLooping(true);
        mp.start();
    }
    /**
     * Stops audio file loop
     */
    public void deactivate(){
        if(mp.isPlaying()){
            mp.stop();
        }
    }

    public void changeMedia(int newResource){
        this.deactivate();
        init(myActivity,newResource);
    }
}
