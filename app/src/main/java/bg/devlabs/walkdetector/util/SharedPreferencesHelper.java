package bg.devlabs.walkdetector.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import bg.devlabs.walkdetector.R;

/**
 * Created by simona on 8/8/17.
 */

public class SharedPreferencesHelper {
    public static boolean shouldTrack(Context context) {
        SharedPreferences sharedPref = getSharedPreference(context);
        return sharedPref.getBoolean(context.getString(R.string.should_track_status), false);
    }

    private static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    @SuppressLint("ApplySharedPref")
    public static void saveTrackStatusPrefs(Context context, boolean shouldTrack) {
        SharedPreferences sharedPref = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.should_track_status), shouldTrack);
        editor.commit();
    }
}
