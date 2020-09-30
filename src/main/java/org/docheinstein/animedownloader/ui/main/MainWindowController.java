package org.docheinstein.animedownloader.ui.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.docheinstein.animedownloader.commons.files.FileHierarchy;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.console.LiveConsoleManager;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.ui.alert.AlertInstance;
import org.docheinstein.animedownloader.ui.base.InstantiableController;
import org.docheinstein.animedownloader.video.VideoInfo;
import org.docheinstein.animedownloader.video.VideoProvider;
import org.docheinstein.commons.thread.ThreadUtil;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.ui.settings.SettingsWindowController;
import org.docheinstein.animedownloader.ui.video.VideoRowController;
import org.docheinstein.commons.types.StringUtil;
import org.openqa.selenium.SessionNotCreatedException;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Controller of {@link MainWindow}
 */
public class MainWindowController
    implements InstantiableController, VideoRowController.VideoRowObserver {

    private static final DocLogger L =
        DocLogger.createForClass(MainWindowController.class);

    /**
     * Contains the visible video rows associated to their controller.
     */
    private Map<VideoRowController, Node> mVideoRows = new LinkedHashMap<>();

    @FXML
    private Node uiRoot;

    @FXML
    private Button uiSettings;

    @FXML
    private Button uiPaste;

    @FXML
    private Button uiOpenDownloadFolder;

    @FXML
    private Button uiStartDownload;

    @FXML
    private ListView<Node> uiDownloadList;

    @FXML
    private Label uiVersion;

    @Override
    public String getFXMLAsset() {
        return "main_window.fxml";
    }

    @FXML
    private void initialize() {

        uiOpenDownloadFolder.setOnMouseClicked(event ->
            ThreadUtil.start(() -> {
                if (!Desktop.isDesktopSupported()) {
                    L.warn("Desktop is not supported: folder can't be opened");
                    return;
                }

                try {
                    // Open the current download folder
                    File downloadFolder = JSettings.instance().getDownloadFolderSetting().getValue();
                    L.verbose("Trying to open " + downloadFolder + " via default file explorer");
                    Desktop.getDesktop().open(downloadFolder);
                } catch (IOException e) {
                    L.warn("Current download folder can't be opened");
                }
            }
        ));

        uiSettings.setOnMouseClicked(event -> {
            Stage stage = FxUtil.createWindow(
                new SettingsWindowController().createNode(),
                Config.App.SETTINGS_TITLE
            );

            stage.setAlwaysOnTop(true);
            stage.show();
        });

        // Paste listener
        uiPaste.setOnMouseClicked(event -> handlePaste());


        uiRoot.addEventHandler(
            KeyEvent.KEY_PRESSED,
            event -> {
                // CTRL + V listener
                if (new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN).match(event))
                    handlePaste();

                // F10 listener (live console)
                else if (new KeyCodeCombination(KeyCode.F10).match(event))
                    handleLiveConsole();

            });

        uiDownloadList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Node> call(ListView<Node> param) {
                ListCell<Node> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(Node item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(null);
                            setGraphic(item);
                        }
                    }
                };

                // Makes the cell fit its parent
                cell.prefWidthProperty().bind(uiDownloadList.widthProperty().subtract(50));

                return cell;
            }
        });

        // Start download button
        uiStartDownload.setOnMouseClicked(event -> {
            L.verbose("Automagically checking for next video to download " +
                "(using static strategy, simultaneous limit = 1)");
            checkForNextVideoToDownload(JSettings.AutomaticDownloadStrategy.Static, 1, false);
        });

        uiDownloadList.getItems().addListener(
            (ListChangeListener<? super Node>) change -> handleVideosChange());

        uiVersion.setText(Config.App.VERSION);

        loadVideosFromCache();

        handleVideosChange();
    }

    /**
     * Handles the CTRL + V event by adding a row for the pasted url.
     */
    private void handlePaste() {
        L.verbose("Detected CTRL + V or Paste action");

        try {
            String url = (String) Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .getContents(DataFlavor.stringFlavor)
                .getTransferData(DataFlavor.stringFlavor);


            String identifier = String.valueOf(System.currentTimeMillis());

            if (!StringUtil.isValid(identifier)) {
                L.warn("Can't add null string to video list");
                return;
            }

            VideoRowController rowController = addVideoToDownloadList(identifier, url, null);

            if (rowController.hasValidProvider()) {

                // Automatically retrieves the video info and stores it
                ThreadUtil.startSafe(() -> {
                    VideoInfo videoInfo = rowController.retrieveVideoInfo();

//                    videoInfo.url = url;
                    saveVideoToCache(identifier, url, videoInfo);

                    // Starts the next possible video, if needed
                    checkForNextVideoToDownloadAutomatically();

                }, throwable -> {
                    L.warn("Exception occurred while handling video: " + throwable.getMessage());
                    if (throwable instanceof SessionNotCreatedException) {
                        Platform.runLater(() ->
                            AlertInstance.SeleniumStartFailed.showAndWait(
                                throwable.getMessage())
                        );
                    } else {
                        Platform.runLater(() ->
                            AlertInstance.GeneralWarning.showAndWait(
                                throwable.getMessage())
                        );
                    }
                });
            }

        } catch (UnsupportedFlavorException | IOException e) {
            L.error("Error occurred while trying to copy data from system clipboard", e);
        }
    }

    /**
     * Handle the live console shortcut combination (toggling the live console)
     */
    private void handleLiveConsole() {
        LiveConsoleManager.instance().toggleLiveConsole();
    }

    /**
     * Adds the video row for the given url and identifier.
     * @param identifier the identifier of the video row
     * @param url the url of the video
     * @param videoInfo additional video info used for initialize the video row
     * @return the controller of the added row
     */
    private VideoRowController addVideoToDownloadList(
        String identifier, String url, VideoInfo videoInfo) {
        L.info("Adding video [" + identifier + "] with URL: " + url);

        VideoRowController videoController = new VideoRowController(
            url, identifier, this, videoInfo
        );

        Node videoRow = videoController.createNode();

        mVideoRows.put(videoController, videoRow);

        uiDownloadList.getItems().add(videoRow);

        // Scroll to bottom
        uiDownloadList.scrollTo(uiDownloadList.getItems().size() - 1);

        return videoController;
    }

    /**
     * Loads the video rows from the file system.
     */
    private void loadVideosFromCache() {
        File[] videos = FileHierarchy.instance().getVideosNode().getFile().listFiles();
        if (videos == null)
            return;

        Arrays.sort(videos);

        if (videos.length == 0) {
            L.verbose("No video to load from cache");
            return;
        }

        for (File video : videos) {
            String videoIdentifier = video.getName();
            String[] videoIdentifierComponents = videoIdentifier.split("\\.");
            if (videoIdentifierComponents.length > 0)
                videoIdentifier = videoIdentifierComponents[0];

            L.verbose("Loading video from cache with identifier: " + videoIdentifier);

            Gson gson = new Gson();

            String videoInfoContent = FileUtil.readFile(video);

            if (!StringUtil.isValid(videoInfoContent)) {
                L.warn("Invalid video info content for: " + video.getAbsolutePath());
                continue;
            }


            JsonObject jsonVideoInfo = gson.fromJson(videoInfoContent, JsonObject.class);

            if (jsonVideoInfo == null) {
                L.warn("Invalid video info content for: " + video.getAbsolutePath());
                continue;
            }

            VideoInfo videoInfo = new VideoInfo();

            JsonElement jsonTitle = jsonVideoInfo.get(Config.Json.VideoCache.KEY_TITLE);
            JsonElement jsonUrl = jsonVideoInfo.get(Config.Json.VideoCache.KEY_URL);

            if (jsonTitle == null || jsonTitle.isJsonNull() ||
                jsonUrl == null || jsonUrl.isJsonNull()) {
                L.warn("Invalid video info content for: " + video.getAbsolutePath());
                continue;
            }

            videoInfo.title = jsonTitle.getAsString();
            String url = jsonUrl.getAsString();

            addVideoToDownloadList(videoIdentifier, url, videoInfo);
        }
    }

    /**
     * Saves the video to the file system.
     * @param identifier the identifier of the video, used as filename
     * @param url the url of the video to download
     * @param videoInfo other video info
     */
    private void saveVideoToCache(String identifier, String url,
                                  VideoInfo videoInfo) {
        L.verbose("Saving video to cache with following details\n" +
                "[ID] " + identifier + "\n" +
                videoInfo
        );

        File outputFile = new File(
            FileHierarchy.instance().getVideosNode().getFile(),
            identifier + ".json");

        if (FileUtil.exists(outputFile)) {
            L.warn("Video already exists in cache; are you tyring to download the same video twice?");
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonVideoInfo = new JsonObject();
        jsonVideoInfo.addProperty(Config.Json.VideoCache.KEY_TITLE, videoInfo.title);
        jsonVideoInfo.addProperty(Config.Json.VideoCache.KEY_URL, url);

        FileUtil.write(outputFile, gson.toJson(jsonVideoInfo));
    }

    /**
     * Deletes the video from the file system.
     * @param identifier the identifier of the video to remove
     */
    private void deleteVideoFromCache(String identifier) {
        L.verbose("Deleting video from cache with identifier: " + identifier);
        FileUtil.delete(new File(
            FileHierarchy.instance().getVideosNode().getFile(),
            identifier + ".json"));
    }

    /**
     * Eventually starts the next available video if the download automatically
     * setting is enabled and it won't exceed the simultaneous video limit and
     * other strategy's constraints.
     */
    private void checkForNextVideoToDownloadAutomatically() {
        // Automatically start the first available video if required
        if (!JSettings.instance().getDownloadAutomaticallySetting().getValue()) {
            L.debug("Automatic download disabled, nothing to start");
            return;
        }

        JSettings settings = JSettings.instance();

        JSettings.AutomaticDownloadStrategy strategy =
            settings.getAutomaticDownloadStrategySetting().getValue();
        int downloadLimit  =
            settings.getSimultaneousVideoLimitSetting().getValue();
        boolean forEachProvider =
            settings.getSimultaneousVideoForEachProvider().getValue();

        checkForNextVideoToDownload(strategy, downloadLimit, forEachProvider);
    }

    /**
     * Eventually starts the next available video if it won't exceed the
     * simultaneous video limit and other strategy's constraints.
     * Do not check for automatic download setting.
     */
    private synchronized void checkForNextVideoToDownload(
        JSettings.AutomaticDownloadStrategy strategy,
        int simultaneousVideoLimit,
        boolean forEachProvider
    ) {

        boolean stillToDownload;
        do {
            if (strategy == JSettings.AutomaticDownloadStrategy.Static) {
                L.verbose("Checking for automatic download using 'Static' strategy");
                stillToDownload = checkForNextVideoToDownloadStaticStrategy(simultaneousVideoLimit, forEachProvider);
            } else if (strategy == JSettings.AutomaticDownloadStrategy.Adaptive) {
                L.verbose("Checking for automatic download using 'Adaptive' strategy");
                stillToDownload = checkForNextVideoToDownloadAdaptiveStrategy(simultaneousVideoLimit, forEachProvider);
                L.verbose("Waiting " +
                    Config.Download.ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_AFTER_A_DOWNLOAD +
                    " before check again for a download");
                ThreadUtil.sleep(Config.Download.ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_AFTER_A_DOWNLOAD * 1000);
            } else {
                L.warn("Do not know strategy: " + strategy);
                stillToDownload = false;
            }
        } while (stillToDownload);
    }

    /*
     * The logic of the static strategy is just download until the current
     * download count is lower than the specified limit, eventually referred
     * to each provider.
     */
    private VideoRowController getNextVideoToDownloadUsingStaticStrategy(int downloadLimit, boolean forEachProvider) {
        Map<VideoProvider, Integer> inDownloadOrGoingToBeDownloadedVideos = getVideosByProvider(
            VideoRowController.VideoDownloadState.Downloading,
            VideoRowController.VideoDownloadState.PreparingDownload
        );
        int inDownloadOrGoingToBeDownloadedCount = 0;

        for (Integer count : inDownloadOrGoingToBeDownloadedVideos.values()) {
            inDownloadOrGoingToBeDownloadedCount += count;
        }

        L.verbose("There are " + inDownloadOrGoingToBeDownloadedCount + " video in download (or going to be downloaded)");
        L.verbose("The download limit is: " + downloadLimit);
        L.verbose("Referred to each provider: " + forEachProvider);

        // Check if the download count exceed the limit, but only if forEachProvider
        // is not true and so the limit is referred to a global limit
        if (!forEachProvider && inDownloadOrGoingToBeDownloadedCount >= downloadLimit) {
            L.verbose("Video won't be started automatically since it would exceed the limit");
            return null;
        }

        L.verbose("Searching for the first available video to start automatically");

        for (Map.Entry<VideoRowController, Node> row : mVideoRows.entrySet()) {
            VideoRowController video = row.getKey();
            if (video.getState() == VideoRowController.VideoDownloadState.ReadyToDownload) {

                int inDownloadForProvider = inDownloadOrGoingToBeDownloadedVideos.getOrDefault(video.getProvider(), 0);

                if (inDownloadForProvider < downloadLimit) {
                    L.verbose("Found video still to download, automatically downloading it");
                    return video;
                }
            }
        }

        L.verbose("No video to download found, doing nothing");
        return null;
    }

    /*
     * The logic of the adaptive strategy is start to download videos until
     * one of the following conditions is met.
     * 1) Just like static download, if the current download count is not lower
     * then the specified limit, nothing else is automatically put in download
     * 2) Moreover, if the current sum of the download bandwidth exceed the
     * specified limit, nothing else is automatically put in download
     */
    private boolean checkForNextVideoToDownloadAdaptiveStrategy(
        int simultaneousDownloadLimit, boolean forEachProvider) {

        VideoRowController videoToDownload =
            getNextVideoToDownloadUsingStaticStrategy(simultaneousDownloadLimit, forEachProvider);

        if (videoToDownload == null)
            return false; // Nothing even with static strategy, doing nothing

        L.verbose("Found video that could be download, checking for bandwidth" +
                " before proceed");

        int bandwidthLimit = JSettings.instance().getBandwidthLimit().getValue();
        L.verbose("Bandwidth limit is: " + (bandwidthLimit / 1000) + "KB/s");

        // Check for at least a few second if the current bandwidth is below the threshold
        int attemptAt0Bandwidth = 0;
        for (int attempt = 0; attempt < Config.Download.ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_UNDER_THRESHOLD_BEFORE_DOWNLOAD; attempt++) {
            int currentBandwidth = 0;

            // Unlike static strategy, here we have to check for bandwidth too
            for (Map.Entry<VideoRowController, Node> row : mVideoRows.entrySet()) {
                VideoRowController video = row.getKey();
                int bw = video.getInstantBandwidth();
                L.verbose("Bandwidth of video " + video.getVideoInfo().title + " is " + (bw / 1000) + "KB/s");
                currentBandwidth += bw;
            }

            L.verbose("#" + (attempt + 1) + " Total current bandwidth is " + (currentBandwidth / 1000) + "KB/s");

            if (currentBandwidth > bandwidthLimit) {
                L.verbose("Can't download since bandwidth limit would be exceeded");
                return false; // Current bandwidth is above the threshold, doing nothing
            }

            if (currentBandwidth <= 0) {
                attemptAt0Bandwidth++;
            }

            if (attemptAt0Bandwidth ==
                Config.Download.ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_UNDER_THRESHOLD_BEFORE_DOWNLOAD_IF_CURRENT_BANDWIDTH_IS_0) {
                break;
            }

            if (attempt < Config.Download.ADAPTIVE_STRATEGY_SECONDS_TO_WAIT_UNDER_THRESHOLD_BEFORE_DOWNLOAD - 1) {
                L.verbose("Current bandwidth is below the threshold, waiting 1s before next check...");
                ThreadUtil.sleep(1000);
            }
        }

        // If we are here, then for at least a few seconds the current bandwidth has been
        // below than the limit, thus actually download the video

        L.verbose("Actually downloading video using 'Adaptive' strategy");
        videoToDownload.download();
        return true;
    }

    private boolean checkForNextVideoToDownloadStaticStrategy(
        int simultaneousDownloadLimit, boolean forEachProvider) {

        VideoRowController videoToDownload =
            getNextVideoToDownloadUsingStaticStrategy(simultaneousDownloadLimit, forEachProvider);
        if (videoToDownload == null)
            return false;
        videoToDownload.download();
        return true;
    }

    private Map<VideoProvider, Integer> getVideosByProvider(
        VideoRowController.VideoDownloadState ...allowedStates) {

        Map<VideoProvider, Integer> inDownloadVideos = new HashMap<>();

        for (VideoRowController videoRow : getVideos(allowedStates)) {
            inDownloadVideos.put(
                videoRow.getProvider(),
                inDownloadVideos.getOrDefault(videoRow.getProvider(), 0) + 1
            );
        }

        return inDownloadVideos;
    }

    private List<VideoRowController> getVideos(
        VideoRowController.VideoDownloadState... allowedStates) {
        List<VideoRowController> filteredVideos = new ArrayList<>();

        for (Map.Entry<VideoRowController, Node> row : mVideoRows.entrySet()) {
            VideoRowController videoRow = row.getKey();

            // No filter
            if (allowedStates == null || allowedStates.length <= 0) {
                filteredVideos.add(videoRow);
            }
            else {
                // Check between each allowed state
                VideoRowController.VideoDownloadState videoState = videoRow.getState();
                for (VideoRowController.VideoDownloadState allowedState : allowedStates) {
                    if (allowedState == videoState)
                        filteredVideos.add(videoRow);
                }
            }
        }

        return filteredVideos;
    }

    private void handleVideosChange() {
        // Download button
        List<VideoRowController> videos = getVideos(
            VideoRowController.VideoDownloadState.ReadyToDownload
        );

        uiStartDownload.setDisable(videos.size() <= 0);

        checkForNextVideoToDownloadAutomatically();
    }

    private void removeVideo(VideoRowController row, boolean removeFromUI, boolean removeFromCache) {
        L.verbose("Removing row of video; from UI = " +
            removeFromUI + "; from cache = " + removeFromCache);

        if (removeFromUI)
            uiDownloadList.getItems().remove(mVideoRows.remove(row));
        if (removeFromCache)
            deleteVideoFromCache(row.getIdentifier());

        handleVideosChange();
    }

    @Override
    public void onRowRemovalRequired(VideoRowController row) {
        removeVideo(row, true, true);
    }

    @Override
    public void onDownloadEnd(VideoRowController row) {
        removeVideo(row, JSettings.instance().getRemoveAfterDownloadSetting().getValue(), true);
    }

    @Override
    public void onDownloadStateChanged(VideoRowController row) {
        VideoInfo info = row.getVideoInfo();

        L.verbose("A video state has changed: " +
            (info != null ? info.title : "<null>") +
            " => " + row.getState().name());

//        handleVideosChange();
    }
}
