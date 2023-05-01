package demo;

import enums.MatchStrategy;
import flow.BasicFlow;
import jnet.PcapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.collections.Pair;
import utils.DBUtil;
import utils.FileUtil;
import utils.ParseUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Experiment_App {

    public static final Logger log = LoggerFactory.getLogger(Experiment_App.class);

    public static final double SUM_THRESHOLD = 0.6;

    public static Pair<Integer, LinkedList<Integer>> matchApp(String sniPath, List<File> csvPath, MatchStrategy strategy) throws Exception {
        if (sniPath!=null && !sniPath.isEmpty()) {
            List<File> snis = FileUtil.getAllFile(new File(sniPath));
            if (snis==null) return null;
            for (File sniFile : snis) {
                Map<String, String> sniFromPcap = PcapUtil.getSNIFromPcap(sniFile.getAbsolutePath());
                if (sniFromPcap!=null) ParseUtil.SNI.putAll(sniFromPcap);
            }
        }

        switch (strategy) {
            case ERGODIC:
                //遍历策略 针对指纹库当中的每个APP,遍历计算
                return ergodic(csvPath);
            case GREEDY:
                //贪心策略 每层只针对局部最优的路径进行匹配
               return greedy(csvPath);
            case TOP_K:
                //top-k策略 针对当前层结果最优的前几条路径进行匹配,top10 - top5 - top3 - top1 - top1
                return topK(csvPath);
            default:
                log.error("Unacceptable Strategy.");
                return null;
        }
    }

    public static Pair<Integer, LinkedList<Integer>> ergodic(List<File> csvPath) throws Exception {
        List<Integer> apps;
        try {
            apps = DBUtil.getAppsFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Fail to get Apps from DB.");
            return null;
        }

        ParseUtil.MULTIFLOW_SIMILARITY_THRESHOLD = 0.1;

        double maxSim=0;
        int maxApp = 0;
        LinkedList<Integer> maxBehaviors = null;
        for (int appId : apps) {
            int currentDepth = 0;

            Pair<LinkedList<Integer>, Double> pair = recur_ergodic(0, csvPath, currentDepth, appId);
            log.info("App:" + appId + " " + pair.second() + " [" + pair.first() + "]");
            if (pair.second()>maxSim) {
                maxSim = pair.second();
                maxApp = appId;
                maxBehaviors = pair.first();
            }

        }
        if (maxSim / csvPath.size() >= SUM_THRESHOLD) return new Pair<>(maxApp,maxBehaviors);
        else return new Pair<>(-1,null);
    }

    private static Pair<LinkedList<Integer>,Double> recur_ergodic(int index, List<File> csvPath, int currentDepth, int appId) throws Exception {
        if (index>=csvPath.size()) {
            log.info("recurDebug return out of index");
            return new Pair<>(new LinkedList<>(),0.0);
        }

        File csv = csvPath.get(index);
        List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(csv);
        List<Pair<Integer, Pair<Integer, Double>>> pairList = ParseUtil.matchBehavior_ergodic(flows, currentDepth, appId, false);

        double maxSim = 0;
        LinkedList<Integer> maxBehavior = null;
        for (Pair<Integer, Pair<Integer, Double>> pair : pairList) {
            int toDepth = pair.first();
            int behavior = pair.second().first();
            double sim = pair.second().second();
            Pair<LinkedList<Integer>, Double> rPair = recur_ergodic(index + 1, csvPath, toDepth, appId);
            LinkedList<Integer> behaviors = rPair.first();
            double rSim = rPair.second();

            double temp = sim + rSim;
            if (temp>=maxSim) {
                maxSim = temp;
                maxBehavior = new LinkedList<>(behaviors);
                maxBehavior.add(behavior);
            }
        }
        log.info("recurDebug:" + (maxBehavior==null?"null":maxBehavior.toString()) + " " + maxSim);
        return new Pair<>(maxBehavior, maxSim);
    }

    public static Pair<Integer, LinkedList<Integer>> greedy(List<File> csvPath) throws Exception {
        List<Integer> apps;
        try {
            apps = DBUtil.getAppsFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Fail to get Apps from DB.");
            return null;
        }
        Map<Integer,Pair<LinkedList<Integer>,Double>> res = new HashMap<>();
        ParseUtil.MULTIFLOW_SIMILARITY_THRESHOLD = 0.1;
        for (int appId : apps) {
            int currentDepth = 0;
            double sum = 0;
            int count = 0;
            LinkedList<Integer> behaviors = new LinkedList<>();
            for (File csv : csvPath) {
                List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(csv);
                Pair<Integer, Pair<Integer, Double>> pair = ParseUtil.matchBehavior_greedy(flows, currentDepth, appId, false);
                int toDepth = pair.first();
                if (toDepth<0)  continue;
                behaviors.add(pair.second().first());
                currentDepth = toDepth;
                sum+=pair.second().second();
                count++;
            }
            res.put(appId,new Pair<>(behaviors,sum/count));
        }

        int temp = -1;
        double max = 0.0;
        for (int appId: res.keySet()) {
            Pair<LinkedList<Integer>, Double> pair = res.get(appId);
            if (pair.second()>max) {
                temp = appId;
                max = pair.second();
            }
        }
        if (max>SUM_THRESHOLD)  return new Pair<>(temp,res.get(temp).first());
        else return new Pair<>(-1,null);
    }

    public static Pair<Integer, LinkedList<Integer>> topK(List<File> csvPath) {
        return null;
    }

    public static void testErgodic(String path) throws Exception {
        String csvPath = path + File.separator + "csvs";
        String sniPath = path + File.separator + "snis";
        List<File> csvs = FileUtil.getAllFile(new File(csvPath));
        Pair<Integer, LinkedList<Integer>> pair = matchApp(sniPath, csvs, MatchStrategy.ERGODIC);
        if (pair!=null) log.info(pair.toString());
    }

    public static void testGreedy(String path) throws Exception {
        String csvPath = path + File.separator + "csvs";
        String sniPath = path + File.separator + "snis";
        List<File> csvs = FileUtil.getAllFile(new File(csvPath));
        Pair<Integer, LinkedList<Integer>> pair = matchApp(sniPath, csvs, MatchStrategy.GREEDY);
        if (pair!=null) log.info(pair.toString());
    }

    public static void main(String[] args) throws Exception {
        String path = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\testData\\4\\vk1";
        long ergodic_start = System.nanoTime();
        testErgodic(path);
        long ergodic_end = System.nanoTime();

        Thread.sleep(1);

        long greedy_start = System.nanoTime();
        testGreedy(path);
        long greedy_end = System.nanoTime();

        log.info("Ergodic : " + (ergodic_end-ergodic_start));//more comprehensive
        log.info("Greedy  : " + (greedy_end-greedy_start));//faster
    }

}
