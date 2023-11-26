package s4y.solutions.waytoday.sdk.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrackerClient")
public class TrackerClientTest {
    final TrackerClient client = new TrackerClient();

    @Nested
    @DisplayName("ping")
    public class PingTest {
        @Test
        void ping_shouldReturnPongWithoutWorkload() throws Exception {
            // Arrange
            // Act
            String pong = client.getPong(null);
            // Assert
            assertThat(pong).isEqualTo("");
        }

        @Test
        void ping_shouldReturnPongWitWorkload() throws Exception {
            // Arrange
            // Act
            String pong = client.getPong("workload");
            // Assert
            assertThat(pong).isEqualTo("workload");
        }
    }
}
