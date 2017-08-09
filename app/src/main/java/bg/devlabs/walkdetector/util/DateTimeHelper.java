package bg.devlabs.walkdetector.util;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by simona on 8/9/17.
 */

public class DateTimeHelper {
    // tag used for logging purposes
    private static final String TAG = DateTimeHelper.class.getSimpleName();

    public static boolean isTimeValid(String startTime) {
        Pattern p = Pattern.compile("^([0-1]\\d|2[0-3]):([0-5]\\d)$");
        Matcher m = p.matcher(startTime);
        return m.matches();
    }


    public static long getCalendarTimeMillis(String startTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(startTime.substring(0, 2))); // For 1 PM or 2 PM
        calendar.set(Calendar.MINUTE, Integer.valueOf(startTime.substring(3, 5)));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();


    }

}
