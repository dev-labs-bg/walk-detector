package bg.devlabs.walkdetector.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import bg.devlabs.walkdetector.R;

/**
 * Created by Simona Stoyanova on 8/8/17.
 * simona@devlabs.bg
 * <p>
 * A helper class which simplifies Shared preference read/write operations
 */
public class SharedPreferencesHelper {
    public static final String DEFAULT_TIME = "00:00";

    /**
     * @param context needed in order to access the Shared preferences API and in order to read String keys
     * @return whether the app should detect walking
     */
    public static boolean shouldDetectWalking(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getBoolean(context.getString(R.string.should_track_status), false);
    }

    /**
     * @param context     needed in order to access the Shared preferences API and in order to read String keys
     * @param shouldTrack whether the app should detect walking
     */
    @SuppressLint("ApplySharedPref")
    public static void saveShouldDetectStatus(Context context, boolean shouldTrack) {
        SharedPreferences sharedPref = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.should_track_status), shouldTrack);
        editor.commit();
    }

    /**
     * @param context needed in order to access the Shared preferences API
     * @return SharedPreferences instance that can be user for read/write operations
     */
    private static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    /**
     * @param context     needed in order to access the Shared preferences API and in order to read String keys
     * @param checkPeriod how often the app should check for steps count in order to detect walking activity
     */
    @SuppressLint("ApplySharedPref")
    static void saveCheckPeriod(Context context, int checkPeriod) {
        SharedPreferences sharedPref = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.check_period_key), checkPeriod);
        editor.commit();
    }

    /**
     * @param context needed in order to access the Shared preferences API and in order to read String keys
     */
    static int readCheckPeriod(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getInt(context.getString(R.string.check_period_key), 180);
    }

    /**
     * @param context needed in order to access the Shared preferences API and in order to read String keys
     */
    static String readStartTime(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getString(context.getString(R.string.alarm_start_time_key), DEFAULT_TIME);
    }

    /**
     * @param context   needed in order to access the Shared preferences API and in order to read String keys
     * @param startTime when to auto start of the service
     */
    @SuppressLint("ApplySharedPref")
    public static void saveStartTime(Context context, String startTime) {
        SharedPreferences sharedPref = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.alarm_start_time_key), startTime);
        editor.commit();
    }

    /**
     * @param context needed in order to access the Shared preferences API and in order to read String keys
     */
    static String readEndTime(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getString(context.getString(R.string.alarm_end_time_key), DEFAULT_TIME);
    }

    /**
     * @param context needed in order to access the Shared preferences API and in order to read String keys
     * @param endTime when to auto stop of the service
     */
    @SuppressLint("ApplySharedPref")
    public static void saveEndTime(Context context, String endTime) {
        SharedPreferences sharedPref = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.alarm_end_time_key), endTime);
        editor.commit();
    }
}
