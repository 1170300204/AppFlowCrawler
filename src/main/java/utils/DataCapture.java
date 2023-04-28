package utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.ParseUtil.EDITCAP_PATH;

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

    public static void testData4(int from, int count) {
        int threshold = 15;
        try {
            for (int i = from ; i < from + count; i++) {
                CommandUtil.startTcpdumpNormal(i);
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(15);

//                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/tab_discover')]")).click();
//                DriverUtil.sleep(20);
//
//                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/iv_icon_right')]")).click();
//                DriverUtil.sleep(15);
//
//                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/photo')]")).click();
//                DriverUtil.sleep(15);
//
//                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/profile_send_gift')]")).click();
//                DriverUtil.sleep(15);
                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/post_profile_btn')]")).click();
                DriverUtil.sleep(15);//1646

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/profile_send_gift')]")).click();
                DriverUtil.sleep(15);//1654

                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\2\\pcaps");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void profile_gift(int count) {
        int threshold = 15;
        StringBuilder timestamps;
        try {
            for (int i = 1 ; i <=count; i++) {
                timestamps = new StringBuilder("");
                CommandUtil.startTcpdumpNormal(i);
                Timestamp load_start = new Timestamp(System.currentTimeMillis());
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(8);
                Timestamp load_end = new Timestamp(System.currentTimeMillis());
                addTimeStamp(timestamps,load_start,load_end,"load"+i,0,1);
                DriverUtil.sleep(7);

                Timestamp profile_start = new Timestamp(System.currentTimeMillis());
                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/post_profile_btn')]")).click();
                DriverUtil.sleep(8);
                Timestamp profile_end = new Timestamp(System.currentTimeMillis());
                addTimeStamp(timestamps,profile_start,profile_end,"profile"+i,1,2);
                DriverUtil.sleep(7);

                Timestamp gift_start = new Timestamp(System.currentTimeMillis());
                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/profile_send_gift')]")).click();
                DriverUtil.sleep(8);
                Timestamp gift_end = new Timestamp(System.currentTimeMillis());
                addTimeStamp(timestamps,gift_start,gift_end,"gift"+i,2,3);
                DriverUtil.sleep(7);

                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps");
                String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\timestamp"+i+".txt";
                writeTimeStamp(timestamps,i,timestampFile);
                split(timestampFile,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\" + i + ".pcap");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void uploadPic(int from, int count) {
        StringBuilder timestamps;
        try {
            for (int i = from ; i <from+count; i++) {
                timestamps = new StringBuilder("");
                CommandUtil.startTcpdumpNormal(i);
                Timestamp load_start = new Timestamp(System.currentTimeMillis());
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(8);
                Timestamp load_end = new Timestamp(System.currentTimeMillis());
                addTimeStamp(timestamps,load_start,load_end,"load"+i,0,1);
                DriverUtil.sleep(7);

                Timestamp uploadPic_start = new Timestamp(System.currentTimeMillis());
                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/tab_menu')]")).click();
                DriverUtil.sleep(3);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/menu_photos')]")).click();
                DriverUtil.sleep(4);

                List<WebElement> elements = driver.findElements(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/content')]"));
                if (elements.size()>=2) {
                    elements.get(1).click();
                    DriverUtil.sleep(4);


                    driver.findElement(By.xpath("//android.widget.ImageView[@content-desc=\"更多选项\"]")).click();
                    DriverUtil.sleep(2);

                    driver.findElement(By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.LinearLayout[1]/android.widget.RelativeLayout")).click();
                    DriverUtil.sleep(2);

                    driver.findElement(By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout[2]/android.widget.FrameLayout[1]/android.widget.FrameLayout/android.support.v7.widget.RecyclerView/android.widget.ImageView["+(i%3+1)+"]")).click();
                    DriverUtil.sleep(3);

                    driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/acv_bottom_panel_counter')]")).click();
                    DriverUtil.sleep(15);
                    Timestamp uploadPic_end = new Timestamp(System.currentTimeMillis());
                    addTimeStamp(timestamps,uploadPic_start,uploadPic_end,"uploadPic"+i,1,2);
                    DriverUtil.sleep(5);
                }
                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps");
                String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\timestamp"+i+".txt";
                writeTimeStamp(timestamps,i,timestampFile);
                split(timestampFile,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\" + i + ".pcap");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void postWithPic(int from, int count) {
        StringBuilder timestamps;
        try {
            for (int i = from ; i <from+count; i++) {
                timestamps = new StringBuilder("");
                CommandUtil.startTcpdumpNormal(i);
//                Timestamp load_start = new Timestamp(System.currentTimeMillis());
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.vkontakte.android", "com.vkontakte.android.MainActivity");
                DriverUtil.sleep(12);
//                Timestamp load_end = new Timestamp(System.currentTimeMillis());
//                addTimeStamp(timestamps,load_start,load_end,"load"+i,0,1);
//                DriverUtil.sleep(8);

                Timestamp postWithPic_start = new Timestamp(System.currentTimeMillis());
                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/create_post_gallery_image')]")).click();
                DriverUtil.sleep(3);

//                driver.findElement(By.xpath("//*[contains(@resource-id,'/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout/android.view.ViewGroup/android.support.v7.widget.RecyclerView/android.widget.ImageView[1]')]")).click();
                log.info("Please select pic");
                DriverUtil.sleep(8);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/acv_bottom_panel_counter')]")).click();
                DriverUtil.sleep(8);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.vkontakte.android:id/posting_done_button')]")).click();
                DriverUtil.sleep(15);
                Timestamp postWithPic_end = new Timestamp(System.currentTimeMillis());

                addTimeStamp(timestamps,postWithPic_start,postWithPic_end,"postWithPic"+i,1,2);


                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps");
                String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\timestamp"+i+".txt";
                writeTimeStamp(timestamps,i,timestampFile);
                split(timestampFile,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\" + i + ".pcap");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void weiboUploadPic(int from, int count) {
        StringBuilder timestamps;
        try {
            for (int i = from ; i <from+count; i++) {
                timestamps = new StringBuilder("");
                CommandUtil.startTcpdumpNormal(i);
                Timestamp load_start = new Timestamp(System.currentTimeMillis());
                AppiumDriver driver = DriverUtil.getAndroidAppiumDriverNormal("com.sina.weibo", ".MainTabActivity");
                DriverUtil.sleep(15);
                Timestamp load_end = new Timestamp(System.currentTimeMillis());
                addTimeStamp(timestamps,load_start,load_end,"load1"+i,0,1);
                DriverUtil.sleep(5);


                driver.findElement(By.xpath("//*[contains(@resource-id,'com.sina.weibo:id/rltitleSave')]")).click();
                DriverUtil.sleep(5);

                driver.findElement(By.xpath("//android.widget.TextView[@content-desc=\"图片\"]")).click();
                DriverUtil.sleep(6);

                Timestamp postWithPic_start = new Timestamp(System.currentTimeMillis());
//                driver.findElement(By.xpath("//*[contains(@resource-id,'/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout/android.view.ViewGroup/android.support.v7.widget.RecyclerView/android.widget.ImageView[1]')]")).click();
                log.info("Please select pic");
                DriverUtil.sleep(8);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.sina.weibo:id/btn_confirm_edit')]")).click();
                DriverUtil.sleep(8);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.sina.weibo:id/nextLayout')]")).click();
                DriverUtil.sleep(8);

                driver.findElement(By.xpath("//*[contains(@resource-id,'com.sina.weibo:id/rightBtn_wrapper')]")).click();
                DriverUtil.sleep(15);
                Timestamp postWithPic_end = new Timestamp(System.currentTimeMillis());

                addTimeStamp(timestamps,postWithPic_start,postWithPic_end,"postWithPic"+i,1,2);


                driver.quit();
                CommandUtil.endTcpdumpNormal(i,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps");
                String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\timestamp"+i+".txt";
                writeTimeStamp(timestamps,i,timestampFile);
                split(timestampFile,"D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\pcaps\\" + i + ".pcap");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommandUtil.executeCmd("adb -s 127.0.0.1:62001 shell killall tcpdump ");
        }
    }

    public static void main(String[] args) throws Exception {
//        testData1(1, 20);
//        testData2(21,20);
//        testData3(41,30);
//        testData4(72,19);
//        profile_gift(10);
//        uploadPic(17,4);
//        postWithPic(10,1);

        weiboUploadPic(2,9);
    }

    public static void addTimeStamp(StringBuilder timestampBuilder, Timestamp startTime, Timestamp endTime, String action, long startDepth, long endDepth) {
        timestampBuilder.append(startTime.toString()).append("\t").append(endTime.toString()).append("\t").append(action).append("\t")
                .append(startDepth).append("\t").append(endDepth).append("\t").append("\n");
    }

    public static void writeTimeStamp(StringBuilder timestamps,int i, String timestampFile) {
        try {
            File file = new File(timestampFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(writer);
            out.write(timestamps.toString());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to write TimeStamp File");
        }
    }

    public static ArrayList<String> split(String timestampFile, String pcapFile) {
        ArrayList<String[]> timeStamps = ParseUtil.getTimeStamps(timestampFile);
        ArrayList<String> outputFiles = new ArrayList<>();
        for (String[] cols : timeStamps) {
            File pcap = new File(pcapFile);
            String outputFile;
            if (!pcap.exists()) {
                log.error("no pcap");
                return null;
            }
            String fileName = cols[2];
            outputFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\ouput\\" + fileName + ".pcap";
            String cmd = EDITCAP_PATH + " -F libpcap -T ether -A \"" + cols[0] + "\" -B \"" + cols[1]+ "\" \"" + pcapFile + "\" \"" + outputFile + "\"";
            // -F libpcap -T ether
            outputFiles.add(outputFile);
//            System.out.println(cmd);
            CommandUtil.executeCmdNormal(cmd);
            log.info("Split pcap from " +  cols[0] + " to " + cols[1] + " , Output : " + outputFile);
        }
//        System.out.println(outputFiles);
        return outputFiles;
    }

}
