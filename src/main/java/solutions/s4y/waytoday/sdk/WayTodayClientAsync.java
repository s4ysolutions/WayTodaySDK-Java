package solutions.s4y.waytoday.sdk;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is asynchronous wrapper around WayTodayClient
 * It is thread safe
 */
public class WayTodayClientAsync extends WayTodayClient{
    final ExecutorService executor = Executors.newCachedThreadPool();
    @SuppressWarnings("unused")
    public WayTodayClientAsync(IPersistedState persistedState) {
        super(persistedState);
    }
    WayTodayClientAsync(IPersistedState persistedState, GrpcClient grpcClient) {
        super(persistedState, grpcClient);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Future<String> submitRequestNewTrackerId(@Nullable String prevId) {
        return executor.submit(() -> requestNewTrackerId(prevId));
    }

    public void submitUploadLocations() {
        executor.execute(this::uploadLocations);
    }
}
