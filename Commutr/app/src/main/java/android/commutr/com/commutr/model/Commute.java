package android.commutr.com.commutr.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by oviroa on 1/29/15.
 */
public class Commute implements Serializable
{
//            'device_identifier': 'string - the unique installation id',
//            'email': 'string - user email',
//            'unique_id': 'string - format (very important) is "device_identifier|2014-01-01 AM"',
//            'transport_mode_to_pickup': 'int - 1 = walk, 2 = bike, 5 = driver_or_passenger'
//            'transport_mode_to_dropoff' : 'int - 3 = driver, 4 = passenger',
//            'pickup_location': 'string - ORND1 for the real one and TEST for testing',
//            'dropoff_location': 'string - SFFIDI',
//            'scheduled_pickup_arrival_time': 'int - unix timestamp of the selected scheduled pickup arrival time'
//            'confirm_time': 'int - unix timestamp of the moment the confirm button was pressed'

    private String device_identifier;

    private String email;

    private String unique_id;

    private int transport_mode_to_pickup;

    private int transport_mode_to_dropoff;

    private String pickup_location;

    private String dropoff_location;

    private long scheduled_pickup_arrival_time;

    private long confirm_time;

    private long cancel_time;

    public long getCancelTime() {
        return cancel_time;
    }

    public void setCancelTime(Long cancel_time){
        this.cancel_time = cancel_time;
    }


    public void setDeviceIdentifier(String device_identifier) {
        this.device_identifier = device_identifier;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a");
        Calendar calendar = Calendar.getInstance();
        String currentDate = sdf.format(calendar.getTimeInMillis());
        this.unique_id = device_identifier + "|" + currentDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTransportModeToPickup(int transport_mode_to_pickup) {
        this.transport_mode_to_pickup = transport_mode_to_pickup;
    }

    public void setTransportModeToDropoff(int transport_mode_to_dropoff) {
        this.transport_mode_to_dropoff = transport_mode_to_dropoff;
    }

    public void setPickupLocation(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public void setDropoffLocation(String dropoff_location) {
        this.dropoff_location = dropoff_location;
    }

    public void setScheduledPickupArrivalTime(long scheduled_pickup_arrival_time) {
        this.scheduled_pickup_arrival_time = scheduled_pickup_arrival_time;
    }

    public void setConfirmTime(long confirm_time) {
        this.confirm_time = confirm_time;
    }

    public String getDeviceIdentifier() {
        return device_identifier;
    }

    public String getEmail() {
        return email;
    }

    public int getTransportModeToPickup() {
        return transport_mode_to_pickup;
    }

    public int getTransportModeToDropoff() {
        return transport_mode_to_dropoff;
    }

    public String getPickupLocation() {
        return pickup_location;
    }

    public String getDropoffLocation() {
        return dropoff_location;
    }

    public long getScheduledPickupArrivalTime() {
        return scheduled_pickup_arrival_time;
    }

    public long getConfirmTime() {
        return confirm_time;
    }


    public Commute() {
    }
}
