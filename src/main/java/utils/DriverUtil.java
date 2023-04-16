package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.functions.AppiumFunction;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DriverUtil {
    public static final Logger log = LoggerFactory.getLogger(DriverUtil.class);
    public static AppiumDriver driver;
    private static int deviceHeight;
    private static int deviceWidth;
    private static final int APP_START_WAIT_TIME = 20;
    private static int screenshotCount = 0;
    private static final int scale = 1;
    private static final int DEFAULT_PICTURE_POINT_RADIUS = 20;

    public static AppiumDriver getAndroidAppiumDriverNormal(String appPackage, String appActivity) throws Exception {
        log.info("App Package: " + appPackage);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "127.0.0.1:62001");
        capabilities.setCapability(MobileCapabilityType.UDID, "127.0.0.1:62001");
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 1800);
        capabilities.setCapability("appPackage", appPackage);

        capabilities.setCapability("appActivity", appActivity);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); //Don't delete app data
        capabilities.setCapability("unicodeKeyboard", true); //支持中文输入
        capabilities.setCapability("resetKeyboard", true); //重置输入法为系统默认

        String url = "http://0.0.0.0:4723/wd/hub";

        log.info("URL: " + url);
        driver = new AndroidDriver(new URL(url), capabilities);

        deviceHeight = driver.manage().window().getSize().getHeight();
        deviceWidth = driver.manage().window().getSize().getWidth();

        return driver;

    }

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

    public static MobileElement findElement(By by, int waitSeconds) {
        log.info(LoggerUtil.getMethodName());
        AppiumDriverWait wait = AppiumDriverWait.getInstance(driver, waitSeconds);

        AppiumFunction<AppiumDriver, WebElement> waitFunction = var1 -> {
            WebElement elem = null;
            try {
                elem = var1.findElement(by);
            } catch (Exception e) {
                log.error("Element : " + by.toString() + " is not founded! Polling again ...");
            }
            if (null != elem) {
                boolean display = elem.isDisplayed();
                if (!display) {
                    log.error("Element : " + by.toString() + " is found but not displayed");
                    elem = null;
                } else {
                    log.info("Element " + by.toString() + " is found.");
                }
            }
            return elem;
        };
        return (MobileElement) wait.until(waitFunction);
    }

    public static List<MobileElement> findElements(By by, int waitSeconds) {
        log.info(LoggerUtil.getMethodName() + by.toString());

        AppiumDriverWait wait = AppiumDriverWait.getInstance(driver, waitSeconds);

        AppiumFunction<AppiumDriver, List<MobileElement>> waitFunction = var1 -> {
            List<MobileElement> list = new ArrayList<>();
            try {
                list = var1.findElements(by);
            } catch (Exception e) {
                log.info("Element : " + by + " not found! Polling again ...");
            }
            int size = list.size();
            if (0 == size) {
                list = null;
                log.info(by + " List size Zero");
            } else {
                log.info(by + " List size : " +size);
            }
            return list;
        };
        return wait.until(waitFunction);
    }

    public static List<MobileElement> findElements(By by) {
        log.info(LoggerUtil.getMethodName());
        return findElements(by, (int) ConfigUtil.getDefaultWaitSec());
    }

    public static List<MobileElement> findElemsWithoutException(By by) {
        log.info(LoggerUtil.getMethodName());

        List<MobileElement> list = null;
        try {
            list = findElements(by, (int) ConfigUtil.getDefaultWaitSec());
        } catch (Exception e) {
            log.info("Elements " + by.toString() + " not found.");
        }

        return list;
    }

    public static MobileElement findElement(By by) {
        return findElement(by, (int) ConfigUtil.getDefaultWaitSec());
    }

    public static MobileElement findElementWithoutException(By by) {
        MobileElement elem = null;
        try {
            elem = findElement(by, (int) ConfigUtil.getDefaultWaitSec());
        } catch (Exception e) {
            log.info("Element " + by.toString() + " not found.");
        }
        return elem;
    }

    public static MobileElement findElemByIdWithoutException(String id, int second) {
        log.info(LoggerUtil.getMethodName());

        MobileElement elem = null;
        try {
            elem = findElement(By.id(id), second);
        } catch (Exception e) {
            log.info("Element " + id + " is not found.");
        }
        return elem;
    }

    public static boolean elemCheckById(String id, int second) {
        log.info(LoggerUtil.getMethodName());

        boolean ret = false;
        MobileElement elem = findElemByIdWithoutException(id, second);
        if (null != elem) {
            ret = true;
        }
        log.info(id + " " + ret);
        return ret;
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

    public static void takeScreenShot(String name, int x, int y, int radius) {
        sleep(1);

        File screenshot;
        try {
            log.info("Taking ScreenShot ... ");
            screenshot = driver.getScreenshotAs(OutputType.FILE);

            if (x >= 0 && y >= 0 && radius > 0) {
                FileOutputStream fileOutputStream = null;
                try {
                    BufferedImage image = ImageIO.read(screenshot);
                    Graphics2D g2d = image.createGraphics();
                    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
                    g2d.setComposite(ac);
                    int rb = 2 * radius;
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
                    g2d.setColor(Color.RED);
                    g2d.fillOval(x - rb, y - rb, 2 * rb, 2 * rb);            //填充一个椭圆形
                    fileOutputStream = new FileOutputStream(screenshot);
                    ImageIO.write(image, "png", fileOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("Fail to take ScreenShot With Point");
                } finally {
                    if (fileOutputStream!=null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("Fail to close Picture " + screenshot.getName());
                        }
                    }
                }
            }

            if (null == name) {
                FileUtils.copyFile(screenshot, new File(ConfigUtil.getScreenshotDir() + File.separator + ConfigUtil.getDatetime() + ".png"));
            } else {
                FileUtils.copyFile(screenshot, new File(ConfigUtil.getScreenshotDir() + File.separator + ConfigUtil.getDatetime() + "_" + name + ".png"));
            }

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

    public static void takeScreenShot() {
        takeScreenShot(null, -1, -1, -1);
    }

    public static void takeScreenShot(String name) {
        takeScreenShot(name, -1, -1 ,-1);
    }

    public static void takeScreenShot(int x, int y) {
        takeScreenShot(null, x, y, DEFAULT_PICTURE_POINT_RADIUS);
    }

    public static void takeScreenShot(String name, int x, int y) {
        takeScreenShot(name, x, y, DEFAULT_PICTURE_POINT_RADIUS);
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

    public static void switchApp() {
        //连续输入两次APP_SWITCH可以切换到最近的应用程序
        DriverUtil.pressKeyCode(AndroidKey.APP_SWITCH);
        DriverUtil.sleep(0.8);
        DriverUtil.pressKeyCode(AndroidKey.APP_SWITCH);
    }

    public static void goHome() {
        pressKeyCode(AndroidKey.HOME);
        sleep(1);
    }

    public static int internalNextInt(int origin, int bound) {
        Random random = new Random();

        if (origin < bound) {
            int n = bound - origin;
            if (n > 0) {
                return random.nextInt(n) + origin;
            } else {  // range not representable as int
                int r;
                do {
                    r = random.nextInt();
                } while (r < origin || r > bound);
                return r;
            }
        } else {
            //return random.nextInt();
            log.info("!!!!origin>=bound");
            return bound - origin;
        }
    }

    public static void swipe(int startX, int startY, int endX, int endY) {
        log.info("scroll from : startX " + startX + ", startY " + startY + ", to  endX " + endX + ",endY " + endY);

        try {
            TouchAction touchAction = new TouchAction(driver);
            PointOption pointStart = PointOption.point(startX, startY);
            PointOption pointEnd = PointOption.point(endX, endY);

            WaitOptions waitOption = WaitOptions.waitOptions(Duration.ofMillis(1000));
            touchAction.press(pointStart).waitAction(waitOption).moveTo(pointEnd).release().perform();
        } catch (Exception e) {
            log.error("Fail to scroll from : startX " + startX + ", startY " + startY + ", to  endX " + endX + ",endY " + endY);
            e.printStackTrace();
        }
    }

    public static void verticallySwipe(boolean scrollDown) {
        log.info(LoggerUtil.getMethodName());
        Dimension dimension = driver.manage().window().getSize();
        //在屏幕中央垂直滑动屏幕高度一半的距离
        int height = dimension.getHeight();
        int width = dimension.getWidth();
        if (scrollDown)
            swipe(dimension.getWidth() / 2, dimension.getWidth() / 2, dimension.getHeight() / 2, dimension.getHeight() - 50);
        else
            swipe(dimension.getWidth() / 2, dimension.getWidth() / 2, dimension.getHeight() / 2, 50);
    }

}
