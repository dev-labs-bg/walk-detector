package bg.devlabs.walkdetector.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import bg.devlabs.walkdetector.R;
/**
 * Created by Simona Stoyanova on 8/9/17.
 * simona@devlabs.bg
 * <p>
 * A helper class which simplifies Permission checking and requesting
 */

public class PermissionsHelper {
    // tag used for logging purposes
    private static final String TAG = PermissionsHelper.class.getSimpleName();
    // request code for permissions
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    /**
     * Return the current state of the permissions needed.
     */
    public static boolean checkPermissions(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("WrongViewCast")
    public static void requestPermissions(Activity activity) {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.d(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    activity.findViewById(R.id.main_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.d(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    public static void onRequestPermissionsResult(int requestCode,
                                           @NonNull int[] grantResults,
                                                  PermissionResultListener listener) {
        Log.d(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.d(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                listener.onPermissionGranted();
            } else {
                listener.onPermissionDenied();
            }
        }
    }

    /**
     * This methods are called in onRequestPermissionResult depending on the grantResults
     */
    public   interface PermissionResultListener {
         void onPermissionGranted();
         void onPermissionDenied();
    }
}
