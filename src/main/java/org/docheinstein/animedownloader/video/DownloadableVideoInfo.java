package org.docheinstein.animedownloader.video;

import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.types.StringUtil;

/**
 * Encapsulates details info about a video.
 * <p>
 * This contains also info related to the video download such us
 * the direct link for perform the download and the video size.
 */
public class DownloadableVideoInfo extends VideoInfo {

    private static final DocLogger L =
        DocLogger.createForClass(DownloadableVideoInfo.class);

    /** Direct link to the video resource. */
    public String directLink = null;

    /** Filename of the video. */
    public String filename = null;

    /** Size in byte of the video. */
    public long size = 0; // bytes

    @Override
    public String toString() {
        return "\n\n" +
            "[TITLE]: " + title + "\n" +
            "[FILENAME]: " + filename + "\n" +
            "[DIRECT_LINK]: " + directLink + "\n" +
            "[SIZE]: " + size + "\n";
    }

    /**
     * Builds a detailed video info from a basic one.
     * @param info the basic video info
     * @return the details video info
     */
    public static DownloadableVideoInfo fromVideoInfo(VideoInfo info) {
        if (info == null)
            return null;
        DownloadableVideoInfo downloadInfo = new DownloadableVideoInfo();
        downloadInfo.title = info.title;
        if (info instanceof DownloadableVideoInfo) {
            downloadInfo.filename = ((DownloadableVideoInfo) info).filename;
        } else {
            downloadInfo.filename = info.title;
        }
        return downloadInfo;
    }

    /**
     * Returns a clone of the downloaded video info.
     * @param info the info to clone
     * @return the cloned instance
     */
    public static DownloadableVideoInfo cloned(DownloadableVideoInfo info) {
//        L.debug("Cloning: " + info);
        DownloadableVideoInfo clone = new DownloadableVideoInfo();
        clone.merge(info);
//        L.debug("Post cloned info: " + info);
        return clone;
    }

    /**
     * Merges two detailed video info into a single one with the non-null
     * values of the two, creating a new info instance.
     * <p>
     * If both are not null, the values of the first instance is kept
     * @param info1 the primary video info
     * @param info2 the secondary video info
     * @return the merged video info
     */
    public static DownloadableVideoInfo merged(DownloadableVideoInfo info1,
                                               DownloadableVideoInfo info2) {
        L.debug("Pre merged() info 1: " + info1);
        L.debug("Pre merged() info 2: " + info2);

        if (info1 != null) {
            info1.merge(info2);
            L.debug("Post info1.merge(info2) info: " + info1);
            return cloned(info1);
        }

        L.debug("Post merged() info: " + info2);
        return cloned(info2);
    }

    /**
     * Merges the given video info with this one, using the non-null values
     * of the second where the values of this instance are null.
     * @param info the video info to merge with this
     */
    public void merge(DownloadableVideoInfo info) {
        if (info == null)
            return;

        L.debug("Pre merge() info1: " + this);
        L.debug("Pre merge() info2: " + info);

        if (!StringUtil.isValid(directLink))
            directLink = info.directLink;
        if (!StringUtil.isValid(filename))
            filename = info.filename;
        if (!StringUtil.isValid(title))
            title = info.title;
        if (size <= 0)
            size = info.size;

        L.debug("Post merge() info: " + this);
    }

    /**
     * Returns whether this video info provides valid download info (direct
     * link and file name).
     * @return whether this video info provides valid download info
     */
    public boolean providesDownloadInfo() {
        return
            StringUtil.isValid(directLink) &&
            StringUtil.isValid(filename);
    }
}
