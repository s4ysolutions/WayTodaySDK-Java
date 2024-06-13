package solutions.s4y.waytoday.sdk;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is asynchronous wrapper around WayTodayClient
 * It is thread safe
 */
public class WayTodayClientAsync extends WayTodayClient {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public WayTodayClientAsync(IPersistedState persistedState) {
        super(persistedState);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Future<String> submitRequestNewTrackerId(@Nullable String prevId) {
        return executor.submit(() -> requestNewTrackerId(prevId));
    }

    public void submitUploadLocations() {
        executor.execute(this::uploadLocations);
    }

    WayTodayClientAsync(IPersistedState persistedState, GrpcClient grpcClient) {
        super(persistedState, grpcClient);
    }
}