package org.docheinstein.animedownloader.ui.alert;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Contains the methods for create the {@link StyledAlert}.
 */
public class AlertFactory {

    /**
     * Creates a new styled error dialog.
     * @param title the title of the alert
     * @param contextText the text of the alert
     * @param buttons the buttons of the alert
     * @return the alert
     */
    public static StyledAlert newError(String title, String contextText, ButtonType... buttons) {
        return newAlert(Alert.AlertType.ERROR, title, contextText, buttons);
    }

    /**
     * Creates a new styled warning dialog.
     * @param title the title of the alert
     * @param contextText the text of the alert
     * @param buttons the buttons of the alert
     * @return the alert
     */
    public static StyledAlert newWarn(String title, String contextText, ButtonType... buttons) {
        return newAlert(Alert.AlertType.WARNING, title, contextText, buttons);
    }

    /**
     * Creates a new styled info dialog.
     * @param title the title of the alert
     * @param contextText the text of the alert
     * @param buttons the buttons of the alert
     * @return the alert
     */
    public static StyledAlert newInfo(String title, String contextText, ButtonType... buttons) {
        return newAlert(Alert.AlertType.INFORMATION, title, contextText, buttons);
    }

    /**
     * Creates a new styled dialog of the given type.
     * @param type type of alert
     * @param title the title of the alert
     * @param contextText the text of the alert
     * @param buttons the buttons of the alert
     * @return the alert
     */
    public static StyledAlert newAlert(Alert.AlertType type, String title,
                                       String contextText, ButtonType... buttons) {
        StyledAlert i = new StyledAlert(title, type, contextText, buttons);
        String headerText = null;
        switch (type) {
            case ERROR:
                headerText = "Error";
                break;
            case WARNING:
                headerText = "Warning";
                break;
            case INFORMATION:
                headerText = "Information";
                break;
        }
        if (headerText != null)
            i.setHeaderText(headerText);
        // else := leave the default one
        return i;
    }
}
