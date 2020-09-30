package org.docheinstein.animedownloader.downloader.base;

import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.http.HttpDownloader;
import org.docheinstein.commons.http.HttpRequester;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.thread.ThreadUtil;
import org.docheinstein.commons.types.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.docheinstein.animedownloader.commons.constants.Const.Math.M;

/**
 * Basic marionette that handles the download of the video on a single file
 * (most of the sites provide a single video file, but not all the sites;
 * some of them provide a video splitted between multiple segment, e.g. VVVID).
 */
public abstract class VideoFileMarionetteDownloader extends ChromeMarionetteDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(VideoFileMarionetteDownloader.class);

    private HttpDownloader mDownloader;

    public VideoFileMarionetteDownloader(String downloadUrl,
                                         File outputPath,
                                         File driverPath,
                                         boolean ghostMode,
                                         DownloadableVideoInfo info,
                                         VideoDownloadObserver downloadObserver) {
        super(downloadUrl, outputPath, driverPath, ghostMode, info, downloadObserver);
    }

    /**
     * Returns the direct video link.
     * <p>
     * This is site specific and cannot be handled here.
     * @return the direct video link
     */
    protected abstract String getVideoLink();

    @Override
    public DownloadableVideoInfo startDownload() {
        L.debug("startDownload has been called, current info: " + mVideoInfo);
        if (mVideoInfo == null || !mVideoInfo.providesDownloadInfo()) {
            L.debug("Actually retrieving download info");
            retrieveDownloadableVideoInfo();
        }
        else {
            // Can happen when the download is stopped and resumed again
            // Should not happen for cached video since we WANT to fetch
            // the direct link at runtime and not trust any cached direct
            // link (which may have been expired)
            L.debug("Skipping download info retrieval; using filename = " +
                StringUtil.toEmptyIfNull(mVideoInfo.filename) +
                " and direct link = " +
                StringUtil.toEmptyIfNull(mVideoInfo.directLink));
        }
        L.debug("Before doDownload info is: " + mVideoInfo);
        ThreadUtil.start(this::doDownload);
        return mVideoInfo;
    }

    @Override
    public void abortDownload() {
        // Notifies anyhow
        if (mObserver != null)
            mObserver.onVideoDownloadAborted();

        if (mDownloader == null) {
            L.warn("Can't stop download since underlying HttpDownloader is null");
            return;
        }

        mDownloader.enableDownload(false);
    }

    @Override
    public DownloadableVideoInfo retrieveDownloadableVideoInfoStrict() {
        // Obtain the download video info from the HEAD request to the file
        // of the retrieved direct link

        DownloadableVideoInfo info = new DownloadableVideoInfo();
        info.directLink = getVideoLink();

        L.verbose("Retrieving video info (size, filename) of: " + info.directLink);

        HttpRequester.Response headResponse = HttpRequester
            .head(info.directLink)
            .allowRedirect(true)
            .initialized()
            .userAgent("curl/7.52.1")
            .accept("*/*")
            .send();

        L.verbose("Got HEAD response");

        printHeaderFields(headResponse.getHeaderFields());
        info.size = headResponse.getContentLength();

        return info;
    }


    /**
     * Actually starts the download of the video.
     */
    private void doDownload() {
        L.info("Downloading video from direct link: " + mVideoInfo.directLink);
        L.debug("Video info of about to be downloaded video: " + mVideoInfo);

        if (!StringUtil.isValid(mVideoInfo.filename)) {
            L.warn("Invalid filename, randomizing it");
            mVideoInfo.filename = String.valueOf(mVideoInfo.hashCode());
        }

        File outputFile;

        if (FileUtil.exists(mDownloadFolder))
            outputFile = new File(mDownloadFolder, mVideoInfo.filename);
        else
            // Save in current directory as fallback plan
            outputFile = new File(mVideoInfo.filename);

        L.info("Video will be downloaded to: " + outputFile.getAbsolutePath());

        boolean fileAlreadyExists = FileUtil.exists(outputFile);

        if (fileAlreadyExists)
            L.info("Video already exists, it will be resumed if possible");

        final long alreadyDownloadedBytes =
            fileAlreadyExists ? outputFile.length() : 0;

        if (mObserver != null)
            mObserver.onVideoDownloadStarted();

        try {
            mDownloader = new HttpDownloader();

            boolean downloadFinished = mDownloader.download(
                mVideoInfo.directLink,
                outputFile.getAbsolutePath(),
                (downloadedBytes) -> {
                    if (mObserver != null)
                        mObserver.onVideoDownloadProgress(
                            alreadyDownloadedBytes + downloadedBytes,
                            System.currentTimeMillis());
                },
                (int) M
            );

            if (downloadFinished && mObserver != null)
                mObserver.onVideoDownloadFinished();
        } catch (IOException e) {
            L.error("Error occurred while download the video", e);
        }

    }

    /**
     * Prints the header fields; for debugging purpose.
     * @param hf the header fields of an HTTP packet
     */
    protected void printHeaderFields(Map<String, List<String>> hf) {
        hf.forEach((k, vs) -> {
            L.verbose("Key = " + k);
            vs.forEach(v -> L.verbose("--Value = " + v));
        });
    }
}
