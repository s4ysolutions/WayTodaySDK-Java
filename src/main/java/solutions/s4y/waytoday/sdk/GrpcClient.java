package solutions.s4y.waytoday.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.slf4j.Logger;
import solutions.s4y.waytoday.sdk.wsse.Wsse;
import solutions.s4y.waytoday.grpc.TrackerGrpc;
import solutions.s4y.waytoday.grpc.TrackerOuterClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

class GrpcClient {
    static class CloseableChannel implements AutoCloseable {
        final public ManagedChannel get;

        CloseableChannel(ManagedChannel channel) {
            this.get = channel;
        }

        @Override
        public void close() {
            get.shutdown();
        }
    }

    static private Logger logger = org.slf4j.LoggerFactory.getLogger(GrpcClient.class);

    // TODO: should be annotation
    @Nonnull
    static String readStartArgument(@Nonnull String name, @Nullable String def) {
        String value = System.getProperty(name);
        if (value != null)
            return value;
        value = System.getenv(name);
        if (value != null)
            return value;
        if (def != null)
            return def;
        logger.error("Missing required argument: " + name);
        return "";
    }
    static String readStartArgument(@Nonnull String name) {
        return readStartArgument(name, null);
    }

    private final Metadata.Key<String> wsseKey = Metadata.Key.of("wsse", Metadata.ASCII_STRING_MARSHALLER);
    private final String principal;
    private final String secret;
    private final boolean tls;
    private final String host;
    private final int port;
    private final String provider;

    /**
     * @param principal mandatory string to identify the application against WayToday server.
     *               Currently, it might be any characters string.
     * @param secret mandatory string to authorize the application against WayToday server.
     *               Currently, it might be any characters string.
     * @param provider optional string to be passed with every location up to the server and
     *                 down to clients in order to identify the locations sent by the application.
     *                 Keep it as shot as possible.
     */

    GrpcClient(String principal, String secret, boolean tls, String host, int port, String provider){
        this.principal = principal;
        this.secret = secret;
        this.tls = tls;
        this.host = host;
        this.port = port;
        this.provider = provider;
    }

    // TODO: diagnostics for missed values
    GrpcClient() {
        this(readStartArgument("GRPC_PRINCIPAL"),
                readStartArgument("GRPC_PASSWORD"),
                Boolean.parseBoolean(readStartArgument("GRPC_TLS")),
                readStartArgument("GRPC_HOST"),
                Integer.parseInt(readStartArgument("GRPC_PORT")),
                readStartArgument("GRPC_PROVIDER", "none"));
    }

    private CloseableChannel channel() throws NoSuchAlgorithmException {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(host, port);
        if (!tls)
            channelBuilder.usePlaintext();

        Metadata headers = new Metadata();
        String token = Wsse.getToken(principal, secret);
        headers.put(wsseKey, token);
        channelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(headers));

        return new CloseableChannel(channelBuilder.build());
    }

    public String ping(@Nullable String payload) throws Exception {
        final TrackerOuterClass.PingRequest.Builder reqBuilder = TrackerOuterClass
                .PingRequest
                .newBuilder();
        if (payload != null) {
            reqBuilder.setPayload(payload);
        }

        final TrackerOuterClass.PingRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.PongResponse response = stub.ping(req);
            return response.getPayload();
        }
    }

    public String generateTrackerID(@Nullable String prevId) throws Exception {
        final TrackerOuterClass.GenerateTrackerIDRequest.Builder reqBuilder = TrackerOuterClass
                .GenerateTrackerIDRequest
                .newBuilder();

        if (prevId != null) {
            reqBuilder.setPrevTid(prevId);
        }

        final TrackerOuterClass.GenerateTrackerIDRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.GenerateTrackerIDResponse response = stub.generateTrackerID(req);
            return response.getTid();
        }
    }
    public String generateTrackerID() throws Exception {
       return generateTrackerID(null);
    }

    public Boolean testTrackerID(@Nonnull String tid) throws Exception {
        final TrackerOuterClass.TestTrackerIDRequest.Builder reqBuilder = TrackerOuterClass
                .TestTrackerIDRequest
                .newBuilder()
                .setTid(tid);

        final TrackerOuterClass.TestTrackerIDRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.TestTrackerIDResponse response = stub.testTrackerID(req);
            return response.getOk();
        }
    }

    public Boolean freeTrackerID(@Nonnull String tid) throws Exception {
        final TrackerOuterClass.FreeTrackerIDRequest.Builder reqBuilder = TrackerOuterClass
                .FreeTrackerIDRequest
                .newBuilder()
                .setTid(tid);

        final TrackerOuterClass.FreeTrackerIDRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.FreeTrackerIDResponse response = stub.freeTrackerID(req);
            return response.getOk();
        }
    }

    public List<Location> getLocations(@Nonnull String tid, int limit) throws Exception {
        final TrackerOuterClass.GetLocationsRequest.Builder reqBuilder = TrackerOuterClass
                .GetLocationsRequest
                .newBuilder()
                .setTid(tid)
                .setLimit(limit);

        final TrackerOuterClass.GetLocationsRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            ArrayList<Location> locations = new ArrayList<>();
            stub.getLocations(req).getItemsList().forEach(item -> locations.add(LocationGrpcBridge.fromProto(item)));
            return locations;
        }
    }

    public Boolean addLocations(@Nonnull String tid, @Nonnull List<Location> locations) throws Exception {
        final TrackerOuterClass.AddLocationsRequest.Builder reqBuilder = TrackerOuterClass
                .AddLocationsRequest
                .newBuilder()
                .setTid(tid);
        for (Location location : locations) {
            reqBuilder.addLocations(LocationGrpcBridge.toProto(location));
        }

        final TrackerOuterClass.AddLocationsRequest req = reqBuilder.build();

        try (CloseableChannel channel = channel()) {
            TrackerGrpc.TrackerBlockingStub stub = TrackerGrpc.newBlockingStub(channel.get);
            TrackerOuterClass.AddLocationResponse response = stub.addLocations(req);
            return response.getOk();
        }
    }

}
