package solutions.s4y.waytoday.sdk;

import javax.annotation.Nonnull;

/**
 * Interface for listening to track ID changes
 * Used for asynchronous track ID retrieval
 */
@FunctionalInterface
public interface ITrackIdChangeListener {
    void onTrackId(@Nonnull String trackID);
}
