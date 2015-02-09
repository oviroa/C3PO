package android.commutr.com.commutr.model;

/**
 * Created by oviroa on 2/8/15.
 */
public class TurboCommute {
    public long getEstimatedPickupDepartureTime() {
        return estimated_pickup_departure_time;
    }

    public long getEstimatedPickupWaitTime() {
        return estimated_pickup_wait_time;
    }

    public String getKey() {
        return key;
    }

    public long getSystemCancelTimestamp() {
        return system_cancel_timestamp;
    }

    public long getSystemConfirmTimestamp() {
        return system_confirm_timestamp;
    }

    public long getSystemWaitlistTimestamp() {
        return system_waitlist_timestamp;
    }

    public String getUniqueId() {
        return unique_id;
    }

    public void setEstimatedPickupDepartureTime(long estimated_pickup_departure_time) {
        this.estimated_pickup_departure_time = estimated_pickup_departure_time;
    }

    public void setEstimatedPickupWaitTime(long estimated_pickup_wait_time) {
        this.estimated_pickup_wait_time = estimated_pickup_wait_time;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSystemCancelTimestamp(long system_cancel_timestamp) {
        this.system_cancel_timestamp = system_cancel_timestamp;
    }

    public void setSystemConfirmTimestamp(long system_confirm_timestamp) {
        this.system_confirm_timestamp = system_confirm_timestamp;
    }

    public void setSystemWaitlistTimestamp(long system_waitlist_timestamp) {
        this.system_waitlist_timestamp = system_waitlist_timestamp;
    }

    public void setUniqueId(String unique_id) {
        this.unique_id = unique_id;
    }

    private long estimated_pickup_departure_time;
    private long estimated_pickup_wait_time;
    private String key;
    private long system_cancel_timestamp;
    private long system_confirm_timestamp;
    private long system_waitlist_timestamp;
    private String unique_id;
}
