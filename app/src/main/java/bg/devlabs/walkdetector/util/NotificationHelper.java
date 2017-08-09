package bg.devlabs.walkdetector.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import bg.devlabs.walkdetector.MainActivity;
import bg.devlabs.walkdetector.R;

/**
 * Created by simona on 8/9/17.
 */

public class NotificationHelper {
    // tag used for logging purposes
    private static final String TAG = NotificationHelper.class.getSimpleName();

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void sendNotification(String message, Context context) {
        playNotificationSound(context);

        Log.d(TAG, "sendNotification " + message);
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage("com.charitymilescm.android");
        if (notificationIntent == null) {
            notificationIntent = new Intent(context, MainActivity.class);
        }

        notificationIntent.putExtra("notificationDetails", message);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
                .setColor(Color.RED)
                .setContentTitle(context.getString(R.string.walking_detected))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private static void playNotificationSound(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
