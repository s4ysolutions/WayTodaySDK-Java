package solutions.s4y.waytoday.sdk;

/**
 * Interface for listening to status changes of the uploader
 *
 * Used for asynchronous notifications of the uploader status
 */
@FunctionalInterface
public interface IUploadingLocationsStatusChangeListener {
    void onStatusChange(UploadingLocationsStatus status);
}
