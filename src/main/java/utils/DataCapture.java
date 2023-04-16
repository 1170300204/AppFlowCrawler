package utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCapture {
    public static final Logger log = LoggerFactory.getLogger(DataCapture.class);

    public static void testData1(int from, int count) throws Exception {

        int threshold = 15;
        try {
            for (int i = from ; i < from + count; i++) {
                CommandUtil.startTcpdumpNormal(i);
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/post_profile_btn')]")).click();
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/profile_photo')]")).click();
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/likes')]")).click();
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/photo_viewer_comments')]")).click();
                DriverUtil.sleep(15);

                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\2\\pcaps");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void testData2(int from, int count) throws Exception {

        int threshold = 15;
        try {
            for (int i = from ; i < from + count; i++) {
                CommandUtil.startTcpdumpNormal(i);
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/post_profile_btn')]")).click();
                DriverUtil.sleep(15);//1646

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/profile_send_gift')]")).click();
                DriverUtil.sleep(15);//1654

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/action')]")).click();
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/photo')]")).click();
                DriverUtil.sleep(15);

                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\2\\pcaps");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void testData3(int from, int count) throws Exception {
        int threshold = 15;
        try {
            for (int i = from ; i < from + count; i++) {
                CommandUtil.startTcpdumpNormal(i);
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.tencent.android.qqdownloader", "com.tencent.assistantv2.activity.MainActivity");
                DriverUtil.sleep(15);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.tencent.android.qqdownloader:id/xo')]")).click();
                DriverUtil.sleep(15);

                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\2\\pcaps");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void main(String[] args) throws Exception {
//        testData1(1, 20);
//        testData2(21,20);
        testData3(41,30);
    }

}
