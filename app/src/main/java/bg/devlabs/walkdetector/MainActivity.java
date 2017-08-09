package bg.devlabs.walkdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import bg.devlabs.walkdetector.util.SettingsManager;
import bg.devlabs.walkdetector.util.SharedPreferencesHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    // tag used for logging purposes
    private static final String TAG = MainActivity.class.getSimpleName();
    // request code for permissions
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @BindView(R.id.checked_period_edit_text)
    EditText checkedPeriodEditText;
    @BindView(R.id.detector_fab)
    FloatingActionButton detectorFab;
    boolean shouldDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        showCurrentState();
    }

    /**
     * Shows the saved check period edit text
     * Shows the correct state of the FAB depending on if the detector is started or stopped
     */
    private void showCurrentState() {
        checkedPeriodEditText.setText(String.valueOf(SettingsManager.getInstance(this).CHECKED_PERIOD_SECOND));
        updateButtonStates();
    }

    /**
     * Checks for permissions and request them if needed
     * Updates button icon to stop
     * Saves the new status to Shared Preferences
     * Calls the WalkDetectService, which start detecting
     */
    private void startDetection() {
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }
        showStopIcon();
        SharedPreferencesHelper.saveShouldDetectStatus(this, true);
        //starting the service with the startDetection command
        Intent intent = new Intent(this, WalkDetectService.class);
        startService(intent);
    }

    /**
     * Animates start icon to stop icon
     */
    private void showStopIcon() {
        AnimatedVectorDrawable animatedVectorDrawable =
                (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_stop);
        if (animatedVectorDrawable != null) {
            detectorFab.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.start();
        }
    }

    /**
     * Animates stop icon to start icon
     */
    private void showPlayIcon() {
        AnimatedVectorDrawable animatedVectorDrawable =
                (AnimatedVectorDrawable) getDrawable(R.drawable.avd_stop_to_play);
        if (animatedVectorDrawable != null) {
            detectorFab.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.start();
        }
    }

    /**
     * Updates button icon to start
     * Saves the new status to Shared Preferences
     * Calls the WalkDetectService, which start detecting
     */
    private void stopDetection() {
        showPlayIcon();
        SharedPreferencesHelper.saveShouldDetectStatus(this, false);
        //starting the service with the stopDetection command
        Intent intent = new Intent(this, WalkDetectService.class);
        startService(intent);
    }

    /**
     * Updates button states depending on the saved to Shared preferences should detect status
     */
    private void updateButtonStates() {
        if (SharedPreferencesHelper.shouldDetectWalking(this)) {
            shouldDetect = true;
            detectorFab.setImageResource(R.drawable.ic_stop_white_24dp);
        } else {
            shouldDetect = false;
            detectorFab.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
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
        // We're communicating this message in a Snack bar since this is a sample app, but
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

    @OnClick({R.id.update_check_period_button, R.id.detector_fab})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.update_check_period_button:
                onUpdateButtonClicked();
                break;
            case R.id.detector_fab:
                onDetectorStateFabClicked();
                break;
        }
    }

    /**
     * Stops or starts detection depending on the current state
     */
    private void onDetectorStateFabClicked() {
        if (shouldDetect) {
            stopDetection();
        } else {
            startDetection();
        }
        shouldDetect = !shouldDetect;
    }

    private void onUpdateButtonClicked() {
        checkedPeriodEditText.clearFocus();
        hideKeyboard();
        try {
            int checkPeriod = Integer.valueOf(checkedPeriodEditText.getText().toString());
            if (checkPeriod < 60 || checkPeriod > 600) {
                Toast.makeText(this, R.string.error_check_period_not_recommended, Toast.LENGTH_SHORT).show();
            }
            SettingsManager.getInstance(this).saveNewCheckPeriod(this, checkPeriod);
            Toast.makeText(this, R.string.check_period_updated, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_parsing_check_period, Toast.LENGTH_SHORT).show();
        }

    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
