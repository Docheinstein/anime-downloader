import org.docheinstein.animedownloader.driver.QuitAwareChromeDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.util.logging.Level;

public class AnimeDownloaderTest {
    static QuitAwareChromeDriver d;

    public static void main(String[] args) {
        System.setProperty(
            "webdriver.chrome.driver",
            "/home/stefano/Develop/Java/AnimeDownloader/drivers/chromedriver"
        );

        ChromeOptions co = new ChromeOptions();

        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        co.merge(caps);

        d = new QuitAwareChromeDriver(co);

        doGet();
    }

    public static void doGet() {
        d.get("https://www.google.com/");

        LogEntries logEntries = d.manage().logs().get(LogType.PERFORMANCE);

        System.out.println("Printing log entries");

        for (LogEntry logEntry : logEntries) {
            System.out.println(logEntry.getMessage());
        }
    }
}
