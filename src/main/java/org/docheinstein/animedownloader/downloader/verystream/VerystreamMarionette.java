package org.docheinstein.animedownloader.downloader.verystream;

import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.downloader.base.VideoFileMetaDescriptionMarionetteDownloader;
import org.docheinstein.animedownloader.jsettings.JSetting;
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
 * Specific marionette able to download video from "https://verystream.com".
 */
public class VerystreamMarionette extends VideoFileMetaDescriptionMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(VerystreamMarionette.class);

    private static final String GET_TOKEN_URL = "https://%s/gettoken/%s";
    private static final String VIDEO_LINK_ID = "videolink";

    public VerystreamMarionette(String downloadUrl,
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

        String[] urlComponents = mDownloadUrl.split("https://");

        if (urlComponents.length <= 1) {
            L.error("Unexpected url, cannot proceed: " + mDownloadUrl);
            return null;
        }

        String domain = mDownloadUrl.split("https://")[1].split("/")[0];

        L.verbose("Domain: " + domain);

        WebDriver driver = getDriver();

        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        L.verbose("Waiting for video link container to be added to the page");

        (new WebDriverWait(driver, 10L)).until((ExpectedCondition<Boolean>) d -> {
            WebElement videoElement = driver.findElement(By.id(VIDEO_LINK_ID));
            String videoElementContent = videoElement.getAttribute("innerHTML");
            L.debug("Current content of direct link container is: " + videoElementContent);
            return !videoElementContent.isEmpty();
        });

        WebElement directLinkContainer = driver.findElement(By.id(VIDEO_LINK_ID));
        String directLinkContent = directLinkContainer.getAttribute("innerHTML");

        // https://verystream.com/gettoken/ + d4Wm5apdff1~1558600369~109.168.0.0~aPrk2U5a
        // OR (depending on the domain)
        // https://woof.tube.com/gettoken/ + d4Wm5apdff1~1558600369~109.168.0.0~aPrk2U5a

        String normalizedDirectLink = String.format(GET_TOKEN_URL, domain, directLinkContent);

        L.verbose("Container of the direct link found; (normalized) 'src' is: " + normalizedDirectLink);

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return normalizedDirectLink;
    }
}

