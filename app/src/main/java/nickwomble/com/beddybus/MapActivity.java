package nickwomble.com.beddybus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    private Place myDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng location = place.getLatLng();
                recenterMap(location,place.getAddress().toString());
                myDestination = place;
                enableLockIn();
            }

            @Override
            public void onError(Status status) {
                myDestination = null;
                Toast.makeText(getApplicationContext(),"Failed to load selected location",Toast.LENGTH_LONG).show();
                disableLockIn();
            }
        });
//        autocompleteFragment.on
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.431064, -74.190827), 6));
    }

    private void recenterMap(LatLng chosenLocation,String addr){
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 8));
        mMap.addMarker(new MarkerOptions()
                .position(chosenLocation)
                .title(addr));
    }
    private Button getLockButton(){
        return (Button) this.findViewById(R.id.lockIn);
    }
    private void enableLockIn() {
        Button lock = getLockButton();
        lock.setVisibility(View.VISIBLE);
        lock.setOnClickListener(this);
    }
    private void disableLockIn(){
        Button lock = getLockButton();
        lock.setVisibility(View.INVISIBLE);
        lock.setOnClickListener(null);
    }

    @Override
    public void onClick(View view) {
        if( myDestination != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", myDestination.getLatLng().latitude);
            resultIntent.putExtra("lng",myDestination.getLatLng().longitude);
            resultIntent.putExtra("address",myDestination.getAddress());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(),"No Location chosen",Toast.LENGTH_LONG).show();
            disableLockIn();
        }
    }
}
