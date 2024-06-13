package solutions.s4y.waytoday.sdk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WayTodayClientAsyncTest {
    static class Locations {
        static Location getDummyLocation(String id) {
            return new Location(id, "", 0, 0, 0, 0, System.currentTimeMillis(), 0, false,"", 0, 0,"");
        }

        static List<Location> getDummyLocations(@SuppressWarnings("SameParameterValue") int count) {
            List<Location> locations = new ArrayList<>();
            locations.add(getDummyLocation("first"));
            locations.add(getDummyLocation("second"));
            for (int i = 0; i < count - 4; i++) {
                locations.add(getDummyLocation(String.valueOf(i)));
            }
            locations.add(getDummyLocation("penultimate"));
            locations.add(getDummyLocation("second"));
            return locations;
        }
    }

    private WayTodayClientAsync client;
    private final IPersistedState state = mock();
    private GrpcClient grpcClient;

    @BeforeEach
    public void setUp() throws Exception {
        when(state.getTrackerId()).thenReturn("test-tracker-id");
        grpcClient = mock(GrpcClient.class);
        when(grpcClient.addLocations(any(), any())).thenReturn(true);
        client = new WayTodayClientAsync(state, grpcClient);
    }

    @AfterEach
    public void tearDown() {
        reset(state);
        reset(grpcClient);
    }

    @Test
    public void testSubmitRequestNewTrackerId_shouldCallUploadingStateChangeListenersWithUploading() {
        // Arrange
        IErrorsListener listenere = mock(IErrorsListener.class);
        client.addErrorsListener(listenere);

        IUploadingLocationsStatusChangeListener listener = mock(IUploadingLocationsStatusChangeListener.class);
        ArgumentCaptor<UploadingLocationsStatus> captor = ArgumentCaptor.forClass(UploadingLocationsStatus.class);
        Locations.getDummyLocations(WayTodayClient.PACK_SIZE * 2 - 1).forEach(client::enqueueLocationToUpload);
        client.addUploadingLocationsStatusChangeListener(listener);
        // Act
        client.submitUploadLocations();
        // Assert
        verify(listener, timeout(1000).times(2)).onStatusChange(captor.capture());
        List<UploadingLocationsStatus> statuses = captor.getAllValues();
        assertThat(statuses.get(0)).isEqualTo(UploadingLocationsStatus.UPLOADING);
        assertThat(statuses.get(1)).isEqualTo(UploadingLocationsStatus.EMPTY);

        verify(listenere, never()).onError(any());
    }
    @Test
    public void testSubmitRequestNewTrackerId_shouldCallUploadingStateChangeListenersWithErrorIfFalse() throws Exception {
        // Arrange
        IErrorsListener listenere = mock(IErrorsListener.class);
        client.addErrorsListener(listenere);

        IUploadingLocationsStatusChangeListener listener = mock(IUploadingLocationsStatusChangeListener.class);
        ArgumentCaptor<UploadingLocationsStatus> captor = ArgumentCaptor.forClass(UploadingLocationsStatus.class);
        Locations.getDummyLocations(WayTodayClient.PACK_SIZE * 2 - 1).forEach(client::enqueueLocationToUpload);
        client.addUploadingLocationsStatusChangeListener(listener);
        when(grpcClient.addLocations(any(), any())).thenReturn(false);
        // Act
        client.submitUploadLocations();
        // Assert
        verify(listener, timeout(1000).times(2)).onStatusChange(captor.capture());
        List<UploadingLocationsStatus> statuses = captor.getAllValues();
        assertThat(statuses.get(0)).isEqualTo(UploadingLocationsStatus.UPLOADING);
        assertThat(statuses.get(1)).isEqualTo(UploadingLocationsStatus.ERROR);

        verify(listenere, never()).onError(any());
    }
    @Test
    public void testSubmitRequestNewTrackerId_shouldCallUploadingStateChangeListenersWithErrorIfException() throws Exception {
        // Arrange
        IErrorsListener listenere = mock(IErrorsListener.class);
        ArgumentCaptor<WayTodayError> errorCaptor = ArgumentCaptor.forClass(WayTodayError.class);
        client.addErrorsListener(listenere);

        IUploadingLocationsStatusChangeListener listener = mock(IUploadingLocationsStatusChangeListener.class);
        ArgumentCaptor<UploadingLocationsStatus> captor = ArgumentCaptor.forClass(UploadingLocationsStatus.class);
        Locations.getDummyLocations(WayTodayClient.PACK_SIZE * 2 - 1).forEach(client::enqueueLocationToUpload);
        client.addUploadingLocationsStatusChangeListener(listener);
        when(grpcClient.addLocations(any(), any())).thenThrow(new Exception("Test"));
        // Act
        client.submitUploadLocations();
        // Assert
        verify(listener, timeout(1000).times(2)).onStatusChange(captor.capture());
        List<UploadingLocationsStatus> statuses = captor.getAllValues();
        assertThat(statuses.get(0)).isEqualTo(UploadingLocationsStatus.UPLOADING);
        assertThat(statuses.get(1)).isEqualTo(UploadingLocationsStatus.ERROR);

        verify(listenere, times(1)).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getCause().getMessage()).isEqualTo("Test");
    }

    @Test
    public void testSubmitRequestNewTrackerId_shouldCallUploadingStateChangeListenersWithUploadingAndEmpty() throws Exception {
        // Arrange
        ITrackIdChangeListener listener = mock(ITrackIdChangeListener.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        client.addTrackIdChangeListener(listener);
        final String trackId = System.currentTimeMillis() + "";
        when(grpcClient.generateTrackerId(argThat(some -> true))).thenReturn(trackId);
        // Act
        client.submitRequestNewTrackerId(null);
        // Assert
        verify(listener, timeout(1000)).onTrackId(captor.capture());
        List<String> statuses = captor.getAllValues();
        assertThat(statuses.get(0)).isEqualTo(trackId);
    }
}
