package android.commutr.com.commutr.Utils;

import android.util.Log;

/**
 * Created by oviroa on 1/23/15.
 * Logger utility
 */
public class Logger {


    public static String logPrefix = "COMMUTR :: ";

    public static void warn(String header, String body){

        Log.w(logPrefix + header, body);
    }


}