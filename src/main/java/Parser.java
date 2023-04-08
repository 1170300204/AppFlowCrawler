import flow.BasicFlow;
import jnet.PcapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ParseUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class Parser {

    public static final Logger log = LoggerFactory.getLogger(Parser.class);

    public static void parse(String pkgTime) {
        String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\pcaps\\timestamp.txt";
        String pcapFIle = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\pcaps\\com.vkontakte.android.pcap";
        String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\csvs";
//        String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\sni.txt";

        ParseUtil.setSNI(pcapFIle);
        ParseUtil.extract(timestampFile, pcapFIle, csvPath);
    }

    public static void match(List<String> files, boolean flag){
         //todo 网络多流发掘 输入一个pcap 加密应用识别
        try {
            Set<String> dbSNIs = ParseUtil.getSNIFromDB(1);
            if (dbSNIs == null) return;
            dbSNIs.retainAll(ParseUtil.SNI.values());
            System.out.println(dbSNIs);
            if (dbSNIs.size()==0) {
                log.info("No matching SNI information was found");
                return;
            }
        } catch (Exception e) {
            log.error("Failed to retrieve SNI information");
        }


        String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input";
        int currentDepth = 0;
        for (String file : files) {
            String csvFile = ParseUtil.cicFlowMeter(file, csvPath);
            List<BasicFlow> flows = ParseUtil.getFlowsFromCsv(new File(csvFile));
//            flows.forEach(flow->log.info(flow.getServerHost()));
            int toDepth;
            try {
                toDepth = ParseUtil.match(flows, currentDepth, 1, flag);
                if (toDepth<0) {
                    log.info("Fail to match, try next.");
                    continue;
                }
                currentDepth = toDepth;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void match(List<List<BasicFlow>> input, int appId) {
        //todo 非网络多流发掘 输入一组多流 应用/行为识别
        if (null == input || input.isEmpty()) {
            log.info("Input NULL, Nothing to Match.");
            return;
        }
        int currentDepth = 0;
        try{
            for (List<BasicFlow> flows : input) {
                int temp = currentDepth;
                currentDepth = ParseUtil.match(flows, currentDepth, appId, false);
                if (currentDepth < 0) {
                    log.info("No Matching Multi-Flow Found. Stop at " + input.indexOf(flows) + " [appId : " + appId + " , Depth : " + temp + "] :");
                    flows.forEach(flow -> log.info(flow.toString()));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to match. Please check the error print.");
        }
    }

    public static void main(String[] args) {
        //todo POtype calculate
//        parse("com.vkontakte.android-2023-04-03_15-17-53");
//        parse("com.vkontakte.android-2023-04-03_15-56-07");
//        parse("com.vkontakte.android-2023-04-03_16-00-40");
//        parse("com.vkontakte.android-2023-04-03_16-24-17");
//        parse("com.vkontakte.android-2023-04-03_19-54-56");

//        parse("com.vkontakte.android-2023-04-06_21-28-08");
//        parse("com.vkontakte.android-2023-04-06_22-05-07");
//
//        parse("com.vkontakte.android-2023-04-07_09-32-20");
//        parse("com.vkontakte.android-2023-04-07_09-58-39");
//        parse("com.vkontakte.android-2023-04-07_10-21-59");
//        parse("com.vkontakte.android-2023-04-07_10-37-19");
//
//        parse("com.vkontakte.android-2023-04-07_13-04-17");
//        parse("com.vkontakte.android-2023-04-07_13-32-50");
//        parse("com.vkontakte.android-2023-04-07_14-11-13");
//        parse("com.vkontakte.android-2023-04-07_14-37-23");
//        parse("com.vkontakte.android-2023-04-07_15-00-17");
//
//        parse("com.vkontakte.android-2023-04-07_15-39-49");
//        parse("com.vkontakte.android-2023-04-07_18-25-50");
//        parse("com.vkontakte.android-2023-04-07_18-53-26");
//        parse("com.vkontakte.android-2023-04-07_19-09-22");
//        parse("com.vkontakte.android-2023-04-07_19-39-27");
//
//        parse("com.vkontakte.android-2023-04-07_20-01-27");
//        parse("com.vkontakte.android-2023-04-07_20-39-45");
//        parse("com.vkontakte.android-2023-04-07_21-03-18");
//        parse("com.vkontakte.android-2023-04-07_21-28-31");
//        parse("com.vkontakte.android-2023-04-07_21-51-04");

    }

}
