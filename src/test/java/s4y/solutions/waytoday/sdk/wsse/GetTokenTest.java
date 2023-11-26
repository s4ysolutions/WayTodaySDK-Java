package s4y.solutions.waytoday.sdk.wsse;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class GetTokenTest {
    static private Map<String, String> parseHeader(String header) {
        final Map<String, String> result = new HashMap<>();
        final String[] parts = header.split(",");
        for (String part : parts) {
            final String[] keyValue = part.split("=");
            final String key = keyValue[0];
            final String value = keyValue.length == 2 ? keyValue[1] : keyValue[1] + "=";

            result.put(key, value.replaceAll("\"", ""));
        }
        return result;
    }

    @Test
    void wsse_shouldCreateToken() {
        assertThatNoException().isThrownBy(
                () -> Wsse.getToken("user", "password"));
    }

    @Test
    void wsse_shouldCreateVerifiableToken() throws NoSuchAlgorithmException {
        // Arrange
        // Act
        final String token = Wsse.getToken("user", "password");
        final Map<String, String> headers = parseHeader(token);
        // Assert
        assertThat(headers).size().isEqualTo(4);
        assertThat(headers).containsKey("Username");
        assertThat(headers).containsKey("PasswordDigest");
        assertThat(headers).containsKey("nonce");
        assertThat(headers).containsKey("Created");

        final String digest = Wsse.testDigest("password", headers.get("nonce"), headers.get("Created"));
        assertThat(digest).isEqualTo(headers.get("PasswordDigest"));
    }
}
