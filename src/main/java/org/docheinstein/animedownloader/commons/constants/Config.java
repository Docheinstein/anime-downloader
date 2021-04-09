package org.docheinstein.animedownloader.commons.constants;

import org.docheinstein.commons.system.OSType;
import org.docheinstein.commons.system.SystemUtil;


/** Contains application's config. */
public class Config {

    /** Values concern the application itself. */
    public static class App {
        public static final String TITLE = "Anime Downloader";
        public static final String SETTINGS_TITLE = "Settings";
        public static final String LIVE_CONSOLE_TITLE = "Live console (F10)";

        public static final int PREF_WIDTH = 800;
        public static final int PREF_HEIGHT = 600;

        public static final int MIN_WIDTH = 480;
        public static final int MIN_HEIGHT = 320;

        public static final int LIVE_CONSOLE_WIDTH = 800;
        public static final int LIVE_CONSOLE_HEIGHT = 600;

        public static final String VERSION = "0.9.12";
    }

    /** Contains the application's files names. */
    public static class Files {
        public static final String SETTINGS = "settings.json";
        public static final String BASE_CHROMEDRIVER = "chromedriver";
    }

    /** Contains the application's folders names. */
    public static class Folders {
        public static final String SETTINGS = "settings";
        public static final String LOGS = "logs";
        public static final String VIDEOS = "videos";
        public static final String TMP = "tmp";
        public static final String DRIVERS = "drivers";
    }

    /** Contains the relative paths of the resources of this application. */
    public static class Resources {
        public static final String ASSETS = "assets/";
        public static final String IMAGES = "images/";
        public static final String CSS = "css/";

//        public static final String CHROME_DRIVER;
//
//        static {
//            OSType os = SystemUtil.getCurrentOperatingSystemType();
//
//            if (os == OSType.Linux)
//                CHROME_DRIVER = "chromedriver/chromedriver_linux64.zip";
//            // Win and mac are actually not bundled within the app
//            else if (os == OSType.Windows)
//                CHROME_DRIVER = "chromedriver/chromedriver_windows64.zip";
//            else if (os == OSType.Mac)
//                CHROME_DRIVER = "chromedriver/chromedriver_mac64.zip";
//            else
//                CHROME_DRIVER = null;
//        }
    }

    /**
     * Defines the keys used for the jsons of the application.
     */
    public static class Json {
        public static class VideoCache {
            public static final String KEY_URL =    "url";
            public static final String KEY_TITLE =  "title";
        }

        public static class Settings {
            public static final String SIMULTANEOUS_VIDEO_LIMIT = "simultaneous_video_limit";
            public static final String SIMULTANEOUS_VIDEO_LIMIT_FOR_EACH_PROVIDER = "simultaneous_video_limit_for_each_provider";
            public static final String BANDWIDTH_LIMIT = "bandwidth_limit";
            public static final String CHROME_DRIVER = "chrome_driver";
            public static final String CHROME_DRIVER_GHOST_MODE = "chrome_driver_ghost_mode";
            public static final String CLOSE_DRIVER_ON_DOWNLOAD_STARTED = "close_driver_on_download_start";
            public static final String CLOSE_DRIVER_ON_INFO_RETRIEVED = "close_driver_on_info_retrieved";
            public static final String FFMPEG = "ffmpeg";
            public static final String YOUTUBE_DL = "youtube_dl";
            public static final String LOGGING = "logging";
            public static final String FLUSH_LOGS = "flush_logs";
        }
    }

    /**
     * Configurations of download strategy.
     */
    public static class Download {
        public static final int ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_UNDER_THRESHOLD_BEFORE_DOWNLOAD_IF_CURRENT_BANDWIDTH_IS_0 = 2;
        public static final int ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_UNDER_THRESHOLD_BEFORE_DOWNLOAD = 10;
        public static final int ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_AFTER_A_DOWNLOAD = 5;
    }

    /** Debug configurations. */
    public static class Debug {
        public static final boolean DUMP_PAGES = true;
    }
}
