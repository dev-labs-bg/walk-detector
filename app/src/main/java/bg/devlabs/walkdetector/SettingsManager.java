package bg.devlabs.walkdetector;

/**
 * Created by simona on 8/9/17.
 * simona@devlabs.bg
 * This class manages settings retrieval and saving
 */

class SettingsManager {
    // How long will the checked for walking activity period be
    static final int CHECKED_PERIOD_SECOND = 180; //180 seconds = 3 minutes
    // How much will the app wait for response until a timeout exception is thrown
    static final int AWAIT_PERIOD_SECOND = 60; // 60 seconds = 1 min
    // How often will the app query the client for walking activity
    static final int OBSERVABLE_PERIOD_SECOND = CHECKED_PERIOD_SECOND + AWAIT_PERIOD_SECOND;
    // Walking slow (2 mph)	67 steps per minute which is almost one step per second
    private static final int SLOW_WALKING_STEPS_PER_SECOND = 1;
    // The calculated amount of steps if the user was walking during the checked period of time
    // For example 180 seconds * 1 step at a second = 180 steps
    // This value is used to determine if the user was walking trough the checked period of time
    static final int COUNT_STEPS_WALKING = CHECKED_PERIOD_SECOND * SLOW_WALKING_STEPS_PER_SECOND;
}
