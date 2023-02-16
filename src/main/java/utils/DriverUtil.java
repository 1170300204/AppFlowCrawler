package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.remote.MobileCapabilityType;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public final class DriverUtil {
    public static final Logger log = LoggerFactory.getLogger(DriverUtil.class);
    public static AppiumDriver driver;
    private static int deviceHeight;
    private static int deviceWidth;
    private static final int APP_START_WAIT_TIME = 20;
    private static int screenshotCount = 0;
    private static final int scale = 1;

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

        deviceHeight = driver.manage().window().getSize().getHeight();
        deviceWidth = driver.manage().window().getSize().getWidth();

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

    public static String getPageSource() {
        return getPageSourceWithSleep(1);
    }

    public static String getPageSourceWithSleep(double seconds) {
        log.info("Get PageSource ...");

        if (seconds > 0) {
            sleep(seconds);
        }

        String xml = "";
        try {
            //含有特殊字符&#时,后续操作会报异常
            xml = driver.getPageSource().replace("&#","").replace("UTF-8","gbk");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to get PageSource");
        }
        return xml;
    }

    public static String getCurrentActivity() {
        String currentActivity;
        try {
            currentActivity = ((AndroidDriver) driver).currentActivity();
            return currentActivity;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to get Current Activity");
            currentActivity = "android";
        }
        return currentActivity;
    }

    public static int getScreenScale() {
        return scale;
    }

    public static int getDeviceHeight() {
        return deviceHeight;
    }

    public static int getDeviceWidth() {
        return deviceWidth;
    }

    public static void takeScreenShot() {
        sleep(1);

        File screenshot;
        try {
            log.info("Taking ScreenShot ... ");
            screenshot = driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File(ConfigUtil.getScreenshotDir() + File.separator + ConfigUtil.getDatetime() + ".png"));

            if (!ConfigUtil.getIsEnableDeleteScreen())
                return;
            File screenshotDir = new File(ConfigUtil.getScreenshotDir());
            File[] files = screenshotDir.listFiles();
            if (files != null && files.length > ConfigUtil.getScreenShotCount()) {
                File deleteFile = files[0];
                for (File file : files) {
                    if (file.getName().compareTo(deleteFile.getName()) < 0) {
                        deleteFile = file;
                    }
                }
                log.info("The number of screenshots exceeds the limit. The oldest screenshots will be deleted : " + deleteFile);
                log.info("Delete States : " + deleteFile.delete());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to take ScreenShot");
        }
    }

    public static void doBack() {
        log.info("Do Back");

        pressKeyCode(AndroidKey.BACK);

        sleep(1);
    }

    public static void pressKeyCode(AndroidKey keycode) {
        ((AndroidDriver) driver).pressKey(new KeyEvent(keycode));
    }

    public static boolean isExit(String udid, String packageName) {
        if (null == packageName) {
            return false;
        }
        packageName = packageName.toLowerCase();
        boolean exitStatus = true;
        String res = CommandUtil.executeCmd("adb -s " + udid + " shell ps | findstr " + packageName);
        if (!res.endsWith(packageName) && !res.contains(packageName + "\n")) {
            log.error("Get a CRASH : " + packageName);
            exitStatus = false;
        }
        return exitStatus;
    }

    public static void restartApp() {
        log.info("Restarting App ...");
        goHome();
        try {
            driver.quit();
            getAndroidAppiumDriver(ConfigUtil.getPackageName(), ConfigUtil.getMainActivity(), ConfigUtil.getUdid(), ConfigUtil.getPort());
            sleep(APP_START_WAIT_TIME);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to Restart App");
        }

    }

    public static void goHome() {
        pressKeyCode(AndroidKey.HOME);
        sleep(1);
    }
}
