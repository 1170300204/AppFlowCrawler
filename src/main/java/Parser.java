import flow.BasicFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ParseUtil;

import java.util.List;

public class Parser {

    public static final Logger log = LoggerFactory.getLogger(Parser.class);

    public static void parse(String pkgTime) {
        String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\pcaps\\timestamp.txt";
        String pcapFIle = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\pcaps\\com.vkontakte.android.pcap";
        String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\csvs";
        String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\" + pkgTime + "\\sni.txt";

        ParseUtil.getSNI(pcapFIle, sniPath);
        ParseUtil.extract(timestampFile, pcapFIle, csvPath);
    }

    public static void match(String mPcapFile) {
         //todo 网络多流发掘 输入一个pcap 加密应用识别

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
                currentDepth = ParseUtil.match(flows, currentDepth, appId);
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
        parse("com.vkontakte.android-2023-04-03_15-17-53");
        parse("com.vkontakte.android-2023-04-03_15-56-07");
        parse("com.vkontakte.android-2023-04-03_16-00-40");
        parse("com.vkontakte.android-2023-04-03_16-24-17");

//        parse("");
//        match();
    }

}
