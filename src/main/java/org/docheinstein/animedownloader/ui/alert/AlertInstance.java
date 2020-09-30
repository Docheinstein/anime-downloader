package org.docheinstein.animedownloader.ui.alert;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import org.docheinstein.animedownloader.ui.main.MainWindow;

/**
 * Alert types used by the application.
 */
public enum  AlertInstance {
    MP4ConversionFailed(
        Alert.AlertType.ERROR,
        "Conversion to MP4 failed",
        "Conversion to MP4 has failed.\n" +
        "Check that ffmpeg is either available in the current " +
        "environment path or that its path is specified in the settings."
    ),
    SeleniumStartFailed(
        Alert.AlertType.ERROR,
        "Selenium failed to start",
        "Session cannot be created, failed with error:\n\n%s"
    ),
    FolderDoesNotExists(
        Alert.AlertType.ERROR,
        "Folder can't be opened",
        "Folder can't be opened, maybe it does not exists?"
    ),
    GeneralWarning(
        Alert.AlertType.WARNING,
        "Unexpected exception occurred",
        "An exception occurred\n\n%s"
    ),
    GeneralError(
        Alert.AlertType.ERROR,
        "Unexpected exception occurred",
        "An exception occurred"
    );

    /**
     * Creates a new alert type
     * @param type type of the alert
     * @param title title of the alert
     * @param content content of the alert
     */
    AlertInstance(Alert.AlertType type, String title, String content) {
        mType = type;
        mTitle = title;
        mContent = content;
    }

    private Alert.AlertType mType;
    private String mTitle;
    private String mContent;

    /**
     * Shows the alert using the given args for format the content
     * with {@link String#format(String, Object...)}.
     * @param contentArgs the args of the content, if needed
     */
    public void show(Object... contentArgs) {
        Alert a = AlertFactory.newAlert(
            mType, mTitle, String.format(mContent, contentArgs));
        centerAlertInScene(a);
        fixPlasma5Bug(a);
        a.show();
    }

    /**
     * Shows the alert using the given args for format the content
     * with {@link String#format(String, Object...)} and waits for it.
     * @param contentArgs the args of the content, if needed
     */
    public void showAndWait(Object... contentArgs) {
        Alert a = AlertFactory.newAlert(
            mType, mTitle, String.format(mContent, contentArgs));
        centerAlertInScene(a);
        fixPlasma5Bug(a);
        a.showAndWait();
    }

    /**
     * Fix KDE bug that doesn't let the window appear
     * @param a the alert
     */
    private static void fixPlasma5Bug(Alert a) {
        // TODO: hope that it will be fixed
        // https://bugs.openjdk.java.net/browse/JDK-8193502
        a.setResizable(true);
        a.onShownProperty().addListener(e -> {
            Platform.runLater(() -> {
                a.setResizable(false);
            });
        });
    }

    /**
     * Centers the alert in the window.
     * @param a the alert
     */
    private static void centerAlertInScene(Alert a) {
        Scene s = MainWindow.instance().getWindow().getScene();
        a.setX(s.getX() + (s.getWidth()) / 2);
        a.setY(s.getY() + (s.getHeight()) / 2);
    }
}
