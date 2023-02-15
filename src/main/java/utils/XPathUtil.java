package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class XPathUtil {

    public static Logger log = LoggerFactory.getLogger(XPathUtil.class);
    public static XPath xPath = XPathFactory.newInstance().newXPath();
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static DocumentBuilder documentBuilder;

    private static long crawlerRunningTime;
    private static long beginTime;
    private static long userLoginInterval;

    public static void initialize(String udid) {
        log.info("initialize XPath module ...");

        crawlerRunningTime = ConfigUtil.getCrawlerRunningTime();
        beginTime = System.currentTimeMillis();
        userLoginInterval = ConfigUtil.getUserLoginInterval();
        userLoginInterval = userLoginInterval <=0 ? 1 : userLoginInterval;

        log.info("The estimated total running time is " + crawlerRunningTime + " min");

        //TODO
    }

    public static String dfsCrawl(String xml, long currentDepth) throws Exception {
        //TODO

        return "";
    }


}
