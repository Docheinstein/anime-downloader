package org.docheinstein.animedownloader.jsettings;

import com.google.gson.*;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.commons.files.FileHierarchy;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.types.StringUtil;

import java.io.File;

/**
 * Manager of the application settings.
 * <p>
 * Is responsible for load the settings from settings.json, update and save
 * those.
 */
public class JSettings {

    public enum AutomaticDownloadStrategy {
        Static("static"),
        Adaptive("adaptive")
        ;

        private String mName;

        AutomaticDownloadStrategy(String name) {
            mName = name;
        }

        public static AutomaticDownloadStrategy fromName(String name) {
            if (name == null)
                return null;
            switch (name) {
                case "static":
                    return Static;
                case "adaptive":
                    return Adaptive;
                default:
                    return null;
            }
        }

        public String getName() {
            return mName;
        }
    }

    private static final JSettings INSTANCE;

    private static final DocLogger L;

    static {
        L = DocLogger.createForClass(JSettings.class);
        INSTANCE = new JSettings();
    }

    private JSettings() {
        loadOrCreateSettings();
    }

    public static JSettings instance() {
        return INSTANCE;
    }

    private final JSettingFile mDownloadFolder =
        new JSettingFile("download_folder", new File("."));

    private final JSettingBoolean mRemoveAfterDownload =
        new JSettingBoolean("remove_after_download", false);

    private final JSettingBoolean mDownloadAutomatically =
        new JSettingBoolean("download_automatically", false);

    private final JSettingImpl<JSettings.AutomaticDownloadStrategy> mAutomaticDownloadStrategy =
        new JSettingImpl<>(
            "automatic_download_strategy", JSettings.AutomaticDownloadStrategy.Static) {
            @Override
            public JsonElement jsonifiedValue(JSettings.AutomaticDownloadStrategy value) {
                return value != null ?
                    new JsonPrimitive(value.getName()) :
                    JsonNull.INSTANCE;
            }

            @Override
            public JSettings.AutomaticDownloadStrategy unjsonifiedValue(JsonElement json) {
                return JSettings.AutomaticDownloadStrategy.fromName(json.getAsString());
            }
        };

    private final JSettingInteger mSimultaneousVideoLimit =
        new JSettingInteger(Config.Json.Settings.SIMULTANEOUS_VIDEO_LIMIT, 1);

    private final JSettingBoolean mSimultaneousVideoLimitForEachProvider =
        new JSettingBoolean(Config.Json.Settings.SIMULTANEOUS_VIDEO_LIMIT_FOR_EACH_PROVIDER, true);

    private final JSettingInteger mBandwidthLimit =
        new JSettingInteger(Config.Json.Settings.BANDWIDTH_LIMIT, 1);

    private final JSettingFile mChromeDriver =
        new JSettingFile(Config.Json.Settings.CHROME_DRIVER,
            FileHierarchy.instance().getChromeDriverNode().getFile());

    private final JSettingBoolean mChromeDriverGhostMode =
        new JSettingBoolean(Config.Json.Settings.CHROME_DRIVER_GHOST_MODE, true);

    private final JSettingBoolean mCloseDriverOnDownloadStarted =
        new JSettingBoolean(Config.Json.Settings.CLOSE_DRIVER_ON_DOWNLOAD_STARTED, true);

    private final JSettingBoolean mCloseDriverOnInfoRetrieved =
        new JSettingBoolean(Config.Json.Settings.CLOSE_DRIVER_ON_INFO_RETRIEVED, true);

    private final JSettingFile mFFmpeg =
        new JSettingFile(Config.Json.Settings.FFMPEG, null);

    private final JSettingFile mYoutubeDl =
        new JSettingFile(Config.Json.Settings.YOUTUBE_DL, null);

    private final JSettingBoolean mLoggingSetting =
        new JSettingBoolean(Config.Json.Settings.LOGGING, false);

    private final JSettingBoolean mFlushSetting =
        new JSettingBoolean(Config.Json.Settings.FLUSH_LOGS, false);

    private final JSettingImpl[] mSettings = new JSettingImpl[] {
        mDownloadFolder, mRemoveAfterDownload, mDownloadAutomatically,
        mAutomaticDownloadStrategy,
        mSimultaneousVideoLimit,
        mSimultaneousVideoLimitForEachProvider,
        mBandwidthLimit,
        mChromeDriver, mChromeDriverGhostMode, mCloseDriverOnDownloadStarted, mCloseDriverOnInfoRetrieved,
        mFFmpeg, mYoutubeDl, mLoggingSetting, mFlushSetting
    };

    public JSetting[] getSettings() {
        return mSettings;
    }

