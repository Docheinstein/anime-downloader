package org.docheinstein.animedownloader.driver;

import org.docheinstein.commons.logger.DocLogger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;

public class QuitAwareChromeDriver extends ChromeDriver implements QuitAwareWebDriver {

    private boolean mQuitted = false;

    private static final DocLogger L = DocLogger.createForClass(QuitAwareChromeDriver.class);

    @Deprecated
    public QuitAwareChromeDriver(Capabilities capabilities) {
        super(capabilities);
    }

    @Override
    public boolean hasQuitted() {
        return mQuitted;
    }

    @Override
    public void quit() {
        super.quit();
        L.debug("Detected quit() over web driver; marking it as quitted");
        mQuitted = true;
    }
}