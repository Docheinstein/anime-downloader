package org.docheinstein.animedownloader.downloader.mixdrop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.downloader.base.VideoFileMarionetteDownloader;
import org.docheinstein.animedownloader.downloader.base.VideoFileMetaDescriptionMarionetteDownloader;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.thread.ThreadUtil;
import org.docheinstein.commons.types.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific marionette able to download video from "https://mixdrop.co".
 */
/*

<div class="vjs-poster" aria-disabled="false"
    style="background-image:
        url(&quot;//s-delivery2.mxdcontent.net/thumbs/63cb08017ce691fadeff8be5a209fc99.jpg&quot;);"
>
</div>

background-image: url("//s-delivery8.mxdcontent.net/thumbs/974f6227ed1653a87b4781bcbddc5bc3.jpg");

https://s-delivery8.mxdcontent.net/v/974f6227ed1653a87b4781bcbddc5bc3.mp4?s=PN1MFLJd_4hYv5kRZkIvvw&e=1586231436

 */
public class MixdropMarionette extends VideoFileMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(MixdropMarionette.class);

    private static final String PLAY_BUTTON_CLASS = "vjs-big-play-button";

    private DownloadableVideoInfo mVideoInfo = null;

    public MixdropMarionette(String downloadUrl,
                             File outputPath,
                             File driverPath,
                             boolean ghostMode,
                             DownloadableVideoInfo info,
                             VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghostMode, info, downloadObserver);
    }


    @Override
    public VideoInfo retrieveVideoInfoStrict() {
        L.debug("mixdrop: retrieveVideoInfoStrict");
        DownloadableVideoInfo info = new DownloadableVideoInfo();

        WebDriver driver = getDriver();
        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        WebElement title = driver.findElement(By.tagName("title"));
        if (title == null) {
            L.warn("Cannot retrieve <title> element");
            return null;
        }

        String titleInnerHtml = title.getAttribute("innerHTML");
        L.debug("Retrieved title: " + titleInnerHtml);

        if (StringUtil.isValid(titleInnerHtml)) {
            info.title = info.filename =
                    titleInnerHtml.replaceFirst("MixDrop - Watch ", "");
            info.filename += ".mp4";
        }

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return info;
    }

    @Override
    public String getVideoLink() {
        L.verbose("Going to fetch page from " + mDownloadUrl);

        WebDriver driver = getDriver();

        driver.get(mDownloadUrl);

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        // Remind the window handle
        String firstTabHandle = driver.getWindowHandle();

        L.verbose("Waiting for video iframe to be added to the page");

        // Wait for iframe
        WebElement videoIframe = (new WebDriverWait(driver, 10L)).until(
                (ExpectedCondition<WebElement>) d -> {
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

            L.verbose("Got iframes list, size: " + iframes.size());

            for (WebElement iframe : iframes) {
                if (iframe.getAttribute("src") != null) {
                    L.verbose("Got right iframe: " + iframe.getText());
                    return iframe;
                }
            }
            return null;
        });

        driver.switchTo().frame(videoIframe);

        L.verbose("Switched to iframe");
        L.verbose("Waiting for play button to be added to the iframe");

        WebElement playButton = (new WebDriverWait(driver, 10L)).until(
                (ExpectedCondition<WebElement>) d -> {
            List<WebElement> buttons = driver.findElements(By.className(PLAY_BUTTON_CLASS));

            if (buttons.isEmpty())
                return null;

            return buttons.get(0);
        });

        while (true) {
            try {
                Actions actions = new Actions(driver);
                actions.moveToElement(playButton).click().perform();
                playButton.click();
                L.debug("Button clicked, going on");
                break;
            } catch (Exception ex) {
                L.warn("Failed to click button, trying again", ex);
            }
        }

        L.debug("Waiting a bit...");
        ThreadUtil.sleep(3000);

        List<String> tabs = new ArrayList<>(driver.getWindowHandles());

        for (String tab : tabs) {
            L.debug("TAB : " + tab);
        }

        L.debug("Switching back to original window");
        driver.switchTo().window(firstTabHandle);

        L.debug("Waiting a bit...");
        ThreadUtil.sleep(3000);

        L.debug("Retrieving log entries");

        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        Gson gson = new Gson();

        L.debug("Printing log entries");

        String directLink = null;

        for (LogEntry logEntry : logEntries) {
//            L.error(logEntry.getMessage());

            JsonObject logEntryJson;

            try {
                logEntryJson = gson.fromJson(
                        logEntry.getMessage(), JsonObject.class);
            } catch (Exception e) {
                L.debug("Can't cast log entry to valid json object");
                continue;
            }

            if (logEntryJson == null) {
                L.debug("Can't cast log entry to valid json object");
                continue;
            }


            JsonObject messageJson = logEntryJson.getAsJsonObject("message");

            if (messageJson == null) {
//                L.debug("Log entry doesn't have a 'message' field");
                continue;
            }

            JsonObject paramsJson = messageJson.getAsJsonObject("params");

            if (paramsJson == null) {
//                L.debug("Log entry doesn't have a 'params' field");
                continue;
            }

            JsonObject requestJson = paramsJson.getAsJsonObject("request");

            if (requestJson == null) {
//                L.debug("Log entry doesn't have a 'request' field");
                continue;
            }

            JsonElement urlJson = requestJson.get("url");

            if (urlJson == null) {
//                L.debug("Log entry doesn't have a 'url' field");
                continue;
            }

            String urlString = urlJson.getAsString();

            if (!StringUtil.isValid(urlString)) {
//                L.debug("Log entry doesn't have a valid 'url' field");
                continue;
            }

            L.debug("Found entry with url field: " + urlString);

            if (urlString.contains("mp4")) {
                L.info("OK: Found entry with mp4 content: " + urlString);
                directLink = urlString;
                break;
            }
        }

        if (directLink == null) {
            L.warn("Nothing found");
        }

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return directLink;
    }
}

