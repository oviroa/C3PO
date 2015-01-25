package android.commutr.com.commutr;

import android.app.Application;
import android.commutr.com.commutr.utils.Logger;

/**
 * Created by oviroa on 1/23/15.
 * Application singleton
 * Used to store state and context of the application when running
 */
public class CommutrApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.warn("Application","started");
    }

}
