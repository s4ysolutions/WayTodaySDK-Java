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
        final static Location dummyLocation = new Location("", 0, 0, 0, 0, System.currentTimeMillis(), 0, false, "", 0, 0);

        static Location getDummyLocation(String id) {
            return new Location(id, "", 0, 0, 0, 0, System.currentTimeMillis(), 0, false,"",0, 0, "");
        }
    }

    private WayTodayClient client;
    private final IPersistedState state = mock();
    private final IErrorsListener errorsListener = mock();

    @BeforeEach
    public void setUp() {
        client = new WayTodayClient(state);
        client.addErrorsListener(errorsListener);
    }

    @AfterEach
    public void tearDown() {
        reset(state);
        reset(errorsListener);
    }

    @Nested
    @DisplayName("No gRPC calls")
    public class NoGrpcCallsTest {
        @BeforeEach
        public void setUp() {
            client.removeErrorsListener(errorsListener);
        }

        @Test
        public void trackIdChangeListeners_canBeAddedAndRemovedOneTime() {
            assertThat(client.trackIdChangeListeners).isEmpty();
            ITrackIdChangeListener l = trackID -> {
            };

            client.addTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeTrackIdChangeListener(l);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void trackIdChangeListeners_canBeAddedAndRemovedMultipleTimes() {
            assertThat(client.trackIdChangeListeners).isEmpty();
            ITrackIdChangeListener l1 = trackID -> {
            };
            ITrackIdChangeListener l2 = trackID -> {
            };

            client.addTrackIdChangeListener(l1);
            client.addTrackIdChangeListener(l2);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(2);
            client.removeTrackIdChangeListener(l1);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeTrackIdChangeListener(l1);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(1);
            client.removeTrackIdChangeListener(l2);
            assertThat(client.trackIdChangeListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void uploadLocationsStatusChangeListeners_canBeAddedAndRemovedOneTime() {
            assertThat(client.uploadingLocationsStatusChangeListeners).isEmpty();
            IUploadingLocationsStatusChangeListener l = trackID -> {
            };

            client.addUploadingLocationsStatusChangeListener(l);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(1);
            client.removeUploadingLocationsStatusChangeListener(l);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void uploadLocationsStatusChangeListeners_canBeAddedAndRemovedMultipleTimes() {
            assertThat(client.uploadingLocationsStatusChangeListeners).isEmpty();
            IUploadingLocationsStatusChangeListener l1 = status -> {
            };
            IUploadingLocationsStatusChangeListener l2 = status -> {
            };

            client.addUploadingLocationsStatusChangeListener(l1);
            client.addUploadingLocationsStatusChangeListener(l2);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(2);
            client.removeUploadingLocationsStatusChangeListener(l1);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(1);
            client.removeUploadingLocationsStatusChangeListener(l1);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(1);
            client.removeUploadingLocationsStatusChangeListener(l2);
            assertThat(client.uploadingLocationsStatusChangeListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void errorListeners_canBeAddedAndRemovedOneTime() {
            assertThat(client.errorsListeners).isEmpty();
            IErrorsListener l = trackID -> {
            };

            client.addErrorsListener(l);
            assertThat(client.errorsListeners.size()).isEqualTo(1);
            client.removeErrorsListener(l);
            assertThat(client.errorsListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void errorListeners_canBeAddedAndRemovedMultipleTimes() {
            assertThat(client.errorsListeners).isEmpty();
            IErrorsListener l1 = error -> {
            };
            IErrorsListener l2 = error -> {
            };

            client.addErrorsListener(l1);
            client.addErrorsListener(l2);
            assertThat(client.errorsListeners.size()).isEqualTo(2);
            client.removeErrorsListener(l1);
            assertThat(client.errorsListeners.size()).isEqualTo(1);
            client.removeErrorsListener(l1);
            assertThat(client.errorsListeners.size()).isEqualTo(1);
            client.removeErrorsListener(l2);
            assertThat(client.errorsListeners.size()).isEqualTo(0);
            verify(errorsListener, never()).onError(any());
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
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void getUploadingLocationsStatus_shouldBeEmptyInitially() {
            // Assert
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
            verify(errorsListener, never()).onError(any());
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
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void enqueueLocationToUpload_shouldNotAddMoreThenMaxLocations() {
            // Arrange
            final String firstId = "first";
            final String secondId = "second";
            client.enqueueLocationToUpload(getDummyLocation(firstId));
            client.enqueueLocationToUpload(getDummyLocation(secondId));
            for (int i = 2; i < WayTodayClient.MAX_LOCATIONS_MEMORY; i++) {
                client.enqueueLocationToUpload(Locations.dummyLocation);
            }
            assert (client.locationsQueue.size() == WayTodayClient.MAX_LOCATIONS_MEMORY);
            // Act
            final String lastId = "last";
            client.enqueueLocationToUpload(getDummyLocation(lastId));
            // Assert
            Deque<Location> queue = client.locationsQueue;
            assertThat(queue.size()).isEqualTo(WayTodayClient.MAX_LOCATIONS_MEMORY);
            assertThat(queue.getFirst().id).isEqualTo(secondId);
            assertThat(queue.getLast().id).isEqualTo(lastId);
            assertThat(queue.contains(getDummyLocation(firstId))).isFalse();
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void uploadLocations_shouldClearQueueLessThanPackSize() throws Exception {
            // Arrange
            for (int i = 0; i < WayTodayClient.PACK_SIZE - 1; i++) {
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
            assertThat(client.locationsQueue).isEmpty();
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
            verify(grpcClient).addLocations(eq(trackId), any());
            assertThat(pack.size()).isEqualTo(WayTodayClient.PACK_SIZE - 1);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void uploadLocations_shouldClearQueueMoreThanPackSize() throws Exception {
            // Arrange
            for (int i = 0; i < WayTodayClient.PACK_SIZE * 2 - 1; i++) {
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
            assertThat(client.locationsQueue).isEmpty();
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.EMPTY);
            verify(grpcClient, times(2)).addLocations(eq(trackId), any());
            assertThat(packs.size()).isEqualTo(2);
            assertThat(packs.get(0).size()).isEqualTo(WayTodayClient.PACK_SIZE);
            assertThat(packs.get(1).size()).isEqualTo(WayTodayClient.PACK_SIZE - 1);
            verify(errorsListener, never()).onError(any());
        }

        @Test
        public void client_shouldHaveQueuedStatusAfterEnqueue() {
            // Act
            client.enqueueLocationToUpload(Locations.dummyLocation);
            // Assert
            assertThat(client.getUploadingLocationsStatus()).isEqualTo(UploadingLocationsStatus.QUEUED);
            verify(errorsListener, never()).onError(any());
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
            verify(errorsListener, never()).onError(any());
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
            verify(errorsListener, never()).onError(any());
        }

        @AfterEach
        public void tearDown() {
            reset(grpcClient);
        }
    }
}