package nickwomble.com.beddybus.alarmSystem;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Womble on 9/9/2017.
 */

public class LocationAlarmManager extends Observable{
    private String directionHead = "https://maps.googleapis.com/maps/api/directions/json?origin=";
    private String directionTail="&mode=drive&key="+ "AIzaSyDaS955G_Cgw4S4tubXJk1t_t2194Sk_VI";
    private int bufferTime = 60 * 5; //seconds
    private int checkInterval = 0;//miliseconds
    private RequestQueue queue;
    private MediaManager mediaManager =null;
    private boolean alarmComplete = false;

    private String lastReadingValue = "Location is being updated";

    LocationAlarmManager(Context context, int _checkInterval) {
        queue = Volley.newRequestQueue(context);
        checkInterval= _checkInterval;
        reset();
    }
    public void reset(){
        mediaManager=null;
        alarmComplete = false;
    }

    public void set(Location myLocation, Location destination, Activity activity){
        String strLocation = myLocation.getLatitude() +","+myLocation.getLongitude();
        String strDestination = destination.getLatitude() + ","+destination.getLongitude();
        enQueue(directionHead+strLocation+"&destination="+strDestination+directionTail, activity);
    }
    private void enQueue(String url, final Activity activity){
        if(alarmComplete()){
            return;
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            int secondsLeft = getSecondsFromJson(response);
                            if (hasArrived(secondsLeft)){
                                mediaManager = new MediaManager(activity);
                                mediaManager.activate();
                                alarmComplete=true;
                            }
                            lastReadingValue = secondsToRealTime(secondsLeft);
                            setChanged();
                            notifyObservers();
                        } catch (JSONException oops){
                            oops.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        queue.add(jsObjRequest);
    }

    private String secondsToRealTime(int secondsLeft) {
        long longVal = secondsLeft;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;
        return hours+"hr, "+mins+"mins";
    }

    private int getSecondsFromJson(JSONObject res) throws JSONException {
        JSONArray routeArray = res.getJSONArray("routes");
        JSONObject aRoute = routeArray.getJSONObject(0);
        JSONObject legs = aRoute.getJSONArray("legs").getJSONObject(0);
        JSONObject duration = legs.getJSONObject("duration");
        return duration.getInt("value");
    }

    /**
     * Calculates if we are close enough to wake matt up
     * @param secondsLeft number of seconds till arrival at given location
     * @return true if arrival time is less than the buffered time and interval time combined
     */
    private boolean hasArrived(int secondsLeft){
        if( secondsLeft - bufferTime - (checkInterval/1000 ) <=0){
            return true;
        }else{
            return false;
        }
    }
    public void cancel(){
        if(mediaManager !=null){
            mediaManager.deactivate();
        }
        reset();
    }

    public boolean alarmComplete(){
        return alarmComplete;
    }
    void enroll(Observer me){
        this.addObserver(me);
    }
    @Override
    public String toString(){
        return lastReadingValue;
    }
}
