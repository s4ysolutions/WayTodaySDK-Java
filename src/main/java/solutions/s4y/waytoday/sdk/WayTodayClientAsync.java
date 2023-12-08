package solutions.s4y.waytoday.sdk;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WayTodayClientAsync extends WayTodayClient{
    ExecutorService executor = Executors.newCachedThreadPool();
    public WayTodayClientAsync(IPersistedState persistedState) {
        super(persistedState);
    }

    public Future<String> submitRequestNewTrackerId(@Nullable String prevId) {
        return executor.submit(() -> requestNewTrackerId(prevId));
    }

    public void submitUploadLocations() {
        executor.execute(this::uploadLocations);
    }
}
