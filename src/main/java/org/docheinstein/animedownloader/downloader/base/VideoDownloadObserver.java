package org.docheinstein.animedownloader.downloader.base;

/**
 * Interface used to listen to events from the video download.
 */
public interface VideoDownloadObserver {

    /** Called when the video download starts. */
    void onVideoDownloadStarted();

    /**
     * Called on video download progress
     * @param downloadedBytes the amount of downloaded bytes
     * @param millis the time in millis of the last chunk download
     */
    void onVideoDownloadProgress(long downloadedBytes, long millis);

    /**
     * Called when the video download is finished successfully.
     * <p>
     * This method is not called if the download is aborted.
     */
    void onVideoDownloadFinished();

    /**
     * Called when the video download is aborted by the user
     * or for other reasons.
     */
    void onVideoDownloadAborted();

    /**
     * Called when a new size of the video has been detected.
     * @param videoSizeBytes the size in bytes of the video
     * @param certainly true if the size is certainly right or
     *                  false if it is just an estimation
     */
    void onVideoSizeDetected(long videoSizeBytes, boolean certainly);
}

