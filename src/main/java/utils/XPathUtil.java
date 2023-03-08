package utils;

import enums.PackageStatus;
import io.appium.java_client.MobileElement;
import jdk.nashorn.internal.runtime.regexp.joni.constants.Traverse;
import org.openqa.selenium.By;
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
    private static final Map<String, Long> clickedActivityMap = new HashMap<>();
    private static final HashMap<String, HashSet<String>> clickedElementMap = new LinkedHashMap<>();
    private static final HashSet<String> mark = new LinkedHashSet<>();
    private static final HashMap<String, Long> pageMap = new HashMap<>();

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
    private static final boolean verticalSwipe = ConfigUtil.getIsEnableVerticalSwipe();
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

    public static String dfsCrawl(String xml, long currentDepth, String previousPageStructure) throws Exception {
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

        String currentPageStructure = getPageStructure(xml, clickXpath);
        if (null == previousPageStructure) {
            previousPageStructure = currentPageStructure;
        }
        String afterPageStructure = currentPageStructure;
        String currentXml = xml;
        if(!runningStates) {
            log.info("Running States Change. Ending the Task ...");
            return currentXml;
        }

        
        //当前的深度增加逻辑有问题，需要在深度增加之前添加判断逻辑
        //TODO
//        if (isSamePage(previousPageStructure, currentPageStructure))
//            currentDepth++;
        if (!pageMap.containsKey(currentPageStructure)) {
            pageMap.put(currentPageStructure, currentDepth);
        } else {
            //如果经过一番操作又回到了之前初始界面等之前经历过的界面，则需要将当前的深度更新
            long pageDepth = pageMap.get(currentPageStructure);
            if (currentDepth < pageDepth) {
                pageMap.put(currentPageStructure,currentDepth);
                log.info("Depth update from " + pageDepth + " to " + currentDepth);
            }
            log.info("Back to Map Page (Depth:" + currentDepth + ") :\n");
            log.info(currentPageStructure);
        }

        if (backKeyTriggerList.size() > 0) {
            for (String backKey: backKeyTriggerList) {
                if (currentXml.contains(backKey)) {
                    log.info("Trigger Back Key [" + backKey + "], Doing Back ...");
                    if (ConfigUtil.getIsEnableScreenShot())
                        DriverUtil.takeScreenShot();
                    doBackIfValid(currentPageStructure);
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
                    doBackIfValid(currentPageStructure);
                    currentXml = DriverUtil.getPageSource();
                    return currentXml;
                }
            }
        }

        DriverUtil.takeScreenShot("dep" + currentDepth);
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
            doBackIfValid(currentPageStructure);
            return currentXml;
        }

        log.info("Current Depth : " + currentDepth);
        if (currentDepth > maxDepth) {
//            runningStates = false;
            log.info("Max Traversal Depth Exceeded [" + currentDepth + " > " + maxDepth + " ], Returning ...");
            currentXml = DriverUtil.getPageSource();
            currentPageStructure = getPageStructure(xml, clickXpath);
            if (!isSamePage(previousPageStructure, currentPageStructure)) {
                doBackIfValid(currentPageStructure);
            }
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
            int nodeIndex = nodes.getLength()-length-1;
            log.info("Element #" + nodeIndex + "------------------------");
//            Node node = nodes.item(length);
            Node node = nodes.item(nodeIndex);
            String nodeXpath = getNodeXpath(node, false);

            if (null == nodeXpath) {
                log.info("Node Xpath NULL, Skipping");
                continue;
            }
            if (blackNodeXpathSize != 0 && nodeXpathBlackSet.contains(nodeXpath)) {
                log.info("Trigger BlackList Xpath Item : " + nodeXpath + ", Skipping ...");
                continue;
            }

            //判断当前元素是否被点击过, 并记录
            if (mark.add(nodeXpath)) {
                for (String item : nodeWhiteList) {
                    if (nodeXpath.contains(item.trim())) {
                        log.info("Node in WhiteList, Not Limited by the number of Clicks");
                        mark.remove(nodeXpath);
                    }
                }

                MobileElement element = DriverUtil.findElementWithoutException(By.xpath(nodeXpath));
                //元素未找到则重新遍历当前页面
                if (null == element) {
                    xpathNotFoundElementList.add(nodeXpath);
                    log.info("Node not Found in Current UI. Stopping Current iteration ...");
                    break;
                }
                //todo 记录按钮点击的前后深度变化,抽象点击元素为一个标识符
                currentXml = clickElement(element, currentXml);
                afterPageStructure = getPageStructure(currentXml,clickXpath);
                //发生了页面变化, 检查完毕后进入更深一层的递归
                if (!isSamePage(currentPageStructure, afterPageStructure) && runningStates) {
                    log.info("======== Page Change From\n");
                    log.info(currentPageStructure);
                    log.info("To\n");
                    log.info(afterPageStructure);
                    log.info("======== New Page UI ========");
                    packageName = getAppName(currentXml);
                    if (PackageStatus.VALID != getPackageStatus(packageName, true)) {
                        currentXml = DriverUtil.getPageSource();
                        //
                        afterPageStructure = getPageStructure(currentXml, clickXpath);
                        break;
                    }
                    //进入子UI遍历
                    dfsCrawl(currentXml, currentDepth+1, currentPageStructure);

                    //从子UI返回
                    if (!runningStates) {
                        break;
                    }
                    currentXml = DriverUtil.getPageSource();
                    packageName = getAppName(currentXml);
                    if (PackageStatus.VALID != getPackageStatus(packageName, false)) {
                        break;
                    }
                    afterPageStructure = getPageStructure(currentXml,clickXpath);

                    if (isSamePage(currentPageStructure, afterPageStructure)) {
                        log.info("Page Not Change after Recursive return");
                    } else {
                        log.info("Page Change after Recursive return, Stop Traversing Current Page");
                        break;
                    }
                }
            } else {
                log.info("The Element has been Clicked : " + nodeXpath + "\n");
            }
        }

        log.info("\n\n======== DONE. RunningStates : " + runningStates + " ========\n\n");

        if (!runningStates) {
            log.info("Current Package Name Changed : " + packageName + ". RunningStates is False, Returning ...");
            //TODO
            return currentXml;
        }

        log.info(length + " nodes left");
        boolean isDoBack = true;
        //此时完了所有的页面元素并且UI也未发生变化, 结束该深度的遍历, 回退到上一层
        if (length < 0 && isSamePage(currentPageStructure, afterPageStructure)) {
            //如果允许滑动操作, 则需要观察滑动之后页面是否发生了变化, 如果发生了变化说明这一层的遍历还没有进行完全
            if (verticalSwipe) {
                log.info("Check : Enable Vertically Swipe");
                DriverUtil.verticallySwipe(false);
                currentXml = DriverUtil.getPageSource();

                currentPageStructure = afterPageStructure;
                afterPageStructure = getPageStructure(currentXml, clickXpath);
                if (!isSamePage(currentPageStructure, afterPageStructure)) {
                    log.info("Page Change after Vertically swipe, Continue Current Depth Traverse ...");
                    isDoBack = false;
                    currentXml = dfsCrawl(currentXml, currentDepth, currentPageStructure);
                } else {
                    log.info("No Page Change after Vertically swipe");
                }
            }
            log.info("All Nodes in Current Page iterated");

            if (isDoBack) {
                if (null != backKeyXpath) {
                    log.info("Getting Back Key ...");
                    MobileElement element = DriverUtil.findElementWithoutException(By.xpath(backKeyXpath));
                    if (null != element) {
                        log.info("Back Key found, Doing Back ...");
                        currentXml = clickElement(element, currentXml);
                        return currentXml;
                    } else {
                        log.info("No Back Key found");
                    }
                }

                //检查当前页面是否含有tab bar
                currentXml = clickTapBarElement(currentXml, tabBarXpath);
                if (null != currentXml) {
                    log.info("Tab Bar Change, Continue Traversing");
                    //TODO 点击tab之后深度是否需要 + 1
                    dfsCrawl(currentXml, currentDepth, currentPageStructure);
                } else {
                    DriverUtil.doBack();
                    DriverUtil.takeScreenShot();

                    currentXml = DriverUtil.getPageSource();
                    packageName = getAppName(currentXml);

                    if (pressBackCount <= 0) {
                        log.info("Do Back Count Zero, Stop Crawling ...");
                        runningStates = false;
                    }
                    if (pressBackCount > 0 && PackageStatus.VALID != getPackageStatus(packageName, false)) {
                        log.info("Do Back Count not Zero(" + pressBackCount + ") and Package Status not Valid, Restarting app ...");
                        DriverUtil.restartApp();
                        pressBackCount--;
                        currentXml = DriverUtil.getPageSource();
                        //重启之后深度归零
                        dfsCrawl(currentXml, 0, null);
                    }
                }
            }
        } else {
            //仍未完成遍历或页面发生了变化，继续当前页面的遍历
            log.info("UI Change. Continue Current Depth(" + currentDepth + ") Traverse ...");
            currentXml = DriverUtil.getPageSource();
            dfsCrawl(currentXml, currentDepth, previousPageStructure);
        }

        log.info("================ Complete current UI Traverse. Depth before return is " + currentDepth +" ================");
        //TODO
        if (!isSamePage(previousPageStructure, currentPageStructure)){
            doBackIfValid(currentPageStructure);
        }
        return currentXml;
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
                pageStrcture.append(nodeXpath).append("\n");
            }
        }
        return pageStrcture.toString();
    }

    public static boolean isSamePage(String page1, String page2) {
        //TODO 页面比较逻辑
        if (null == page1 || null ==page2) {
            log.error(LoggerUtil.getMethodName() + " : Null Page Compare");
            return false;
        }
        boolean res = page1.equals(page2);
//        if (!res && runningStates) {
////            DriverUtil.takeScreenShot();
//            log.info("Page Change : From \n" + page1 + " To \n" + page2);
//        }
        return res;
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
            log.error("Invalid Package Name : " + packageName + "trying Switch App ...");
            DriverUtil.switchApp();
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

    public static String clickElement(MobileElement element, String xml) {
        log.info(LoggerUtil.getMethodName());
        String page;

        try {
            String activityName = DriverUtil.getCurrentActivity();
            Long clickCount = clickedActivityMap.get(activityName);
            if (null == clickCount) {
                clickCount = 1L;
            } else {
                clickCount++;
            }
            clickedActivityMap.put(activityName, clickCount);
            log.info("Click Map Update : " + activityName + " - " + clickCount);

            String tag = element.getText();
            if (!Objects.equals(tag, "")) {
                HashSet<String> clickedElementSet = clickedElementMap.get(activityName);
                if (null != clickedElementSet) {
                    clickedElementSet.add(tag);
                } else {
                    clickedElementMap.put(activityName, new HashSet<>(Collections.singleton(tag)));
                }
            }

            String temp;
            String elementStr = element.toString();
            try {
                List<String> inputClassList = ConfigUtil.getInputClassList();
                List<String> inputTextList = ConfigUtil.getInputTextList();
                int classSize = inputTextList.size();
                for (String elementClass : inputClassList) {
                    temp = elementClass;
                    if (elementStr.contains(elementClass)) {
                        String text = inputTextList.get(DriverUtil.internalNextInt(0, classSize));
//                        element.setValue(text);
                        element.sendKeys(text);
                        log.info("Element " + temp + " set Text : " + text);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Fail to set Text for " + elementStr);
            }

            int x = element.getCenter().getX();
            int y = element.getCenter().getY();

            DriverUtil.takeScreenShot(activityName, x, y);
            element.click();

            log.info("Click " + clickCount + "th, X: "  + x + " ,Y: " + y);

            DriverUtil.sleep(3);
            page = DriverUtil.getPageSource();
            PackageStatus status = getPackageStatus(appName, false);

            if (PackageStatus.VALID != status) {
                page = DriverUtil.getPageSource();
            }
            if (clickCount >= ConfigUtil.getMaxClickCount()) {
                runningStates = false;
            }

//            String temp = "";
//
//            try {
//                String elementStr = element.toString();
//                List<String> inputClassList = ConfigUtil.getInputClassList();
//                List<String> inputTextList = ConfigUtil.getInputTextList();
//                int classSize = inputTextList.size();
//                for (String elementClass : inputClassList) {
//                    temp = elementClass;
//                    if (elementStr.contains(elementClass)) {
//                        String text = inputTextList.get(DriverUtil.internalNextInt(0, classSize));
//                        element.setValue(text);
//                        log.info("Element " + temp + " set Text : " + text);
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Fail to set Text for " + temp);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to Click Element : " + element);
            clickFailureElementList.add(element.toString());
            page = DriverUtil.getPageSource();
        }
        return page;
    }

    public static String clickTapBarElement(String xml, String tabBarXpath) throws Exception {
        log.info(LoggerUtil.getMethodName());
        if (tabBarXpath == null)    return null;

        String ret = null;
        log.info("Current Tab Bar Xpath : " + tabBarXpath);

        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList nodes = (NodeList) xPath.evaluate(tabBarXpath, document, XPathConstants.NODESET);

        int length = nodes.getLength();
        log.info("TarBar Nodes Length : " + length);
        if (length == 0) {
            log.info("No Tab Bar Element Found");
            return null;
        }

        while (--length >= 0) {
            Node temp = nodes.item(length);
            String nodeXpath = getNodeXpath(temp, false);
            if (nodeXpath == null)  continue;
            if (mark.add(nodeXpath)) {
                MobileElement element = DriverUtil.findElementWithoutException(By.xpath(nodeXpath));
                if (null == element) {
                    log.info("Tab Bar Element : " + nodeXpath + " Not Found. Breaking ...");
                    break;
                }
                log.info("Tab Bar Element : " + nodeXpath + " Found. Clicking ...");
                ret = clickElement(element, xml);
                DriverUtil.takeScreenShot("tab" + length);
                break;
            }
        }
        if (length == 0) {
            log.info("All Tab Bar Elements iterated");
        }
        return ret;
    }

    private static void doBackIfValid(String currentPageStructure) {
        if (!pageMap.containsKey(currentPageStructure)) {
            log.error("DoBack Page Map Error : CurrentPage not in Page Map");
            DriverUtil.doBack();
        } else if (pageMap.get(currentPageStructure) > 0) {
            DriverUtil.doBack();
        }
    }

    //调试用信息输出
    public static void shutDownInfoPrint() {
        log.info("Shut Down Data Check --------");
        log.info("Page map:");
//        log.info(pageMap.toString());
        HashSet<Long> values = new HashSet<>(pageMap.values());
        for (long i:values) {
            log.info("Depth " + i);
            for (String key:pageMap.keySet()) {
                if (i == pageMap.get(key))
                    log.info(key);
            }
            log.info("");
        }
        log.info("Clicked Activity Map:");
        log.info(clickedActivityMap.toString());
        log.info("-----------------------------");
    }

}
