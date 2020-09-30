package org.docheinstein.animedownloader.downloader.youtube;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.downloader.base.VideoDownloader;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.thread.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class YoutubeDownloader implements VideoDownloader {

    private static final DocLogger L =
        DocLogger.createForClass(YoutubeDownloader.class);

    /** Observer of the video download. */
    protected VideoDownloadObserver mObserver;

    /** URL of the site (it is not the definitive link of the video). */
    protected String mDownloadUrl;

    /** Download folder. */
    protected File mDownloadFolder;

    /** Video info. */
    protected DownloadableVideoInfo mVideoInfo;

    /** The process that runs the youtube-dl downloader */
    protected Process mDownloadProcess;

    /*
    /**The job that polls the filesize for figure out the filesize * /
    protected YoutubeFileWatcher mDownloadProgressWatcher;

    private class YoutubeFileWatcher implements Runnable {

        private volatile boolean mRunning;

        public void stop() {
            mRunning = false;
        }

        @Override
        public void run() {
            mRunning = true;

            if (mVideoInfo == null) {
                L.warn("Cannot watch file without filename info");
                return;
            }

            File outFile = new File(mDownloadFolder, mVideoInfo.filename + ".part");
            L.verbose("Starting filesize watcher over : " + outFile.getAbsolutePath());

            long lastsize = outFile.length();

            while (mRunning) {
                ThreadUtil.sleep(1000);

                long cursize = outFile.length();

                L.verbose("Current yt video size: " + cursize);

                if (mRunning && mObserver != null) {
                    mObserver.onVideoDownloadProgress(cursize - lastsize, System.currentTimeMillis());
                }

                lastsize = cursize;
            }
        }
    }
    */

    public YoutubeDownloader(String downloadUrl,
                             File downloadFolder,
                             DownloadableVideoInfo info,
                             VideoDownloadObserver downloadObserver) {
        mDownloadUrl = downloadUrl;
        mDownloadFolder = downloadFolder;
        mVideoInfo = info;
        mObserver = downloadObserver;
    }

    @Override
    public VideoInfo retrieveVideoInfo() {
        JsonObject jsonInfo = retrieveYoutubeVideoInfo();

        if (jsonInfo == null || jsonInfo.isJsonNull()) {
            L.warn("Invalid json info");
            return null;
        }

        String title = jsonInfo.get("title").getAsString();
        String filename = jsonInfo.get("_filename").getAsString();

        L.verbose("Title retrieved: \n" + title);
        L.verbose("Filename retrieved: \n" + filename);

        DownloadableVideoInfo info = new DownloadableVideoInfo();
        info.title = title;
        info.filename = filename;

        mVideoInfo = DownloadableVideoInfo.merged(
            mVideoInfo,
            info
        );

        return mVideoInfo;
    }

    @Override
    public DownloadableVideoInfo startDownload() {
//        https://www.youtube.com/watch?v=j-reZDyn2XE
        try {
            String[] ytCommand =  buildYoutubeDlCommand(
                "-o",
                mDownloadFolder.getAbsolutePath() + "/" + mVideoInfo.filename,
                mDownloadUrl
            );

            L.verbose("Launching youtube-dl command for download video");
            L.verbose("Command: " + String.join(" ", ytCommand));
            mDownloadProcess = Runtime.getRuntime().exec(ytCommand);

            if (mObserver != null)
                mObserver.onVideoDownloadStarted();

            ThreadUtil.start(() -> {
                try {
                    mDownloadProcess.waitFor();

                    if (mObserver != null)
                        mObserver.onVideoDownloadFinished();
                } catch (InterruptedException e) {
                    L.warn("Download failed (or aborted)", e);
                }
            });

            DownloadableVideoInfo info = new DownloadableVideoInfo();
            info.size = retrieveYoutubeVideoFilesize();

            mVideoInfo = DownloadableVideoInfo.merged(
                mVideoInfo,
                info
            );

            // Furthermore we have to poll the filesize
            // for notify the progress of the download
//            mDownloadProgressWatcher = new YoutubeFileWatcher();
//            new Thread(mDownloadProgressWatcher).start();

            return mVideoInfo;

        } catch (IOException e) {
            L.warn("Download failed (or aborted)", e);
        }

        return null;
    }

    @Override
    public void abortDownload() {
        if (mObserver != null)
            mObserver.onVideoDownloadAborted();

        if (mDownloadProcess != null) {
            L.debug("Aborting youtube-dl process");
            mDownloadProcess.destroy();
            mDownloadProcess = null;
        } else {
            L.warn("Null download process, nothing to abort");
        }

//        if (mDownloadProgressWatcher != null) {
//            L.debug("Interrupting youtube video file watcher");
//            mDownloadProgressWatcher.stop();
//            mDownloadProgressWatcher = null;
//        } else {
//            L.warn("Null watcher, nothing to interrupt");
//        }
    }

    private JsonObject retrieveYoutubeVideoInfo() {
        try {

            String[] ytCommand = buildYoutubeDlCommand(
                "-s",
                "--print-json",
                mDownloadUrl
            );

            L.verbose("Launching youtube-dl --print-json command");
            L.verbose("Command: " + String.join(" ", ytCommand));
            Process proc = Runtime.getRuntime().exec(ytCommand);

            proc.waitFor();

            String jsonInfoStr = FileUtil.readResource(proc.getInputStream());

            L.verbose("Info retrieval output: \n" + jsonInfoStr);

            Gson gson = new Gson();
            return gson.fromJson(jsonInfoStr, JsonObject.class);
        } catch (InterruptedException | IOException e) {
            L.warn("Download failed (or aborted)", e);
        }

        return null;
    }

    private int retrieveYoutubeVideoFilesize() {
        JsonObject jsonInfo = retrieveYoutubeVideoInfo();

        if (jsonInfo == null || jsonInfo.isJsonNull()) {
            L.warn("Invalid json info");
            return 0;
        }

        int maxFilesize = 0;

        JsonArray formats = jsonInfo.get("formats").getAsJsonArray();
        for (int i = 0; i < formats.size(); i++) {
            JsonObject format = formats.get(i).getAsJsonObject();
            if (!format.isJsonNull() && format.has("filesize")) {
                JsonElement e = format.get("filesize");
                if (!e.isJsonNull()) {
                    int formatSize = e.getAsInt();
                    maxFilesize = Math.max(maxFilesize, formatSize);
                }
            }
        }

        return maxFilesize;

    }

    private String[] buildYoutubeDlCommand(String... args) {
        File youtubeExecutable = JSettings.instance().getYoutubeDlSetting().getValue();

        String youtubeExecutableString =
            youtubeExecutable != null ?
                youtubeExecutable.getAbsolutePath() :
                "youtube-dl"; // Search in $PATH

        return
            Stream.concat(Arrays.stream(
                new String[] {youtubeExecutableString}), Arrays.stream(args))
                .toArray(String[]::new);
    }
}
