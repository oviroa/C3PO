package android.commutr.com.commutr.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.services.LocationSubmissionService;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.util.Calendar;

/**
 * Created by oviroa on 2/3/15.
 * Registers alarms for various calendar events
 */
public class Alarms {

    public static void registerLocationAlarms(Context appContext, Calendar commuteCalendar) {


        registerLocationTrackingStart(appContext, commuteCalendar);
        registerLocationTrackingEnd(appContext, commuteCalendar);

    }

    public static void unRegisterLocationAlarms(Context appContext) {

        unregisterLocationTrackingStart(appContext);
        unregisterLocationTrackingEnd(appContext);

    }


    private static void startLocationTrackingService(Context appContext) {

        final Intent locationIntent = new Intent(appContext,LocationSubmissionService.class);
        appContext.startService(locationIntent);
    }


    private static void registerStartAlarm(Context appContext, Calendar calendar) {

        //alarm intent
        Intent alarmStartIntent = new Intent(appContext, LocationSubmissionService.class);

        alarmStartIntent.putExtra("ACTION_TYPE","CONNECT");

        ((AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE)).set
                (
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        PendingIntent.getService
                                (
                                        appContext,
                                        0,
                                        alarmStartIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT)
                );

    }



    private static void registerLocationTrackingStart(Context appContext, Calendar commuteCalendar) {


        Calendar calendarStart = Calendar.getInstance();

        //if today
        if(DateUtils.isToday(commuteCalendar.getTimeInMillis())){

            Logger.warn("today","wha?");

            //if current time is past 6 but before 8
            if(calendarStart.get(Calendar.HOUR_OF_DAY) > appContext.getResources().getInteger(R.integer.earliest_location_set_time)){

                startLocationTrackingService(appContext);

            } else {
                //start alarm at 6
                calendarStart.set(Calendar.HOUR_OF_DAY, appContext.getResources().getInteger(R.integer.earliest_location_set_time));
                calendarStart.set(Calendar.MINUTE,0);
                registerStartAlarm(appContext, calendarStart);
            }

        } else {

            //start alarm at 6
//            calendarStart = commuteCalendar;
//            calendarStart.set(Calendar.HOUR_OF_DAY, appContext.getResources().getInteger(R.integer.earliest_location_set_time));
//            calendarStart.set(Calendar.MINUTE,0);

            calendarStart.set(Calendar.HOUR_OF_DAY,23);
            calendarStart.set(Calendar.MINUTE,00);

            registerStartAlarm(appContext, calendarStart);

            //unregisterLocationTrackingStart(appContext);

        }

    }

    private static void registerLocationTrackingEnd(Context appContext, Calendar commuteCalendar) {


    }

    private static void unregisterLocationTrackingStart(Context appContext) {

        //alarm intent
        Intent alarmStartIntent = new Intent(appContext, LocationSubmissionService.class);

        ((AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE)).cancel
                (
                        PendingIntent.getService
                                (
                                        appContext,
                                        0,
                                        alarmStartIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                );

    }

    private static void unregisterLocationTrackingEnd(Context appContext) {

    }

}
