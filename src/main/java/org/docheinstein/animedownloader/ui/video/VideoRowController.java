package org.docheinstein.animedownloader.ui.video;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.downloader.base.VideoDownloader;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.ui.alert.AlertInstance;
import org.docheinstein.animedownloader.video.DownloadableVideoInfo;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.animedownloader.commons.constants.Resources;
import org.docheinstein.animedownloader.downloader.base.VideoDownloadObserver;
import org.docheinstein.animedownloader.ui.base.InstantiableController;
import org.docheinstein.animedownloader.video.VideoProvider;
import org.docheinstein.commons.thread.ThreadUtil;
import org.docheinstein.commons.types.StringUtil;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.docheinstein.animedownloader.commons.constants.Const.Math.M;

/**
 * Controller for a video row
 */
public class VideoRowController
    implements InstantiableController, VideoDownloadObserver {

    /**
     * Observer of this video row.
     */
    public interface VideoRowObserver {
        /**
         * Called when the download ends.
         * @param row the row whose download is ended
         */
        void onDownloadEnd(VideoRowController row);

        /**
         * Called when the state of the video row changes.
         * @param row the row controller whose state is changed
         */
        void onDownloadStateChanged(VideoRowController row);

        /**
         * Called when the video row is asked to be removed
         * @param row the row controller to remove
         */
        void onRowRemovalRequired(VideoRowController row);
    }

    private static final DocLogger L =
        DocLogger.createForClass(VideoRowController.class);

    /**
     * State of the vizdeo download.
     */
    public enum VideoDownloadState {
        /** Initial state */
        None,

        /** Retrieving the basic video info (i.e. the title). */
        RetrievingInfo,

        /** The video should still be downloaded but the basic info has
         * already been retrieved.*/
        ReadyToDownload,

        /** The video will be downloaded but now is in preparation
         * (retrieving direct link, ...). */
        PreparingDownload,

        /** The download of the video is in progress. */
        Downloading,

        /** The video has been download successfully. */
        Downloaded,

        /** The video began but has been aborted. */
        Aborted
    }

    /** Current video state. */
    private VideoDownloadState mCurrentVideoState = VideoDownloadState.None;

    /** Identifier of the video represented by this controller. */
    private final String mIdentifier;

    /** Observers of this video row. */
    private final VideoRowObserver mObserver;

    private DownloadableVideoInfo mVideoInfo;

    /** Link of the video to download. */
    private final String mUrl;

    /** Time of the last chunk download (kept for calculate the download speed). */
    private long mLastDownloadChunkMillis;

    /** Bytes downloaded as of the last chunk download (kept for calculate the download speed). */
    private long mLastDownloadChunkBytes = 0;

    private int mLastBytesPerSecond = 0;

    /** Underlying download used for actually download the video.*/
    private VideoDownloader mDownloader;

    /** Detected provider of the video to download. */
    private VideoProvider mProvider;

    /** Folder the video will be download to. */
    private File mDownloadFolder;

    /** Queue for handle the updates to the UI without overlap between async callbacks. */
    private final ExecutorService mDownloadedBytesUIUpdater
        = Executors.newSingleThreadExecutor();


    @FXML
    private AnchorPane uiRoot;

    @FXML
    private Label uiTitle;

    @FXML
    private Label uiLink;

    @FXML
    private Button uiStartStopOpen;

    @FXML
    private ImageView uiStartStopOpenImage;

    @FXML
    private Button uiRemove;

    @FXML
    private Region uiPercentage;

    @FXML
    private Pane uiDownloadInfo;

    @FXML
    private Label uiSize;

    @FXML
    private Label uiCurrent;

    @FXML
    private Label uiSpeed;

    @FXML
    private Pane uiSpeedContainer;

    @FXML
    private ImageView uiProviderLogo;

    @FXML
    private ProgressIndicator uiPreDownloadSpinner;


    @Override
    public String getFXMLAsset() {
        return "video_row.fxml";
    }

    public VideoRowController(String url, String identifier,
                              VideoRowObserver observer) {
        this(url, identifier, observer, null);
    }

    public VideoRowController(String url, String identifier,
                              VideoRowObserver observer, VideoInfo videoInfo) {
        mUrl = url;
        mIdentifier = identifier;
        mObserver = observer;
        mVideoInfo = DownloadableVideoInfo.fromVideoInfo(videoInfo);
    }


    @FXML
    private void initialize() {
        boolean notifyStateChange = false;

        if (mVideoInfo != null && mVideoInfo.providesMetaInfo()) {
            L.debug("Valid video info (" + StringUtil.toEmptyIfNull(mVideoInfo.title) +
                    ") initializing with state ReadyToDownload");
            mCurrentVideoState = VideoDownloadState.ReadyToDownload;
            notifyStateChange = true;
        }

        mProvider = VideoProvider.getProviderForURL(mUrl);

        // Stuff initialized even if the provider is wrong

        // Remove button
        uiRemove.setOnMouseClicked(event -> {
            L.verbose("Removing video row");

            if (mCurrentVideoState == VideoDownloadState.Downloading)
                abortVideoDownload();

            // Notify the observer
            notifyRowRemovalRequired();
        });

        // Link
        uiLink.setText(mUrl);

        if (mProvider == null) {
            L.warn("The pasted link doesn't belong to any valid provider");
            // Download is not allowed if the provider is not recognized
            FxUtil.setExistent(uiStartStopOpen, false);
            return;
        }

        // Stuff initialized only if the provider is valid

        // Logo
        uiProviderLogo.setImage(mProvider.getLogo());

        // Start/Stop button
        uiStartStopOpen.setOnMouseClicked(event -> {
            if (mCurrentVideoState == VideoDownloadState.ReadyToDownload ||
                mCurrentVideoState == VideoDownloadState.Aborted)
                download();
            else if (mCurrentVideoState == VideoDownloadState.Downloading)
                abortVideoDownload();
            else if (mCurrentVideoState == VideoDownloadState.Downloaded)
                openDownloadFolder();
            else if (mCurrentVideoState == VideoDownloadState.RetrievingInfo ||
                     mCurrentVideoState == VideoDownloadState.PreparingDownload ||
                     mCurrentVideoState == VideoDownloadState.None)
                L.verbose("No action bound to current state(" + mCurrentVideoState + ")");
        });

        updateUI();

        if (notifyStateChange)
            notifyDownloadStateChanged();
    }


    /**
     * Whether the download url belongs to a known provider.
     * @return whether this video url can be download with a known provider
     */
    public boolean hasValidProvider() {
        return mProvider != null;
    }

    /**
     * Returns the video provider of this row.
     * @return the video provider
     */
    public VideoProvider getProvider() {
        return mProvider;
    }

    /**
     * Returns the current state for the download of the video.
     * @return the video download state
     */
    public VideoDownloadState getState() {
        return mCurrentVideoState;
    }

    /**
     * Returns the bandwidth of the last second of download expressed in bytes
     * per seconds.
     * @return the instant bandwidth
     */
    public int getInstantBandwidth() {
        if (mCurrentVideoState == VideoDownloadState.Downloading)
            return mLastBytesPerSecond;
        return 0;
    }

    /**
     * Returns the identifier of the video.
     * @return the identifier of the video
     */
    public String getIdentifier() {
        return mIdentifier;
    }

    /**
     * Returns the video info.
     * @return the video info
     */
    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }

    /**
     * Starts the video download and changes the controller video state.
     */
    public void download() {
        initDownloader();

        L.verbose("Download will be processed for url: " + mUrl);

        changeStateAndUpdateUI(VideoDownloadState.PreparingDownload);

        ThreadUtil.start(() -> {
            DownloadableVideoInfo downloadInfo = mDownloader.startDownload();

            mVideoInfo = DownloadableVideoInfo.merged(
                mVideoInfo,
                downloadInfo
            );

            updateUI();
        });
    }

    /**
     * Retrieves the video info.
     * <p>
     * The info is retrieved from the page, thus the selenium driver is
     * used for perform the action.
     * @return the video info
     */
    public VideoInfo retrieveVideoInfo() {
        changeStateAndUpdateUI(VideoDownloadState.RetrievingInfo);
        initDownloader();
        VideoInfo info = mDownloader.retrieveVideoInfo();
        mVideoInfo = DownloadableVideoInfo.merged(
            mVideoInfo, DownloadableVideoInfo.fromVideoInfo(info));
        // Update video info accordingly to the just retrieved video info
        changeStateAndUpdateUI(VideoDownloadState.ReadyToDownload);
        return mVideoInfo;
    }

    /**
     * Initializes the underlying download of this provider.
     */
    private synchronized void initDownloader() {
        if (!hasValidProvider()) {
            L.warn("Can't start video download is provider is invalid");
            return;
        }

        // Initializes if needed, only the first time
        if (mDownloader == null) {
            mDownloadFolder = JSettings.instance().getDownloadFolderSetting().getValue();

            mDownloader = mProvider.createDownloader(
                mUrl,
                mDownloadFolder,
                JSettings.instance().getChromeDriverSetting().getValue(),
                JSettings.instance().getChromeDriverGhostModeSetting().getValue(),
                mVideoInfo,
                VideoRowController.this
            );
        }
    }

    /**
     * Stops the download of the video.
     */
    private void abortVideoDownload() {
        if (mDownloader == null) {
            L.warn("Can't abort download since it is not started yet!");
            return;
        }

        mDownloader.abortDownload();
    }

    /**
     * Opens the folder where video has been download to.
     */
    private void openDownloadFolder() {
        ThreadUtil.start(() -> {
            if (!Desktop.isDesktopSupported()) {
                L.warn("Desktop is not supported: folder can't be opened");
                return;
            }

            try {
                // mDownloadFolder is kept instead of retrieving the
                // path from setting since the setting could have been changed
                // after the video download
                L.verbose("Trying to open " + mDownloadFolder
                    + " via default file explorer");
                Desktop.getDesktop().open(mDownloadFolder);
            } catch (IOException e) {
                L.warn("Folder " + mDownloadFolder.getAbsolutePath() + " can't be opened");
            } catch (IllegalArgumentException iae) {
                L.warn("Folder " + mDownloadFolder.getAbsolutePath() + " can't be opened");
                Platform.runLater(AlertInstance.FolderDoesNotExists::showAndWait);
            }
        });
    }

    /**
     * Changes the row state and updates the UI accordingly.
     * @param state the new state
     */
    private void changeStateAndUpdateUI(VideoDownloadState state) {
        L.verbose("Changing video row state to: " + state);
        mCurrentVideoState = state;
        updateUI();
        notifyDownloadStateChanged();
    }

    /**
     * Updates the UI based on the current state, eventually scheduling the
     * task on the UI thread.
     */
    private void updateUI() {
        Platform.runLater(this::doUpdateUI);
    }

    /**
     * Actually updates the UI based on the current state.
     */
    private void doUpdateUI() {
        // BEGIN Button

        String buttonTooltip = null;
        Image buttonImage = null;

        if (mCurrentVideoState == VideoDownloadState.ReadyToDownload ||
            mCurrentVideoState == VideoDownloadState.Aborted) {
            buttonTooltip = "Download";
            buttonImage = Resources.UI.START;
        }
        else if (mCurrentVideoState == VideoDownloadState.Downloading) {
            buttonTooltip = "Stop download";
            buttonImage = Resources.UI.STOP;

        }
        else if (mCurrentVideoState == VideoDownloadState.Downloaded) {
            buttonTooltip = "Open video folder";
            buttonImage = Resources.UI.OPEN_FOLDER;
        }

        if (buttonImage != null)
            uiStartStopOpenImage.setImage(buttonImage);
        if (buttonTooltip != null)
            Tooltip.install(uiStartStopOpen, new Tooltip(buttonTooltip));

        // END Button

        // BEGIN Spinner | Button

        // Don't let the user do something in transactional phases (.Initializing)

        boolean transitionPhase =
            mCurrentVideoState == VideoDownloadState.RetrievingInfo ||
            mCurrentVideoState == VideoDownloadState.PreparingDownload ||
            mCurrentVideoState == VideoDownloadState.None;

        FxUtil.setExistent(uiStartStopOpen, !transitionPhase);
        FxUtil.setExistent(uiPreDownloadSpinner, transitionPhase);

        // END Spinner | Button

        // BEGIN Percentage bar

        if (mCurrentVideoState == VideoDownloadState.Downloading)
            // Removes any style (in case of resumed download the bar was orange)
            FxUtil.setClass(uiPercentage, "percentage-bar-background");
        else if (mCurrentVideoState == VideoDownloadState.Downloaded) {
            FxUtil.addClass(uiPercentage, "finished");
            AnchorPane.setRightAnchor(uiPercentage, (double) 0); // Attach to right
        }
        else if (mCurrentVideoState == VideoDownloadState.Aborted)
            FxUtil.addClass(uiPercentage, "aborted");

        // END Percentage bar

        // BEGIN Video info
        if (mVideoInfo != null) {
            if (StringUtil.isValid(mVideoInfo.title))
                uiTitle.setText(mVideoInfo.title);
            if (mVideoInfo.size > 0)
                onVideoSizeDetected(mVideoInfo.size, true);
        }
        // END Video info

        // BEGIN Download finished

        if (mCurrentVideoState == VideoDownloadState.Downloaded) {
            if (mVideoInfo != null)
                uiCurrent.setText(String.valueOf(mVideoInfo.size / M));
            uiSpeedContainer.setVisible(false);
        }

        // END Download finished

        // BEGIN Download info

        uiDownloadInfo.setVisible(
            mCurrentVideoState == VideoDownloadState.Downloading ||
            mCurrentVideoState == VideoDownloadState.Aborted
        );

        // END Download info
    }

    @Override
    public void onVideoDownloadStarted() {
        L.info("Download of " + mVideoInfo.title + " is actually started");
        mLastDownloadChunkMillis = System.currentTimeMillis();
        changeStateAndUpdateUI(VideoDownloadState.Downloading);
    }

    @Override
    public void onVideoDownloadProgress(long downloadedBytes, long millis) {
        if (mCurrentVideoState != VideoDownloadState.Downloading) {
            L.warn("Video download progress callback received even if the " +
                   "video is not downloading; doing nothing");
            return;
        }

        mDownloadedBytesUIUpdater.execute(() -> {

            long deltaMillis = millis - mLastDownloadChunkMillis;
            long deltaBytes = downloadedBytes - mLastDownloadChunkBytes;

            if (deltaMillis < 0 || deltaBytes < 0) {
                L.warn("Not handling negative delta millis or bytes");
                return;
            }

            if (deltaMillis < 1000) {
                // Wait a little bit more
                return;
            }

            L.debug("Downloaded bytes: " + downloadedBytes + " of " + mVideoInfo.size);

            mLastDownloadChunkBytes = downloadedBytes;
            mLastDownloadChunkMillis = millis;

            // deltaMillis : deltaBytes = 1000 : mLastBytesPerSecond
            // mLastBytesPerSecond = deltaBytes  * 1000 / deltaMillis

            int kilobytesPerSecond = Integer.max(0, (int) (deltaBytes / deltaMillis));
            mLastBytesPerSecond = Integer.max(0, (int) (deltaBytes * 1000 / deltaMillis));

            L.debug("Delta time: " + deltaMillis);
            L.debug("Delta bytes: " + deltaBytes);
            L.debug("KB/s = dt/dbytes = " + kilobytesPerSecond);

            double rateo = (double) downloadedBytes / (double) mVideoInfo.size;
            double percentage = rateo * 100;

            L.debug("Download percentage = " + String.format("%1$,.2f", percentage) + "%");

            double parentWidth = uiRoot.getWidth();
            double percentageBarWidth = parentWidth *
                /* rateo */ (double) downloadedBytes / (double) mVideoInfo.size;

            Platform.runLater(() -> {
                uiCurrent.setText(String.valueOf(downloadedBytes / M));

                if (kilobytesPerSecond >= 0) {
                    uiSpeed.setText(String.valueOf(kilobytesPerSecond));
                } else {
                    L.warn("Download speed below 0; WTF?");
                    uiSpeed.setText("0");
                }

                double newPercentageBarAnchor = parentWidth - percentageBarWidth;

                // Progress bar should always go forward
                // Double rightAnchor = AnchorPane.getRightAnchor(uiPercentage);

                // if (rightAnchor == null
                // Allow negative progress in case of resumed download
                // that doesn't support resume (VVVVID)
                /*|| newPercentageBarAnchor < rightAnchor*/
                //    )
                AnchorPane.setRightAnchor(
                    uiPercentage,
                    newPercentageBarAnchor
                );
            });
        });
    }

    @Override
    public void onVideoDownloadFinished() {
        L.info("Download of " + mVideoInfo.title + " is finished");
        changeStateAndUpdateUI(VideoDownloadState.Downloaded);
        notifyDownloadEnd();
    }

    @Override
    public void onVideoDownloadAborted() {
        L.info("Download of " + mVideoInfo.title + " has been aborted");
        changeStateAndUpdateUI(VideoDownloadState.Aborted);
    }

    @Override
    public void onVideoSizeDetected(long videoSizeBytes, boolean certainly) {
        if (mVideoInfo == null)
            mVideoInfo = new DownloadableVideoInfo();
        mVideoInfo.size = videoSizeBytes;
        final String videoSizeString = String.valueOf(mVideoInfo.size / M);

        Platform.runLater(() ->
            uiSize.setText(
                certainly ?
                    videoSizeString :
                    "~" + videoSizeString));
    }

    /**
     * Notifies the observer about download end.
     */
    private void notifyDownloadEnd() {
        // Notify the observer
        if (mObserver == null) {
            L.warn("Null observer of video row, can't notify");
            return;
        }

        L.verbose("Notifying observer about download end");

        mObserver.onDownloadEnd(this);
    }

    /**
     * Notifies the observer about download end.
     */
    private void notifyDownloadStateChanged() {
        // Notify the observer
        if (mObserver == null) {
            L.warn("Null observer of video row, can't notify");
            return;
        }

        L.verbose("Notifying observer state changed");

        mObserver.onDownloadStateChanged(this);
    }

    /**
     * Notifies the observer that row removal has been required.
     */
    private void notifyRowRemovalRequired() {
        // Notify the observer
        if (mObserver == null) {
            L.warn("Null observer of video row, can't notify");
            return;
        }

        L.verbose("Notifying observer about forced row removal");

        mObserver.onRowRemovalRequired(this);
    }
}
