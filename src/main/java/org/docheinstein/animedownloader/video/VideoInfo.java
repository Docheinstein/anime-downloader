package org.docheinstein.animedownloader.video;

import org.docheinstein.commons.types.StringUtil;

/**
 * Encapsulates minimal info about a video.
 * <p>
 * Actually this means only the video title.
 */
public class VideoInfo {
    /** Title of the video. */
    public String title = null;

    /**
     * Returns whether this video info provides valid meta info (i.e. video
     * title).
     * @return whether this video provides valid meta info
     */
    public boolean providesMetaInfo() {
        return StringUtil.isValid(title);
    }
}
