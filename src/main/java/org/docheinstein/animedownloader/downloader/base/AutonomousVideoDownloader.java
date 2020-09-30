package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.video.DownloadableVideoInfo;

/**
 * Interface that represents a {@link VideoDownloader} able to retrieve the video
 * info by itself.
 */
public interface AutonomousVideoDownloader extends VideoDownloader {

    /**
     * Returns the information about the video download, i.e. the info
     * should contain all the information needed for proceed with the real
     * download of the video.
     * @return the detailed video info
     */
    DownloadableVideoInfo retrieveDownloadableVideoInfo();
}
