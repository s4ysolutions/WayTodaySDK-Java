package s4y.solutions.waytoday.sdk.grps;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import s4y.solutions.waytoday.sdk.wsse.Wsse;

import java.security.NoSuchAlgorithmException;

public class WayTodayChannelBuilder {
    private final String principal;
    private final String secret;
    private final boolean tls;
    private final String host;
    private final int port;

    private final Metadata.Key<String> wsseKey = Metadata.Key.of("wsse", Metadata.ASCII_STRING_MARSHALLER);

    public WayTodayChannelBuilder(String principal, String secret, boolean tls, String host, int port) {
        this.principal = principal;
        this.secret = secret;
        this.tls = tls;
        this.host = host;
        this.port = port;
    }
    public WayTodayChannel build() throws NoSuchAlgorithmException {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(host, port);
        if (!tls)
            channelBuilder.usePlaintext();

        Metadata headers = new Metadata();
        String token = Wsse.getToken(principal, secret);
        headers.put(wsseKey, token);
        channelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(headers));

        return new WayTodayChannel(channelBuilder.build());
    }
}
