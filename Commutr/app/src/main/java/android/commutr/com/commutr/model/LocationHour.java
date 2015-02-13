package android.commutr.com.commutr.model;

import com.orm.SugarRecord;

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
}
