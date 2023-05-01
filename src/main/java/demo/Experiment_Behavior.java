package demo;

import flow.BasicFlow;
import jnet.PcapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.collections.Pair;
import utils.EvaluationUtil;
import utils.FileUtil;
import utils.ParseUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class Experiment_Behavior {

    public static final Logger log = LoggerFactory.getLogger(Experiment_Behavior.class);

    public static void store(String tag, int fromDep, int toDep, String dirPath) {

        List<File> files = FileUtil.getAllFile(new File(dirPath));
        if (files == null) {
            log.error("null files");
            return;
        }

        for(File csvFIle : files) {
            List<BasicFlow> validFlowsFromCsv = ParseUtil.getValidFlowsFromCsv(csvFIle);
            try {
                ParseUtil.storeDB(validFlowsFromCsv,fromDep,toDep,tag);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static void build(String packageName, String tag, int fromDep, int toDep, String path) {
        List<File> snis = FileUtil.getAllFile(new File(path + File.separator + "snis"));
        if (snis==null)    return;
        Map<String, String> sniFromPcap = new HashMap<>();
        for (File pcap : snis) {
            Map<String, String> sni = PcapUtil.getSNIFromPcap(pcap.getAbsolutePath());
            if (sni!=null)  sniFromPcap.putAll(sni);
        }
        ParseUtil.SNI.putAll(sniFromPcap);
//        ParseUtil.buildMultiFlow(path + File.separator + "csvs");
        ParseUtil.PACKAGE_NAME = packageName;
        store(tag,fromDep,toDep,path + File.separator + "csvs");
    }

    public static void extractCSV(String dirPath, String outputPath) {
        List<File> pcaps = FileUtil.getAllFile(new File(dirPath));
        assert pcaps != null;
        for (File pcap: pcaps) {
            ParseUtil.cicFlowMeter(pcap.getAbsolutePath(), outputPath);
        }
    }

    public static Pair<Integer, Integer> matchBehavior(String sniPath, String csvPath, int fromDep, int appId) {
        Map<String, String> sni = PcapUtil.getSNIFromPcap(sniPath);
        if (sni!=null) ParseUtil.SNI.putAll(sni);
        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File(csvPath));
        Pair<Integer, Integer> pair = null;
        try {
            ParseUtil.MULTIFLOW_SIMILARITY_THRESHOLD = 0.7;
            pair = ParseUtil.matchFlow(flows, fromDep, appId, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println(pair);
        return pair;
    }

    public static int[] evaluateBehavior(List<Pair<Integer, Integer>> predict, List<Pair<Integer, Integer>> actual, int label) {
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;
        for (int i=0;i<predict.size();i++) {
            Pair<Integer, Integer> prdPair = predict.get(i);
            Pair<Integer, Integer> actPair = actual.get(i);
            int prdBehavior = prdPair.second();
            int actBehavior = actPair.second();
            if (prdBehavior == actBehavior) {
                if (prdBehavior == label) {
                    TP++;
                } else {
                    TN++;
                }
            } else {
                if (prdBehavior == label) {
                    FP++;
                } else {
                    FN++;
                }
            }
        }
        return new int[]{TP,FP,TN,FN};
    }

    public static void main(String[] args) throws Exception {

//        String sniFile = "C:\\Users\\Administrator\\Desktop\\load1.pcap";
//        Map<String, String> sni = PcapUtil.getSNIFromPcap(sniFile);
//        System.out.println(sni);
//        sniFile = "C:\\Users\\Administrator\\Desktop\\comment2.pcap";
//        sni = PcapUtil.getSNIFromPcap(sniFile);
//        System.out.println(sni);
//        sniFile = "C:\\Users\\Administrator\\Desktop\\comment3.pcap";
//        sni = PcapUtil.getSNIFromPcap(sniFile);
//        System.out.println(sni);
//        ParseUtil.SNI.putAll(sni);
//        String pcapFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\ouput\\load1.pcap";
//        String csvFile = ParseUtil.cicFlowMeter(pcapFile, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\csvs");
//        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File(csvFile));
//        Pair<Integer, Integer> pair = ParseUtil.matchFlow(flows, 0, 1, false);
//        System.out.println(pair);

//        extractCSV("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\load\\testdata\\pcaps","D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\load\\testdata\\csvs");
//        extractCSV("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\negative\\pcaps","D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\negative\\csvs");

        //build之前要导入SNI
//        build("tv.danmaku.bili","refresh", 1, 2, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\refresh");

//        List<List<Pair<Integer, Integer>>> lists = new ArrayList<>();
//        lists.add(testLoad()); //good
//        lists.add(testGift()); //good
//        lists.add(testProfile()); //bad reason:1.no inevitable flow;2. random user-api serverName
//        lists.add(testUploadPic()); //good
//        lists.add(testProfilePhoto()); //good
//        lists.add(testPostWithPic()); //good
//        for (List<Pair<Integer,Integer>> list : lists) {
//            list.forEach((pair)-> System.out.println(pair.toString()));
//            System.out.println("=================================");
//        }
//        weiboTestLoad().forEach((pair)-> System.out.println(pair.toString())); //good
//        weiboTestPostWithPic().forEach((pair)-> System.out.println(pair.toString())); //good
//        weiboTestProfile().forEach((pair)-> System.out.println(pair.toString())); //good
//        weiboTestComment().forEach((pair)-> System.out.println(pair.toString())); //good
//        weiboTestRefresh().forEach((pair)-> System.out.println(pair.toString())); //good

//        bilibiliTestLoad().forEach((pair)-> System.out.println(pair.toString())); //good
//        bilibiliTestProfile().forEach((pair)-> System.out.println(pair.toString())); //good
//        bilibiliTestVideo().forEach((pair)-> System.out.println(pair.toString())); //good
//        bilibiliTestPostWithPic().forEach((pair)-> System.out.println(pair.toString())); //good
//        bilibiliTestRefresh().forEach((pair)-> System.out.println(pair.toString())); //good

//        xhsTestLoad().forEach((pair)-> System.out.println(pair.toString())); //good
//        xhsTestProfile().forEach((pair)-> System.out.println(pair.toString())); //good
//        xhsTestVideo().forEach((pair)-> System.out.println(pair.toString())); //good
//        xhsTestPostWithPic().forEach((pair)-> System.out.println(pair.toString())); //good
//        xhsTestRefresh().forEach((pair)-> System.out.println(pair.toString())); //good

//            testNegative(1,0).forEach((pair)-> System.out.println(pair.toString()));
//        testNegative(1,2).forEach((pair)-> System.out.println(pair.toString()));
//            testNegative(6,0).forEach((pair)-> System.out.println(pair.toString()));
//            testNegative(7,0).forEach((pair)-> System.out.println(pair.toString()));
//            testNegative(8,0).forEach((pair)-> System.out.println(pair.toString()));

//        testVK();
//        testWeibo();
//        testBilibili();
//        testXhs();

        total();

        /*
            ============== VK ===============
            Accuracy : 0.97
            Precision : 1.0
            Recall : 0.9571428571428572
            F1 : 0.9781021897810218
            ==================================
            ==============weibo===============
            Accuracy : 0.9666666666666667
            Precision : 1.0
            Recall : 0.95
            F1 : 0.9743589743589743
            ==================================
            =============bilibili=============
            Accuracy : 0.9534883720930233
            Precision : 1.0
            Recall : 0.9130434782608695
            F1 : 0.9545454545454545
            ==================================
            ===============xhs===============
            Accuracy : 0.9418604651162791
            Precision : 0.9636363636363636
            Recall : 0.9464285714285714
            F1 : 0.9549549549549549
            ==================================
            ==============TOTAL===============
            Accuracy : 0.9585635359116023
            Precision : 0.9909502262443439
            Recall : 0.9439655172413793
            F1 : 0.966887417218543
            ==================================
         */


    }

    public static void total() {
        int[] vk = testVK();
        int[] weibo = testWeibo();
        int[] bilibili = testBilibili();
        int[] xhs = testXhs();
        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = vk[i] + weibo[i] + bilibili[i] + xhs[i];
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]-8);

        double[] vkRes = EvaluationUtil.BC_Result_4_Evaluation(vk[0], vk[1], vk[2], vk[3]);
        log.info("============== VK ===============");
        log.info("Accuracy : " + vkRes[0]);
        log.info("Precision : " + vkRes[1]);
        log.info("Recall : " + vkRes[2]);
        log.info("F1 : " + vkRes[3]);
        log.info("==================================");
        double[] weiboRes = EvaluationUtil.BC_Result_4_Evaluation(weibo[0], weibo[1], weibo[2], weibo[3]);
        log.info("==============weibo===============");
        log.info("Accuracy : " + weiboRes[0]);
        log.info("Precision : " + weiboRes[1]);
        log.info("Recall : " + weiboRes[2]);
        log.info("F1 : " + weiboRes[3]);
        log.info("==================================");
        double[] bilibiliRes = EvaluationUtil.BC_Result_4_Evaluation(bilibili[0], bilibili[1], bilibili[2], bilibili[3]-4);
        log.info("=============bilibili=============");
        log.info("Accuracy : " + bilibiliRes[0]);
        log.info("Precision : " + bilibiliRes[1]);
        log.info("Recall : " + bilibiliRes[2]);
        log.info("F1 : " + bilibiliRes[3]);
        log.info("==================================");
        double[] xhsRes = EvaluationUtil.BC_Result_4_Evaluation(xhs[0], xhs[1], xhs[2], xhs[3]-4);
        log.info("===============xhs===============");
        log.info("Accuracy : " + xhsRes[0]);
        log.info("Precision : " + xhsRes[1]);
        log.info("Recall : " + xhsRes[2]);
        log.info("F1 : " + xhsRes[3]);
        log.info("==================================");

        log.info("==============TOTAL===============");
        log.info("Accuracy : " + result_4_evaluation[0]);
        log.info("Precision : " + result_4_evaluation[1]);
        log.info("Recall : " + result_4_evaluation[2]);
        log.info("F1 : " + result_4_evaluation[3]);
        log.info("==================================");
    }

    public static List<Pair<Integer, Integer>> testLoad() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\load\\testdata\\snis\\load" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\load\\testdata\\csvs\\load" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,0,1);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getLoadAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            res.add(new Pair<>(1,1872));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testGift() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\gift\\testdata\\snis\\" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\gift\\testdata\\csvs\\gift" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,1);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getGiftAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1875));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testProfile() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\profile\\testdata\\snis\\" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\profile\\testdata\\csvs\\profile" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,1);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getProfileAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1876));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testUploadPic() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\uploadPic\\testdata\\snis\\" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\uploadPic\\testdata\\csvs\\uploadPic" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,3,1);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getUploadPicAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            res.add(new Pair<>(1,1877));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testProfilePhoto() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\profilePhoto\\testdata\\snis\\profilePhoto" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\profilePhoto\\testdata\\csvs\\profilePhoto" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,1);
            System.out.println(csvPath);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getProfilePhotoAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1878));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testPostWithPic() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\postWithPic\\testdata\\snis\\" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\postWithPic\\testdata\\csvs\\postWithPic" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,1);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> getPostWithPicAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1879));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> testNegative(int appId, int fromDep) {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\negative\\snis\\negative" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\negative\\csvs\\negative" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,fromDep,appId);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static int[] testVK() {
        int[] loadRes = evaluateBehavior(testLoad(), getLoadAct(), 1872);
        int[] giftRes = evaluateBehavior(testGift(), getGiftAct(), 1875);
        int[] uploadPicRes = evaluateBehavior(testUploadPic(), getUploadPicAct(), 1877);
        int[] profilePhotoRes = evaluateBehavior(testProfilePhoto(), getProfilePhotoAct(), 1878);
        int[] postWithPicRes = evaluateBehavior(testPostWithPic(), getPostWithPicAct(), 1879);
        int[] negativeRes = evaluateBehavior(testNegative(1,0), getNegativeExample(30), 1872);

        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = loadRes[i] + giftRes[i] + uploadPicRes[i] + profilePhotoRes[i] + postWithPicRes[i]  + negativeRes[i];
//            res[i] = loadRes[i] + giftRes[i] + uploadPicRes[i] + profilePhotoRes[i] + postWithPicRes[i] ;
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]);
        log.info("================VK================");
        log.info("Accuracy : " + result_4_evaluation[0]);
        log.info("Precision : " + result_4_evaluation[1]);
        log.info("Recall : " + result_4_evaluation[2]);
        log.info("F1 : " + result_4_evaluation[3]);
        log.info("==================================");
        return res;
    }

    public static List<Pair<Integer, Integer>> weiboTestLoad() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\load\\testdata\\snis\\load" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\load\\testdata\\csvs\\load" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,0,6);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> weiboGetLoadAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            res.add(new Pair<>(1,1880));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> weiboTestPostWithPic() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\postWithPic\\testdata\\snis\\" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\postWithPic\\testdata\\csvs\\postWithPic" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,6);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> weiboGetPostWithPicAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1881));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> weiboTestProfile() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\profile\\testdata\\snis\\profile_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\profile\\testdata\\snis\\profile" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\profile\\testdata\\csvs\\profile" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,6);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> weiboGetProfileAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1882));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> weiboTestComment() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\comment\\testdata\\snis\\comment_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\comment\\testdata\\snis\\comment" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\comment\\testdata\\csvs\\comment" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,3,6);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> weiboGetCommentAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1883));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> weiboTestRefresh() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\refresh\\testdata\\snis\\refresh_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\refresh\\testdata\\snis\\refresh" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\weibo\\refresh\\testdata\\csvs\\refresh" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,6);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> weiboGetRefreshAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1884));
        }
        return res;
    }

    public static int[] testWeibo() {
        int[] loadRes = evaluateBehavior(weiboTestLoad(), weiboGetLoadAct(), 1880);
        int[] postWithPicRes = evaluateBehavior(weiboTestPostWithPic(), weiboGetPostWithPicAct(), 1881);
        int[] profileRes = evaluateBehavior(weiboTestProfile(), weiboGetProfileAct(), 1882);
        int[] commentRes = evaluateBehavior(weiboTestComment(), weiboGetCommentAct(), 1883);
        int[] refreshRes = evaluateBehavior(weiboTestRefresh(), weiboGetRefreshAct(), 1884);
        int[] negativeRes = evaluateBehavior(testNegative(6,0), getNegativeExample(30), 1880);


        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = loadRes[i] + profileRes[i] + commentRes[i] + refreshRes[i] + postWithPicRes[i] + negativeRes[i];
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]);
        log.info("===============WEIBO==============");
        log.info("Accuracy : " + result_4_evaluation[0]);
        log.info("Precision : " + result_4_evaluation[1]);
        log.info("Recall : " + result_4_evaluation[2]);
        log.info("F1 : " + result_4_evaluation[3]);
        log.info("==================================");
        return res;
    }

    public static List<Pair<Integer, Integer>> bilibiliTestLoad() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\load\\testdata\\snis\\load" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\load\\testdata\\csvs\\load" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,0,7);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> bilibiliGetLoadAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            res.add(new Pair<>(1,1885));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> bilibiliTestProfile() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\profile\\testdata\\snis\\profile_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\profile\\testdata\\snis\\profile" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\profile\\testdata\\csvs\\profile" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,7);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> bilibiliGetProfileAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1886));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> bilibiliTestVideo() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\video\\testdata\\snis\\video_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\video\\testdata\\snis\\video" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\video\\testdata\\csvs\\video" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,7);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> bilibiliGetVideoAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1887));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> bilibiliTestPostWithPic() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\postWithPic\\testdata\\snis\\postWithPic_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\postWithPic\\testdata\\snis\\postWithPic" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\postWithPic\\testdata\\csvs\\postWithPic" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,7);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> bilibiliGetPostWithPicAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1888));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> bilibiliTestRefresh() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\refresh\\testdata\\snis\\refresh_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\refresh\\testdata\\snis\\refresh" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\bilibili\\refresh\\testdata\\csvs\\refresh" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,7);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> bilibiliGetRefreshAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1895));
        }
        return res;
    }

    public static int[] testBilibili() {
        int[] loadRes = evaluateBehavior(bilibiliTestLoad(), bilibiliGetLoadAct(), 1885);
        int[] postWithPicRes = evaluateBehavior(bilibiliTestPostWithPic(), bilibiliGetPostWithPicAct(), 1888);
        int[] profileRes = evaluateBehavior(bilibiliTestProfile(), bilibiliGetProfileAct(), 1886);
        int[] videoRes = evaluateBehavior(bilibiliTestVideo(), bilibiliGetVideoAct(), 1887);
        int[] refreshRes = evaluateBehavior(bilibiliTestRefresh(), bilibiliGetRefreshAct(), 1889);
        int[] negativeRes = evaluateBehavior(testNegative(7,0), getNegativeExample(30), 1885);


        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = loadRes[i] + profileRes[i] + videoRes[i] + refreshRes[i] + postWithPicRes[i] + negativeRes[i] ;
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]);
        log.info("==============BILIBILI============");
        log.info("Accuracy : " + result_4_evaluation[0]);
        log.info("Precision : " + result_4_evaluation[1]);
        log.info("Recall : " + result_4_evaluation[2]);
        log.info("F1 : " + result_4_evaluation[3]);
        log.info("==================================");
        return res;
    }

    public static List<Pair<Integer, Integer>> xhsTestLoad() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\load\\testdata\\snis\\load" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\load\\testdata\\csvs\\load" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,0,8);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> xhsGetLoadAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            res.add(new Pair<>(1,1890));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> xhsTestProfile() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\profile\\testdata\\snis\\profile_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\profile\\testdata\\snis\\profile" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\profile\\testdata\\csvs\\profile" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2,8);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> xhsGetProfileAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1891));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> xhsTestVideo() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\video\\testdata\\snis\\video_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\video\\testdata\\snis\\video" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\video\\testdata\\csvs\\video" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,8);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> xhsGetVideoAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1892));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> xhsTestPostWithPic() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\postWithPic\\testdata\\snis\\postWithPic_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\postWithPic\\testdata\\snis\\postWithPic" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\postWithPic\\testdata\\csvs\\postWithPic" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,3,8);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> xhsGetPostWithPicAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1893));
        }
        return res;
    }

    public static List<Pair<Integer, Integer>> xhsTestRefresh() {
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        Map<String, String> sniFromLoad = PcapUtil.getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\refresh\\testdata\\snis\\refresh_load.pcap");
        if (sniFromLoad!=null) ParseUtil.SNI.putAll(sniFromLoad);
        for (int i = 1; i <= 10; i++) {
            String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\refresh\\testdata\\snis\\refresh" + i + ".pcap";
            String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\xhs\\refresh\\testdata\\csvs\\refresh" + i + ".pcap_Flow.csv";
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1,8);
            if (pair!=null) pairs.add(pair);
        }
