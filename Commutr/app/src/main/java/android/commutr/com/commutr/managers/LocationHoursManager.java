package android.commutr.com.commutr.managers;

import android.commutr.com.commutr.model.LocationHour;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by oviroa on 2/16/15.
 */
public class LocationHoursManager {

    private static Calendar commuteStartDate(Calendar calendar) {
        Calendar referenceDate = Calendar.getInstance();
        referenceDate.setTimeInMillis(calendar.getTimeInMillis());
        if(referenceDate.get(Calendar.HOUR_OF_DAY) < 12) {
            //AM
            referenceDate.set(Calendar.HOUR_OF_DAY,0);
        } else {
            //PM
            referenceDate.set(Calendar.HOUR_OF_DAY,12);
        }
        referenceDate.set(Calendar.MINUTE,0);
        referenceDate.set(Calendar.SECOND,0);
        return referenceDate;
    }

    private static Calendar commuteEndDate(Calendar calendar) {
        Calendar referenceDate = Calendar.getInstance();
        referenceDate.setTimeInMillis(calendar.getTimeInMillis());
        if(referenceDate.get(Calendar.HOUR_OF_DAY) < 12) {
            //AM
            referenceDate.set(Calendar.HOUR_OF_DAY,11);
        } else {
            //PM
            referenceDate.set(Calendar.HOUR_OF_DAY,23);
        }
        referenceDate.set(Calendar.MINUTE,59);
        referenceDate.set(Calendar.SECOND,59);
        return referenceDate;
    }

    public static  List<LocationHour> getOpenLocationHours(Calendar commuteTime) {
        List<LocationHour> locations= LocationHour.listAll(LocationHour.class);
        List<LocationHour> validLocations = new ArrayList<LocationHour>();
        for (LocationHour location : locations) {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(location.getStartTime());
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(location.getEndTime());
            Calendar commuteStartDate = commuteStartDate(commuteTime);
            Calendar commuteEndDate = commuteEndDate(commuteTime);
            if (location.isEnclosedInRange(commuteStartDate, commuteEndDate)){
                validLocations.add(location);
            }
        }
        return validLocations;
    }

}
