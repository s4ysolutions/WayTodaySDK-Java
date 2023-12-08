package solutions.s4y.waytoday.sdk;

/**
 * A wrapper for exceptions thrown by the SDK
 */
public class WayTodayError extends Exception {
    public WayTodayError(String message, Throwable cause) {
        super(message, cause);
    }
}
