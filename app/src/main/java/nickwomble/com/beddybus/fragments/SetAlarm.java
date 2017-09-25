package nickwomble.com.beddybus.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import nickwomble.com.beddybus.MapActivity;
import nickwomble.com.beddybus.MikesTestStuff.AlarmServiceReceiver;
import nickwomble.com.beddybus.R;
import nickwomble.com.beddybus.SettingsActivity;
import nickwomble.com.beddybus.alarmSystem.AlarmNotificationManager;
import nickwomble.com.beddybus.alarmSystem.MediaManager;
import nickwomble.com.beddybus.alarmSystem.VibratorManager;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetAlarm.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetAlarm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetAlarm extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String LAST_DESTINATION_ADDRESS = "last_dest_address";
    public static final String LAST_DESTINATION_LAT = "last_dest_lat";
    public static final String LAST_DESTINATION_LON = "last_dest_lon";

    private OnFragmentInteractionListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private final int PLACE_PICKER_REQUEST = 1;
    private final int SETTINGS_FRAGMENT_ID = 2;
    private static final int LOCATION_PERMISSION_NUMBER = 749;
    private AlarmServiceReceiver alarmServiceReceiver;
    private Location selectedLocation;
    private MediaManager mediaManager;
    private VibratorManager vibratorManager;
    private AlarmNotificationManager alarmNotificationManager;

    public SetAlarm() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SetAlarm.
     */
    public static SetAlarm newInstance() {
        SetAlarm fragment = new SetAlarm();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askPermissions();

        // setup for moosic and other stuff
        mediaManager = new MediaManager(getActivity());
        vibratorManager = new VibratorManager(getActivity());
        alarmNotificationManager = new AlarmNotificationManager(getActivity());

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        alarmServiceReceiver = new AlarmServiceReceiver(this);

        /*
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        String lastDestination = sharedPref.getString(LAST_DESTINATION_ADDRESS, "");

        if(lastDestination != null && lastDestination.length() > 0)
        {
            Location dest = new Location(getString( R.string.app_name ));
            setDestinationView(lastDestination);
            dest.setLatitude( Double.doubleToLongBits(sharedPref.getLong(LAST_DESTINATION_LAT, Double.doubleToLongBits(0))) );
            dest.setLongitude( Double.doubleToLongBits(sharedPref.getLong(LAST_DESTINATION_LON, Double.doubleToLongBits(0))) );
            selectedLocation = dest;
        }*/

    }

    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_NUMBER);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_set_alarm, container, false);

        // Set Location
        final Button btnSetLocation = (Button) myView.findViewById(R.id.alarm_btn_set_location);
        btnSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationPicker = new Intent(getContext(), MapActivity.class);
                startActivityForResult(locationPicker, PLACE_PICKER_REQUEST);
            }

        });

        // Settings
        final Button btnSettings = (Button) myView.findViewById(R.id.alarm_settings_btn);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_FRAGMENT_ID);
            }

        });

        return myView;
    }

    /**
     * Retrieves the primary button of the application
     * @return Button
     */
    public Button getPrimaryButton(){
        return (Button) getView().findViewById(R.id.alarm_btn_start);
    }

    public Button getSettingsButton(){
        return (Button) getView().findViewById(R.id.alarm_settings_btn);
    }

    /**
     * Overwrites the primary button's onClick listener to activate the alarm when pressed
     */
    public void addStartListener(){
        final Button btnPrimary = getPrimaryButton();
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedLocation != null ) {
                    alarmServiceReceiver.startAlarmService(selectedLocation);
                    btnPrimary.setBackgroundResource(R.drawable.go_button_active);
                    btnPrimary.setText(R.string.label_stop_app);
                    getSettingsButton().setEnabled(false);
                    addStopListener();
                }else{
                    Toast.makeText(getContext(),"Destination not set", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    /**
     * Overwrites the primary button's onClick listener to deactive the alarm when pressed
     */
    private void addStopListener(){
        final Button btnPrimary = getPrimaryButton();
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmServiceReceiver.initiateStopAlarmService();
                alarmTurnedOff();
                btnPrimary.setBackgroundResource(R.drawable.go_button_inactive);
                btnPrimary.setText(R.string.label_start_app);
                getSettingsButton().setEnabled(true);
                addStartListener();
            }

        });
    }

    private void setDestinationView(String text){
        Log.d("SetAlarm", text);
        ((TextView)getView().findViewById(R.id.alarm_destination)).setText(text);
    }

    public void setTimeView(String time){

        ((TextView)getView().findViewById(R.id.alarm_clock)).setText(time);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addStartListener();
    }

    private void alarmTurnedOff()
    {
        // gg no re
        mediaManager.deactivate();
        vibratorManager.stopPeriodicVibration();
        alarmServiceReceiver.initiateStopAlarmService();
    }

    public void triggerAlarm()
    {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);

        boolean notificationsEnabled = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_NOTIF, true); // read from shared preferences
        boolean mediaEnabled = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_MEDIA, true); // read from shared preferences
        boolean vibrateEnabled = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_VIBRATE, true); // read from shared preferences

        // triggered
        if(notificationsEnabled)
            alarmNotificationManager.sendAlarmNotification();

        // MELEEEEEEEEEEEE
        if(mediaEnabled)
            mediaManager.activate();

        if(vibrateEnabled)
            vibratorManager.startPeriodicVibration();
    }

    public void triggerNoPermissionsToast()
    {
        Toast.makeText(getActivity(),"No Location Permissions",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case PLACE_PICKER_REQUEST:
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    Location dest = new Location(getString( R.string.app_name ));

                    double lat = data.getDoubleExtra("lat", 0.0);
                    dest.setLatitude( lat );
                    editor.putLong(LAST_DESTINATION_LAT, Double.doubleToRawLongBits(lat));

                    double lon = data.getDoubleExtra("lng", 0.0);
                    dest.setLongitude( lon  );
                    editor.putLong(LAST_DESTINATION_LON, Double.doubleToRawLongBits(lon));

                    selectedLocation = dest;
                    String address = data.getStringExtra("address");
                    setDestinationView(address);
                    editor.putString(LAST_DESTINATION_ADDRESS, address);

                    editor.commit();
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_NUMBER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //GRANTED

                } else {

                }

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
