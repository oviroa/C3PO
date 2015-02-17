package android.commutr.com.commutr.services;

import android.app.PendingIntent;
import android.app.Service;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.model.LocationPoint;
import android.commutr.com.commutr.utils.Alarms;
import android.commutr.com.commutr.utils.Installation;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by oviroa on 2/3/15.
 */
public class LocationSubmissionService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    LocationListener {

    private Geofence geofence;
    private ArrayList<Geofence> currentGeofences = new ArrayList<Geofence>();
    private RequestQueue locationPointVolley;
    private final Object TAG = new Object();
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    public LocationSubmissionService(){
    }

    @Override
    public void onConnected(Bundle bundle) {
        Logger.warn("CONN","ECTED");
        googleApiClient = ((CommutrApp)getApplicationContext()).getGoogleApiClient();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(getApplicationContext().getResources().getInteger(R.integer.location_update_interval));
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
        registerCheckinGeofence();
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
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {
             case CommutrApp.CONNECT:
                 ((CommutrApp)getApplicationContext()).setGoogleApiClient( new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build());
                 ((CommutrApp)getApplicationContext()).getGoogleApiClient().connect();
                 Alarms.startActivityRecognition(getApplicationContext());
                 mixpanel.track(getResources().getString(R.string.location_monitoring_started), null);
                 break;
            case CommutrApp.DISCONNECT:
                 if(((CommutrApp)getApplicationContext()).getGoogleApiClient() != null
                         && ((CommutrApp)getApplicationContext()).getGoogleApiClient().isConnected()) {
                     unregisterCheckinGeofence();
                     LocationServices.FusedLocationApi
                             .removeLocationUpdates(((CommutrApp) getApplicationContext()).getGoogleApiClient(), this);
                     ((CommutrApp)getApplicationContext()).getGoogleApiClient().disconnect();
                     ((CommutrApp)getApplicationContext()).setGoogleApiClient(null);
                 }
                 Alarms.stopActivityRecognition(getApplicationContext());
                 mixpanel.track(getResources().getString(R.string.location_monitoring_stopped), null);
                 stopSelf();
                 break;
         }
        return Service.START_REDELIVER_INTENT;
    }

    private void registerCheckinGeofence() {

        Commute commute = DataManager.getInstance().getCachedCommute(getApplicationContext());
        if(commute != null) {
            TypedValue outValue = new TypedValue();
            getResources().getValue(R.dimen.default_location_lat, outValue, true);
            android.commutr.com.commutr.model.Location location = getPickupLocation(commute.getPickupLocation());
            float lat = location.getLatitude();
            float lon = location.getLongitude();
            geofence = new Geofence.Builder()
                    .setRequestId(Integer.toString(0))
                    .setNotificationResponsiveness(10000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion
                            (
                                    lat,
                                    lon,
                                    getResources().getInteger(R.integer.geofence_radius)
                            )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE).build();
            currentGeofences.add(0,geofence);
            LocationServices.GeofencingApi.
                    addGeofences
                            (
                                    googleApiClient,
                                    currentGeofences,
                                    getGeoFencePendingIntent()
                            );
        }
    }

    private android.commutr.com.commutr.model.Location getPickupLocation(String name) {
        android.commutr.com.commutr.model.Location location =
                android.commutr.com.commutr.model.Location.find
                        (android.commutr.com.commutr.model.Location.class, "code=?", name).get(0);
        return location;
    }

    private PendingIntent getGeoFencePendingIntent() {
        Intent intent = new Intent(getApplicationContext(), GeofenceService.class);
        return PendingIntent.getService
                (
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
    }

    private void unregisterCheckinGeofence() {
        Logger.warn("FENCE","UN-REGISTERED");
        LocationServices.GeofencingApi.removeGeofences(((CommutrApp)getApplicationContext()).getGoogleApiClient(),
                getGeoFencePendingIntent());
    }

}
