package android.commutr.com.commutr.model;

import com.orm.SugarRecord;

import java.util.Calendar;

/**
 * Created by oviroa on 2/12/15.
 */
public class LocationHour extends SugarRecord<LocationHour> {

    public LocationHour() {
    }

    private long start_time;
    private long end_time;
    private Location dropoff_location;
    private Location pickup_location;
    private boolean recurring;

    public long getStartTime() {
        return start_time;
    }

    public long getEndTime() {
        return end_time;
    }

    public Location getDropoffLocation() {
        return dropoff_location;
    }

    public Location getPickupLocation() {
        return pickup_location;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }

    public void setEndTime(long end_time) {
        this.end_time = end_time;
    }

    public void setDropoffLocation(Location dropoff_location) {
        this.dropoff_location = dropoff_location;
    }

    public void setPickupLocation(Location pickup_location) {
        this.pickup_location = pickup_location;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    private boolean isWithinRange(Calendar calendar) {
        // time is before our start time, recurring doesnt go back, so this is an easy NO
        if(calendar.getTimeInMillis() < this.getStartTime()*1000) {
            return false;
        }

        // not recurring
        if(!this.recurring) {
            return this.isWithinExactRange(calendar);
        }

        // recurring but not the same day of the week, dont even do the math
        if(calendar.get(Calendar.DAY_OF_WEEK) != startTimeWeekDay()) {
            return false;
        }

        // translate time to the same day as start and test if they are within bounds
        return (isWithinExactRange(translateTimeToStartDay(calendar)));
    }

    private boolean isWithinExactRange(Calendar calendar) {
        long timestamp = calendar.getTimeInMillis();
        return timestamp >= getStartTime() && timestamp <= getEndTime();
    }


    public boolean isEnclosedInRange(Calendar startTime, Calendar endTime) {
        // end time is before our start time, recurring doesn't go back, so this is an easy NO
        if(endTime.getTimeInMillis() < startTime.getTimeInMillis()) {
            return false;
        }
        // not recurring
        if(!this.isRecurring()) {
            return isEnclosedInExactRange(startTime, endTime);
        }
        // recurring but not the same day of the week, dont even do the math
        if(startTime.get(Calendar.DAY_OF_MONTH) != startTimeWeekDay()) {
            return false;
        }
        // translate time to the same day as start and test if they are within bounds
        return isEnclosedInExactRange(translateTimeToStartDay(startTime), translateTimeToStartDay(endTime));
    }

    private boolean isEnclosedInExactRange(Calendar startTime, Calendar endTime) {
        long startTimestamp = startTime.getTimeInMillis();
        long endTimestamp = endTime.getTimeInMillis();
        if(startTimestamp >= endTimestamp) {
            return false;
        }
        return startTimestamp <= this.getStartTime()*1000 &&  this.getEndTime()*1000 <= endTimestamp;
    }

    private Calendar translateTimeToStartDay(Calendar calendar) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(this.getStartTime()*100);
        return translateTime(calendar, startCalendar);
    }

    private Calendar translateTime(Calendar keepHMSTime, Calendar keepYMDTime) {
        keepHMSTime.set(Calendar.YEAR, keepYMDTime.get(Calendar.YEAR));
        keepHMSTime.set(Calendar.MONTH, keepYMDTime.get(Calendar.MONTH));
        return keepHMSTime;
    }

    Calendar startTimeTranslated(Calendar calendar) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(this.getStartTime()*1000);
        return (this.recurring
        ? translateTime(startCalendar, calendar)
        : startCalendar);
    }

    Calendar endTimeTranslated(Calendar calendar) {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(this.getEndTime()*1000);
        return (this.recurring
        ? translateTime(endCalendar, calendar)
        : endCalendar);
    }

    private int startTimeWeekDay() {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(this.getStartTime()*1000);
        return startCalendar.get(Calendar.DAY_OF_WEEK);
    }
}
