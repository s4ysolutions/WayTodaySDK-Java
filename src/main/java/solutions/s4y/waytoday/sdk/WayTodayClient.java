package solutions.s4y.waytoday.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main class of the SDK providing the API to the WayToday service
 * <p>
 * The API consists of two parts:
 * 1. The methods to request and release a tracking id(trackId).
 * 2. The methods to sent locations to the WayToday service and to get the status of the uploading.
 */
public class WayTodayClient {
    private static final AtomicBoolean isUploading = new AtomicBoolean();
    private static final AtomicBoolean isError = new AtomicBoolean();
    private final IPersistedState persistedState;
    private final GrpcClient grpcClient;

    final ArrayList<IErrorsListener> errorsListeners =
            new ArrayList<>(2);
    final ArrayList<ITrackIdChangeListener> trackIdChangeListeners =
            new ArrayList<>(2);
    final List<IUploadingLocationsStatusChangeListener> uploadLocationsStatusChangeListeners =
            new ArrayList<>(2);
    private final Deque<Location> locationsQueue = new LinkedList<>();
    private final static int MAX_LOCATIONS_MEMORY = 500;
    private final static int PACK_SIZE = 16;
    protected Logger logger = LoggerFactory.getLogger(WayTodayClient.class);

    /**
     * Creates an instance of the client, intended to be used in tests only.
     *
     * @param persistedState an implementation of {@link  IPersistedState IPersistedState} to store
     *                       the state of the client.
     * @param grpcClient     an implementation of GrpcClient to communicate
     *                       with the WayToday service
     */
    WayTodayClient(IPersistedState persistedState, GrpcClient grpcClient) {
        this.persistedState = persistedState;
        this.grpcClient = grpcClient;
    }

    /**
     * Creates an instance of the client, intended to be used in production.
     *
     * @param persistedState an implementation of {@link IPersistedState} to store
     *                       the state of the client.
     */
    public WayTodayClient(IPersistedState persistedState) {
        this(persistedState, new GrpcClient());
    }

    /**
     * Subscribes listener to the responses on the request to retrieve new track id.
     *
     * @param listener the implementation of  {@link ITrackIdChangeListener} to handle
     *                 the notification
     */
    public void addOnTrackIdChangeListener(@Nonnull ITrackIdChangeListener listener) {
        synchronized (trackIdChangeListeners) {
            trackIdChangeListeners.add(listener);
        }
    }

    /**
     * Unsubscribes listener from the responses on the request to retrieve new track id.
     *
     * @param listener the implementation of {@link ITrackIdChangeListener} used
     *                 in the previous call of {@link #addOnTrackIdChangeListener addOnTrackIdChangeListener}
     */
    public void removeOnTrackIdChangeListener(@Nonnull ITrackIdChangeListener listener) {
        trackIdChangeListeners.remove(listener);
    }

    /**
     * Subscribe the listener to changes of the uploading locations status.
     *
     * @param listener an implementation of {@link IUploadingLocationsStatusChangeListener}
     */

    public void addUploadLocationsStatusChangeListener(IUploadingLocationsStatusChangeListener listener) {
        synchronized (uploadLocationsStatusChangeListeners) {
            uploadLocationsStatusChangeListeners.add(listener);
        }
    }


    /**
     * Unsubscribe the listener from changes of the uploading locations status.
     *
     * @param listener an implementation of {@link IUploadingLocationsStatusChangeListener}
     *                 used in the previous call of {@link #addUploadLocationsStatusChangeListener addUploadStatusChangeListener}
     */

    public void removeUploadLocationsStatusChangeListener(IUploadingLocationsStatusChangeListener listener) {
        synchronized (uploadLocationsStatusChangeListeners) {
            uploadLocationsStatusChangeListeners.remove(listener);
        }
    }

    /**
     * Subscribe the listener to errors.
     *
     * @param listener an implementation of {@link IErrorsListener}
     */

    public void addErrorsListener(IErrorsListener listener) {
        synchronized (errorsListeners) {
            errorsListeners.add(listener);
        }
    }

