package solutions.s4y.waytoday.sdk;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WayTodayClientTest {
    private WayTodayClient client;
    private final IPersistedState mockState = new IPersistedState() {
        @NotNull
        @Override
        public String getTrackerId() {
            return null;
        }

        @Override
        public void setTrackerId(@NotNull String trackerID) {

        }

        @Override
        public boolean isTrackerIDSet() {
            return false;
        }
    };

    @BeforeEach
    public void setUp() {
        client = new WayTodayClient(mockState);
    }
    @Nested
    @DisplayName("listeners")
    public class ListenersTest {
        @Test
        public void client_canAddAndRemoveListener() {
            assertThat(client.trackIdChangeListeners).isEmpty();
            ITrackIdChangeListener l = trackID -> {
            };

            client.addOnTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeOnTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(0);
        }

        @Test
        public void idService_canAddAndRemoveListeneres() {
            assertThat(client.trackIdChangeListeners).isEmpty();
            ITrackIdChangeListener l1 = trackID -> {
            };
            ITrackIdChangeListener l2 = trackID -> {
            };

            client.addOnTrackIdChangeListener(l1);
            client.addOnTrackIdChangeListener(l2);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(2);
            client.removeOnTrackIdChangeListener(l1);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeOnTrackIdChangeListener(l1);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeOnTrackIdChangeListener(l2);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(0);
        }
    }
}
