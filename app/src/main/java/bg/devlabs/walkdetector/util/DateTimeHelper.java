package bg.devlabs.walkdetector.util;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Simona Stoyanova on 8/9/17.
 * simona@devlabs.bg
 * <p>
 * A helper class which validates the entered time values
 * and helps with Calendar calculations for the every day alarm times
 */

public class DateTimeHelper {
    // tag used for logging purposes
    private static final String TAG = DateTimeHelper.class.getSimpleName();

    /**
     * Validates the user input
     *
     * @param startTime user input time
     * @return true if the format is correct and false otherwise
     */
    public static boolean isTimeValid(String startTime) {
        // Pattern that matches the 24 hour minutes time format
        // For example 23:59 is a valid input
        Pattern p = Pattern.compile("^([0-1]\\d|2[0-3]):([0-5]\\d)$");
        Matcher m = p.matcher(startTime);
        return m.matches();
    }

    /**
     * Sets the hours and minutes to the calendar, so it can be used for everyday alarms
     *
     * @param startTime used to get the hour and minutes in the HH:mm format
     * @return time in millis  of the calendar
     */
    public static long getCalendarTimeMillis(String startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(startTime.substring(0, 2))); // For 1 PM or 2 PM
        calendar.set(Calendar.MINUTE, Integer.valueOf(startTime.substring(3, 5)));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
