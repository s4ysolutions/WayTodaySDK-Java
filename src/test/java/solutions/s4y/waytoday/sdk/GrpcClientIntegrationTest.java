package solutions.s4y.waytoday.sdk;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrackerClient")
public class GrpcClientIntegrationTest {
    GrpcClient client;

    @BeforeEach
    public void setUp() {
        client = new GrpcClient("test-provider-implicit");
    }

    @Nested
    @DisplayName("ping")
    public class PingTest {
        @Test
        void ping_shouldReturnPongWithoutWorkload() throws Exception {
            // Arrange
            // Act
            String pong = client.ping(null);
            // Assert
            assertThat(pong).isEqualTo("");
        }

        @Test
        void ping_shouldReturnPongWitWorkload() throws Exception {
            // Arrange
            // Act
            String pong = client.ping("workload");
            // Assert
            assertThat(pong).isEqualTo("workload");
        }
    }

    @Nested
    @DisplayName("generateTrackerID")
    public class GenerateTrackerIDTest {
        @Test
        void generateTrackerID_shouldReturnGeneratedTrackerID() throws Exception {
            // Arrange
            // Act
            String trackerID = client.generateTrackerId();
            // Assert
            assertThat(trackerID).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getLocations")
    public class GetLocationsTest {
        @Test
        void getLocations_shouldReturnLocations() throws Exception {
            // Arrange
            // Act
            List<Location> locations = client.getLocations("tid", 1);
            // Assert
            assertThat(locations).isEmpty();
        }
    }

    @Nested
    @DisplayName("addLocations")
    public class AddLocationsTest {
        @Test
        void addLocations_shouldNotThrow() throws Exception {
            // Arrange
            List<Location> locations = new ArrayList<>();
            locations.add(new Location("id1", "tid1", 0, 0, 0, 0, 0, 0, false, "provider", 0, 0, "sid1"));
            locations.add(new Location("id2", "tid2", 0, 0, 0, 0, 0, 0, false, "provider", 0, 0, "sid1"));
            // Ac
            Boolean ok = client.addLocations("test", locations);
            // Assert
            assertThat(ok).isTrue();
        }
    }

    @Nested
    @DisplayName("testTrackerID")
    public class TestTrackerIDTest {
        @Test
        void testTrackerID_shouldReturnTrue() throws Exception {
            // Arrange
            // Act
            Boolean ok = client.testTrackerId("non-existing-tracker-id");
            // Assert
            // TODO: This should return false
            assertThat(ok).isTrue();
        }
    }

    @Nested
    @DisplayName("freeTrackerID")
    public class FreeTrackerIDTest {
        @Test
        void freeTrackerID_shouldReturnTrue() throws Exception {
            // Arrange
            // Act
            Boolean ok = client.freeTrackerId("non-existing-tracker-id");
            // Assert
            // NOTE: false means that the tracker id already was freed and in the pool
            assertThat(ok).isNotNull();
        }
    }

    @Nested
    @DisplayName("Integrated locations tests")
    public class GetAndFrrTrackerIDTest {
        @Test
        void locations_shouldBeEmpty() throws Exception {
            // Arrange
            client.freeTrackerId("test_locations_shouldBeEmpty");
            Thread.sleep(1000);
            // Act
            List<Location> locations = client.getLocations("test_locations_shouldBeEmpty", 10);
            // Assert
            assertThat(locations).isEmpty();
        }

        @Test
        void locations_shouldBeAdded() throws Exception {
            // Arrange
            client.freeTrackerId("test_locations_shouldBeAdded");
            Thread.sleep(1000);
            List<Location> locations0 = client.getLocations("test_locations_shouldBeAdded", 1);
            assertThat(locations0).isEmpty();
            List<Location> locations = new ArrayList<>();
            locations.add(new Location("a", "1", 1, 0, 0, 1, 0, 0, false, "test-provider-explicit", 0, 0, "sid1"));
            locations.add(new Location("b", "2", 2, 10, 0, 2, 1, 0, false,"", 0, 0, "sid2"));
            // Act
            Boolean ok = client.addLocations("test_locations_shouldBeAdded", locations);
            Thread.sleep(2000);
            List<Location> locations2 = client.getLocations("test_locations_shouldBeAdded", 10);
            // Assert
            assertThat(ok).isTrue();
            assertThat(locations2).hasSize(2);
            assertThat(locations2.get(0).id).isEqualTo("a");
            assertThat(locations2.get(0).lat).isEqualTo(1);
            assertThat(locations2.get(0).tid).isEqualTo("test_locations_shouldBeAdded");
            assertThat(locations2.get(0).provider).isEqualTo("test-provider-explicit");
            assertThat(locations2.get(1).id).isEqualTo("b");
            assertThat(locations2.get(1).lat).isEqualTo(2);
            assertThat(locations2.get(1).tid).isEqualTo("test_locations_shouldBeAdded");
            assertThat(locations2.get(1).provider).isEqualTo("test-provider-implicit");
        }
    }
}