    public JSettingFile getDownloadFolderSetting() {
        return mDownloadFolder;
    }

    public JSettingBoolean getRemoveAfterDownloadSetting() {
        return mRemoveAfterDownload;
    }

    public JSettingImpl<JSettings.AutomaticDownloadStrategy> getAutomaticDownloadStrategySetting() {
        return mAutomaticDownloadStrategy;
    }

    public JSettingBoolean getDownloadAutomaticallySetting() {
        return mDownloadAutomatically;
    }

    public JSettingInteger getSimultaneousVideoLimitSetting() {
        return mSimultaneousVideoLimit;
    }

    public JSettingBoolean getSimultaneousVideoForEachProvider() {
        return mSimultaneousVideoLimitForEachProvider;
    }

    public JSettingInteger getBandwidthLimit() {
        return mBandwidthLimit;
    }

    public JSettingFile getChromeDriverSetting() {
        return mChromeDriver;
    }

    public JSettingBoolean getChromeDriverGhostModeSetting() {
        return mChromeDriverGhostMode;
    }

    public JSettingBoolean getCloseDriverOnDownloadStarted() {
        return mCloseDriverOnDownloadStarted;
    }

    public JSettingBoolean getCloseDriverOnInfoRetrieved() {
        return mCloseDriverOnInfoRetrieved;
    }

    public JSettingFile getFFmpegSetting() {
        return mFFmpeg;
    }

    public JSettingFile getYoutubeDlSetting() {
        return mYoutubeDl;
    }

    public JSettingBoolean getLoggingSetting() {
        return mLoggingSetting;
    }

    public JSettingBoolean getFlushSetting() {
        return mFlushSetting;
    }

    /**
     * Saves the settings to the settings.json file.
     * @return whether the save has been ok
     */
    public boolean saveSettings() {
        boolean saveOk = saveSettingsNoFlush();

        if (saveOk) {
            L.verbose("Notifying settings");
            for (JSettingImpl setting : mSettings)
                setting.flushValue();
        } else {
            L.warn("Cannot handle settings.json");
        }

        return saveOk;
    }

    /**
     * Parse the content of the setting.json and set the current settings
     * accordingly.
     * @param jsonSettings the json content to parse
     * @return whether the parse has been successful
     */
    private boolean parseSettings(JsonObject jsonSettings) {
        if (jsonSettings == null)
            return false;

        for (JSettingImpl setting : mSettings) {
            JsonElement json = jsonSettings.get(setting.getSettingName());
            if (json == null) {
                L.warn("Cannot find property " + setting.getSettingName() + " in settings.json");
                return false;
            }
            setting.updateValueFromJson(jsonSettings.get(setting.getSettingName()));
        }

        // Every property exists and has been read
        return true;
    }

    /**
     * Loads the settings.json into the current settings if it exists, otherwise
     * create a new default settings.json.
     */
    private void loadOrCreateSettings() {
        boolean readOk = false;

        File settingFile = FileHierarchy.instance().getSettingsNode().getFile();
        if (FileUtil.exists(settingFile)) {
            L.verbose("Settings file already exists, loading values from it");
            String settingsContent = FileUtil.readFile(settingFile);
            L.verbose("Loaded settings: " + settingsContent);

            if (StringUtil.isValid(settingsContent)) {
                try {
                    Gson gson = new Gson();
                    readOk = parseSettings(gson.fromJson(settingsContent, JsonObject.class));
                } catch (JsonSyntaxException ex) {
                    L.warn("Cannot parse settings.json content");
                }
            }
        }

        boolean proceed;

        if (!readOk) {
            L.verbose("Settings file cannot be read, creating it");
            proceed = saveSettingsNoFlush();
        } else {
            L.verbose("Settings file read successfully");
            proceed = true;
        }

        if (proceed) {
            L.verbose("Notifying settings");
            for (JSettingImpl setting : mSettings)
                setting.flushValue();
        } else {
            L.warn("Cannot handle settings.json");
        }
    }

    /**
     * Actually save the settings into the settings.json file
     * but do not perform settings flush nor notify.
     * @return whether the save has been ok
     */
    private boolean saveSettingsNoFlush() {
        L.verbose("Saving settings to settings.json");

        JsonObject settings = new JsonObject();

        for (JSettingImpl setting : mSettings) {
            JsonElement settingValue = setting.getFutureValueJson();
            L.debug(setting.getSettingName() + " = " + settingValue);
            settings.add(setting.getSettingName(), settingValue);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        return FileUtil.write(
            FileHierarchy.instance().getSettingsNode().getFile(),
            gson.toJson(settings));
    }
}