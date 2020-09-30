package org.docheinstein.animedownloader.console;

import javafx.stage.Stage;
import org.docheinstein.animedownloader.commons.constants.Config;
import org.docheinstein.animedownloader.commons.utils.FxUtil;

public class LiveConsoleManager {

    private static LiveConsoleManager INSTANCE = new LiveConsoleManager();

    private final Object mConsoleLock = new Object();
    private Stage mCurrentConsole;
    private boolean mOpened = false;

    public static LiveConsoleManager instance() {
        return INSTANCE;
    }

    private LiveConsoleManager() {}

    public void toggleLiveConsole() {
        if (!mOpened) {
            openLiveConsole();
        } else {
            closeLiveConsole();
        }
    }

    public void openLiveConsole() {
        synchronized (mConsoleLock) {
            if (mOpened)
                return;

            mCurrentConsole = FxUtil.createWindow(
                new LiveConsoleController().createNode(),
                Config.App.LIVE_CONSOLE_TITLE
            );

            mCurrentConsole.setWidth(Config.App.LIVE_CONSOLE_WIDTH);
            mCurrentConsole.setHeight(Config.App.LIVE_CONSOLE_HEIGHT);
            mCurrentConsole.setAlwaysOnTop(true);
            mCurrentConsole.show();

            mOpened = true;
        }
    }

    public synchronized void closeLiveConsole() {
        synchronized (mConsoleLock) {
            if (!mOpened)
                return;

            if (mCurrentConsole != null)
                mCurrentConsole.close();

            mOpened = false;
        }
    }
}
