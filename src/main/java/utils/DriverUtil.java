package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public final class DriverUtil {
    public static final Logger log = LoggerFactory.getLogger(DriverUtil.class);
    public static AppiumDriver driver;
    private static int deviceHeight;
    private static int deviceWidth;
    private static final int APP_START_WAIT_TIME = 20;
    private static int screenshotCount = 0;

    public static AppiumDriver getAndroidAppiumDriver(String appPackage, String appActivity, String udid, String port) throws Exception {
        log.info("App Package: " + appPackage);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, udid);
        capabilities.setCapability(MobileCapabilityType.UDID, udid);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 1800);
        capabilities.setCapability("appPackage", appPackage);

        capabilities.setCapability("appActivity", appActivity);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); //Don't delete app data
        capabilities.setCapability("unicodeKeyboard", true); //支持中文输入
        capabilities.setCapability("resetKeyboard", true); //重置输入法为系统默认

        String url = "http://" + ConfigUtil.getServerIp() + ":" + port + "/wd/hub";
        log.info("URL: " + url);
        driver = new AndroidDriver(new URL(url), capabilities);

        return driver;
    }

    public static void sleep(double seconds) {
        log.info(LoggerUtil.getMethodName() + " sleep " + seconds + "s ...");

        try {
            Thread.sleep((int)(seconds * 1000));
        } catch (InterruptedException e) {
            log.info("Fail to sleep");
            e.printStackTrace();
        }

    }


}
