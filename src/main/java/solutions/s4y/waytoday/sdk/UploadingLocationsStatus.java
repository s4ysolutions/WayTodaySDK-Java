package solutions.s4y.waytoday.sdk;

/**
 * Statuses the uploader can be in
 * Intended to be used to show the status of the uploader in UI
 *
 * EMPTY - no locations to upload, the queue is empty
 * QUEUED - locations are queued for upload, but not yet uploaded
 * UPLOADING - locations are being uploaded
 * ERROR - an error occurred during upload, it is reset on next upload attempt
 */

public enum UploadingLocationsStatus {
    EMPTY, QUEUED, UPLOADING, ERROR
}
