package org.docheinstein.animedownloader.ui.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.docheinstein.animedownloader.commons.constants.Config;

import org.docheinstein.animedownloader.commons.constants.Resources;
import org.docheinstein.animedownloader.commons.utils.ApplicationUtil;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.commons.logger.DocLogger;


/**
 * Main application class
 */
public class MainWindow extends Application {

    private static MainWindow INSTANCE;
    private static final DocLogger L = DocLogger.createForClass(MainWindow.class);

    private Stage mWindow;

    public MainWindow() {
        INSTANCE = this;
    }

    /**
     * Returns the unique instance of this application.
     * @return the instance of this application
     */
    public static MainWindow instance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        ApplicationUtil.init();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            L.info("Last window closed, quitting application");
            Platform.exit();
            System.exit(0);
        });

        mWindow = FxUtil.showWindow(
            new MainWindowController().createNode(),
            Config.App.TITLE);
        mWindow.getIcons().setAll(Resources.UI.ICONS);
        mWindow.setMinWidth(Config.App.MIN_WIDTH);
        mWindow.setMinHeight(Config.App.MIN_HEIGHT);
        mWindow.setWidth(Config.App.PREF_WIDTH);
        mWindow.setHeight(Config.App.PREF_HEIGHT);
    }

    @Override
    public void stop() throws Exception {
        L.info("Anime Download has been stopped");
        System.exit(0);
        super.stop();
    }

    /**
     * Returns the window of this application.
     * @return the main window
     */
    public Window getWindow() {
        return mWindow;
    }
}
