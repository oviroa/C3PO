package android.commutr.com.commutr.utils;

import android.util.Log;

/**
 * Created by oviroa on 1/23/15.
 * Logger utility
 */
public class Logger {
    public static String LOG_PREFIX = "COMMUTR :: ";
    public static void warn(String header, String body){
        Log.w(new StringBuilder().append(LOG_PREFIX).append(header).toString(), body);
    }

    public static void debug(String header, String body){
        Log.d(new StringBuilder().append(LOG_PREFIX).append(header).toString(), body);
    }

    public static void error(String header, String body){
        Log.e(new StringBuilder().append(LOG_PREFIX).append(header).toString(), body);
    }

    public static void longInfo(String str) {
        if(str.length() > 4000) {
            Logger.warn("RESPONSE",str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Logger.warn("RESPONSE", str);
    }
}
