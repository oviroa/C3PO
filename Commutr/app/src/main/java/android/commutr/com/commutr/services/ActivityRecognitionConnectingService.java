package android.commutr.com.commutr.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


/**
 * Created by oviroa on 2/5/15.
 */
public class ActivityRecognitionConnectingService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //pending intent
    private PendingIntent activityRecognitionPendingIntent;

    @Override
    protected void onHandleIntent(Intent intent) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        //create intent
        Intent recognitionIntent = new Intent(getApplicationContext(), ActivityRecognitionProcessingService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(getApplicationContext(), 0, recognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {
            case CommutrApp.CONNECT:
                connectActivityRecognitionClient();
                mixpanel.track(getResources().getString(R.string.activity_monitoring_started), null);
                break;
            case CommutrApp.DISCONNECT:
                disconnectActivityRecognitionClient();
                mixpanel.track(getResources().getString(R.string.activity_monitoring_stopped), null);
                break;
        }
    }

    private void connectActivityRecognitionClient() {
        if (((CommutrApp)getApplicationContext()).getActivityRecognitionClient() == null
                || (((CommutrApp)getApplicationContext()).getActivityRecognitionClient() != null
                && !((CommutrApp)getApplicationContext()).getActivityRecognitionClient().isConnected())) {
            ((CommutrApp)getApplicationContext()).setActivityRecognitionClient( new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build());
            //connect if not connected
            if (!(((CommutrApp)getApplicationContext()).getActivityRecognitionClient().isConnected()
                    || ((CommutrApp)getApplicationContext()).getActivityRecognitionClient().isConnecting())) {
                ((CommutrApp)getApplicationContext()).getActivityRecognitionClient().connect();
            }
        }
    }


    private void disconnectActivityRecognitionClient() {
        if (((CommutrApp)getApplicationContext()).getActivityRecognitionClient() != null
                &&((CommutrApp)getApplicationContext()).getActivityRecognitionClient().isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(((CommutrApp)getApplicationContext()).getActivityRecognitionClient()
                    , activityRecognitionPendingIntent);
            ((CommutrApp)getApplicationContext()).getActivityRecognitionClient().disconnect();
            ((CommutrApp)getApplicationContext()).setActivityRecognitionClient(null);
            activityRecognitionPendingIntent.cancel();
            activityRecognitionPendingIntent = null;
        }
        stopSelf();
    }


    public ActivityRecognitionConnectingService() {
        super("ActivityRecognitionConnectingService");
    }

    //client
    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(((CommutrApp)getApplicationContext()).getActivityRecognitionClient(),
                getApplicationContext().getResources().getInteger(R.integer.activity_type_update_interval),
                activityRecognitionPendingIntent);
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
