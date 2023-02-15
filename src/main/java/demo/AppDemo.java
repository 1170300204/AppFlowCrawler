package demo;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class AppDemo {

    private AppiumDriver driver;
    public String appPackage = "com.youdao.calculator";
    public String appActivity = "com.youdao.calculator.activities.MainActivity";


    private void init() throws Exception {

        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability(CapabilityType.BROWSER_NAME, "");
        cap.setCapability("plateformName", "Android");
        cap.setCapability("deviceName", "127.0.0.1:62001");
        cap.setCapability("platformVersion","9");

        cap.setCapability("appPackage", appPackage);
        cap.setCapability("appActivity", appActivity);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), cap);

    }

    public void doTest() throws Exception {
        init();

        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        System.out.println("Current window Size: Width " + width + " Height " + height);

//        int x0 = (int)(width * 0.8);  // 起始x坐标
//        int x1 = (int)(height * 0.2);  // 终止x坐标
//        int y = (int)(height * 0.5);  // y坐标
//        for (int i=0; i<5; i++) {
//
//
//            Thread.sleep(1000);
//        }
//
//        driver.findElement(By.id("com.youdao.calculator:id/guide_button")).click();
//        for (int i = 0; i < 6; i++) {
//            driver.findElement(By.xpath("//android.webkit.WebView[@text='Mathbot Editor']")).click();
//            Thread.sleep(1000);
//        }

        System.out.println(driver.getRemoteAddress());
    }

    public static void main(String[] args) throws Exception {
        AppDemo appDemo = new AppDemo();
        appDemo.doTest();
    }
}
