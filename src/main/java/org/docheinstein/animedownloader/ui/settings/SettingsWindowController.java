package org.docheinstein.animedownloader.ui.settings;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.commons.constants.Const;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.console.LiveConsoleController;
import org.docheinstein.animedownloader.console.LiveConsoleManager;
import org.docheinstein.animedownloader.jsettings.JSettings;
import org.docheinstein.animedownloader.ui.base.InstantiableController;
import org.docheinstein.commons.file.FileUtil;
import org.docheinstein.commons.logger.DocLogger;
import org.docheinstein.commons.types.StringUtil;

import java.io.File;

/**
 * Controller for the settings window.
 */
public class SettingsWindowController implements InstantiableController {

    private static final DocLogger L =
        DocLogger.createForClass(SettingsWindowController.class);

    @FXML
    private Node uiRoot;

    @FXML
    private Button uiDownloadFolderButton;

    @FXML
    private Label uiDownloadFolder;

    @FXML
    private CheckBox uiRemoveAfterDownload;

    @FXML
    private CheckBox uiDownloadAutomatically;

    @FXML
    private Node uiAutomaticDownloadContainer;

    @FXML
    private ComboBox<JSettings.AutomaticDownloadStrategy> uiAutomaticDownloadStrategy;

    @FXML
    private Node uiStaticStrategyContainer;

    @FXML
    private Spinner<Integer> uiSimultaneousVideoLimit;

    @FXML
    private CheckBox uiSimultaneousVideoForEachProvider;

    @FXML
    private Node uiAdaptiveStrategyContainer;

    @FXML
    private Spinner<Double> uiBandwidthLimit;

    @FXML
    private Button uiChromeDriverButton;

    @FXML
    private Label uiChromeDriver;


    @FXML
    private CheckBox uiChromeDriverGhostMode;

    @FXML
    private CheckBox uiCloseDriverOnDownloadStarted;

    @FXML
    private CheckBox uiCloseDriverOnInfoRetrieved;


    @FXML
    private Button uiYoutubeDlButton;

    @FXML
    private Label uiFFmpeg;

    @FXML
    private Label uiYoutubeDl;

    @FXML
    private CheckBox uiLogging;

    @FXML
    private CheckBox uiFlush;

    @FXML
    private Button uiCancel;

    @FXML
    private Button uiConsoleButton;

    @FXML
    private Button uiOk;

    @Override
    public String getFXMLAsset() {
        return "settings_window.fxml";
    }

