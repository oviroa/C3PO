package android.commutr.com.commutr.managers;

import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.utils.Logger;
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

    /**
     * Retrieves singleton instance
     * @return
     */
    public static synchronized DataManager getInstance()
    {
        if (ref == null)
        {	// it's ok, we can call this constructor
            ref = new DataManager();
        }
        return ref;
    }

    /**
     * Do not allow constructor to function
     */
    private DataManager()
    {

    }

    /**
     * Disallows cloning
     */
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }



    public void storeUserEmail(String userEmail, Context context){

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

        try
        {
            Logger.warn("SENDING",(new JSONObject(gson.toJson(commute,Commute.class))).toString());

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

        }
        catch (JSONException e)
        {
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

    public Commute getCachedCommute(Context context)
    {
        //store in mem first
        Commute commute = ((CommutrApp)context).getCurrentCommute();


        if(commute == null)
        {
            SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.commutr_preferences), 0);
            String tripJson = settings.getString(context.getResources().getString(R.string.commutr_current_commute), null);

            //make json
            Gson gson = new Gson();
            commute = gson.fromJson(tripJson, Commute.class);

        }

        return commute;
    }
}
