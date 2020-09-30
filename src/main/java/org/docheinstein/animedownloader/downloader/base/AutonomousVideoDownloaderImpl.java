package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.driver.QuitAwareWebDriver;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.openqa.selenium.WebDriver;

import java.io.File;

public abstract class AutonomousVideoDownloaderImpl implements AutonomousVideoDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(AutonomousVideoDownloaderImpl.class);

    /** Selenium web driver. */
    protected QuitAwareWebDriver mDriver;

    /** Whether the chrome driver should be started silently. */
    protected boolean mGhost;

    /** Observer of the video download. */
    protected VideoDownloadObserver mObserver;

    /** URL of the site (it is not the definitive link of the video). */
    protected String mDownloadUrl;

    /** Download folder. */
    protected File mDownloadFolder;

    /** Chrome driver path. */
    protected File mDriverPath;

    /** Video info. */
    protected DownloadableVideoInfo mVideoInfo;

    public AutonomousVideoDownloaderImpl(String downloadUrl,
                                         File outputPath,
                                         File driverPath,
                                         boolean ghost,
                                         DownloadableVideoInfo info,
                                         VideoDownloadObserver downloadObserver) {
        mDownloadUrl = downloadUrl;
        mDownloadFolder = outputPath;
        mDriverPath = driverPath;
        mGhost = ghost;
        mVideoInfo = info;
        mObserver = downloadObserver;
    }

    /**
     * Initializes the web driver.
     */
    protected abstract void initDriver();

    /**
     * Returns the minimal video info (i.e. the title)
     * @return the minimal video info.
     */
    protected abstract VideoInfo retrieveVideoInfoStrict();

    /**
     * Returns the detailed video info (i.e. the information needed for
     * perform the download).
     * @return the details video info
     */
    protected abstract DownloadableVideoInfo retrieveDownloadableVideoInfoStrict();

    @Override
    public VideoInfo retrieveVideoInfo() {
        L.debug("> retrieveVideoInfo");
        // Retrieve the video info and cache those (merging with the current ones)

        mVideoInfo = DownloadableVideoInfo.merged(
            mVideoInfo,
            DownloadableVideoInfo.fromVideoInfo(retrieveVideoInfoStrict())
        );
        return mVideoInfo;
    }

    @Override
    public DownloadableVideoInfo retrieveDownloadableVideoInfo() {
        L.debug("> retrieveDownloadableVideoInfo");

        // Retrieve the video info and cache those (merging with the current ones)

        DownloadableVideoInfo downloadInfo = retrieveDownloadableVideoInfoStrict();

        L.debug("retrieveDownloadableVideoInfoStrict gave info: " + downloadInfo);

        mVideoInfo = DownloadableVideoInfo.merged(
            mVideoInfo,
            downloadInfo
        );

        return mVideoInfo;
    }

    /**
     * Returns the web driver and initializes it if needed.
     * @return the web driver
     */
    protected WebDriver getDriver() {
        if (mDriver == null) {
            L.debug("Initialized web driver since null");
            initDriver();
        }
        else if (mDriver.hasQuitted()) {
            L.debug("Initializing web driver since has quitted");
            initDriver();
        }
        else {
            L.debug("getDriver skipped initialization; returning valid web driver");
        }

        return mDriver;
    }
}
