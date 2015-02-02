package android.commutr.com.commutr.model;

import java.io.Serializable;

/**
 * Created by oviroa on 2/1/15.
 */
public class Identity implements Serializable {

    private String email;

    private String version;

    private String identifier;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {

        return email;
    }

    public String getVersion() {
        return version;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Identity() {
    }
}
