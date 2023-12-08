package solutions.s4y.waytoday.sdk;

import javax.annotation.Nonnull;

public interface IPersistedState {
    /**
     * @return the current track id or "" if it is not set
     */
    @Nonnull
    String getTrackerId();
    void setTrackerId(@Nonnull String trackerID);
    boolean isTrackerIDSet();
}
