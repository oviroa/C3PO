package android.commutr.com.commutr.model;

/**
 * Created by oviroa on 2/5/15.
 */
public class LocationPoint {

//    {
//        'device_identifier': 'string - the unique installation id', (required)
//            "timestamp" : 'int - unix timestamp when the point was recorded', (required)
//            "lat" : 'string: "37.845231"' (float might work but im not sure) (required)
//            "lon" : 'string: "-122.138371"' (float might work but im not sure) (required)
//            "speed" : "string: 10.0" (float might work but im not sure) (required)
//            "horizontal_accuracy" : "string: 10.0" (float might work but im not sure) (required)
//            "vertical_accuracy" : "string: 10.0" (float might work but im not sure)  (optional)
//            "course" : "string: 10.0" (float might work but im not sure) (optional)
//            "activity" : "string: N/A|Walking|Running|Automotive|Stationary|Cycling"  (optional)
//    }

    private String device_identifier;
    private int timestamp;
    private float lat;
    private float lon;
    private float speed;
    private float horizontal_accuracy;
    private float vertical_accuracy;
    private float course;
    private float activity;

    public void setDevice_identifier(String device_identifier) {
        this.device_identifier = device_identifier;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setHorizontalAccuracy(float horizontal_accuracy) {
        this.horizontal_accuracy = horizontal_accuracy;
    }

    public void setVerticalAccuracy(float vertical_accuracy) {
        this.vertical_accuracy = vertical_accuracy;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public void setActivity(float activity) {
        this.activity = activity;
    }

    public String getDeviceIdentifier() {

        return device_identifier;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public float getSpeed() {
        return speed;
    }

    public float getHorizontalAccuracy() {
        return horizontal_accuracy;
    }

    public float getVerticalAccuracy() {
        return vertical_accuracy;
    }

    public float getCourse() {
        return course;
    }

    public float getActivity() {
        return activity;
    }
}
