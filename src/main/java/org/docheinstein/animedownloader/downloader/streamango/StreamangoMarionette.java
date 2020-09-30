package org.docheinstein.animedownloader.downloader.streamango;

import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.downloader.base.VideoFileMetaDescriptionMarionetteDownloader;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

/**
 * Specific marionette able to download video from "https://streamango.com".
 */
public class StreamangoMarionette extends VideoFileMetaDescriptionMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(StreamangoMarionette.class);

    private static final String DIRECT_LINK_CONTAINER_ID = "mgvideo_html5_api";

    public StreamangoMarionette(String downloadUrl,
                                File outputPath,
                                File driverPath,
                                boolean ghostMode,
                                DownloadableVideoInfo info,
                                VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghostMode, info, downloadObserver);
    }


    @Override
    public String getVideoLink() {
        L.verbose("Going to fetch page from " + mDownloadUrl);

        WebDriver driver = getDriver();

        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        L.verbose("Waiting for video link container to be added to the page");

        (new WebDriverWait(driver, 10L)).until((ExpectedCondition<Boolean>) d -> {
            WebElement videoElement = driver.findElement(By.id(DIRECT_LINK_CONTAINER_ID));
            String videoElementContent = videoElement.getAttribute("src");
            L.debug("Current 'src' of direct link container is: " + videoElementContent);
            return !videoElementContent.isEmpty();
        });

        WebElement directLinkContainer = driver.findElement(By.id(DIRECT_LINK_CONTAINER_ID));
        String directLinkContent = directLinkContainer.getAttribute("src");

        String normalizedDirectLink =
            directLinkContent.startsWith("https://") ?
                directLinkContent :
                "https://" + directLinkContent;


        L.verbose("Container of the direct link found; (normalized) 'src' is: " + normalizedDirectLink);

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return normalizedDirectLink;
    }
}