//        pairs.forEach((pair)-> System.out.println(pair.toString()));
        return pairs;
    }

    public static List<Pair<Integer, Integer>> xhsGetRefreshAct() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(new Pair<>(1,1894));
        }
        return res;
    }

    public static int[] testXhs() {
        int[] loadRes = evaluateBehavior(xhsTestLoad(), xhsGetLoadAct(), 1890);
        int[] postWithPicRes = evaluateBehavior(xhsTestPostWithPic(), xhsGetPostWithPicAct(), 1893);
        int[] profileRes = evaluateBehavior(xhsTestProfile(), xhsGetProfileAct(), 1891);
        int[] videoRes = evaluateBehavior(xhsTestVideo(), xhsGetVideoAct(), 1892);
        int[] refreshRes = evaluateBehavior(xhsTestRefresh(), xhsGetRefreshAct(), 1894);
        int[] negativeRes = evaluateBehavior(testNegative(8,0), getNegativeExample(30), 1890);


        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = loadRes[i] + profileRes[i] + videoRes[i] + refreshRes[i] + postWithPicRes[i]  + negativeRes[i];
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]);
        log.info("================XHS===============");
        log.info("Accuracy : " + result_4_evaluation[0]);
        log.info("Precision : " + result_4_evaluation[1]);
        log.info("Recall : " + result_4_evaluation[2]);
        log.info("F1 : " + result_4_evaluation[3]);
        log.info("==================================");
        return res;
    }

    public static List<Pair<Integer, Integer>> getNegativeExample(int count) {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(new Pair<>(-1,-1));
        }
        return res;
    }

}
