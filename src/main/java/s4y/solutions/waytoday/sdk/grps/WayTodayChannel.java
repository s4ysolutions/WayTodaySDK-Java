package s4y.solutions.waytoday.sdk.grps;

import io.grpc.ManagedChannel;

public class WayTodayChannel implements AutoCloseable {
    final public ManagedChannel get;

    WayTodayChannel(ManagedChannel channel) {
        this.get = channel;
    }


    @Override
    public void close() {
        get.shutdown();
    }
}
