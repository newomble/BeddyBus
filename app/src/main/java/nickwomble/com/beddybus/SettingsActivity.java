package nickwomble.com.beddybus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import nickwomble.com.beddybus.R;
import nickwomble.com.beddybus.fragments.SetAlarm;

/**
 * Created by MikeM on 9/24/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREF_NOTIF = "notifications_enabled";
    public static final String SHARED_PREF_MEDIA = "media_enabled";
    public static final String SHARED_PREF_VIBRATE = "vibrate_enabled";
    public static final String SHARED_PREF_BUFFER = "buffer_time";

    Switch notifications_switch;
    Switch media_switch;
    Switch vibrate_switch;

    SharedPreferences sharedPreferences;

    EditText buffer_time_text;

    public SettingsActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);

        notifications_switch = (Switch) findViewById(R.id.notification_switch);
        notifications_switch.setChecked(sharedPreferences.getBoolean(SHARED_PREF_NOTIF, true));
        Log.d("SettingsActivity", "onCreate: in sharedpref notif switch: "+sharedPreferences.getBoolean(SHARED_PREF_NOTIF, true));
        media_switch = (Switch) findViewById(R.id.music_switch);
        media_switch.setChecked(sharedPreferences.getBoolean(SHARED_PREF_MEDIA, true));
        vibrate_switch = (Switch) findViewById(R.id.vibrate_switch);
        vibrate_switch.setChecked(sharedPreferences.getBoolean(SHARED_PREF_VIBRATE, true));

        buffer_time_text = (EditText) findViewById(R.id.buffer_time_val);
        // this is stored as seconds but will be shown as minutes
        int buffer_time_value = sharedPreferences.getInt(SHARED_PREF_BUFFER, R.string.buffer_time_val) / 60;
        buffer_time_text.setText(buffer_time_value+"");

        addUIListeners();
    }

    private void addUIListeners()
    {
        notifications_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SHARED_PREF_NOTIF, isChecked);
                Log.d("SettingsActivity", "onCheckedChanged: "+isChecked);
                editor.commit();
            }
        });

        media_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SHARED_PREF_MEDIA, isChecked);
                editor.commit();
            }
        });

        vibrate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SHARED_PREF_VIBRATE, isChecked);
                editor.commit();
            }
        });

        // Settings
        final Button backBtn = (Button) findViewById(R.id.save_settings);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(settingsAreValid()) {
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivityForResult(mainActivity, MainActivity.MAIN_ACTIVITY_ID);
                }
            }

        });
    }

    private boolean settingsAreValid()
    {
        return isBufferTimeValid();
    }

    private boolean isBufferTimeValid()
    {
        String inputString = buffer_time_text.getText().toString();
        boolean inputIsValid = true;
        int inputInt = -1;

        try {
            inputInt = Integer.parseInt(inputString);

            if (inputInt <= 0 )
            {
                inputIsValid = false;
            }

        }catch (NumberFormatException youDummy)
        { inputIsValid = false; }

        if(!inputIsValid)
        {
            int previousVal = sharedPreferences.getInt(SHARED_PREF_BUFFER, R.string.buffer_time_val) / 60;
            buffer_time_text.setText(previousVal + "");
            Toast.makeText(getApplicationContext(),"Sound Alarm Distance time (MINUTES) must be greater than 0.", Toast.LENGTH_LONG).show();
        }
        else
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // the value gets stored as seconds so we will convert here
            inputInt = inputInt * 60;
            editor.putInt(SHARED_PREF_BUFFER, inputInt);
            editor.commit();
        }

        return inputIsValid;
    }
}
