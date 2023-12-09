package solutions.s4y.waytoday.sdk;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static solutions.s4y.waytoday.sdk.WayTodayClientTest.Locations.getDummyLocation;

public class WayTodayClientTest {
    static class Locations {
        static Location dummyLocation = new Location("", 0, 0, 0, 0, System.currentTimeMillis(), 0, false, 0, 0);

        static Location getDummyLocation(String id) {
            return new Location(id, "", 0, 0, 0, 0, System.currentTimeMillis(), 0, false, 0, 0);
        }
    }

    private WayTodayClient client;
    private final IPersistedState state = mock();

    @BeforeEach
    public void setUp() {
        client = new WayTodayClient(state);
    }

    public void tearDown() {
        reset(state);
    }

    @Nested
    @DisplayName("No gRPC calls")
    public class NoGrpcCallsTest {
        @Test
        public void trackIdChangeListeners_canBeAddedAndRemovedOneTime() {
            assertThat(client.trackIdChangeListeners).isEmpty();
            ITrackIdChangeListener l = trackID -> {
            };

            client.addOnTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeOnTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(0);
        }

        @Test
        public void trackIdChangeListeners_canBeAddedAndRemovedMultipleTimes() {
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

        @Test
        public void getTrackerId_shouldGetFromState() {
            // Arrange
            when(state.getTrackerId()).thenReturn("test");
            // Act
            String trackId = client.getCurrentTrackerId();
            // Assert
            assertThat(trackId).isEqualTo("test");
            verify(state).getTrackerId();
        }

        @Test
        public void getUploadingLocationsStatus_shouldBeEmptyInitially() {
            // Assert
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
        }
    }


    @Nested
    @DisplayName("Mocked gRPC calls")
    public class WayTodayClientMockedGrpcTest {
        private GrpcClient grpcClient;

        @BeforeEach
        public void setUp() {
            grpcClient = mock(GrpcClient.class);
            client = new WayTodayClient(state, grpcClient);
        }

        @Test
        public void enqueueLocationToUpload_shouldSetUploadingLocationsStatusToQueued() {
            // Act
            client.enqueueLocationToUpload(Locations.dummyLocation);
            // Assert
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.QUEUED);
        }

        @Test
        public void enqueueLocationToUpload_shouldNotAddMoreThenMaxLocations() {
            // Arrange
            final String firstId = "first";
            final String secondId = "second";
            client.enqueueLocationToUpload(getDummyLocation(firstId));
            client.enqueueLocationToUpload(getDummyLocation(secondId));
            for (int i = 2; i < WayTodayClient.testGetMaxLocations(); i++) {
                client.enqueueLocationToUpload(Locations.dummyLocation);
            }
            assert (client.testLocationsQueue().size() == WayTodayClient.testGetMaxLocations());
            // Act
            final String lastId = "last";
            client.enqueueLocationToUpload(getDummyLocation(lastId));
            // Assert
            Deque<Location> queue = client.testLocationsQueue();
            assertThat(queue.size()).isEqualTo(WayTodayClient.testGetMaxLocations());
            assertThat(queue.getFirst().id).isEqualTo(secondId);
            assertThat(queue.getLast().id).isEqualTo(lastId);
            assertThat(queue.contains(getDummyLocation(firstId))).isFalse();
        }

        @Test
        public void uploadLocations_shouldClearQueueLessThanPackSize() throws Exception {
            // Arrange
            for (int i = 0; i < WayTodayClient.testGetPackSize() - 1; i++) {
                client.enqueueLocationToUpload(Locations.dummyLocation);
            }
            final String trackId = "test_uploadLocations";
            when(state.getTrackerId()).thenReturn(trackId);
            List<Location> pack = new ArrayList<>();
            when(grpcClient.addLocations(eq(trackId), any())).thenAnswer(invocation -> {
                pack.clear();
                pack.addAll(invocation.getArgument(1));
                return true;
            });

            // Act
            client.uploadLocations();
            // Assert
            assertThat(client.testLocationsQueue()).isEmpty();
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
            verify(grpcClient).addLocations(eq(trackId), any());
            assertThat(pack.size()).isEqualTo(WayTodayClient.testGetPackSize() - 1);
        }

        @Test
        public void uploadLocations_shouldClearQueueMoreThanPackSize() throws Exception {
            // Arrange
            for (int i = 0; i < WayTodayClient.testGetPackSize() * 2 - 1; i++) {
                client.enqueueLocationToUpload(Locations.dummyLocation);
            }
            final String trackId = "test_uploadLocations2";
            when(state.getTrackerId()).thenReturn(trackId);
            List<List<Location>> packs = new ArrayList<>();
            when(grpcClient.addLocations(eq(trackId), any())).thenAnswer(invocation -> {
                List<Location> pack = new ArrayList<>(invocation.getArgument(1));
                packs.add(pack);
                return true;
            });

            // Act
            client.uploadLocations();
            // Assert
            assertThat(client.testLocationsQueue()).isEmpty();
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
            verify(grpcClient, times(2)).addLocations(eq(trackId), any());
            assertThat(packs.size()).isEqualTo(2);
            assertThat(packs.get(0).size()).isEqualTo(WayTodayClient.testGetPackSize());
            assertThat(packs.get(1).size()).isEqualTo(WayTodayClient.testGetPackSize() - 1);
        }

        @Test
        public void client_shouldHaveQueuedStatusAfterEnqueue() {
            // Act
            client.enqueueLocationToUpload(Locations.dummyLocation);
            // Assert
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.QUEUED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"some", ""})
        @NullSource
        public void generateTrackerId_shouldReturnGrpcResponse(@Nullable String prevId) throws Exception {
            // Arrange
            when(grpcClient.generateTrackerId(argThat(some -> true))).thenReturn("test");
            // Act
            String trackId = client.requestNewTrackerId(prevId);
            // Assert
            assertThat(trackId).isEqualTo("test");
            verify(grpcClient).generateTrackerId(argThat(some -> true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"some", ""})
        @NullSource
        public void generateTrackerId_shouldSetTrackerIdStateOnce(@Nullable String prevId) throws Exception {
            // Arrange
            when(grpcClient.generateTrackerId(argThat(some -> true))).thenReturn("test");
            // Act
            String trackId = client.requestNewTrackerId(prevId);
            // Assert
            verify(state).setTrackerId(trackId);
        }

        @AfterEach
        public void tearDown() {
            reset(grpcClient);
        }
    }
}
