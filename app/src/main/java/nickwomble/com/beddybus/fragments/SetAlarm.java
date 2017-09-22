package nickwomble.com.beddybus.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Observable;
import java.util.Observer;

import nickwomble.com.beddybus.MapActivity;
import nickwomble.com.beddybus.R;
import nickwomble.com.beddybus.alarmSystem.WakeupScheduler;

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
        GoogleApiClient.OnConnectionFailedListener, Observer {

    private OnFragmentInteractionListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private final int PLACE_PICKER_REQUEST = 1;
    private static final int LOCATION_PERMISSION_NUMBER = 749;
    private WakeupScheduler wakeupScheduler=null;


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
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        wakeupScheduler = new WakeupScheduler( LocationServices.getFusedLocationProviderClient(getActivity()),getActivity() );
        wakeupScheduler.notifyProgress(this);
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
        final Button btnSetLocation = (Button) myView.findViewById(R.id.alarm_btn_set_location);
        btnSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Location dest = new Location("beddyBus");
//                dest.setLatitude(43.087926);
//                dest.setLongitude(-77.674505);
//                wakeupScheduler.setDestination(dest);
                Intent locationPicker = new Intent(getContext(), MapActivity.class);
                startActivityForResult(locationPicker,PLACE_PICKER_REQUEST);
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

    /**
     * Overwrites the primary button's onClick listener to activate the alarm when pressed
     */
    public void addStartListener(){
        final Button btnPrimary = getPrimaryButton();
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wakeupScheduler.startWakeups() ) {
                    btnPrimary.setBackgroundResource(R.drawable.go_button_active);
                    btnPrimary.setText(R.string.label_stop_app);
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
                wakeupScheduler.cancel();
                btnPrimary.setBackgroundResource(R.drawable.go_button_inactive);
                btnPrimary.setText(R.string.label_start_app);
                addStartListener();
            }

        });
    }

    private void setDestinationView(String text){
        ((TextView)getView().findViewById(R.id.alarm_destination)).setText(text);
    }
    private void setTimeView(String time){
        ((TextView)getView().findViewById(R.id.alarm_clock)).setText(time);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addStartListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case PLACE_PICKER_REQUEST:
                    Location dest = new Location(getString( R.string.app_name ));
                    dest.setLatitude( data.getDoubleExtra("lat", 0.0) );
                    dest.setLongitude(data.getDoubleExtra("lng", 0.0) );
                    wakeupScheduler.setDestination(dest);
                    setDestinationView(data.getStringExtra("address"));
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

    @Override
    public void update(Observable observable, Object o) {
        setTimeView(observable.toString());
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
