package android.commutr.com.commutr.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by oviroa on 1/24/15.
 * Used to poll various client settings or states
 */
public class ClientUtility {

    private static String TEST_DOMAIN = "@getcommutr.com";

    /**
     * Returns true if device contected or connecting to the Internet
     * @param context Context
     * @return Boolean
     */
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Retrieves emails for user from account list
     * @param context
     * @return
     */
    public static List<String> getUserEmails(Context context){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        List<String> emailList = new ArrayList<String>();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                if(!emailList.contains(account.name)) {
                    emailList.add(account.name);
                }
            }
        }
        return emailList;
    }

    /**
     * Returns true if location on
     * @param context Context
     * @return Boolean
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    public static Boolean isLocationAllowed(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Secure.getString(context.getContentResolver(),
                    Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Secure.getInt(context.getContentResolver(),
                        Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {
                case Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Secure.LOCATION_MODE_BATTERY_SAVING:
                case Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }

    /**
     * Returns true if email is valid
     * @param editable
     * @return
     */
    public static Boolean isEmailValid(Editable editable) {
        if (editable == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(editable).matches();
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(Logger.LOG_PREFIX, Log.DEBUG)) {
                Logger.debug("Google Play services","available");
            }
            return true;
        } else {
            Logger.error("Google Play services","unavailable");
            return false;
        }
    }

    public static boolean isSuperUser(String email) {
        return email.contains(TEST_DOMAIN);
    }
}
