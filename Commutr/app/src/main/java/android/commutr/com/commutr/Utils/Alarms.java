package android.commutr.com.commutr.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.services.ActivityRecognitionConnectingService;
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
        unRegisterLocationAlarms(appContext);
        stopActivityRecognition(appContext);
        registerLocationTrackingStart(appContext, commuteCalendar);
        registerLocationTrackingEnd(appContext, commuteCalendar);
    }

    public static void unRegisterLocationAlarms(Context appContext) {
        stopLocationTrackingService(appContext);
        stopActivityRecognition(appContext);
        unregisterLocationTrackingStart(appContext);
        unregisterLocationTrackingEnd(appContext);
    }

    public static void stopActivityRecognition(Context context) {
        final Intent recognitionIntent = new Intent(context, ActivityRecognitionConnectingService.class);
        recognitionIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.DISCONNECT);
        context.startService(recognitionIntent);
    }

    public static void startActivityRecognition(Context context) {
        final Intent recognitionIntent = new Intent(context, ActivityRecognitionConnectingService.class);
        recognitionIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.CONNECT);
        context.startService(recognitionIntent);
    }

    private static void startLocationTrackingService(Context appContext) {
        final Intent locationIntent = new Intent(appContext,LocationSubmissionService.class);
        locationIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.CONNECT);
        appContext.startService(locationIntent);
    }

    private static void stopLocationTrackingService(Context appContext) {
        final Intent locationIntent = new Intent(appContext,LocationSubmissionService.class);
        locationIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.DISCONNECT);
        appContext.startService(locationIntent);
    }


    private static void registerStartAlarm(Context appContext, Calendar calendar) {
        Intent alarmStartIntent = new Intent(CommutrApp.CONNECT);
        alarmStartIntent.setClass(appContext, LocationSubmissionService.class);
        alarmStartIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.CONNECT);
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

    private static void registerEndAlarm(Context appContext, Calendar calendar) {
        Intent alarmEndIntent = new Intent(CommutrApp.DISCONNECT);
        alarmEndIntent.setClass(appContext, LocationSubmissionService.class);
        alarmEndIntent.putExtra(CommutrApp.ACTION_TYPE, CommutrApp.DISCONNECT);
        ((AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE)).set
                (
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        PendingIntent.getService
                                (
                                        appContext,
                                        0,
                                        alarmEndIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT)
                );
    }

    private static void registerLocationTrackingStart(Context appContext, Calendar commuteCalendar) {
        Calendar calendarStart = Calendar.getInstance();
        //if today
        if(DateUtils.isToday(commuteCalendar.getTimeInMillis())){
            //if current time is past 6 but before 8
            if(calendarStart.get(Calendar.HOUR_OF_DAY) > appContext.getResources().getInteger(R.integer.earliest_location_set_time)){
                startLocationTrackingService(appContext);
            } else {
                //start alarm at 6
                calendarStart.set(Calendar.HOUR_OF_DAY, appContext.getResources().getInteger(R.integer.earliest_location_set_time));
                calendarStart.set(Calendar.MINUTE,0);
                calendarStart.set(Calendar.SECOND,0);
                Logger.warn("REGISTER","CALENDAR START");
//                calendarStart.set(Calendar.HOUR_OF_DAY,15);
//                calendarStart.set(Calendar.MINUTE,57);
//                calendarStart.set(Calendar.SECOND,0);
//                registerStartAlarm(appContext, calendarStart);
            }
        } else {

            //start alarm at 6
//            calendarStart = commuteCalendar;
//            calendarStart.set(Calendar.HOUR_OF_DAY, appContext.getResources().getInteger(R.integer.earliest_location_set_time));
//            calendarStart.set(Calendar.MINUTE,0);
//            calendarStart.set(Calendar.SECOND,0);
            Logger.warn("REGISTER","CALENDAR START");
            calendarStart.set(Calendar.HOUR_OF_DAY,17);
            calendarStart.set(Calendar.MINUTE,47);
            calendarStart.set(Calendar.SECOND,0);
            registerStartAlarm(appContext, calendarStart);
        }

    }

    private static void registerLocationTrackingEnd(Context appContext, Calendar commuteCalendar) {
//        commuteCalendar.set(Calendar.HOUR_OF_DAY,appContext.getResources().getInteger(R.integer.latest_location_set_time));
//        commuteCalendar.set(Calendar.MINUTE,0);
//        commuteCalendar.set(Calendar.SECOND,0);
//        registerEndAlarm(appContext, commuteCalendar);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(Calendar.HOUR_OF_DAY,21);
        calendarEnd.set(Calendar.MINUTE,30);
        calendarEnd.set(Calendar.SECOND,0);
        registerEndAlarm(appContext, calendarEnd);
    }

    private static void unregisterLocationTrackingStart(Context appContext) {
        Intent alarmStartIntent = new Intent(CommutrApp.CONNECT);
        alarmStartIntent.setClass(appContext, LocationSubmissionService.class);
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
        //alarm intent
        Intent alarmEndIntent = new Intent(CommutrApp.DISCONNECT);
        alarmEndIntent.setClass(appContext, LocationSubmissionService.class);
        ((AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE)).cancel
                (
                        PendingIntent.getService
                                (
                                        appContext,
                                        0,
                                        alarmEndIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                );
    }

}
