package solutions.s4y.waytoday.sdk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WayTodayClient {
    private static final AtomicBoolean isUploading = new AtomicBoolean();
    private static final AtomicBoolean isError = new AtomicBoolean();
    private final IPersistedState persistedState;
    private final GrpcClient grpcClient;

    final ArrayList<IErrorsListener> errorsListeners = new ArrayList<>(2);
    final ArrayList<ITrackIdChangeListener> trackIdChangeListeners = new ArrayList<>(2);
    final List<IUploadingLocationsStatusChangeListener> uploadingLocationsStatusChangeListeners = new ArrayList<>(2);
    final Deque<Location> locationsQueue = new LinkedList<>();
    final static int MAX_LOCATIONS_MEMORY = 500;
    final static int PACK_SIZE = 16;

    public WayTodayClient(IPersistedState persistedState) {
        this(persistedState, new GrpcClient());
    }

    public void addErrorsListener(IErrorsListener listener) {
        synchronized (errorsListeners) {
            errorsListeners.add(listener);
        }
    }

    public void addTrackIdChangeListener(@Nonnull ITrackIdChangeListener listener) {
        synchronized (trackIdChangeListeners) {
            trackIdChangeListeners.add(listener);
        }
    }

    public void addUploadingLocationsStatusChangeListener(IUploadingLocationsStatusChangeListener listener) {
        synchronized (uploadingLocationsStatusChangeListeners) {
            uploadingLocationsStatusChangeListeners.add(listener);
        }
    }

    public void enqueueLocationToUpload(Location location) {
        synchronized (locationsQueue) {
            locationsQueue.addLast(location);
            if (locationsQueue.size() > MAX_LOCATIONS_MEMORY) {
                locationsQueue.removeFirst();
            }
        }
        notifyUploadLocationsState();
    }

    @Nonnull
    public String getCurrentTrackerId() {
        return persistedState.getTrackerId();
    }

    public boolean hasTrackerId() {
        return persistedState.hasTrackerId();
    }

    public UploadingLocationsStatus getUploadingLocationsStatus() {
        if (isError.get())
            return UploadingLocationsStatus.ERROR;
        if (isUploading.get())
            return UploadingLocationsStatus.UPLOADING;
        int size;
        synchronized (locationsQueue) {
            size = locationsQueue.size();
        }
        if (size > 0)
            return UploadingLocationsStatus.QUEUED;
        return UploadingLocationsStatus.EMPTY;
    }

    public void removeErrorsListener(IErrorsListener listener) {
        synchronized (errorsListeners) {
            errorsListeners.remove(listener);
        }
    }

    public void removeTrackIdChangeListener(@Nonnull ITrackIdChangeListener listener) {
        trackIdChangeListeners.remove(listener);
    }

    public void removeUploadingLocationsStatusChangeListener(IUploadingLocationsStatusChangeListener listener) {
        synchronized (uploadingLocationsStatusChangeListeners) {
            uploadingLocationsStatusChangeListeners.remove(listener);
        }
    }

    public String requestNewTrackerId(@Nullable String prevId) {
        try {
            String id = grpcClient.generateTrackerId(prevId);
            persistedState.setTrackerId(id);
            notifyTrackIdChange(id);
            return id;
        } catch (Exception e) {
            notifyError(new WayTodayError("Error while requesting new tracker id", e));
            return "";
        }
    }

    public void uploadLocations() {
        String tid = getCurrentTrackerId();
        if (tid.isEmpty()) return;
        if (locationsQueue.isEmpty()) return;
        if (!isUploading.compareAndSet(false, true))
            return;
        isError.set(false);
        notifyUploadLocationsState();
        try {
            uploadQueue(tid);
        } catch (Exception e) {
            isError.set(true);
        } finally {
            isUploading.set(false);
            notifyUploadLocationsState();
        }
    }

    WayTodayClient(IPersistedState persistedState, GrpcClient grpcClient) {
        this.persistedState = persistedState;
        this.grpcClient = grpcClient;
    }

    private void notifyError(WayTodayError error) {
        List<IErrorsListener> listeners;
        synchronized (errorsListeners) {
            listeners = new ArrayList<>(errorsListeners);
        }
        for (IErrorsListener listener : listeners) {
            listener.onError(error);
        }
    }

    private void notifyTrackIdChange(@Nonnull String trackId) {
        List<ITrackIdChangeListener> listeners;
        synchronized (trackIdChangeListeners) {
            listeners = new ArrayList<>(trackIdChangeListeners);
        }
        for (ITrackIdChangeListener listener : listeners) {
            listener.onTrackId(trackId);
        }
    }

    private void notifyUploadLocationsState() {
        UploadingLocationsStatus status = getUploadingLocationsStatus();
        List<IUploadingLocationsStatusChangeListener> listeners;
        synchronized (uploadingLocationsStatusChangeListeners) {
            listeners = new ArrayList<>(uploadingLocationsStatusChangeListeners);
        }
        for (IUploadingLocationsStatusChangeListener listener : listeners) {
            listener.onStatusChange(status);
        }
    }

    private void uploadQueue(@Nonnull final String tid) {
        List<Location> pack = new ArrayList<>();
        for (; ; ) {
            synchronized (locationsQueue) {
                int packSize = Math.min(locationsQueue.size(), PACK_SIZE);
                for (int i = 0; i < packSize; i++) {
                    Location head = locationsQueue.peekFirst();
                    if (head != null) {
                        pack.add(head);
                    }
                }
            }
            if (pack.isEmpty()) {
                break;
            }
            try {
                Boolean ok = grpcClient.addLocations(tid, pack);
                if (ok) {
                    for (int i = 0; i < pack.size(); i++) {
                        synchronized (locationsQueue) {
                            locationsQueue.pollFirst();
                        }
                    }
                } else {
                    isError.set(true);
                    break;
                }
            } catch (Exception e) {
                notifyError(new WayTodayError("Error while uploading locations", e));
                isError.set(true);
                break;
            }
            pack.clear();
        }
    }
}