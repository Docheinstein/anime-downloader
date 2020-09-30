package org.docheinstein.animedownloader.downloader.vvvvid;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.commons.constants.Const;
import org.docheinstein.animedownloader.commons.files.FileHierarchy;
import org.docheinstein.animedownloader.downloader.base.ChromeMarionetteDownloader;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.ui.alert.AlertInstance;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.adt.Wrapper;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.http.HttpDownloader;
import org.docheinstein.commons.http.HttpRequester;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.thread.ThreadUtil;
import org.docheinstein.commons.types.StringUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.docheinstein.animedownloader.commons.constants.Const.Math.M;

/**
 * Specific marionette able to download video from "https://vvvvid.it".
 */
public class VVVVIDMarionette
    extends ChromeMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForTag("{VVVVID_MARIONETTE}");

    private static final String LOGIN_URL = "https://www.vvvvid.it/user/login";
    private static final String VIDEO_INFO_URL_TEMPLATE = "https://www.vvvvid.it/vvvvid/video/%s/notlogged?conn_id=%s";
    private static final String JSON_DATA_KEY = "data";
    private static final String JSON_CONNECTION_ID_KEY = "conn_id";
    private static final String JSON_TITLE_KEY = "title";

    private static final int VVVVID_BANDWIDTH_CAP = 3 * Const.Units.MB;

    /** Whether download is enabled. */
    private boolean mDownloadEnabled;

    /** Amount of downloaded bytes. */
    private long mDownloadedBytes = 0;

    /** Current segment in download incremental number. */
    private int mSegmentIncrementalNumber = 1;

    public VVVVIDMarionette(String downloadUrl,
                            File outputPath,
                            File driverPath,
                            boolean ghost,
                            DownloadableVideoInfo info,
                            VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghost, info, downloadObserver);
    }

    @Override
    public VideoInfo retrieveVideoInfoStrict() {
        // We cannot obtain the video title from the root page since
        // it is hidden at the beginning (until ads finish).
        // For retrieve the title in a fast way we have to
        // 1) Obtain a connection id by sending a request to https://www.vvvvid.it/user/login
        // 2) Send a request to https://www.vvvvid.it/vvvvid/video/%s/notlogged?conn_id=%s
        //    using the just retrieved connection id and the video identifier
        //    which can be figured out from the URL link

        VideoInfo info = new VideoInfo();

        WebDriver driver = getDriver();

        // Obtain a connection ID

        driver.get(LOGIN_URL);

        // Take the <pre> json content

        L.verbose("Got login response: " + driver.getPageSource());

        WebElement connIdPre = driver.findElement(By.tagName("pre"));

        if (connIdPre == null) {
            L.warn("Failed to retrieve <pre> from " + LOGIN_URL);
            return null;
        }

        String connIdPreContent = connIdPre.getText();

        L.verbose("Got login json: " + connIdPreContent);

        Gson gson = new Gson();

        // Grab the data.conn_id

        JsonObject jsonLoginResponse = gson.fromJson(connIdPreContent, JsonObject.class);

        JsonElement jsonLoginData = jsonLoginResponse.get(JSON_DATA_KEY);

        if (jsonLoginData == null) {
            L.warn("Failed to retrieve " + JSON_DATA_KEY + " from json response");
            return null;
        }

        JsonElement connIdJson = jsonLoginData.getAsJsonObject().get(JSON_CONNECTION_ID_KEY);

        if (connIdJson == null) {
            L.warn("Failed to retrieve " + JSON_CONNECTION_ID_KEY + " from json response");
            return null;
        }

        String connId = connIdJson.getAsString();

        L.verbose("We have a VVVVID connection id: " + connId);

        // Figure out the video id

        String[] downloadUrlComponents = mDownloadUrl.split("/");

        if (downloadUrlComponents.length  < 3) {
            L.warn("Unexpected url found; must have at least two components after a '/' split");
            return null;
        }

        String penultimateComponent = downloadUrlComponents[downloadUrlComponents.length - 3];

        L.verbose("Video identifier is: " + penultimateComponent);

        String videoInfoRequestUrl = String.format(VIDEO_INFO_URL_TEMPLATE, penultimateComponent, connId);

        L.verbose("Going to send video info request to : " + videoInfoRequestUrl);

        // Send request to the video info url
        driver.get(videoInfoRequestUrl);

        // Take the <pre> json content

        String videoInfoResponse = driver.getPageSource();

        L.verbose("Got video info response: " + videoInfoResponse);

        WebElement videoInfoPre = driver.findElement(By.tagName("pre"));

        if (videoInfoPre == null) {
            L.warn("Failed to retrieve <pre> from " + videoInfoRequestUrl);
            return null;
        }

        String videoInfoPreContent = videoInfoPre.getText();

        L.verbose("Got video info json: " + connIdPreContent);

        // Grab the data.title

        JsonObject videoInfoJsonResponse = gson.fromJson(videoInfoPreContent, JsonObject.class);

        JsonElement videoInfoData = videoInfoJsonResponse.get(JSON_DATA_KEY);

        if (videoInfoData == null) {
            L.warn("Failed to retrieve " + JSON_DATA_KEY + " from video json response");
            return null;
        }

        JsonElement videoInfoTitle = videoInfoData.getAsJsonObject().get(JSON_TITLE_KEY);

        if (videoInfoTitle == null) {
            L.warn("Failed to retrieve " + JSON_CONNECTION_ID_KEY + " from video json response");
            return null;
        }

        String videoTitle = videoInfoTitle.getAsString();

        L.verbose("Retrieved video title: " + videoTitle);

        info.title = videoTitle;

        if (JSettings.instance().getCloseDriverOnInfoRetrieved().getValue())
            driver.quit();

        return info;
    }

    @Override
    public DownloadableVideoInfo startDownload() {
        retrieveDownloadableVideoInfo();
        ThreadUtil.start(this::doDownload);
        return mVideoInfo;
    }

    @Override
    public void abortDownload() {
        if (mObserver != null)
            mObserver.onVideoDownloadAborted();

        mDownloadEnabled = false;
        mDownloadedBytes = 0;
        mSegmentIncrementalNumber = 1;
    }

    @Override
    protected DownloadableVideoInfo retrieveDownloadableVideoInfoStrict() {
        DownloadableVideoInfo videoInfo = new DownloadableVideoInfo();

        WebDriver driver = getDriver();

        driver.get(mDownloadUrl);

        if (!watchAds()) {
            L.warn("Failed to watch ads; aborting");
            return null;
        }

        String title = driver.findElement(
            By.className("player-info-show")).getText();
        if (StringUtil.isValid(title)) {
            L.verbose("Got valid title for video: " + title);
            videoInfo.title = videoInfo.filename = title;
        }

        if (Config.Debug.DUMP_PAGES)
            L.debug("Fetched paged:\n" + driver.getPageSource());

        if (!StringUtil.isValid(videoInfo.title)) {
            L.warn("Cannot get valid video title, generating a random one");
            videoInfo.title = videoInfo.filename = String.valueOf(System.currentTimeMillis() / 1000);
        }

        // Retrieves index file link and use it as direct link
        String directLink =  getIndexFileLink();

        if (StringUtil.isValid(directLink)) {
            String normalizedindexLink = directLink.replaceFirst("\\?null=0", "");
            L.verbose("Going to retrieve content of index file from " + normalizedindexLink);
            videoInfo.directLink = normalizedindexLink;
        } else {
            L.error("Index link can't be retrieved from network logs; error will occur");
        }

        // We should leave the web driver now, otherwise the download of the
        // segments will continue on the web driver and will slow us down

        if (JSettings.instance().getCloseDriverOnDownloadStarted().getValue())
            driver.quit();

        return videoInfo;
    }

    /**
     * Try to passes the robot check by clicking on the #apCheckContainer element.
     */
    private void passRobotCheck() {
        L.verbose("Trying to pass robot check");

        WebDriver driver = getDriver();
        boolean robotCheckPassed = false;

        do {
            try {
                WebElement robotChecker = driver.findElement(By.id("apCheckContainer"));
                if (robotChecker != null) {
                    L.verbose("Clicking on #apCheckContainer since VVVVID thinks we are a robot");
                    robotChecker.click();
                }
                else {
                    L.verbose("Allright, VVVVID doesn't think we're a robot");
                    robotCheckPassed = true;
                }
            } catch (NoSuchElementException e) {
                L.verbose("Allright, VVVVID doesn't think we're a robot");
                robotCheckPassed = true;
            }
            L.warn("Robot check not passed after click, doing it again within 1s");
            ThreadUtil.sleep(1000);
        } while (!robotCheckPassed);
    }

    /**
     * Watches the ads, therefore waits until the ads has gone away.
     * @return whether we watched all the ads successfully
     */
    private boolean watchAds() {
        L.verbose("Settling down and watching ads...");
        WebDriver driver = getDriver();

        final int MAX_ATTEMPTS = 10;

        Wrapper<Long> debugCurrentLoadMillis = new Wrapper<>(System.currentTimeMillis());
        int estimatedAdsSecs = 120;

        Wrapper<Integer> attempt = new Wrapper<>(0);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            final int currentI = i;

            passRobotCheck();

            boolean playerInfoShowFound = false;

            try {
                playerInfoShowFound = (new WebDriverWait(driver, estimatedAdsSecs, 2000)).until((ExpectedCondition<Boolean>) d -> {
                    try {
                        L.verbose("[" + attempt.get() + "] Checking if player-info-show is available");
                        attempt.set(attempt.get() + 1);
                        WebElement videoElement = driver.findElement(By.className("player-info-show"));
                        return videoElement != null;
                    } catch (NoSuchElementException e) {
                        L.debug("=> not available after " +
                                (System.currentTimeMillis() - debugCurrentLoadMillis.get()) / 1000 +
                                "s of the " + currentI + "* attempt");
                        return false;
                    }
                });
            } catch (TimeoutException te) {}

            if (!playerInfoShowFound) {
                L.debug("player-info-show not found in " + estimatedAdsSecs + "sec; refreshing");
                driver.navigate().refresh();

                estimatedAdsSecs = (int) (estimatedAdsSecs * 1.15);
                debugCurrentLoadMillis.set(System.currentTimeMillis());
                continue;
            }

            L.verbose("player-info-show found; ads completed");
            return true;
        }

        return false;
    }

    /**
     * Reloads the page until no ad is loaded and thus the video is loaded.
     * <p>
     * Another option would be wait for the ad to finish, but reload is actually
     * faster.
     */
    private void skipAds() {
        L.verbose("Going to skip VVVVID ads");

        WebDriver driver = getDriver();

        final int MAX_ATTEMPTS = 25;

        int estimatedLoadMillis = 5000;

        // Increase the estimated load time after each attempt

        for (int attemptCount = 1; attemptCount <= MAX_ATTEMPTS; attemptCount++) {

            estimatedLoadMillis = (int) (estimatedLoadMillis * 1.15);

            L.verbose("Loading attempt [" + attemptCount + "] - sleeping for " + estimatedLoadMillis);

            ThreadUtil.sleep(estimatedLoadMillis);

            passRobotCheck();

            L.verbose("Sleeping again after robot check for " + estimatedLoadMillis);

            ThreadUtil.sleep(estimatedLoadMillis);

            WebElement playerInfoShow = null;
            try {
                playerInfoShow = driver.findElement(By.className("player-info-show"));
            }
            catch (NoSuchElementException e) {
                L.debug("player-info-show element no found; cannot skip ads right now");
            }

            // The last refresh didn't lead to ads
            if (playerInfoShow != null) {
                L.verbose("Element 'player-info-show' can't be retrieved => ads skipped");
                return; // Ads skipped
            }

            L.verbose("There are ads, refreshing now for skip those");
            driver.navigate().refresh();

            // 1.15 Seems a decent factor
            // After 20 iterations from 5000ms it goes to 81832ms (5000 * 1.15^20).
        }
    }

    /**
     * Returns the link of the index file which contains the url of every
     * segment that compose the video.
     * @return the url of the segments index file
     */
    private String getIndexFileLink() {
        L.verbose("Seeking for index file url");
        WebDriver driver = getDriver();

        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        Gson gson = new Gson();

        L.debug("Printing log entries");

        for (LogEntry logEntry : logEntries) {
            L.debug(logEntry.getMessage());

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
                L.debug("Log entry doesn't have a 'message' field");
                continue;
            }

            JsonObject paramsJson = messageJson.getAsJsonObject("params");

            if (paramsJson == null) {
                L.debug("Log entry doesn't have a 'params' field");
                continue;
            }

            JsonObject requestJson = paramsJson.getAsJsonObject("request");

            if (requestJson == null) {
                L.debug("Log entry doesn't have a 'request' field");
                continue;
            }

            JsonElement urlJson = requestJson.get("url");

            if (urlJson == null) {
                L.debug("Log entry doesn't have a 'url' field");
                continue;
            }

            String urlString = urlJson.getAsString();

            if (!StringUtil.isValid(urlString)) {
                L.debug("Log entry doesn't have a valid 'url' field");
                continue;
            }

            L.debug("Found entry with url field. Value is: " + urlString);

            if (urlString.contains("index")) {
                L.verbose("Found index url in logs: " + urlString);
                return urlString;
            }
        }

        return null;
    }

    /**
     * Retrieves the content of the index file at the given url
     * @param indexLink the url of the index file
     * @return the content of the index file retrieved from the given url
     */
    private String getIndexContent(String indexLink) {
        if (!mDownloadEnabled)
            return null;

        HttpRequester.Response response = HttpRequester.get(indexLink).send();

        if (!response.hasBeenPerformed()) {
            L.error("Index file can't be retrieved");
            return null;
        }

        return response.getResponseBody();
    }

    /**
     * Returns the list of segment that compose the video as specified
     * by the content of the index file
     * @param indexContent the content of the index file to parse
     * @return the segments of the vide
     */
    private List<String> getSegmentsFromIndexContent(String indexContent) {
        if (!mDownloadEnabled)
            return null;

        L.verbose("Reading segments from index file");
        List<String> segments = new ArrayList<>();

        Scanner scanner = new Scanner(indexContent);

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            L.debug(line);
            if (!line.startsWith("#"))
                segments.add(line);
        }

        return segments;
    }

    /**
     * Actually starts the download, retrieving the segments from the direct link.
     * @return whether the download finished successfully
     */
    private boolean doDownload() {
        mDownloadEnabled = true;

        String indexContent = getIndexContent(mVideoInfo.directLink);

        if (!StringUtil.isValid(indexContent)) {
            L.error("Index content is invalid; giving up");
            return false;
        }

        List<String> segmentLinks = getSegmentsFromIndexContent(indexContent);

        if (!StringUtil.isValid(indexContent)) {
            L.error("Can't figure out valid segment links from index file; giving up");
            return false;
        }

        File temporarySegmentsFolder =
            new File(FileHierarchy.instance().getTmpNode().getFile(),
                    String.valueOf(System.currentTimeMillis()));

        if (!FileUtil.ensureFolderExistence(temporarySegmentsFolder)) {
            L.warn("Temporary folder can't be created");
            return false;
        }

        boolean downloadOk = doDownload(segmentLinks, temporarySegmentsFolder);

        if (downloadOk) {
            String mergeFileName = mVideoInfo.filename + ".ts";

            File mergeFile = new File(temporarySegmentsFolder, mergeFileName);

            joinSegments(mergeFile, temporarySegmentsFolder);

            String outFileName = mVideoInfo.filename + ".mp4";

            convertToMP4(mergeFile, new File(mDownloadFolder, outFileName));
        }

        // Clean up temporary folder
        L.verbose("Removing temporary folder (" + temporarySegmentsFolder + ")");
        FileUtil.deleteRecursive(temporarySegmentsFolder);

        return downloadOk;
    }

    /**
     * Actually starts the download of the segments.
     * @param segmentLinks the segments link
     * @param segmentsFolder the output folder for the segment
     * @return whether the download has been completed successfully
     */
    private boolean doDownload(List<String> segmentLinks, File segmentsFolder) {
        if (!mDownloadEnabled)
            return false;

        int segmentCount = segmentLinks.size();

        L.verbose("Downloading " + segmentCount + " segments");

        mSegmentIncrementalNumber = 1;

        if (mObserver != null)
            mObserver.onVideoDownloadStarted();

        for (String segmentLink : segmentLinks) {

            if (!mDownloadEnabled) {
                L.verbose("Download has been aborted");
                return false;
                // Does not fire onVideoDownloadFinished()
            }

            L.verbose("Downloading segment: " + segmentLink);
            String segmentFilename =
                String.format("%05d", mSegmentIncrementalNumber) + ".ts";

            try {
                File segmentOutputFile  = new File(segmentsFolder, segmentFilename);

                boolean downloaded = new HttpDownloader().download(
                    segmentLink,
                    segmentOutputFile.getAbsolutePath()
                );

                if (downloaded) {
                    long curMillis = System.currentTimeMillis();

                    mDownloadedBytes += segmentOutputFile.length();

                    int remainingSegmentCount = segmentCount - mSegmentIncrementalNumber;

                    L.debug("Downloaded segment is " + segmentOutputFile.length() + " bytes");
                    L.debug("Already downloaded bytes are so " + mDownloadedBytes);
                    long estimatedVideoSize =
                        remainingSegmentCount * (mDownloadedBytes / mSegmentIncrementalNumber)
                            + mDownloadedBytes;

                    L.debug("Estimated video size: " + estimatedVideoSize);

                    mVideoInfo.size = estimatedVideoSize;

                    notifySizeToObserver(mVideoInfo.size);

                    if (mObserver != null)
                        mObserver.onVideoDownloadProgress(mDownloadedBytes, curMillis);

//                    L.debug("Sleeping for 1000ms to avoid robot detection");
//                    ThreadUtil.sleep(1000);
                } else {
                    L.error("Segment download has failed, something bad will happen");
                }

            } catch (IOException e) {
                L.error("Segment download failed!", e);
                return false;
            }

            mSegmentIncrementalNumber++;
        }

        if (mObserver != null)
            mObserver.onVideoDownloadFinished();

        return true;
    }

    /**
     * Joins the segment into the given output file.
     * <p>
     * The segments will be joined in alphabetically order
     * @param outputFile the output file
     * @param segmentsFolder the folder containing the segment to join
     */
    private void joinSegments(File outputFile, File segmentsFolder) {
        File[] segments = segmentsFolder.listFiles();

        if (segments == null) {
            L.warn("No segment to join has been found");
            return;
        }

        Arrays.sort(segments, File::compareTo);

        L.verbose("Joining " + segments.length + " segments into one");

        FileUtil.mergeFiles(
            outputFile,
            segments
        );
    }

    /**
     * Converts the given .ts input file to an .mp4 output file.
     * <p>
     * Uses ffmpeg.
     * @param input the .ts file
     * @param output the output .mp4 file
     */
    private void convertToMP4(File input, File output) {
        File ffmpegExecutable = JSettings.instance().getFFmpegSetting().getValue();
        String ffmpegExecutableString =
            ffmpegExecutable != null ?
                ffmpegExecutable.getAbsolutePath() :
                "ffmpeg"; // Search in $PATH

        String[] mergeCommand = new String[] {
            ffmpegExecutableString,
            "-y",
            "-i", input.getAbsolutePath(),
            "-acodec", "copy",
            "-vcodec", "copy",
            output.getAbsolutePath()
        };

        L.verbose("Converting " + input.getAbsolutePath() + " to MP4");
        L.verbose("Conversion command: " + String.join(" ", mergeCommand));

        boolean conversionOk = false;

        try {
            L.verbose("Starting conversion...");
            Runtime.getRuntime().exec(mergeCommand).waitFor();
            L.verbose("Conversion finished!");

            if (FileUtil.exists(output))
                conversionOk = true;

        } catch (InterruptedException | IOException e) {}

        if (!conversionOk) {
            L.error("Conversion failed. Maybe ffmpeg is not available?");
            javafx.application.Platform.runLater(AlertInstance.MP4ConversionFailed::show);
        }
    }

    /**
     * Notifies the observes about the new video size.
     * @param size the size to notify
     */
    private void notifySizeToObserver(long size) {
        L.verbose("Estimated video size: " + size / M + "MB");
        if (mObserver != null)
            mObserver.onVideoSizeDetected(size, false);
    }
}
