package org.docheinstein.animedownloader.ui.alert;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.commons.utils.ResourceUtil;

/**
 * A {@link Alert} that is automatically styled with the stylesheet
 * used by this application.
 *
 * @see Alert
 */
public class StyledAlert extends Alert {

    /**
     * Creates a new styled alert dialog.
     * @param title the title of the alert
     * @param alertType the type of alert
     * @param contentText the text of the alert
     * @param buttons the buttons of the alert
     */
    public StyledAlert(String title, AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        setTitle(title);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        attachStylesheet();
    }

    /**
     * Attaches the stylesheet used by this application to this dialog so
     * that it will be styled with the style of the stylesheet.
     */
    private void attachStylesheet() {
        FxUtil.addStylesheet(getDialogPane(), ResourceUtil.getStyleURL("style.css"));
    }
}
