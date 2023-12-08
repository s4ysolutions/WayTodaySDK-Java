package solutions.s4y.waytoday.sdk;

/**
 * Interface for listening to errors
 * The SDK never throws exceptions, instead it calls onError() on the
 * subscribed listeners
 */

@FunctionalInterface
public interface IErrorsListener {
    void onError(WayTodayError error);
}
