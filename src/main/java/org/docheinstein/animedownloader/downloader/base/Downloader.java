package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.video.DownloadableVideoInfo;

/**
 * Interface that represents a downloader of resource.
 */
public interface Downloader {

    /**
     * Starts the download and returns the video info about the just started video download.
     * <p>
     * The downloader should return all the available info about the video.
     * @return the video info
     */
    DownloadableVideoInfo startDownload();

    /** Aborts the download. */
    void abortDownload();
}
