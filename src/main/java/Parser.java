import flow.BasicFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ParseUtil;

import java.util.List;

public class Parser {

    public static final Logger log = LoggerFactory.getLogger(Parser.class);

    public static void parse() {
        String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\pcaps\\timestamp.txt";
        String pcapFIle = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\pcaps\\com.vkontakte.android.pcap";
        String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\csvs";
        String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\sni.txt";

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

    }

}
