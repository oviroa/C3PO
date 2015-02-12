package android.commutr.com.commutr.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.commutr.com.commutr.CommuteActivity;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.utils.Logger;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

/**
 * Created by oviroa on 2/6/15.
 */
public class GeofenceService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Logger.warn("Geofence Error","code :: " + errorCode);
        } else {
            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                Logger.warn("Pickup Geofence", "ENTER");
                Intent checkInIntent = new Intent(CommutrApp.CHECK_IN_EVENT);
                DataManager.getInstance().cacheCheckInStatus("in",getApplicationContext());
                sendBroadcast(checkInIntent);
                showGeofenceEnterNotification();
            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                Logger.warn("Pickup Geofence", "EXIT");
                DataManager.getInstance().cacheCheckInStatus(null,getApplicationContext());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showGeofenceEnterNotification() {
        final Intent notificationIntent = new Intent(getApplicationContext(), CommuteActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getResources().getString(R.string.welcome))
                .setColor(getResources().getColor(R.color.top_bar_background))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                        + getApplicationContext().getPackageName() + "/raw/whisper"))
                .setContentText(getResources().getString(R.string.arrived))
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true).build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
