package utils;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

    public static Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    private static ConfigUtil configUtil;

    private static String udid;

    private static Map<String, Object> items;

    //default config items
    public static final String APPIUM_SERVER_IP = "APPIUM_SERVER_IP";
    public static final String APPIUM_PORT = "APPIUM_PORT";

    private static String serverIp = "0.0.0.0";
    private static String port = "4723";

    //Critical element config items
    public static String PACKAGE_NAME = "PACKAGE_NAME";
    public static String MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static String ANDROID_CLICK_XPATH_HEADER = "ANDROID_CLICK_XPATH_HEADER";

    private static String packageName;
    private static String mainActivity;
    private static String androidClickXpathHeader;

    //General config items
    public static final String ENABLE_SCREEN_SHOT = "ENABLE_SCREEN_SHOT";
    public static final String GENERATE_VIDEO = "GENERATE_VIDEO";
    public static final String SCREENSHOT_COUNT = "SCREENSHOT_COUNT";
    public static final String ENABLE_DELETE_SCREEN = "ENABLE_DELETE_SCREEN";
    public static final String CRASH_PIC_COUNT = "CRASH_PIC_COUNT";
    public static final String MAX_DEPTH = "MAX_DEPTH";
    public static final String MAX_CLICK_COUNT = "MAX_CLICK_COUNT";
    public static final String DEFAULT_WAIT_SEC = "DEFAULT_WAIT_SEC";
    public static final String DEFAULT_POLLING_INTERVAL_SEC = "DEFAULT_POLLING_INTERVAL_SEC";
    public static final String IGNORE_CRASH = "IGNORE_CRASH";
    public static final String ENABLE_VERTICAL_SWIPE = "ENABLE_VERTICAL_SWIPE";
    public static final String CRAWLER_RUNNING_TIME = "CRAWLER_RUNNING_TIME";
    public static final String USER_LOGIN_INTERVAL = "USER_LOGIN_INTERVAL";

    private static boolean isEnableScreenShot;
    private static boolean isGenerateVideo;
    private static long screenShotCount;
    private static boolean isEnableDeleteScreen;
    private static long crashPicCount;
    private static long maxDepth;
    private static long maxClickCount;
    private static long defaultWaitSec;
    private static long defaultPollingIntervalSec;
    private static boolean isIgnoreCrash;
    private static boolean isEnableVerticalSwipe;
    private static long crawlerRunningTime;
    private static long userLoginInterval;

    //List
    public static final String INPUT_CLASS_LIST = "INPUT_CLASS_LIST";
    public static final String INPUT_TEXT_LIST = "INPUT_TEXT_LIST";
    public static final String PRESS_BACK_TEXT_LIST = "PRESS_BACK_TEXT_LIST";
    public static final String PRESS_BACK_PACKAGE_LIST = "PRESS_BACK_PACKAGE_LIST";
    public static final String PRESS_BACK_ACTIVITY_LIST = "PRESS_BACK_ACTIVITY_LIST";
    public static final String ITEM_BLACKLIST = "ITEM_BLACKLIST";
    public static final String ANDROID_VALID_PACKAGE_LIST = "ANDROID_VALID_PACKAGE_LIST";
    public static final String ITEM_WHITE_LIST = "ITEM_WHITE_LIST";
    public static final String ANDROID_EXCLUDE_TYPE = "ANDROID_EXCLUDE_TYPE";
    public static final String NODE_NAME_EXCLUDE_LIST = "NODE_NAME_EXCLUDE_LIST";
    public static final String STRUCTURE_NODE_NAME_EXCLUDE_LIST = "STRUCTURE_NODE_NAME_EXCLUDE_LIST";

    private static ArrayList<String> inputClassList;
    private static ArrayList<String> inputTextList;
    private static ArrayList<String> pressBackTextList;
    private static ArrayList<String> pressBackPackageList;
    private static ArrayList<String> pressBackActivityList;
    private static ArrayList<String> itemBlackList;
    private static ArrayList<String> androidValidPackageList;
    private static ArrayList<String> itemWhiteList;
    private static ArrayList<String> androidExcludeType;
    private static ArrayList<String> nodeNameExcludeList;
    private static ArrayList<String> structureNodeNameExcludeList;


    public static ConfigUtil initialize(String file, String udid) {
        log.info("Initialize Configuration");

        setUdid(udid);

        items = new HashMap<>();
        try {
            log.info("Reading ConfigFile ...");

            InputStream inputStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            configUtil = new ConfigUtil();
            Map<String, Object> temp = yaml.load(inputStream);
            ArrayList<String> keys = new ArrayList<>(Arrays.asList("GENERAL", "DEFAULT_VALUE", "LIST", "CRITICAL_ELEMENT", udid));
            if (temp.get(udid) != null) {
                keys.add(udid);
            }

            //读取各项设置
            for (String key: keys) {
                Map<String, Object> miniTemp = (Map<String, Object>) temp.get(key);
                if (miniTemp != null) {
                    for(String itemKey : miniTemp.keySet()) {
                        items.put(itemKey, miniTemp.get(itemKey));
                    }
                }
            }
            serverIp = getStringValue(APPIUM_SERVER_IP);
            port = getStringValue(APPIUM_PORT);

            packageName = getStringValue(PACKAGE_NAME);
            mainActivity = getStringValue(MAIN_ACTIVITY);
            androidClickXpathHeader = getStringValue(ANDROID_CLICK_XPATH_HEADER);

            isEnableScreenShot = getBooleanValue(ENABLE_SCREEN_SHOT, true);
            if (isEnableScreenShot) {
                isGenerateVideo = getBooleanValue(GENERATE_VIDEO, true);
            }

            screenShotCount = getLongValue(SCREENSHOT_COUNT);
            isEnableDeleteScreen = getBooleanValue(ENABLE_DELETE_SCREEN, false);
            crashPicCount = getLongValue(CRASH_PIC_COUNT);
            maxDepth = getLongValue(MAX_DEPTH);
            maxClickCount = getLongValue(MAX_CLICK_COUNT);
            defaultWaitSec = getLongValue(DEFAULT_WAIT_SEC);
            defaultPollingIntervalSec = getLongValue(DEFAULT_POLLING_INTERVAL_SEC);
            isIgnoreCrash = getBooleanValue(IGNORE_CRASH, true);
            isEnableVerticalSwipe = getBooleanValue(ENABLE_VERTICAL_SWIPE, false);
            crawlerRunningTime = getLongValue(CRAWLER_RUNNING_TIME);
            userLoginInterval = getLongValue(USER_LOGIN_INTERVAL);

            inputClassList = getListValue(INPUT_CLASS_LIST);
            inputTextList = getListValue(INPUT_TEXT_LIST);
            pressBackTextList = getListValue(PRESS_BACK_TEXT_LIST);
            pressBackPackageList = getListValue(PRESS_BACK_PACKAGE_LIST);
            pressBackActivityList = getListValue(PRESS_BACK_ACTIVITY_LIST);
            itemBlackList = getListValue(ITEM_BLACKLIST);
            androidValidPackageList = getListValue(ANDROID_VALID_PACKAGE_LIST);
            itemWhiteList = getListValue(ITEM_WHITE_LIST);
            androidExcludeType = getListValue(ANDROID_EXCLUDE_TYPE);
            nodeNameExcludeList = getListValue(NODE_NAME_EXCLUDE_LIST);
            structureNodeNameExcludeList = getListValue(STRUCTURE_NODE_NAME_EXCLUDE_LIST);

        } catch (FileNotFoundException e) {
            log.error("Fail to load config file");
            e.printStackTrace();
        }
        return configUtil;
    }

    public static String getStringValue(String key) {
        String value = String.valueOf(items.get(key));
        return value.equals("null") ? null : value.trim();
    }

    public static long getLongValue(String key) {
        Integer value = (Integer) items.get(key);
        return value == null ? -100 : value.longValue();
    }

    public static boolean getBooleanValue(String key, boolean defaultValue) {
        Boolean value = (Boolean) items.get(key);
        return value == null ? defaultValue : value;
    }

    public static ArrayList<String> getListValue(String key) {
        ArrayList<String> list = (ArrayList<String>) items.get(key);
        return list == null ? new ArrayList<>() : list;
    }

    public static String getUdid() {
        return udid;
    }

    public static void setUdid(String udid) {
        ConfigUtil.udid = udid;
    }

    public static String getServerIp() {
        return serverIp;
    }

    public static void setServerIp(String serverIp) {
        ConfigUtil.serverIp = serverIp;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ConfigUtil.port = port;
    }

    public static String getPackageName() {
        return packageName;
    }

    public static void setPackageName(String packageName) {
        ConfigUtil.packageName = packageName;
    }

    public static String getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(String mainActivity) {
        ConfigUtil.mainActivity = mainActivity;
    }

    public static String getAndroidClickXpathHeader() {
        return androidClickXpathHeader;
    }

    public static void setAndroidClickXpathHeader(String androidClickXpathHeader) {
        ConfigUtil.androidClickXpathHeader = androidClickXpathHeader;
    }

    public static boolean getIsEnableScreenShot() {
        return isEnableScreenShot;
    }

    public static void setIsEnableScreenShot(boolean isEnableScreenShot) {
        ConfigUtil.isEnableScreenShot = isEnableScreenShot;
    }

    public static boolean getIsGenerateVideo() {
        return isGenerateVideo;
    }

    public static void setIsGenerateVideo(boolean isGenerateVideo) {
        ConfigUtil.isGenerateVideo = isGenerateVideo;
    }

    public static long getScreenShotCount() {
        return screenShotCount;
    }

    public static void setScreenShotCount(long screenShotCount) {
        ConfigUtil.screenShotCount = screenShotCount;
    }

    public static boolean getIsEnableDeleteScreen() {
        return isEnableDeleteScreen;
    }

    public static void setIsEnableDeleteScreen(boolean isEnableDeleteScreen) {
        ConfigUtil.isEnableDeleteScreen = isEnableDeleteScreen;
    }

    public static long getCrashPicCount() {
        return crashPicCount;
    }

    public static void setCrashPicCount(long crashPicCount) {
        ConfigUtil.crashPicCount = crashPicCount;
    }

    public static long getMaxDepth() {
        return maxDepth;
    }

    public static void setMaxDepth(long maxDepth) {
        ConfigUtil.maxDepth = maxDepth;
    }

    public static long getMaxClickCount() {
        return maxClickCount;
    }

    public static void setMaxClickCount(long maxClickCount) {
        ConfigUtil.maxClickCount = maxClickCount;
    }

    public static long getDefaultWaitSec() {
        return defaultWaitSec;
    }

    public static void setDefaultWaitSec(long defaultWaitSec) {
        ConfigUtil.defaultWaitSec = defaultWaitSec;
    }

    public static long getDefaultPollingIntervalSec() {
        return defaultPollingIntervalSec;
    }

    public static void setDefaultPollingIntervalSec(long defaultPollingIntervalSec) {
        ConfigUtil.defaultPollingIntervalSec = defaultPollingIntervalSec;
    }

    public static boolean getIsIgnoreCrash() {
        return isIgnoreCrash;
    }

    public static void setIsIgnoreCrash(boolean isIgnoreCrash) {
        ConfigUtil.isIgnoreCrash = isIgnoreCrash;
    }

    public static boolean getIsEnableVerticalSwipe() {
        return isEnableVerticalSwipe;
    }

    public static void setIsEnableVerticalSwipe(boolean isEnableVerticalSwipe) {
        ConfigUtil.isEnableVerticalSwipe = isEnableVerticalSwipe;
    }

    public static long getCrawlerRunningTime() {
        return crawlerRunningTime;
    }

    public static void setCrawlerRunningTime(long crawlerRunningTime) {
        ConfigUtil.crawlerRunningTime = crawlerRunningTime;
    }

    public static long getUserLoginInterval() {
        return userLoginInterval;
    }

    public static void setUserLoginInterval(long userLoginInterval) {
        ConfigUtil.userLoginInterval = userLoginInterval;
    }

    public static ArrayList<String> getInputClassList() {
        return inputClassList;
    }

    public static void setInputClassList(ArrayList<String> inputClassList) {
        ConfigUtil.inputClassList = inputClassList;
    }

    public static ArrayList<String> getInputTextList() {
        return inputTextList;
    }

    public static void setInputTextList(ArrayList<String> inputTextList) {
        ConfigUtil.inputTextList = inputTextList;
    }

    public static ArrayList<String> getPressBackTextList() {
        return pressBackTextList;
    }

    public static void setPressBackTextList(ArrayList<String> pressBackTextList) {
        ConfigUtil.pressBackTextList = pressBackTextList;
    }

    public static ArrayList<String> getPressBackPackageList() {
        return pressBackPackageList;
    }

    public static void setPressBackPackageList(ArrayList<String> pressBackPackageList) {
        ConfigUtil.pressBackPackageList = pressBackPackageList;
    }

    public static ArrayList<String> getPressBackActivityList() {
        return pressBackActivityList;
    }

    public static void setPressBackActivityList(ArrayList<String> pressBackActivityList) {
        ConfigUtil.pressBackActivityList = pressBackActivityList;
    }

    public static ArrayList<String> getItemBlackList() {
        return itemBlackList;
    }

    public static void setItemBlackList(ArrayList<String> itemBlackList) {
        ConfigUtil.itemBlackList = itemBlackList;
    }

    public static ArrayList<String> getAndroidValidPackageList() {
        return androidValidPackageList;
    }

    public static void setAndroidValidPackageList(ArrayList<String> androidValidPackageList) {
        ConfigUtil.androidValidPackageList = androidValidPackageList;
    }

    public static ArrayList<String> getItemWhiteList() {
        return itemWhiteList;
    }

    public static void setItemWhiteList(ArrayList<String> itemWhiteList) {
        ConfigUtil.itemWhiteList = itemWhiteList;
    }

    public static ArrayList<String> getAndroidExcludeType() {
        return androidExcludeType;
    }

    public static void setAndroidExcludeType(ArrayList<String> androidExcludeType) {
        ConfigUtil.androidExcludeType = androidExcludeType;
    }

    public static ArrayList<String> getNodeNameExcludeList() {
        return nodeNameExcludeList;
    }

    public static void setNodeNameExcludeList(ArrayList<String> nodeNameExcludeList) {
        ConfigUtil.nodeNameExcludeList = nodeNameExcludeList;
    }

    public static ArrayList<String> getStructureNodeNameExcludeList() {
        return structureNodeNameExcludeList;
    }

    public static void setStructureNodeNameExcludeList(ArrayList<String> structureNodeNameExcludeList) {
        ConfigUtil.structureNodeNameExcludeList = structureNodeNameExcludeList;
    }
}
