package android.commutr.com.commutr.services;

import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.utils.Alarms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by oviroa on 2/6/15.
 * Runs when phine reboots
 * Resets alarms
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Commute commute = DataManager.getInstance().getCachedCommute(context.getApplicationContext());
        if(commute != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(commute.getScheduledPickupArrivalTime() * 1000);
            Alarms.registerLocationAlarms(context.getApplicationContext(), calendar);
        }
    }
}
