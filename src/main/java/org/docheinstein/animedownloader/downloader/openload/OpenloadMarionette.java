package org.docheinstein.animedownloader.downloader.openload;

import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.downloader.base.VideoFileMetaDescriptionMarionetteDownloader;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.commons.http.HttpRequester;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.types.StringUtil;

import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;

import java.util.List;
import java.util.Map;

/**
 * Specific marionette able to download video from "https://openload.co".
 */
public class OpenloadMarionette extends VideoFileMetaDescriptionMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(OpenloadMarionette.class);

    /** Template of the video's direct link */
    private static final String DIRECT_LINK_CURRENT_TEMPLATE = "https://openload.co/stream/%s";

    /** ID of the container of the video's direct link. */
    private static final String DIRECT_LINK_CURRENT_CONTAINER_ID = "DtsBlkVFQx";

    public OpenloadMarionette(String downloadUrl,
                              File outputPath,
                              File driverPath,
                              boolean ghostMode,
                              DownloadableVideoInfo info,
                              VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghostMode, info, downloadObserver);
    }

    /**
     * Returns the direct link o the video.
     * @return the direct link of the video
     */
    @Override
    protected String getVideoLink() {
        L.verbose("Going to fetch page from " + mDownloadUrl);

        WebDriver driver = getDriver();
        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        L.verbose("Waiting for video CDN link container to be added to the page");

        (new WebDriverWait(driver, 10L)).until((ExpectedCondition<Boolean>) d -> {
            WebElement videoElement = driver.findElement(By.id(DIRECT_LINK_CURRENT_CONTAINER_ID));
            String videoElementContent = videoElement.getAttribute("textContent");
            L.debug("Current content of direct link container is: " + videoElementContent);
            return
                !videoElementContent.isEmpty() &&
                    !videoElementContent.contains("HERE IS THE LINK");
        });

        WebElement partialCDNLinkContainer = driver.findElement(By.id(DIRECT_LINK_CURRENT_CONTAINER_ID));
        String partialCDNLink = partialCDNLinkContainer.getAttribute("textContent");

        L.verbose("Container of the CDN link found; content is: " + partialCDNLink);

        String cdnLink = String.format(DIRECT_LINK_CURRENT_TEMPLATE, partialCDNLink);

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        Map<String, List<String>> headerFields = HttpRequester
            .head(cdnLink)
            .allowRedirect(false)
            .send()
            .getHeaderFields();

        printHeaderFields(headerFields);

        List<String> directLinks = headerFields.get("Location");

        if (directLinks == null || directLinks.size() < 1) {
            L.error("Can't retrieve 'Location' header");
            return null;
        }

        String streamDirectLink = directLinks.get(0);

        L.verbose("Direct link location resolved to: " + streamDirectLink);

        return streamDirectLink;
    }
}