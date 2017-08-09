package bg.devlabs.walkdetector.util;

import android.content.Context;

/**
 * Created by Simona Stoyanova on 8/9/17.
 * simona@devlabs.bg
 * <p>
 * Singleton class which handles check period read/ write and store operations
 * The moment the instance is created it reads the saved check period from storage
 */

public class SettingsManager {
    private static SettingsManager ourInstance;

    // How long will the checked for walking activity period be
    public int CHECKED_PERIOD_SECOND = 180; //180 seconds = 3 minutes
    // How much will the app wait for response until a timeout exception is thrown
    public static final int AWAIT_PERIOD_SECOND = 60; // 60 seconds = 1 min
    // How often will the app query the client for walking activity
    public int OBSERVABLE_PERIOD_SECOND = CHECKED_PERIOD_SECOND + AWAIT_PERIOD_SECOND;
    // Walking slow (2 mph)	67 steps per minute which is almost one step per second
    private static final int SLOW_WALKING_STEPS_PER_SECOND = 1;
    // The calculated amount of steps if the user was walking during the checked period of time
    // For example 180 seconds * 1 step at a second = 180 steps
    // This value is used to determine if the user was walking trough the checked period of time
    public int COUNT_STEPS_WALKING = CHECKED_PERIOD_SECOND * SLOW_WALKING_STEPS_PER_SECOND;


    public static SettingsManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new SettingsManager(context);
        }
        return ourInstance;
    }

    /**
     * Reads the saved value for checked period
     * @param context needed for shared preferences read operations
     */
    private SettingsManager(Context context) {
        CHECKED_PERIOD_SECOND = SharedPreferencesHelper.readCheckPeriod(context);
    }

    public void saveNewCheckPeriod(Context context, int checkPeriod) {
        SharedPreferencesHelper.saveCheckPeriod(context, checkPeriod);
        CHECKED_PERIOD_SECOND = checkPeriod;
    }
}
