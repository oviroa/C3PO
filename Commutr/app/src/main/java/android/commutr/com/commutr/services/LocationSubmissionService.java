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
    private boolean isDisconnecting = false;

    public LocationSubmissionService(){
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
                 isDisconnecting = false;
                 Alarms.startActivityRecognition(getApplicationContext());
                 mixpanel.track(getResources().getString(R.string.location_monitoring_started), null);
                 break;
            case CommutrApp.DISCONNECT:
                 isDisconnecting = true;
                 Alarms.stopActivityRecognition(getApplicationContext());
                 mixpanel.track(getResources().getString(R.string.location_monitoring_stopped), null);
                 break;
        }
        initiateLocationClient();
        return Service.START_REDELIVER_INTENT;
    }

    private void initiateLocationClient() {
        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if(isDisconnecting && googleApiClient.isConnected()) {
            disconnectLocationClient();
        }
        else if(!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    private void disconnectLocationClient() {
        unregisterCheckinGeofence();
        LocationServices.FusedLocationApi
                .removeLocationUpdates(googleApiClient, this);
        googleApiClient.disconnect();
        googleApiClient = null;
        stopSelf();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(getApplicationContext().getResources().getInteger(R.integer.location_update_interval));
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
        registerCheckinGeofence();
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

    private void registerCheckinGeofence() {
        Commute commute = DataManager.getInstance().getCachedCommute(getApplicationContext());
        if(commute != null) {
            TypedValue outValue = new TypedValue();
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
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                getGeoFencePendingIntent());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
}
