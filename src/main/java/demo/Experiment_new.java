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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experiment_new {

    public static final Logger log = LoggerFactory.getLogger(Experiment_new.class);

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

    public static Pair<Integer, Integer> matchBehavior(String sniPath, String csvPath, int fromDep) {
        Map<String, String> sni = PcapUtil.getSNIFromPcap(sniPath);
        if (sni!=null) ParseUtil.SNI.putAll(sni);
        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File(csvPath));
        Pair<Integer, Integer> pair = null;
        try {
            ParseUtil.MULTIFLOW_SIMILARITY_THRESHOLD = 0.70;
            pair = ParseUtil.matchFlow(flows, fromDep, 1, false);
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
        return new int[]{TP,TN,FP,FN};
    }

    public static void main(String[] args) throws Exception {
////        build("com.vkontakte.android","uploadPic", 0, 1, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\uploadPic");
//        String pcapFile = "C:\\Users\\Administrator\\Desktop\\uploadPic_test_1.pcap";
//        Map<String, String> sni = PcapUtil.getSNIFromPcap(pcapFile);
//        ParseUtil.SNI.putAll(sni);
//        String csvFile = ParseUtil.cicFlowMeter(pcapFile, "C:\\Users\\Administrator\\Desktop");
//        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File(csvFile));
////        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File("C:\\Users\\Administrator\\Desktop\\uploadPic_test_1.pcap_Flow.csv"));
//        Pair<Integer, Integer> pair = ParseUtil.matchFlow(flows, 0, 1, false);
//        System.out.println(pair);

//        String sniFile = "C:\\Users\\Administrator\\Desktop\\post1.pcap";
//        Map<String, String> sni = PcapUtil.getSNIFromPcap(sniFile);
//        System.out.println(sni);
//        ParseUtil.SNI.putAll(sni);
//        String pcapFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\ouput\\load1.pcap";
//        String csvFile = ParseUtil.cicFlowMeter(pcapFile, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\temp\\csvs");
//        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(new File(csvFile));
//        Pair<Integer, Integer> pair = ParseUtil.matchFlow(flows, 0, 1, false);
//        System.out.println(pair);

//        extractCSV("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\postWithPic\\pcaps","D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\postWithPic\\csvs");

        //build之前要导入SNI
//        build("com.vkontakte.android","postWithPic", 1, 2, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\3\\vk\\postWithPic");

        List<List<Pair<Integer, Integer>>> lists = new ArrayList<>();
//        lists.add(testLoad()); //good
//        lists.add(testGift()); //good
//        lists.add(testProfile()); //bad reason:1.no inevitable flow;2. random user-api serverName
//        lists.add(testUploadPic()); //good
//        lists.add(testProfilePhoto()); //good
//        lists.add(testPostWithPic()); //good
        for (List<Pair<Integer,Integer>> list : lists) {
            list.forEach((pair)-> System.out.println(pair.toString()));
            System.out.println("=================================");
        }

        int[] loadRes = evaluateBehavior(testLoad(), getLoadAct(), 1872);
        int[] giftRes = evaluateBehavior(testGift(), getGiftAct(), 1875);
        int[] uploadPicRes = evaluateBehavior(testUploadPic(), getUploadPicAct(), 1877);
        int[] profilePhotoRes = evaluateBehavior(testProfilePhoto(), getProfilePhotoAct(), 1878);
        int[] postWithPicRes = evaluateBehavior(testPostWithPic(), getPostWithPicAct(), 1879);

        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            res[i] = loadRes[i] + giftRes[i] + uploadPicRes[i] + profilePhotoRes[i] + postWithPicRes[i];
        }
        double[] result_4_evaluation = EvaluationUtil.BC_Result_4_Evaluation(res[0], res[1], res[2], res[3]);
        log.info("==================================");
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,0);
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2);
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1);
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,3);
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,2);
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
            Pair<Integer, Integer> pair = matchBehavior(sniPath, csvPath,1);
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
}
