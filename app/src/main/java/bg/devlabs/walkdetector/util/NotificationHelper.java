package bg.devlabs.walkdetector.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import bg.devlabs.walkdetector.MainActivity;
import bg.devlabs.walkdetector.R;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

/**
 * Created by Simona Stoyanova on 8/9/17.
 * simona@devlabs.bg
 * <p>
 * A helper class which simplifies Shared preference read/write operations
 */

public class NotificationHelper {
    // tag used for logging purposes
    private static final String TAG = NotificationHelper.class.getSimpleName();
    private static final String APP_TO_OPEN_PACKAGE_NAME = "com.charitymilescm.android";

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void sendNotification(String message, Context context) {
        playNotificationSound(context);

        Log.d(TAG, "sendNotification " + message);
        // Create an explicit content Intent that starts the APP_TO_OPEN_PACKAGE_NAME.
        Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage(APP_TO_OPEN_PACKAGE_NAME);
        // If the app is not installed on the device the Home screen is opened
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
        NotificationCompat.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID);
        } else {
            // It is deprecated but the definition above is not supported on older versions
            builder = new NotificationCompat.Builder(context);
        }
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

    /**
     * Plays the default notification sound
     *
     * @param context needed to get the notification ringtone from RingtoneManager
     */
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