    /**
     * Unsubscribe the listener from errors.
     *
     * @param listener an implementation of {@link IErrorsListener}
     *                 used in the previous call of {@link #addErrorsListener addErrorsListener}
     */

    public void removeErrorsListener(IErrorsListener listener) {
        synchronized (errorsListeners) {
            errorsListeners.remove(listener);
        }
    }

    /**
     * Requests new track id from the server synchronously.
     *
     * @param prevId - existing track id to be released
     * @return new track id or empty string if the request failed
     */
    public String requestNewTrackerId(@Nullable String prevId) {
        try {
            String id = grpcClient.generateTrackerId(prevId);
            persistedState.setTrackerId(id);
            notifyTrackIdChange(id);
            return id;
        } catch (Exception e) {
            logger.error("Error while requesting new tracker id", e);
            notifyError(new WayTodayError("Error while requesting new tracker id", e));
            return "";
        }
    }

    /**
     * @return trackId - the track id to be released
     */
    @Nonnull
    public String getCurrentTrackerId() {
        return persistedState.getTrackerId();
    }

    /**
     * Adds the location to the queue of the locations to be uploaded
     * and returns immediately.
     *
     * @param location - a Geo location to be added to the queue
     */
    public void enqueueLocationToUpload(Location location) {
        synchronized (locationsQueue) {
            locationsQueue.addLast(location);
            if (locationsQueue.size() > MAX_LOCATIONS_MEMORY) {
                locationsQueue.removeFirst();
            }
        }
        notifyUploadLocationsState();
    }

    /**
     * Uploads the locations from the queue to the server synchronously.
     */
    public void uploadLocations() {
        String tid = getCurrentTrackerId();
        if (tid.isEmpty()) return;
        if (locationsQueue.isEmpty()) return;
        // TODO: notify
        if (isUploading.compareAndSet(false, true))
            return;
        isError.set(false);
        notifyUploadLocationsState();
        try {
            uploadQueue(tid);
        } catch (Exception e) {
            isError.set(true);
            notifyUploadLocationsState();
        } finally {
            isUploading.set(false);
            notifyUploadLocationsState();
        }
    }

    /**
     * @return current status of the uploading locations
     */
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
        /*
        if (BuildConfig.DEBUG) {
            boolean changed = false;
            if (sIsError != sPrevIsError) {
                sPrevIsError = sIsError;
                changed = true;
            }
            if (sIsUploading != sPrevIsUploading) {
                sPrevIsUploading = sIsUploading;
                changed = true;
            }
            int size = uploadQueue.size();
            if (size != sPrevSize) {
                sPrevSize = size;
                changed = true;
            }
            if (changed) {
                UploadStatus uploadStatus = uploadStatus();
                if (sPrevUploadStatus == uploadStatus) {
                    ErrorsObservable.notify(new Exception("Status must not be the same"), true);
                }
                notifyUploadStatus();
                sPrevUploadStatus = uploadStatus;
            } else {
                ErrorsObservable.notify(new Exception("Should never be called without changes"), true);
            }
        } else {
         */
        UploadingLocationsStatus status = getUploadingLocationsStatus();
        List<IUploadingLocationsStatusChangeListener> listeners;
        synchronized (uploadLocationsStatusChangeListeners) {
            listeners = new ArrayList<>(uploadLocationsStatusChangeListeners);
        }
        for (IUploadingLocationsStatusChangeListener listener : listeners) {
            listener.onStatusChange(status);
        }
    }

    private void uploadQueue(@Nonnull final String tid) {
        List<Location> pack = new ArrayList<>();
        for (; ; ) {
            // prepare pack
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
                    // unknown error
                    isError.set(true);
                    break;
                }
            } catch (Exception e) {
                // network error
                logger.error("Error while uploading locations", e);
                notifyError(new WayTodayError("Error while uploading locations", e));
                isError.set(true);
                break;
            }
            pack.clear();
        }
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
}
