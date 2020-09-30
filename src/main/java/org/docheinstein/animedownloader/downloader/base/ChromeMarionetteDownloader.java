package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.driver.QuitAwareChromeDriver;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.logging.Level;

/**
 * Entity that represents a download of video that use {@link WebDriver}
 * to retrieve video info/link, more specifically a Chrome Driver.
 */
public abstract class ChromeMarionetteDownloader extends AutonomousVideoDownloaderImpl {

    private static final DocLogger L =
        DocLogger.createForClass(ChromeMarionetteDownloader.class);

    public ChromeMarionetteDownloader(String downloadUrl,
                                      File outputPath,
                                      File driverPath,
                                      boolean ghost,
                                      DownloadableVideoInfo info,
                                      VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghost, info, downloadObserver);
    }

    @Override
    protected void initDriver() {
        System.setProperty(
            "webdriver.chrome.driver",
            mDriverPath.getAbsolutePath()
        );

        ChromeOptions co = new ChromeOptions();

        // Invisibility
        if (mGhost) {
            L.verbose("Ghost chrome driver required");
            co.addArguments("--headless");
            // co.addArguments("--mute-audio");
        }
        else {
            L.verbose("Visible chrome driver required");
        }

        // Mute anyway
        co.addArguments("--mute-audio");

        // Logging, for access network resources
        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        caps.setCapability("goog:loggingPrefs", logPrefs);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        // Accept untrusted certs
        caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        caps.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        co.merge(caps);

        mDriver = new QuitAwareChromeDriver(co);
    }
}
