package utils;

import enums.PackageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.util.*;

public class XPathUtil {

    public static Logger log = LoggerFactory.getLogger(XPathUtil.class);
    public static XPath xPath = XPathFactory.newInstance().newXPath();
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;

    private static boolean runningStates = true;

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
    private static String tabBarXpath;
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
    //元素的最小体型
    private static int nodeTolerance = 5;

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
        StringBuilder tabBarBuilder = null;
        String androidTabBarId = ConfigUtil.getAndroidTabBarId();
        clickBuilder = new StringBuilder("//*[");
        clickBuilder.append(ConfigUtil.getAndroidClickXpathHeader());//@clickable="true"
        if (null != androidTabBarId) {
            tabBarBuilder = new StringBuilder("//*[" + androidTabBarId + "]/descendant-or-self::*[@clickable=\"true\"]");
            clickBuilder.append(" and not(ancestor-or-self::*[").append(androidTabBarId).append("])");
        }
        for(String item : ConfigUtil.getAndroidExcludeType()) {
            clickBuilder.append(" and @class!=" + "\"").append(item).append("\"");
        }
        clickBuilder.append("]");
        clickXpath = clickBuilder.toString();
        if (null != tabBarBuilder) {
            tabBarXpath = tabBarBuilder.toString();
            log.info("Tab Bar XPath: " + tabBarXpath);
        } else {
            log.info("No Tab Bar XPath");
        }
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
//        log.info("Window Handler: " + DriverUtil.driver.getWindowHandle());

        //检查运行时间是否在时限内
        long currentTime = System.currentTimeMillis();
        if ((currentTime - beginTime) > (crawlerRunningTime * 60 * 1000)) {
            log.info("======== Program has been running for " + (currentTime - beginTime)/1000/60 + " min.  ========");
            log.info("======== Ending the Task ========");
            runningStates = false;
            return xml;
        }

        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList nodes = (NodeList) xPath.evaluate(clickXpath, document, XPathConstants.NODESET);
        int length = nodes.getLength();
        log.info("UI nodes length: " + length);

        String previousPageStructure = XPathUtil.getPageStructure(xml, clickXpath);
        String afterPageStructure = previousPageStructure;
        String currentXml = xml;
        if(!runningStates) {
            log.info("Running States Change. Ending the Task");
            return currentXml;
        }

        if (backKeyTriggerList.size() > 0) {
            for (String backKey: backKeyTriggerList) {
                if (currentXml.contains(backKey)) {
                    log.info("Trigger Back Key [" + backKey + "], Pressing it ...");
                    if (ConfigUtil.getIsEnableScreenShot())
                        DriverUtil.takeScreenShot();
                    DriverUtil.doBack();
                    currentXml = DriverUtil.getPageSource();
                    return currentXml;
                }
            }
        }
        if (pressBackActivityList.size() > 0) {
            String currentActivity = DriverUtil.getCurrentActivity();
            for (String backKey: pressBackActivityList) {
                if (currentActivity.contains(backKey)) {
                    log.info("Trigger Back Key Activity [" + backKey + "], Pressing Back Key ...");
                    if (ConfigUtil.getIsEnableScreenShot())
                        DriverUtil.takeScreenShot();
                    DriverUtil.doBack();
                    currentXml = DriverUtil.getPageSource();
                    return currentXml;
                }
            }
        }

        //遍历前进行一些必要的检查
        String packageName = getAppName(currentXml);
        String currentActivity = DriverUtil.getCurrentActivity();
        log.info("======== Traverse Check ========");
        log.info("Current Package Name : " + packageName);
        log.info("Current Activity : " + currentActivity);
        //检查当前的包名是否是有效的，是否发生了Crash或者需要重启等操作
        if (PackageStatus.VALID != getPackageStatus(packageName, true)) {
            log.info("Invalid Package : " + packageName + ", Skipping");
            currentXml = DriverUtil.getPageSource();
            return currentXml;
        }
        currentDepth++;
        log.info("Current Depth : " + currentDepth);
        if (currentDepth > maxDepth) {
            runningStates = false;
            log.info("Max Traversal Depth Exceeded [" + currentDepth + " > " + maxDepth + " ], Returning ...");
            currentXml = DriverUtil.getPageSource();
            return currentXml;
        }
        log.info("NodeList Length : " + length);
        if (length == 0) {
            log.error("None UI Node is found. Checking Tab Bar Element ...");
        }

        getTabBarElement(currentXml, tabBarXpath);
        //黑名单XPath
        Set<String> nodeXpathBlackSet = getBlackNodeXpathSet(document);
        int blackNodeXpathSize = nodeXpathBlackSet.size();
        log.info("Black XPath Set Length : " + blackNodeXpathSize);
        log.info("================================");

        //遍历UI中的Node
        while (--length >= 0 && runningStates) {
            log.info("Element #" + length);






        }






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

