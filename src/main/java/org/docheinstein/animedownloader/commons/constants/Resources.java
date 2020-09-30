package org.docheinstein.animedownloader.commons.constants;

import javafx.scene.image.Image;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.commons.utils.ResourceUtil;

/** Contains preloaded resources. */
public class Resources {

    /** Contains resource of the UI (e.g. images) */
    public static class UI {

        public static final Image ICONS[];
        public static final Image START;
        public static final Image STOP;
        public static final Image OPEN_FOLDER;
        public static final Image OPENLOAD;
        public static final Image VVVVID;
        public static final Image STREAMANGO;
        public static final Image VERYSTREAM;
        public static final Image YOUTUBE;
        public static final Image MIXDROP;

        static {
            ICONS = new Image[] {
                FxUtil.createImage(
                    ResourceUtil.getImageStream("logo.png"))
            };

            START = FxUtil.createImage(
                ResourceUtil.getImageStream("start.png"));

            STOP = FxUtil.createImage(
                ResourceUtil.getImageStream("stop.png"));

            OPEN_FOLDER = FxUtil.createImage(
                ResourceUtil.getImageStream("folder.png"));

            OPENLOAD = FxUtil.createImage(
                ResourceUtil.getImageStream("openload.png"));

            VVVVID = FxUtil.createImage(
                ResourceUtil.getImageStream("vvvvid.jpg"));

            STREAMANGO = FxUtil.createImage(
                ResourceUtil.getImageStream("streamango.png"));

            VERYSTREAM = FxUtil.createImage(
                ResourceUtil.getImageStream("verystream.png"));

            YOUTUBE = FxUtil.createImage(
                ResourceUtil.getImageStream("youtube.png"));

            MIXDROP = FxUtil.createImage(
                ResourceUtil.getImageStream("mixdrop.png"));
        }

    }
}
