package android.commutr.com.commutr.services;

import android.app.Service;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.model.LocationPoint;
import android.commutr.com.commutr.utils.Installation;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

/**
 * Created by oviroa on 2/3/15.
 */
public class LocationSubmissionService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    LocationListener{


    //request queue for server calls
    private RequestQueue locationPointVolley;
    //tag for Volley
    private final Object TAG = new Object();

    private LocationRequest mLocationRequest;

    public LocationSubmissionService()
    {

    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(120000);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                CommutrApp.googleApiClient, mLocationRequest, this);

    }


    private void startActivityRecognition() {

        final Intent recognitionIntent = new Intent(getApplicationContext(), ActivityRecognitionConnectingService.class);
        recognitionIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.CONNECT);
        startService(recognitionIntent);
    }


    private void stopActivityRecognition() {

        final Intent recognitionIntent = new Intent(getApplicationContext(), ActivityRecognitionConnectingService.class);
        recognitionIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.DISCONNECT);
        startService(recognitionIntent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(locationPointVolley == null) {
            locationPointVolley = Volley.newRequestQueue(getApplicationContext());
        }

        DataManager.getInstance().sendLocationPoint
                (
                        getLocationPoint(location),
                        getApplicationContext(),
                        locationPointVolley,
                        TAG,
                        new Response.Listener<JSONObject>() {

                            public void onResponse(JSONObject result) {

                                Logger.warn("LOCATION POINT",result.toString());

                                if(result.has("error")) {
                                    Logger.warn("LOCATION POINT ERROR",result.toString());
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {

                                error.printStackTrace();

                            }
                        }


                );

    }

    private LocationPoint getLocationPoint(Location location){

        LocationPoint point = new LocationPoint();

        point.setDeviceIdentifier(Installation.id(getApplicationContext()));
        point.setTimestamp(System.currentTimeMillis() / 1000L);
        point.setLat(location.getLatitude());
        point.setLon(location.getLongitude());
        point.setSpeed(location.getSpeed());
        point.setHorizontalAccuracy(location.getAccuracy());
        point.setActivity(CommutrApp.activityType);

        return point;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

         switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {

             case CommutrApp.CONNECT:

                 CommutrApp.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

                 CommutrApp.googleApiClient.connect();

                 startActivityRecognition();

                 break;

             case CommutrApp.DISCONNECT:

                 if(CommutrApp.googleApiClient != null) {

                     LocationServices.FusedLocationApi
                             .removeLocationUpdates(CommutrApp.googleApiClient, this);
                     CommutrApp.googleApiClient.disconnect();
                     CommutrApp.googleApiClient = null;
                 }

                 stopActivityRecognition();

                 stopSelf();

                 break;
         }

        return Service.START_REDELIVER_INTENT;

    }

}
