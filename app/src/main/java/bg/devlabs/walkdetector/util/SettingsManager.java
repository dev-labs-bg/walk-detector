package bg.devlabs.walkdetector.util;

import android.content.Context;

import static bg.devlabs.walkdetector.util.SharedPreferencesHelper.DEFAULT_TIME;

/**
 * Created by Simona Stoyanova on 8/9/17.
 * simona@devlabs.bg
 * <p>
 * Singleton class which handles check period read/ write and store operations
 * The moment the instance is created it reads the saved check period from storage
 */

public class SettingsManager {
    private static SettingsManager ourInstance;

    // How much will the app wait for response until a timeout exception is thrown
    public static final int AWAIT_PERIOD_SECONDS = 60; // 60 seconds = 1 min
    // Walking slow (2 mph)	67 steps per minute which is almost one step per second
    private static final int SLOW_WALKING_STEPS_PER_SECOND = 1;

    // How long will the checked for walking activity period be
    public int checkedPeriodSeconds = 180; //180 seconds = 3 minutes
    // How often will the app query the client for walking activity
    public int observablePeriodSeconds = checkedPeriodSeconds + AWAIT_PERIOD_SECONDS;
    // The calculated amount of steps if the user was walking during the checked period of time
    // For example 180 seconds * 1 step at a second = 180 steps
    // This value is used to determine if the user was walking trough the checked period of time
    public int neededStepsCountForWalking = checkedPeriodSeconds * SLOW_WALKING_STEPS_PER_SECOND;
    // should the detection run all day
    public boolean isAllDay;
    //if there is an alarm set the detection will work from startTime to endTime
    public String startTime, endTime;

    public static SettingsManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new SettingsManager(context);
        }
        return ourInstance;
    }

    /**
     * Reads the saved value for checked period
     *
     * @param context needed for shared preferences read operations
     */
    private SettingsManager(Context context) {
        checkedPeriodSeconds = SharedPreferencesHelper.readCheckPeriod(context);
        startTime = SharedPreferencesHelper.readStartTime(context);
        endTime = SharedPreferencesHelper.readEndTime(context);

        //check if hte is all day is active or the user has set an alarm
        isAllDay = startTime.equals(DEFAULT_TIME) && endTime.equals(DEFAULT_TIME);
    }

    public void saveNewCheckPeriod(Context context, int checkPeriod) {
        SharedPreferencesHelper.saveCheckPeriod(context, checkPeriod);
        checkedPeriodSeconds = checkPeriod;
    }
}
