package android.commutr.com.commutr.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;


/**
 * Created by oviroa on 2/5/15.
 */
public class ActivityRecognitionConnectingService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final int ACTIVITY_UPDATES_INTERVAL = 5000;
    //pending intent
    private PendingIntent activityRecognitionPendingIntent;

    @Override
    protected void onHandleIntent(Intent intent) {

        //create intent
        Intent recognitionIntent = new Intent(getApplicationContext(), ActivityRecognitionProcessingService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(getApplicationContext(), 0, recognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {

            case CommutrApp.CONNECT:
                connectActivityRecognitionClient();
                break;

            case CommutrApp.DISCONNECT:
                disconnectActivityRecognitionClient();
                break;
        }

    }

    private void connectActivityRecognitionClient() {

        if (CommutrApp.activityRecognitionClient == null
                || (CommutrApp.activityRecognitionClient != null
                && !CommutrApp.activityRecognitionClient.isConnected()))
        {
            //create location client, assign callback for succesfull and unsuccessfull conncestions)
            CommutrApp.activityRecognitionClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();



            //connect if not connected
            if (!(CommutrApp.activityRecognitionClient.isConnected() || CommutrApp.activityRecognitionClient.isConnecting())) {
                CommutrApp.activityRecognitionClient.connect();

            }
        }
        else// if client is not null and connected, request activity updates
        {
            // Request activity updates
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(CommutrApp.activityRecognitionClient, 3000, activityRecognitionPendingIntent);
            stopSelf();
        }

    }


    private void disconnectActivityRecognitionClient() {

        if (CommutrApp.activityRecognitionClient != null
                && CommutrApp.activityRecognitionClient.isConnected()) {

            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(CommutrApp.activityRecognitionClient, activityRecognitionPendingIntent);
            CommutrApp.activityRecognitionClient.disconnect();
            CommutrApp.activityRecognitionClient = null;

        }
        stopSelf();
    }


    public ActivityRecognitionConnectingService() {
        super("ActivityRecognitionConnectingService");
    }

    //client
    @Override
    public void onConnected(Bundle bundle) {

        Logger.warn("CONNECT","ON");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(CommutrApp.activityRecognitionClient, 3000, activityRecognitionPendingIntent);
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
