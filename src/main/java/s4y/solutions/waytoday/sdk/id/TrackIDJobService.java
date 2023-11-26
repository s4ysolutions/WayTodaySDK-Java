package s4y.solutions.waytoday.sdk.id;

import io.grpc.ManagedChannelBuilder;

public class TrackIDJobService {
    ManagedChannelBuilder<?> getChannelBuilder(String host, int port) {
        return ManagedChannelBuilder
                .forAddress(host, port);
    }

    void getNewTrackId() {
        // TrackIDJobService.newBlockingStub(getChannelBuilder("localhost", 50051).build());

    }
}
