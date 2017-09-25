package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import nickwomble.com.beddybus.R;

/**
 * Created by Womble on 9/8/2017.
 * Handles audio
 */

public class MediaManager {
    private final int DEFAULT_AUDIO = R.raw.meleeeeee;
    private Activity myActivity = null;
    private MediaPlayer mp;
    private AudioManager audioManager;

    public MediaManager(Activity activity){
        this.init(activity, DEFAULT_AUDIO);
    }
    public MediaManager(Activity activity, int resource){
        this.init(activity,resource);
    }


    private void init(Activity activity, int resource){
        myActivity = activity;
        mp =  MediaPlayer.create(activity, resource);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Plays audio file on loop
     */
    public void activate(){

        // Request audio focus -- attempts to pauses other music
        int result = audioManager.requestAudioFocus(focusChangeListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (myActivity != null)
        {
            mp =  MediaPlayer.create(myActivity, DEFAULT_AUDIO);
        }
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

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {

                    switch (focusChange) {
                        default:
                            deactivate();
                            break;
                    }
                }
            };
}
