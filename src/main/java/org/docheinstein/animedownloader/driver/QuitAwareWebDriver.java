package org.docheinstein.animedownloader.driver;

import org.openqa.selenium.WebDriver;

public interface QuitAwareWebDriver extends WebDriver {
    boolean hasQuitted();
}
