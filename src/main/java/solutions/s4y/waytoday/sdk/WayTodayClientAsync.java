package solutions.s4y.waytoday.sdk;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WayTodayClientAsync extends WayTodayClient{
    ExecutorService executor = Executors.newCachedThreadPool();
    public WayTodayClientAsync(IPersistedState persistedState) {
        super(persistedState);
    }

    public String requestNewTrackerId(@Nullable String prevId) {

    }
}
