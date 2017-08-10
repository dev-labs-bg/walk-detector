package bg.devlabs.walkdetector;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import bg.devlabs.walkdetector.util.DateTimeHelper;
import bg.devlabs.walkdetector.util.PermissionsHelper;
import bg.devlabs.walkdetector.util.SettingsManager;
import bg.devlabs.walkdetector.util.SharedPreferencesHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static bg.devlabs.walkdetector.util.DateTimeHelper.isTimeValid;

/**
 * Created by Simona Stoyanova on 8/8/17.
 * simona@devlabs.bg
 * <p>
 * This is the UI from which a Walk Detector Service can be stopped or started
 * <p>
 * Once selected, the state is saved to shared preferences.
 * The state can e changed from the three dot Menu
 */
public class MainActivity extends AppCompatActivity implements PermissionsHelper.PermissionResultListener {
    // tag used for logging purposes
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.checked_period_edit_text)
    EditText checkedPeriodEditText;
    @BindView(R.id.detector_fab)
    FloatingActionButton detectorFab;
    boolean shouldDetect;
    @BindView(R.id.start_time_edit_text)
    EditText startTimeEditText;
    @BindView(R.id.end_time_edit_text)
    EditText endTimeEditText;
    @BindView(R.id.set_alarm_button)
    Button setAlarmButton;
    @BindView(R.id.alarm_info_layout)
    LinearLayout alarmInfoLayout;
    @BindView(R.id.all_day_check_box)
    CheckBox allDayCheckBox;

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
     * Checks if an alarm has been set and shows it or clicks all day`s check box
     */
    private void showCurrentState() {
        checkedPeriodEditText.setText(String.valueOf(SettingsManager.getInstance(this).checkedPeriodSeconds));
        updateButtonStates();
        boolean isAllDay = SettingsManager.getInstance(this).isAllDay;
        allDayCheckBox.setChecked(isAllDay);
        alarmInfoLayout.setVisibility(isAllDay ? View.INVISIBLE : View.VISIBLE);
        if (!isAllDay) {
            startTimeEditText.setText(SettingsManager.getInstance(this).startTime);
            endTimeEditText.setText(SettingsManager.getInstance(this).endTime);
        }
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
        if (!PermissionsHelper.checkPermissions(this)) {
            PermissionsHelper.requestPermissions(this);
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
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }

    @Override
    public void onPermissionGranted() {
        startDetection();
    }

    /**
     * Handled on permission denied by showing a Snack bar
     * If the Snack bar is clicked the user is sent to settings where the current app is selected
     * so that the user can easily grant the needed permissions
     */
    @SuppressLint("WrongViewCast")
    public void onPermissionDenied() {
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

    /**
     * Handling button clicks using Butterknife
     * @param view the view on which the user has clicked
     */
    @OnClick({R.id.update_check_period_button, R.id.detector_fab, R.id.set_alarm_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.update_check_period_button:
                onUpdateButtonClicked();
                break;
            case R.id.detector_fab:
                onDetectorStateFabClicked();
                break;
            case R.id.set_alarm_button:
                onAlarmButtonClicked();
                break;
        }
    }

    /**
     * Validates if the times are valid
     *
     * If valid:
     * Saves the new auto start and stop times
     * Sets the two alarms for them
     *
     * If invalid:
     * Shows a toast
     */
    private void onAlarmButtonClicked() {
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();
        if (!isTimeValid(startTime) || !isTimeValid(endTime)) {
            Toast.makeText(this, R.string.error_entered_invalid_time, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferencesHelper.saveStartTime(this, startTime);
        SharedPreferencesHelper.saveEndTime(this, endTime);
        setAlarms(startTime, endTime);
    }

    /**
     * Sets the two alarms - one for starting and one for stopping the detection service
     * The alarms are triggered once a day by sending an intent to the service
     * @param startTime at this time an intent will be sent to start the walk activity detection
     * @param endTime at this time an intent will be sent to stop the walk activity detection
     */
    private void setAlarms(String startTime, String endTime) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setAlarm(am,"start", 0, startTime);
        setAlarm(am,"stop", 1, endTime);
        Toast.makeText(this, R.string.set_auto_turn_on_and_off, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param am The system's Alarm Manager
     * @param value what value should be sent to the Service trough the Intent Extras - start or stop
     * @param requestCode each request should have different code
     *                    or else they will override each other
     * @param time at this time each day a intent will be sent to the Service
     */
    public void setAlarm(AlarmManager am, String value, int requestCode, String time) {
        Intent startDetectionIntent = new Intent(this, WalkDetectService.class);
        startDetectionIntent.putExtra("Alarm", value);
        PendingIntent pi = PendingIntent.getService(this, requestCode,
                startDetectionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, DateTimeHelper.getCalendarTimeMillis(time),
                AlarmManager.INTERVAL_DAY, pi);

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

    /**
     * Clears focus of the button
     * Validates the entered check period - if it is too short or too long
     * a toast is shown to the user to warn him
     *
     * The new period is sent to the Setting Manager where all other configuration values are updated
     * and the new setting is saved to the storage
     *
     * If the user has entered an invalid number a toast is shown to inform him
     */
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

    /**
     * Hides the software keyboard
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Handles check state changes on the all_day_check_box
     * Hides and clears the alarm info if the checkbox is checked
     *
     * If it is unchecked shows the alarmInfoLayout, in which the user can enter start and end times
     * for auto stop and start of the detector service
     * @param checked - the new state
     */
    @OnCheckedChanged(R.id.all_day_check_box)
    void onChecked(boolean checked) {
        if (checked) {
            SharedPreferencesHelper.saveStartTime(this, SharedPreferencesHelper.DEFAULT_TIME);
            SharedPreferencesHelper.saveEndTime(this, SharedPreferencesHelper.DEFAULT_TIME);
            startTimeEditText.setText("");
            endTimeEditText.setText("");
        }
        alarmInfoLayout.setVisibility(checked ? View.INVISIBLE : View.VISIBLE);
    }
}