    @FXML
    public void initialize() {
        FxUtil.setExistent(uiAutomaticDownloadContainer, false);
        // FxUtil.setExistent(uiStaticStrategyContainer, false);
        FxUtil.setExistent(uiAdaptiveStrategyContainer, false);

        uiCancel.setOnMouseClicked(event -> closeWindow());

        uiOk.setOnMouseClicked(event -> {
            commitSettings();
            closeWindow();
        });

        uiDownloadFolderButton.setOnMouseClicked(event -> {
            openDownloadDirectoryChooser();
            L.verbose("Change folder button clicked, opening directory chooser");
        });

        uiChromeDriverButton.setOnMouseClicked(event -> {
            openChromeDriverFileChooser();
            L.verbose("Change folder button clicked, opening directory chooser");
        });

        uiYoutubeDlButton.setOnMouseClicked(event -> {
            openYoutubeDlChooser();
            L.verbose("Change ffmpeg folder button clicked, opening directory chooser");
        });

        uiYoutubeDlButton.setOnMouseClicked(event -> {
            openFFmpegFileChooser();
            L.verbose("Change youtube-dl folder button clicked, opening directory chooser");
        });

        uiConsoleButton.setOnMouseClicked(event -> {
            L.verbose("Opening live console");
            openLiveConsole();
        });

        uiDownloadAutomatically.selectedProperty().addListener((observable, oldValue, newValue) -> {
            FxUtil.setExistent(uiAutomaticDownloadContainer, newValue);
        });

        uiAutomaticDownloadStrategy.getItems().setAll(
            JSettings.AutomaticDownloadStrategy.Static,
            JSettings.AutomaticDownloadStrategy.Adaptive
        );

        uiAutomaticDownloadStrategy.valueProperty().addListener((observable, oldValue, newValue) -> {
            FxUtil.setExistent(uiAdaptiveStrategyContainer, newValue == JSettings.AutomaticDownloadStrategy.Adaptive);
            // FxUtil.setExistent(uiStaticStrategyContainer, newValue == JSettings.AutomaticDownloadStrategy.Static);
        });

        uiSimultaneousVideoLimit.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10));

        uiBandwidthLimit.setValueFactory(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 1, 0.1)
        );


        setCurrentDownloadFolderValue(
            JSettings.instance().getDownloadFolderSetting().getValue().getAbsolutePath());
        setCurrentRemoveAfterDownloadValue(
            JSettings.instance().getRemoveAfterDownloadSetting().getValue());
        setCurrentDownloadAutomaticallyValue(
            JSettings.instance().getDownloadAutomaticallySetting().getValue());
        setCurrentAutomaticDownloadStrategyValue(
            JSettings.instance().getAutomaticDownloadStrategySetting().getValue());
        setSimultaneousVideoLimitValue(
            JSettings.instance().getSimultaneousVideoLimitSetting().getValue());
        setSimultaneousVideoLimitForEachProvider(
            JSettings.instance().getSimultaneousVideoForEachProvider().getValue());
        setBandwidthLimit(
            ((double) JSettings.instance().getBandwidthLimit().getValue()) / Const.Units.MB);
        setChromeDriverFile(
            JSettings.instance().getChromeDriverSetting().getValue());
        setChromeDriverGhostModeValue(
            JSettings.instance().getChromeDriverGhostModeSetting().getValue());
        setCloseDriveOnDownloadStarted(
            JSettings.instance().getCloseDriverOnDownloadStarted().getValue());
        setCloseDriverOnInfoRetrieved(
            JSettings.instance().getCloseDriverOnInfoRetrieved().getValue());
        setFFmpegFile(
            JSettings.instance().getFFmpegSetting().getValue());
        setYoutubeDl(
            JSettings.instance().getYoutubeDlSetting().getValue());
        setLoggingValue(
            JSettings.instance().getLoggingSetting().getValue());
        setFlushValue(
            JSettings.instance().getFlushSetting().getValue());
    }

    /**
     * Opens the directory chooser for the download folder and eventually
     * sets the new directory.
     */
    private void openDownloadDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select download folder");
        File dir = directoryChooser.showDialog(uiRoot.getScene().getWindow());

        if (!FileUtil.exists(dir)) {
            L.warn("Invalid download folder has been selected; it won't be changed");
            return;
        }

        L.verbose("Changing download folder to: " + dir.getAbsolutePath());

        setCurrentDownloadFolderValue(dir.getAbsolutePath());
    }

    /**
     * Opens the file chooser for the chrome driver and eventually
     * sets the new chrome driver path.
     */
    private void openChromeDriverFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Chrome Driver");
        File chromeDriver = fileChooser.showOpenDialog(uiRoot.getScene().getWindow());

        if (!FileUtil.exists(chromeDriver)) {
            L.warn("Invalid chrome driver has been selected; it won't be changed");
            return;
        }

        L.verbose("Changing chrome driver path to: " + chromeDriver.getAbsolutePath());

        setChromeDriverFile(chromeDriver);
    }

    /**
     * Opens the file chooser for the ffmpeg executable and eventually
     * sets the new ffmpeg path.
     */
    private void openFFmpegFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ffmpeg");
        File ffmpeg = fileChooser.showOpenDialog(uiRoot.getScene().getWindow());

        if (!FileUtil.exists(ffmpeg)) {
            L.warn("Invalid ffmpeg has been selected; it won't be changed");
            return;
        }

        L.verbose("Changing ffmpeg path to: " + ffmpeg.getAbsolutePath());

        setFFmpegFile(ffmpeg);
    }

    /**
     * Opens the file chooser for the youtube-dl executable and eventually
     * sets the new youtube-dl path.
     */
    private void openYoutubeDlChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select youtube-dl");
        File ffmpeg = fileChooser.showOpenDialog(uiRoot.getScene().getWindow());

        if (!FileUtil.exists(ffmpeg)) {
            L.warn("Invalid youtube-dl has been selected; it won't be changed");
            return;
        }

        L.verbose("Changing youtube-dl path to: " + ffmpeg.getAbsolutePath());

        setYoutubeDl(ffmpeg);
    }

    /**
     * Opens the live console (for see logging messages).
     */
    private void openLiveConsole() {
        LiveConsoleManager.instance().openLiveConsole();
    }

    private void setCurrentDownloadFolderValue(String value) {
        uiDownloadFolder.setText(value);
    }

    private void setCurrentRemoveAfterDownloadValue(boolean value) {
        uiRemoveAfterDownload.selectedProperty().setValue(value);
    }

    private void setCurrentDownloadAutomaticallyValue(boolean value) {
        uiDownloadAutomatically.selectedProperty().setValue(value);
    }

    private void setCurrentAutomaticDownloadStrategyValue(JSettings.AutomaticDownloadStrategy strat) {
        uiAutomaticDownloadStrategy.setValue(strat);
    }

    private void setSimultaneousVideoLimitValue(int value) {
        uiSimultaneousVideoLimit.getValueFactory().setValue(value);
    }

    private void setSimultaneousVideoLimitForEachProvider(boolean value) {
        uiSimultaneousVideoForEachProvider.selectedProperty().setValue(value);
    }

    private void setBandwidthLimit(double mbps) {
        uiBandwidthLimit.getValueFactory().setValue(mbps);
    }

    private void setChromeDriverFile(File file) {
        uiChromeDriver.setText(file != null ? file.getAbsolutePath() : "");
    }

    private void setChromeDriverGhostModeValue(boolean value) {
        uiChromeDriverGhostMode.selectedProperty().setValue(value);
    }

    private void setCloseDriveOnDownloadStarted(boolean value) {
        uiCloseDriverOnDownloadStarted.selectedProperty().setValue(value);
    }

    private void setCloseDriverOnInfoRetrieved(boolean value) {
        uiCloseDriverOnInfoRetrieved.selectedProperty().setValue(value);
    }

    private void setFFmpegFile(File file) {
        uiFFmpeg.setText(file != null ? file.getAbsolutePath() : null);
    }

    private void setYoutubeDl(File file) {
        uiYoutubeDl.setText(file != null ? file.getAbsolutePath() : null);
    }

    private void setLoggingValue(boolean value) {
        uiLogging.selectedProperty().setValue(value);
    }

    private void setFlushValue(boolean value) {
        uiFlush.selectedProperty().setValue(value);
    }
    /**
     * Saves every setting.
     */
    private void commitSettings() {
        // Download folder
        L.verbose("Settings will be actually saved");

        JSettings s = JSettings.instance();

        JSettings.AutomaticDownloadStrategy strat = uiAutomaticDownloadStrategy.getValue();

        s.getDownloadFolderSetting().updateValue(
            new File(uiDownloadFolder.getText()));
        s.getRemoveAfterDownloadSetting().updateValue(
            uiRemoveAfterDownload.isSelected());
        s.getDownloadAutomaticallySetting().updateValue(
            uiDownloadAutomatically.isSelected());
        s.getAutomaticDownloadStrategySetting().updateValue(
            strat);
        s.getSimultaneousVideoLimitSetting().updateValue(
            uiSimultaneousVideoLimit.getValue());
        s.getSimultaneousVideoForEachProvider().updateValue(
            uiSimultaneousVideoForEachProvider.isSelected());
        s.getBandwidthLimit().updateValue(
            strat == JSettings.AutomaticDownloadStrategy.Adaptive ?
                ((int) (uiBandwidthLimit.getValue() * Const.Units.MB)) :
                0);
        s.getChromeDriverSetting().updateValue(
            new File(uiChromeDriver.getText()));
        s.getChromeDriverGhostModeSetting().updateValue(
            uiChromeDriverGhostMode.isSelected());
        s.getCloseDriverOnDownloadStarted().updateValue(
            uiCloseDriverOnDownloadStarted.isSelected());
        s.getCloseDriverOnInfoRetrieved().updateValue(
            uiCloseDriverOnInfoRetrieved.isSelected());
        s.getLoggingSetting().updateValue(
            uiLogging.isSelected());
        s.getFlushSetting().updateValue(
            uiFlush.isSelected());

        String ffmpeg = uiFFmpeg.getText();
        String youtubedl = uiYoutubeDl.getText();

        s.getFFmpegSetting().updateValue(
            StringUtil.isValid(ffmpeg) ? new File(ffmpeg) : null
        );

        s.getYoutubeDlSetting().updateValue(
            StringUtil.isValid(ffmpeg) ? new File(youtubedl) : null
        );

        JSettings.instance().saveSettings();
    }

    /**
     * Closes this window.
     */
    private void closeWindow() {
        ((Stage) uiRoot.getScene().getWindow()).close();
    }

}
