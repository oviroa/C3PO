package android.commutr.com.commutr.managers;

import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.model.Identity;
import android.commutr.com.commutr.model.LocationPoint;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Singleton, manages all data input/output
 * Created by oviroa on 1/29/15.
 */
public class DataManager {

    //singleton instance
    private static DataManager ref = null;
    private static String COMMUTE_URL = "https://just-armor-726.appspot.com/api/commute";
    private static String IDENTITY_URL = "https://just-armor-726.appspot.com/api/device";
    private static String LOCATION_POINT_URL = "https://just-armor-726.appspot.com/api/point";
    private static String COMMUTE_CONFIRMATION_URL = "https://just-armor-726.appspot.com/api/v2/commute/";
    private static String LOCATIONS_URL = "https://just-armor-726.appspot.com/api/location";

    /**
     * Retrieves singleton instance
     * @return
     */
    public static synchronized DataManager getInstance() {
        if (ref == null) {	// it's ok, we can call this constructor
            ref = new DataManager();
        }
        return ref;
    }

    /**
     * Do not allow constructor to function
     */
    private DataManager(){
    }

    /**
     * Disallows cloning
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void storeUserEmail(String userEmail, Context context) {
        //store in memory
        ((CommutrApp)context).setUserEmail(userEmail);
        //store in preferences
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        //if user type not chosen
        if(userEmail != null) {
            //store account
            editor.putString(context.getResources().getString(R.string.commutr_email_address), userEmail);
            editor.commit();
        } else {
            //clear account
            editor.clear().commit();
        }
    }

    public String retrieveUserEmail(Context context){
        //try memory
        String userEmail = ((CommutrApp)context).getUserEmail();
        //if not there, try local storage
        if (userEmail == null) {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            userEmail = settings.getString(context.getResources().getString(R.string.commutr_email_address), null);
            //set memory version if not null
            if(userEmail != null) {
                ((CommutrApp)context).setUserEmail(userEmail);
            }
        }
        return userEmail;
    }

    public void storeCommute(Commute commute,
                             Context context,
                             RequestQueue queue,
                             Object tag,
                             Listener<JSONObject> listener,
                             ErrorListener errorListener) {
        //prepare JSON builder obj
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();
        //Volley submission method
        int method = (Method.PUT);
        try {
            //prep request
            JsonObjectRequest jsonRequest =
                    new JsonObjectRequest
                            (
                                    method,
                                    COMMUTE_URL,
                                    new JSONObject(gson.toJson(commute,Commute.class)),
                                    listener,
                                    errorListener
                            );
            jsonRequest.setShouldCache(false);
            jsonRequest.setTag(tag);
            //add request to Volley que for execution
            queue.add(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void cacheCommute(Commute commute, Context context)
    {
        ((CommutrApp)context).setCurrentCommute(commute);
        //store in preferences
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        //make json
        Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(commute);
        //store trip
        editor.putString(context.getResources().getString(R.string.commutr_current_commute), json);
        editor.commit();

    }

    public Commute getCachedCommute(Context context) {
        //store in mem first
        Commute commute = ((CommutrApp)context).getCurrentCommute();
        if(commute == null) {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            String tripJson = settings.getString(context.getResources().getString(R.string.commutr_current_commute), null);
            //make json
            Gson gson = new Gson();
            commute = gson.fromJson(tripJson, Commute.class);
        }
        return commute;
    }

    public void storeIndentity(Identity identity,
                             Context context,
                             RequestQueue queue,
                             Object tag,
                             Listener<JSONObject> listener,
                             ErrorListener errorListener) {
        //prepare JSON builder obj
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();
        //Volley submission method
        int method = (Method.PUT);
        try {
            //prep request
            JsonObjectRequest jsonRequest =
                    new JsonObjectRequest
                            (
                                    method,
                                    IDENTITY_URL,
                                    new JSONObject(gson.toJson(identity,Identity.class)),
                                    listener,
                                    errorListener
                            );

            jsonRequest.setShouldCache(false);
            jsonRequest.setTag(tag);
            //add request to Volley que for execution
            queue.add(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendLocationPoint(LocationPoint point,
                                  Context context,
                                  RequestQueue queue,
                                  Object tag,
                                  Listener<JSONObject> listener,
                                  ErrorListener errorListener) {
        //prepare JSON builder obj
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();
        //Volley submission method
        int method = (Method.PUT);
        try {
            //prep request
            JsonObjectRequest jsonRequest =
                    new JsonObjectRequest
                            (
                                    method,
                                    LOCATION_POINT_URL,
                                    new JSONObject(gson.toJson(point,LocationPoint.class)),
                                    listener,
                                    errorListener
                            );
            jsonRequest.setShouldCache(false);
            jsonRequest.setTag(tag);
            //add request to Volley que for execution
            queue.add(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cacheCommuteKey(String key, Context context) {

        ((CommutrApp)context).setCommuteKey(key);
        //store in preferences
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        //store key
        editor.putString(context.getResources().getString(R.string.commutr_commute_key), key);
        editor.commit();
    }

    public String getCachedCommuteKey(Context context) {
        //mem first
        String key = ((CommutrApp)context).getCommuteKey();
        if(key == null) {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            key = settings.getString(context.getResources().getString(R.string.commutr_commute_key), null);
        }
        return key;
    }

    public void retrieveCommuteByKey( String key,
                               Context context,
                               RequestQueue queue,
                               Object tag,
                               Listener<JSONObject> listener,
                               ErrorListener errorListener) {
        int method = (Method.PUT);
        //prep request
        JsonObjectRequest jsonRequest =
                new JsonObjectRequest
                        (
                                method,
                                new StringBuilder().append(COMMUTE_CONFIRMATION_URL).append(key).toString(),
                                null,
                                listener,
                                errorListener
                        );

        jsonRequest.setShouldCache(false);
        jsonRequest.setTag(tag);
        //add request to Volley que for execution
        queue.add(jsonRequest);
    }

    public void cacheCommuteRequestStatus(String state, Context context) {

        ((CommutrApp)context).setRequestState(state);
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        editor.putString(context.getResources().getString(R.string.commutr_commute_request_status), state);
        editor.commit();
    }

    public String getCachedCommuteRequestStatus(Context context) {
        //mem first
        String state = ((CommutrApp)context).getRequestState();
        if(state == null) {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            state = settings.getString(context.getResources().getString(R.string.commutr_commute_request_status), null);
        }
        return state;
    }

    public void cacheCheckInStatus(String state, Context context) {
        ((CommutrApp)context).setCheckInState(state);
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        editor.putString(context.getResources().getString(R.string.commutr_check_in_status), state);
        editor.commit();
    }

    public String getCachedCheckInStatus(Context context) {
        String state = ((CommutrApp)context).getCheckInState();
        if(state == null) {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            state = settings.getString(context.getResources().getString(R.string.commutr_check_in_status), null);
        }
        return state;
    }

    public void retrieveLocations(Context context,
                                  RequestQueue queue,
                                  Object tag,
                                  Listener<JSONObject> listener,
                                  ErrorListener errorListener) {
        int method = (Method.GET);
        JsonObjectRequest jsonRequest =
                new JsonObjectRequest
                        (
                                method,
                                LOCATIONS_URL,
                                null,
                                listener,
                                errorListener
                        );
        jsonRequest.setShouldCache(false);
        jsonRequest.setTag(tag);
        queue.add(jsonRequest);
    }

    public void cacheLocationRetrievalTimestamp(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        editor = settings.edit();
        editor.putLong(context.getResources().getString(R.string.commutr_location_retrieval_timestamp), System.currentTimeMillis());
        editor.commit();
    }

    public long getCachedLocationRetrievalTimestamp(Context context) {
        long timestamp = 0;
        SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
        timestamp = settings.getLong(context.getResources().getString(R.string.commutr_location_retrieval_timestamp), 0);
        return timestamp;
    }
}
