package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.util.*;

public class XPathUtil {

    public static Logger log = LoggerFactory.getLogger(XPathUtil.class);
    public static XPath xPath = XPathFactory.newInstance().newXPath();
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static DocumentBuilder documentBuilder;


    private static long crawlerRunningTime;
    private static long beginTime;

    private static String appName;
    private static String appNameXpath;
    private static List<String> packageNameList;
    private static List<String> nodeBlackList;
    private static List<String> nodeWhiteList;
    private static List<String> nodeNameExcludeList;
    private static List<String> structureNodeNameExcludeList;
    private static List<String> pressBackPackageList;
    private static List<String> pressBackActivityList;
    private static List<String> backKeyTriggerList;
    private static final List<String> xpathNotFoundElementList = new ArrayList<>();
    private static final List<String> clickFailureElementList = new ArrayList<>();
    private static String clickXpath;
//    private static String firstLoginElemXpath;
//    private static ArrayList<Map> loginElemList;
    private static Set<String> xpathBlackSet;
    private static int scale;
    private static boolean ignoreCrash;
    private static boolean removedBounds = false;
    private static final boolean swipeVertical = ConfigUtil.getIsEnableVerticalSwipe();
    private static long userLoginInterval;
    private static long userLoginCount = 0;

    //按back键回到主屏后 重启app的次数
    private static int pressBackCount = 3;
    private static long clickCount = 0;
    private static long maxDepth = 0;
    private static String pic = null;
    private static String backKeyXpath = null;
    private static int deviceHeight;
    private static int deviceWidth;

    public static void initialize(String udid) {
        log.info("initialize XPath module ...");

        crawlerRunningTime = ConfigUtil.getCrawlerRunningTime();
        beginTime = System.currentTimeMillis();
        userLoginInterval = ConfigUtil.getUserLoginInterval();
        userLoginInterval = userLoginInterval <=0 ? 1 : userLoginInterval;

        log.info("The estimated total running time is " + crawlerRunningTime + " min");

        //从XML中获取包名
        appNameXpath = "(//*[@package!=''])[1]";
        appName = ConfigUtil.getPackageName().trim();
        packageNameList = new ArrayList<>(Collections.singletonList(appName));
        packageNameList.addAll(ConfigUtil.getAndroidValidPackageList());
        //添加黑名单条目
        nodeBlackList = new ArrayList<>();
        nodeBlackList.addAll(ConfigUtil.getItemBlackList());
        xpathBlackSet = getBlackXpathSet(nodeBlackList);
        //添加白名单条目
        nodeWhiteList = new ArrayList<>();
        nodeWhiteList.addAll(ConfigUtil.getItemWhiteList());

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            log.error("Fail to create document builder");
        }

        //查找元素时使用的xpath的builder
        StringBuilder clickBuilder;

        clickBuilder = new StringBuilder("//*[");
        clickBuilder.append(ConfigUtil.getAndroidClickXpathHeader());//@clickable="true"
        for(String item : ConfigUtil.getAndroidExcludeType()) {
            clickBuilder.append(" and @class!=" + "\"").append(item).append("\"");
        }
        clickBuilder.append("]");
        clickXpath = clickBuilder.toString();

        log.info("Clickable elements xpath: " + clickXpath);

        scale = DriverUtil.getScreenScale();
        deviceHeight = DriverUtil.getDeviceHeight();
        deviceWidth = DriverUtil.getDeviceWidth();

        ignoreCrash = ConfigUtil.getIsIgnoreCrash();
        nodeNameExcludeList = ConfigUtil.getNodeNameExcludeList();
        structureNodeNameExcludeList = ConfigUtil.getStructureNodeNameExcludeList();
        maxDepth = ConfigUtil.getMaxDepth();
        pressBackPackageList = ConfigUtil.getPressBackPackageList();
        pressBackActivityList = ConfigUtil.getPressBackActivityList();
        //backKeyXpath
        backKeyTriggerList = ConfigUtil.getPressBackTextList();

        log.info("======== XPathUtils initialize Test ========");
        log.info("crawlerRunningTime: " + crawlerRunningTime);
        log.info("appName: " + appName);
        log.info("packageNameList: " + String.join(", ",packageNameList));
        log.info("nodeBlackList: " + String.join(", ",nodeBlackList));
        log.info("xpathBlackSet: " + String.join(", ",xpathBlackSet));
        log.info("nodeWhiteList: " + String.join(", ",nodeWhiteList));
        log.info("clickXpath: " + clickXpath);
        log.info("deviceHeight: " + deviceHeight);
        log.info("deviceWidth: " + deviceWidth);
        log.info("ignoreCrash: " + ignoreCrash);
        log.info("nodeNameExcludeList: " + String.join(", ",nodeNameExcludeList));
        log.info("pressBackPackageList: " + String.join(", ",pressBackPackageList));
        log.info("pressBackActivityList: " + String.join(", ",pressBackActivityList));
        log.info("backKeyTriggerList: " + String.join(", ",backKeyTriggerList));
        log.info("============================================");
    }

    public static String dfsCrawl(String xml, long currentDepth) throws Exception {
        //get nodes from page source
        log.info("Method in DFS func. Current depth: " + currentDepth);
        log.info("Window Handler: " + DriverUtil.driver.getWindowHandle());




        //TODO

        return "";
    }

    public static Set<String> getBlackXpathSet(List<String> list) {
        Set<String> set = new HashSet<>();
        for(String item : list) {
            if (item.startsWith("//")) {
                set.add(item);
            }
        }
        return set;
    }



}
