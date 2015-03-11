package android.commutr.com.commutr.services;

import android.app.IntentService;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.utils.Alarms;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CommuteConfirmationRequestService extends IntentService {

    public CommuteConfirmationRequestService() {
        super("CommuteConfirmationRequestService");
    }
    private RequestQueue commuteVolley;
    private final Object TAG = new Object();
    private int retryCount = 0;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            requestCommute();
        }
    }

    private void requestCommute() {
        String key = DataManager.getInstance().getCachedCommuteKey(getApplicationContext());
        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }
        if(key != null) {
            DataManager.getInstance().retrieveCommuteByKey
                    (
                            key,
                            getApplicationContext(),
                            commuteVolley,
                            TAG,
                            new Response.Listener<JSONObject>() {
                                public void onResponse(JSONObject result) {
                                    if (result.has("error")) {
                                        Logger.error("CONFIRMATION", getResources().getString(R.string.commute_error_message));
                                    } else {
                                        processResponse(result);
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
    }

    private void processResponse(JSONObject response) {
        double confirmTime = -1;
        double systemConfirmTime = -1;
        double cancelTime = -1;
        int systemCancelTime = -1;
        int systemWaitlistTime = -1;
        JSONObject commute;
        try {
            if(response.has("commute")) {
                commute = response.getJSONObject("commute");
                if(commute.has("confirm_timestamp")) {
                    confirmTime = (double)commute.get("confirm_timestamp");
                }
                if(commute.has("system_confirm_timestamp")) {
                    systemConfirmTime  = (double)commute.get("system_confirm_timestamp");
                }
                if(commute.has("cancel_timestamp")) {
                    cancelTime = (double)commute.get("cancel_timestamp");
                }
                if(commute.has("system_cancel_timestamp")) {
                    systemCancelTime = (int)commute.get("system_cancel_timestamp");
                }
                if(commute.has("system_waitlist_timestamp")) {
                    systemWaitlistTime = (int)commute.get("system_waitlist_timestamp");
                }

                if(confirmTime > 0 && systemConfirmTime > 0) {
                    broadcastStatus(CommutrApp.REQUEST_CONFIRMATION_STATE, CommutrApp.REQUEST_CONFIRMED, CommutrApp.REQUEST_CONFIRMATION_EVENT);
                } else if (cancelTime > 0 || systemCancelTime > 0) {
                    broadcastStatus(CommutrApp.REQUEST_CONFIRMATION_STATE, CommutrApp.REQUEST_CANCELLED, CommutrApp.REQUEST_CONFIRMATION_EVENT);
                } else if (systemWaitlistTime > 0) {
                    broadcastStatus(CommutrApp.REQUEST_CONFIRMATION_STATE, CommutrApp.REQUEST_WAITLISTED, CommutrApp.REQUEST_CONFIRMATION_EVENT);
                }
                retryCount = 0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
            //try again, 3 times
            if(retryCount < 3 ) {
                Alarms.registerCommuteConfirmationRequest(getApplicationContext());
                retryCount++;
            }
        }
    }

    private void broadcastStatus(String label, String value, String event) {
        Intent requestIntent = new Intent(event);
        DataManager.getInstance().cacheCommuteRequestStatus(value, getApplicationContext());
        requestIntent.putExtra(label, value);
        sendBroadcast(requestIntent);
    }
}
