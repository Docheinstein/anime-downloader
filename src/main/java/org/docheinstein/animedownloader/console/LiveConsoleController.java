package org.docheinstein.animedownloader.console;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import org.docheinstein.animedownloader.ui.base.InstantiableController;
import org.docheinstein.commons.internal.DocCommonsLogger;
import org.docheinstein.commons.logger.DocLogger;

import java.util.HashMap;
import java.util.Map;

public class LiveConsoleController implements InstantiableController {

    private static final Map<DocLogger.LogLevel, String> mLevel2Color = new HashMap<>();

    static {
        mLevel2Color.put(DocLogger.LogLevel.Verbose, "#f6d1f0");
        mLevel2Color.put(DocLogger.LogLevel.Debug, "#dff9d2");
        mLevel2Color.put(DocLogger.LogLevel.Info, "#ccdcf8");
        mLevel2Color.put(DocLogger.LogLevel.Warn, "#fff2ce");
        mLevel2Color.put(DocLogger.LogLevel.Error, "##fccdca");
    }

    @FXML
    private Node uiRoot;

    @FXML
    private ListView<Label> uiMessages;

    @Override
    public String getFXMLAsset() {
        return "live_console.fxml";
    }

    @FXML
    public void initialize() {
        uiRoot.addEventFilter(KeyEvent.KEY_PRESSED,
            event -> {
                if (new KeyCodeCombination(KeyCode.F10).match(event))
                    LiveConsoleManager.instance().toggleLiveConsole();
            });

            DocCommonsLogger.addListener(message ->
                appendMessageLater(DocLogger.LogLevel.Verbose, message));

            DocLogger.addListener((this::appendMessageLater));
    };

    private void appendMessageLater(DocLogger.LogLevel level, String message) {
        Platform.runLater(() -> {
            appendMessage(level, message);
        });
    }

    private void appendMessage(DocLogger.LogLevel level, String message) {

        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + mLevel2Color.getOrDefault(level, "white"));
        label.setPadding(new Insets(5));
        uiMessages.getItems().add(label);
    }
}
