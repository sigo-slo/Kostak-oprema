package si.kostakdd.update;

import android.content.Context;
import android.util.Log;

import si.kostakdd.Constants;

public class UpdateChecker {


    public static void checkForDialog(Context context) {
        if (context != null) {
            new CheckUpdateTask(context, Constants.TYPE_DIALOG, true).execute();
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }
    }


    public static void checkForNotification(Context context) {
        if (context != null) {
            new CheckUpdateTask(context, Constants.TYPE_NOTIFICATION, false).execute();
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }

    }


}
