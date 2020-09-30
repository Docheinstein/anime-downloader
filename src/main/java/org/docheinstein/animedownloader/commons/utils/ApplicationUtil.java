package org.docheinstein.animedownloader.commons.utils;

import org.docheinstein.animedownloader.commons.files.FileHierarchy;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.hierarchy.DNode;
import org.docheinstein.commons.internal.DocCommonsLogger;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.commons.time.TimeUtil;
import org.docheinstein.commons.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

/** Contains utility method for the application. */
public class ApplicationUtil {

    private static final DocLogger L =
        DocLogger.createForClass(ApplicationUtil.class);

    /** Performs the operation needed at the startup of this application. */
    public static void init() {

        if (!FileHierarchy.instance().ensureExistence()) {
            L.warn("Failed to create file hierarchy; bad things will happen");
        }

        DocLogger.enableLogLevel(DocLogger.LogLevel.Debug, true, true);
        DocLogger.enableLogLevel(DocLogger.LogLevel.Verbose, true, true);
        DocLogger.enableLogLevel(DocLogger.LogLevel.Info, true, true);
        DocLogger.enableLogLevel(DocLogger.LogLevel.Warn, true, true);
        DocLogger.enableLogLevel(DocLogger.LogLevel.Error, true, true);
        DocCommonsLogger.enable(true);
        DocCommonsLogger.addListener(message -> L.verbose("@@ " + message));

        updateLoggingOnFilesPreference();

        JSettings.instance().getLoggingSetting().addListener((setting, value) -> {
            L.verbose("Logging setting is changed; updating DocLogger accordingly");
            updateLoggingOnFilesPreference();
        });

        JSettings.instance().getFlushSetting().addListener((setting, value) -> {
            L.verbose("Flush setting is changed; updating DocLogger accordingly");
            updateLoggingOnFilesPreference();
        });

        ensureChromeDriverExistence();
    }

    /**
     * Ensures that the default chrome driver has been extracted from
     * the resources.
     */
    private static void ensureChromeDriverExistence() {
        L.verbose("Ensuring that default chrome driver exists");

//        L.verbose("Chrome driver resource chosen based on the current OS: " + Config.Resources.CHROME_DRIVER);

        File chromeDriver = FileHierarchy.instance().getChromeDriverNode().getFile();
        if (!FileUtil.exists(chromeDriver)) {
            L.error("Chrome driver not found, please download it from https://chromedriver.chromium.org/downloads");
/*            try {
                DNode driversNode = FileHierarchy.instance().getDriversNode();
                L.verbose("Unzipping chrome driver to " + driversNode.getPath());

                ZipUtil.unzip(
                    ResourceUtil.getResourceStream(Config.Resources.CHROME_DRIVER),
                    driversNode.getFile()
                );
            } catch (IOException e) {
                L.warn("Chrome driver unzipping failed");
            }*/
        }

        L.verbose("Ensuring that chrome driver is executable");
        if (!chromeDriver.canExecute()) {
            if (chromeDriver.setExecutable(true)) {
                L.verbose(chromeDriver + " is now executable");
            }
            else {
                L.warn("Executable flag can't be set on default chrome driver");
            }
        }
    }

    /**
     * Enables/disables logging of files based on current settings
     */
    private static void updateLoggingOnFilesPreference() {
        boolean enable = JSettings.instance().getLoggingSetting().getValue();
        boolean flush = JSettings.instance().getFlushSetting().getValue();
        if (enable)
            DocLogger.enableLoggingOnFiles(
                FileHierarchy.instance().getLogsNode().getFile(),
                () -> TimeUtil.nowToString(TimeUtil.Patterns.DATE_CHRONOLOGICALLY_SORTABLE),
                flush
            );
        else
            DocLogger.disableLoggingOnFiles();
    }
}
