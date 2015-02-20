package android.commutr.com.commutr.services;

import android.app.PendingIntent;
import android.app.Service;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


/**
 * Created by oviroa on 2/5/15.
 */
public class ActivityRecognitionConnectingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //pending intent
    private PendingIntent activityRecognitionPendingIntent;
    private GoogleApiClient activityRecognitionClient;
    private boolean isDisconnecting = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        //create intent
        Intent recognitionIntent = new Intent(getApplicationContext(), ActivityRecognitionProcessingService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(getApplicationContext(), 0, recognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {
            case CommutrApp.CONNECT:
                mixpanel.track(getResources().getString(R.string.activity_monitoring_started), null);
                break;
            case CommutrApp.DISCONNECT:
                isDisconnecting = true;
                mixpanel.track(getResources().getString(R.string.activity_monitoring_stopped), null);
                break;
        }
        initiateActivityRecognitionClient();
        return Service.START_REDELIVER_INTENT;
    }

    private void initiateActivityRecognitionClient() {
        if(activityRecognitionClient == null ) {
            activityRecognitionClient =  new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if(isDisconnecting && activityRecognitionClient.isConnected()) {
            disconnectActivityRecognitionClient();
        } else if(!activityRecognitionClient.isConnected() && !activityRecognitionClient.isConnecting()) {
            activityRecognitionClient.connect();
        }
    }

    private void disconnectActivityRecognitionClient() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(activityRecognitionClient, activityRecognitionPendingIntent);
        activityRecognitionClient.disconnect();
        activityRecognitionClient = null;
        activityRecognitionPendingIntent.cancel();
        activityRecognitionPendingIntent = null;
        stopSelf();
    }

    //client
    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(activityRecognitionClient,
                    getApplicationContext().getResources().getInteger(R.integer.activity_type_update_interval),
                    activityRecognitionPendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.warn("AR service","disconnected");
    }
}
