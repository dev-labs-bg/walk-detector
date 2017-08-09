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

    static int readCheckPeriod(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getInt(context.getString(R.string.check_period_key), 180);
    }
}
