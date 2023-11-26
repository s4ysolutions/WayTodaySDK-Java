package s4y.solutions.waytoday.sdk.client;

import s4y.solutions.waytoday.sdk.grps.WayTodayChannel;
import s4y.solutions.waytoday.sdk.grps.WayTodayChannelBuilder;
import solutions.s4y.waytoday.grpc.TrackerGrpc;
import solutions.s4y.waytoday.grpc.TrackerOuterClass;

import javax.annotation.Nullable;

public class TrackerClient {

    final private WayTodayChannelBuilder wayTodayChannelBuilder;

    TrackerClient(String principal, String secret, boolean tls, String host, int port) {
        wayTodayChannelBuilder = new WayTodayChannelBuilder(principal, secret, tls, host, port);
    }

    // TODO: diagnostics for missed values
    TrackerClient() {
        this(System.getenv("GRPC_PRINCIPAL"),
                System.getenv("GRPC_PASSWORD"),
                Boolean.parseBoolean(System.getenv("GRPC_TLS")),
                System.getenv("GRPC_HOST"),
                Integer.parseInt(System.getenv("GRPC_PORT")));
    }

    public String getPong(@Nullable String payload) throws Exception {
        final TrackerOuterClass.PingRequest.Builder reqBuilder = TrackerOuterClass
                .PingRequest
                .newBuilder();
        if (payload != null) {
            reqBuilder.setPayload(payload);
        }
        final TrackerOuterClass.PingRequest req = reqBuilder.build();

        try (WayTodayChannel channel = wayTodayChannelBuilder.build()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.PongResponse response = stub.ping(req);
            return response.getPayload();
        }
    }
}
