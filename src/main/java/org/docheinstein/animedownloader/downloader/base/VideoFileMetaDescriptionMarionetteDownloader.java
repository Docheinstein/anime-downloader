package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * Represents a {@see VideoFileMarionetteDownloader} that retrieves the
 * minimal video info (i.e. the title) from the <meta name="description"> tag
 * of the root HTML page (most of the sites do this).
 */
public abstract class VideoFileMetaDescriptionMarionetteDownloader extends VideoFileMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(VideoFileMetaDescriptionMarionetteDownloader.class);

    public VideoFileMetaDescriptionMarionetteDownloader(String downloadUrl, File outputPath,
                                                        File driverPath, boolean ghostMode,
                                                        DownloadableVideoInfo info,
                                                        VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghostMode, info, downloadObserver);
    }

    @Override
    public VideoInfo retrieveVideoInfoStrict() {
        DownloadableVideoInfo info = new DownloadableVideoInfo();

        WebDriver driver = getDriver();
        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        WebElement metaDescription = driver.findElement(By.name("description"));
        if (metaDescription == null) {
            L.warn("Cannot retrieve <meta name='description' content='...'> element");
            return null;
        }

        info.title = info.filename = metaDescription.getAttribute("content");

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return info;
    }
}
