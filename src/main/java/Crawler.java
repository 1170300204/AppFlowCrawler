import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CommandUtil;
import utils.ConfigUtil;
import utils.DriverUtil;
import utils.XPathUtil;

import java.io.File;
import java.util.Date;

public class Crawler {

    public static final Logger log = LoggerFactory.getLogger(Crawler.class);
    private static Date beginTime;
    private static String udid;
    private AppiumDriver driver;
    private String configFile;

    private static class ShutDownHandler extends Thread {
        @Override
        public void run() {
            log.info("Shutting down ...");
            XPathUtil.shutDownInfoPrint();
            CommandUtil.endTcpdump(ConfigUtil.getPackageName());
        }
    }

    public void doCrawl() throws Exception {

        beginTime = new Date();
        log.info("Begin at " + beginTime);
        configFile = System.getProperty("user.dir") + File.separator + "config" + File.separator + "config.yml";
        configFile = configFile.trim();
        log.info("Config File Path: " + configFile);
        udid = "127.0.0.1:62001";
        ConfigUtil.initialize(configFile, udid);

        CommandUtil.startTcpdump(ConfigUtil.getPackageName());

        driver = DriverUtil.getAndroidAppiumDriver(ConfigUtil.getPackageName(), ConfigUtil.getMainActivity(), ConfigUtil.getUdid(), ConfigUtil.getPort());
        if (driver == null) {
            log.error("Fail to start appium server");
            return;
        }

        log.info(driver.toString());

        Runtime.getRuntime().addShutdownHook(new ShutDownHandler());
        try {
            //等待app启动完毕
            DriverUtil.sleep(10);
            //初始化XPath
            XPathUtil.initialize(udid);
            //获取PageSource
            String pageSource = DriverUtil.getPageSource();

            log.info("======== In App UI/Flow Crawler Func (DFS mode) ========");
            XPathUtil.dfsCrawl(pageSource, 0, null);

            log.info("==================================");
            log.info("======== Complete running ========");
            log.info("==================================");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("========== Fail ==========");
        } finally {
            CommandUtil.endTcpdump(ConfigUtil.getPackageName());
            DriverUtil.driver.quit();
        }

    }


    public static void main(String[] args) throws Exception {
        Crawler crawler = new Crawler();

        crawler.doCrawl();
    }
}
