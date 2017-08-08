package bg.devlabs.walkdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import bg.devlabs.walkdetector.util.SharedPreferencesHelper;

/**
 * Created by Simona Stoyanova on 8/8/17.
 * simona@devlabs.bg
 * <p>
 * This is the UI from which a Walk Detector Service can be stopped or started
 * <p>
 * Once selected, the state is saved to shared preferences.
 * The state can e changed from the three dot Menu
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    //the two menu items, this references are needed in order to update their state later
    MenuItem registerMenuItem;
    MenuItem unregisterMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        registerMenuItem = menu.findItem(R.id.action_register_listener);
        unregisterMenuItem = menu.findItem(R.id.action_unregister_listener);
        fixButtonStates();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_unregister_listener) {
            stopDetection();
            return true;
        }
        if (id == R.id.action_register_listener) {
            startDetection();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startDetection() {
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }
        enableStopDetectionMenuItem();

        SharedPreferencesHelper.saveTrackStatusPrefs(this, true);
        //starting the service with the startDetection command
        Intent intent = new Intent(this, WalkDetectService.class);
        startService(intent);
    }

    private void stopDetection() {
        enableDetectionMenuItem();
        SharedPreferencesHelper.saveTrackStatusPrefs(this, false);
        //starting the service with the stopDetection command
        Intent intent = new Intent(this, WalkDetectService.class);
        startService(intent);
    }

    /**
     * Fixes button states depending on the saved to Shared preferences should detect status - true or false
     */
    private void fixButtonStates() {
        if (SharedPreferencesHelper.shouldTrack(this)) {
            enableStopDetectionMenuItem();
        } else {
            enableDetectionMenuItem();
        }
    }

    private void enableDetectionMenuItem() {
        registerMenuItem.setEnabled(true);
        unregisterMenuItem.setEnabled(false);
    }

    private void enableStopDetectionMenuItem() {
        registerMenuItem.setEnabled(false);
        unregisterMenuItem.setEnabled(true);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("WrongViewCast")
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.d(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.d(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.d(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                startDetection();
            } else {
                onPermissionDenied();
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private void onPermissionDenied() {
        // Permission denied.
        // In this Activity we've chosen to notify the user that they
        // have rejected a core permission for the app since it makes the Activity useless.
        // We're communicating this message in a Snackbar since this is a sample app, but
        // core permissions would typically be best requested during a welcome-screen flow.
        // Additionally, it is important to remember that a permission might have been
        // rejected without asking the user for permission (device policy or "Never ask
        // again" prompts). Therefore, a user interface affordance is typically implemented
        // when permissions are denied. Otherwise, your app could appear unresponsive to
        // touches or interactions which have required permissions.
        Snackbar.make(
                findViewById(R.id.main_activity_view),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, view -> {
                    // Build intent that displays the App settings screen.
                    Intent intent = new Intent();
                    intent.setAction(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .show();
    }
}
