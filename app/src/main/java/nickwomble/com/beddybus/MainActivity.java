package nickwomble.com.beddybus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import nickwomble.com.beddybus.fragments.Holder;
import nickwomble.com.beddybus.fragments.SetAlarm;

public class MainActivity extends AppCompatActivity implements SetAlarm.OnFragmentInteractionListener{

    private Fragment alarmFragment = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    swapFragments(getHomeFragment() );
                    return true;
//                case R.id.navigation_dashboard:
//                    swapFragments(getSecondFragment());
//                    return true;
//                case R.id.navigation_notifications:
//                    swapFragments(getThirdFragment());
//                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        swapFragments(getHomeFragment());
    }

    private void swapFragments(Fragment replacement){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content, replacement);
        transaction.commit();
    }

    /*
        Returns the fragment for the third view
     */
    private Fragment getThirdFragment() {return null;}

    /*
        Returns the fragment for the second view
     */
    private Fragment getSecondFragment() {
        return null;
    }
    /*
        Returns the fragment for the home view
     */
    private Fragment getHomeFragment() {
        alarmFragment = SetAlarm.newInstance();
        return alarmFragment;
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
