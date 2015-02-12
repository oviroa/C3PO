package android.commutr.com.commutr;

import android.app.Application;
import android.commutr.com.commutr.model.Commute;

import com.google.android.gms.common.api.GoogleApiClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * Created by oviroa on 1/23/15.
 * Application singleton
 * Used to store state and context of the application when running
 */

public class CommutrApp extends Application {

    private String userEmail;
    private Commute currentCommute;
    public static final String LOCATION_START_EVENT = "location_start_event";
    public static final String LOCATION_END_EVENT = "location_end_event";
    public static final String ACTION_TYPE = "action_type";
    public static final String CONNECT = "connect";
    public static final String REQUEST_REQUESTED = "requested";
    public static final String REQUEST_CONFIRMED = "confirmed";
    public static final String REQUEST_WAITLISTED = "waitlisted";
    public static final String REQUEST_CANCELLED = "cancelled";
    public static final String REQUEST_CONFIRMATION_EVENT = "request_confirmation_event";
    public static final String REQUEST_CONFIRMATION_STATE = "request_confirmation_state";
    public static final String CHECK_IN_EVENT = "check_in_event";
    public static final String DISCONNECT = "disconnect";
    public static String activityType = "N/A";
    private GoogleApiClient googleApiClient;
    private  GoogleApiClient activityRecognitionClient;
    private String commuteKey;
    private String requestState;
    private String checkInState;

    @Override
    public void onCreate() {
        super.onCreate();
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        mixpanel.track(getResources().getString(R.string.application_started), null);
    }

    public void setCheckInState(String checkInState) {
        this.checkInState = checkInState;
    }

    public String getCheckInState() {

        return checkInState;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public String getRequestState() {

        return requestState;
    }

    public String getCommuteKey() {
        return commuteKey;
    }

    public void setCommuteKey(String commuteKey) {

        this.commuteKey = commuteKey;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public GoogleApiClient getActivityRecognitionClient() {
        return activityRecognitionClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public void setActivityRecognitionClient(GoogleApiClient activityRecognitionClient) {
        this.activityRecognitionClient = activityRecognitionClient;
    }

    public String getUserEmail(){
        return this.userEmail;
    }

    public void setUserEmail(String userEmail){
        this.userEmail = userEmail;
    }

    public void setCurrentCommute(Commute currentCommute) {
        this.currentCommute = currentCommute;
    }

    public Commute getCurrentCommute() {
        return currentCommute;
    }
}
