package bg.devlabs.walkdetector;

import org.junit.Test;

import static bg.devlabs.walkdetector.util.DateTimeHelper.isTimeValid;
import static junit.framework.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void isTimeRegexRight() throws Exception {
        assertEquals(isTimeValid("12:13"), true);
        assertEquals(isTimeValid("12:63"), false);
        assertEquals(isTimeValid("42:13"), false);
    }

    @Test
    public void timeSubstring() throws Exception {
        String startTime = "62:20";
        System.out.println(startTime.substring(0, 2));
        System.out.println(startTime.substring(3, 5));

    }
}