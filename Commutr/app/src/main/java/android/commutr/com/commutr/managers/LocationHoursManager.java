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
        referenceDate.set(Calendar.MILLISECOND,0);
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
        referenceDate.set(Calendar.MILLISECOND,0);
        return referenceDate;
    }

    public static  List<LocationHour> getOpenLocationHours(Calendar commuteTime, String userEmail) {
        List<LocationHour> locations= LocationHour.listAll(LocationHour.class);
        List<LocationHour> validLocations = new ArrayList<LocationHour>();
        for (LocationHour locationHour : locations) {
            Calendar commuteStartDate = commuteStartDate(commuteTime);
            Calendar commuteEndDate = commuteEndDate(commuteTime);
            if (locationHour.isEnclosedInRange(commuteStartDate, commuteEndDate)
                    && locationHour.isVisible(userEmail)){
                validLocations.add(locationHour);
            }
        }
        return validLocations;
    }

}
