package android.commutr.com.commutr.services;

import android.app.IntentService;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
/**
 * Created by oviroa on 2/5/15.
 */
public class ActivityRecognitionProcessingService extends IntentService {

    private static final String TAG ="ActivityRecognition";

    public ActivityRecognitionProcessingService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        CommutrApp.activityType = getFriendlyActivityName(result.getMostProbableActivity().getType());

        Logger.warn("ACTIVITY TYPE",CommutrApp.activityType);
    }

    private static String getFriendlyActivityName(int detected_activity_type){
        switch (detected_activity_type ) {
            case DetectedActivity.IN_VEHICLE:
                return "Automotive";
            case DetectedActivity.ON_BICYCLE:
                return "Cycling";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Stationary";
            default:
                return "N/A";
        }
    }


}


