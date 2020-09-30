package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.video.VideoInfo;

/**
 * Interface that represents a downloader of video.
 */
public interface VideoDownloader extends Downloader {

    /**
     * Returns the minimal information about the video (i.e. the title).
     * @return the minimal video info
     */
    VideoInfo retrieveVideoInfo();
}