    public static Set<String> getBlackNodeXpathSet(Document document) {
        String xpath = "";
        Set<String> nodeSet = new HashSet<>();
        try {
            for (String item : xpathBlackSet) {
                xpath = item;
                NodeList nodes = (NodeList) xPath.evaluate(item, document, XPathConstants.NODESET);
                int length = nodes.getLength();
                while (--length >= 0) {
                    Node temp = nodes.item(length);
                    String nodeXpath = getNodeXpath(temp, false);
                    if (nodeXpath != null) {
                        nodeSet.add(nodeXpath);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to handle Black Key Node XPath : " + xpath);
        }
        return nodeSet;
    }

    public static String getPageStructure(String xml, String xpathExpression) throws Exception {
        log.info(LoggerUtil.getMethodName());

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        StringBuilder pageStrcture = new StringBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList nodes = (NodeList) xPath.evaluate(xpathExpression, document, XPathConstants.NODESET);

        int length = nodes.getLength();
        while (length > 0) {
            length--;
            Node temp = nodes.item(length);
            String nodeXpath = XPathUtil.getNodeXpath(temp, true);
            if (null != nodeXpath) {
                pageStrcture.append("\n").append(nodeXpath).append("\n");
            }
        }
        return pageStrcture.toString();
    }

    public static String getNodeXpath(Node node, boolean structureOnly) {
        int length = node.getAttributes().getLength();
        StringBuilder nodeXpath = new StringBuilder("//" + node.getNodeName() + "[");
        String bounds = "[" + deviceWidth + "," + deviceHeight + "]";

        while (length > 0) {
            length--;
            Node temp = node.getAttributes().item(length);

            String nodeName = temp.getNodeName();
            String nodeValue = temp.getNodeValue();

            if (nodeValue.length() == 0)
                continue;
            if (removedBounds && nodeValue.contains(bounds)) {
                log.warn("Boundary Value : " + nodeValue);
                continue;
            }

            //黑名单
            for (Object item: nodeBlackList) {
                if (nodeValue.contains(String.valueOf(item))) {
                    log.info("BlackList Item : "+ nodeValue);
                    log.info("Skipping ... ");
                    return null;
                }
            }
            if (nodeNameExcludeList.contains(nodeName.toLowerCase()))
                continue;
            if (structureOnly && structureNodeNameExcludeList.contains(nodeName.toLowerCase()))
                continue;

            if (nodeName.equals("bounds")) {
                String value = nodeValue.replace("][",",");// bounds="[0,0][1080,1920]" -> [0,0,1080,1920]
                int index = value.indexOf(",");
                int startX = Integer.parseInt(value.substring(1,index));
                int nextIndex = value.indexOf(",", index + 1);
                int startY = Integer.parseInt(value.substring(index + 1, nextIndex));
                index = value.indexOf(",", nextIndex + 1);
                int endX = Integer.parseInt(value.substring(nextIndex + 1,index));
                int endY = Integer.parseInt(value.substring(index + 1, value.length()-1));

                if (startX < 0 || endX < 0 || startY < 0 || endY < 0
                        || startX > deviceWidth || endX > deviceWidth || startY > deviceHeight || endY > deviceHeight) {
                    log.info("Node out of Screen : " + nodeValue);
                    return null;
                }
                if (Math.abs(endX - startX) < nodeTolerance || Math.abs(endY - startY) < nodeTolerance) {
                    log.info("Node out of Tolerance (" + nodeTolerance +") : " + nodeValue);
                    return null;
                }
            }
            nodeXpath.append("@").append(nodeName).append("=\"").append(nodeValue).append("\"");
            if (length > 0)
                nodeXpath.append(" and ");
        }
        nodeXpath.append("]");
        //需要处理最后一个多余的 and
        return nodeXpath.toString().replace(" and ]", "]");
    }

    private static NodeList getNodeListByXpath(String xml, String appNameXpath) throws Exception {
        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        return (NodeList) xPath.evaluate(appNameXpath, document, XPathConstants.NODESET);
    }

    private static String getAppName(String xml) {
        String name = null;
        NodeList nodes = null;

        try {
            nodes = getNodeListByXpath(xml, appNameXpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == nodes || nodes.getLength() == 0) {
            log.error("AppName Get Null or Empty");
            return name;
        }
        Node node = nodes.item(0);

        String key = "package";
        name = node.getAttributes().getNamedItem(key).getNodeValue();
        return name;
    }

    //对当前的包名状态进行检查，判断是否跳出了待执行APP以及是否需要重启等
    public static PackageStatus getPackageStatus(String packageName, boolean restart) {
        PackageStatus status = PackageStatus.CRASH;
        for (String name : packageNameList) {
            if (null != packageName && packageName.contains(name)) {
                status = PackageStatus.VALID;
                if (pressBackPackageList.contains(packageName)) {
                    status = PackageStatus.DO_BACK;
                    log.info("Current Package [" + packageName + "] in Back List, doing Back ...");
                }
                break;
            }
        }
        runningStates = true;
        if (status != PackageStatus.VALID) {
            log.error("Invalid Package Name : " + packageName);
            DriverUtil.takeScreenShot();
            if (DriverUtil.isExit(ConfigUtil.getUdid(), packageName)) {
                status = PackageStatus.RESTART;
                boolean doBack = false;
                for (String backKey : pressBackPackageList) {
                    if (null != packageName && packageName.contains(backKey)) {
                        doBack = true;
                        break;
                    }
                }
                if (doBack) {
                    status = PackageStatus.DO_BACK;
                    log.info("Trigger Back Key Package [" + packageName + "], doing Back ...");
                    DriverUtil.takeScreenShot();
                    DriverUtil.doBack();
                }
            }

            if ((status == PackageStatus.RESTART || runningStates) && restart) {
                DriverUtil.restartApp();
            }
        }
        log.info("Valid Package : " + packageName);
        return status;
    }

    public static void getTabBarElement(String xml, String tabBarXpath) throws Exception {
        if (null == tabBarXpath)
            return ;

        log.info("Tab Bar XPath : " + tabBarXpath);
        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList nodes = (NodeList) xPath.evaluate(tabBarXpath, document, XPathConstants.NODESET);
        int length = nodes.getLength();
        log.info("Tab Bar Nodes Length : " + length);
        if (length == 0) {
            log.info("None Tab Bar is Found");
            return;
        }

        while (length-- >= 0) {
            Node temp = nodes.item(length);
            String nodeXpath = getNodeXpath(temp, false);
            if (null == nodeXpath)
                continue;
            log.info("Found Tab : " + nodeXpath);
        }
    }

}
