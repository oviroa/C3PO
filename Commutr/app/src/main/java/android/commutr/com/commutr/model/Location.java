package android.commutr.com.commutr.model;

import com.orm.SugarRecord;

/**
 * Created by oviroa on 2/12/15.
 */
public class Location extends SugarRecord<Location> {

    private float latitude;
    private float longitude;
    private String code;
    private String name;
    private boolean is_public;

    public boolean isPublic() {
        return is_public;
    }

    public void setPublic(boolean isPublic) {

        this.is_public = isPublic;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Location() {
    }

}
