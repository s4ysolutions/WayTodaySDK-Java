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

    @SuppressWarnings("unused")
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

    private final AtomicBoolean requestNewTrackerIdProgress = new AtomicBoolean(false);
    private final AtomicBoolean requestNewTrackerIdFailed = new AtomicBoolean(false);

    @SuppressWarnings("unused")
    public boolean isRequestNewTrackerIdInProgress() {
        return requestNewTrackerIdProgress.get();
    }

    @SuppressWarnings("unused")
    public boolean isRequestNewTrackerIdFailed() {
        return requestNewTrackerIdFailed.get();
    }

    public String requestNewTrackerId(@Nullable String prevId) {
        if (!requestNewTrackerIdProgress.compareAndSet(false, true)) {
            return "";
        }
        try {
            requestNewTrackerIdFailed.set(false);
            String id = grpcClient.generateTrackerId(prevId);
            requestNewTrackerIdProgress.set(false);
            persistedState.setTrackerId(id);
            notifyTrackIdChange(id);
            return id;
        } catch (Exception e) {
            requestNewTrackerIdProgress.set(false);
            requestNewTrackerIdFailed.set(true);
            notifyError(new WayTodayError("Error while requesting new tracker id", e));
            return "";
        }
    }

    public void uploadLocations() {
        String tid = getCurrentTrackerId();
        if (tid.isEmpty()) {
            isError.set(true);
            return;
        }
        if (!isUploading.compareAndSet(false, true))
            return;
        isError.set(false);
        notifyUploadLocationsState();
        try {
            uploadQueue(tid);
            isUploading.set(false);
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
            try {
                listener.onError(error);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void notifyTrackIdChange(@Nonnull String trackId) {
        List<ITrackIdChangeListener> listeners;
        synchronized (trackIdChangeListeners) {
            listeners = new ArrayList<>(trackIdChangeListeners);
        }
        for (ITrackIdChangeListener listener : listeners) {
            try{
                listener.onTrackId(trackId);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void notifyUploadLocationsState() {
        UploadingLocationsStatus status = getUploadingLocationsStatus();
        List<IUploadingLocationsStatusChangeListener> listeners;
        synchronized (uploadingLocationsStatusChangeListeners) {
            listeners = new ArrayList<>(uploadingLocationsStatusChangeListeners);
        }
        for (IUploadingLocationsStatusChangeListener listener : listeners) {
            try {
                listener.onStatusChange(status);
            } catch (Exception e) {
                // ignore
            }
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
                isError.set(true);
                notifyError(new WayTodayError("Error while uploading locations", e));
                break;
            }
            pack.clear();
        }
    }
}