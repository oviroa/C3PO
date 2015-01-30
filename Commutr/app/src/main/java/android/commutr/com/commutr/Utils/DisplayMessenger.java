package android.commutr.com.commutr.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by oviroa on 1/24/15.
 * Used to display messages on UI
 */
public class DisplayMessenger {

    public static void showBasicToast(Context context, String message)
    {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 280);
        toast.show();
    }

}
