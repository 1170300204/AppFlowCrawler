import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.collections.Pair;
import utils.EvaluationUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Evaluator {

    public static final Logger log = LoggerFactory.getLogger(Evaluator.class);

    //二分类,
    //label 为制定应用的ID
    public static void BC_Result(List<Pair<Integer, LinkedList<Integer>>> predicted, List<Pair<Integer, LinkedList<Integer>>> actual, int label) {
        if (predicted.isEmpty() || actual.isEmpty() || predicted.size() != actual.size())    return;
        int appTP = 0;
        int appFP = 0;
        int appTN = 0;
        int appFN = 0;

        int count = 0;

        double behaviorAccuracy = 0;
        double behaviorPrecision = 0;
        double behaviorRecall = 0;
        double behaviorF1 = 0;

        for (int i = 0; i < predicted.size(); i++) {

            Pair<Integer, LinkedList<Integer>> prdPair = predicted.get(i);
            int prdApp = prdPair.first();
            LinkedList<Integer> prdBehaviorSequence = prdPair.second();

            Pair<Integer, LinkedList<Integer>> actPair = actual.get(i);
            int actApp = actPair.first();
            LinkedList<Integer> actBehaviorSequence = actPair.second();

            if (prdApp == actApp) {
                if (prdApp == label) {
                    appTP++;
                } else {
                    appTN++;
                }
            } else {
                if (prdApp == label) {
                    appFP++;
                } else {
                    appFN++;
                }
            }

            int behaviorTP;//表示模型检测到的行为序列与实际行为序列匹配的数量
            int behaviorFP;//表示模型检测到的行为序列与实际行为序列不匹配的数量
            int behaviorFN;// 表示实际行为序列中未被检测到的序列数量

            List<Integer> tpList = new ArrayList<>(prdBehaviorSequence);
            tpList.retainAll(actBehaviorSequence);
            behaviorTP = tpList.size();

            List<Integer> fpList = new ArrayList<>(prdBehaviorSequence);
            fpList.removeAll(actBehaviorSequence);
            behaviorFP = fpList.size();

            List<Integer> fnList = new ArrayList<>(actBehaviorSequence);
            fnList.removeAll(prdBehaviorSequence);
            behaviorFN = fnList.size();

            double[] indicators = EvaluationUtil.BC_Result_Behavior_Evaluation(behaviorTP, behaviorFP, behaviorFN);
            if (indicators.length!=4) {
                log.error("Evaluation indicators count Error. Excepted : " + 4 + " , actual : " + indicators.length);
                return;
            }
            behaviorAccuracy += indicators[0];
            behaviorPrecision += indicators[1];
            behaviorRecall += indicators[2];
            behaviorF1 += indicators[3];
            count ++;
        }

        double[] appIndicators = EvaluationUtil.BC_Result_App_Evaluation(appTP, appFP, appTN, appFN);
        if (appIndicators.length!=4) {
            log.error("App Evaluation indicators count Error. Excepted : " + 4 + " , actual : " + appIndicators.length);
            return;
        }
        log.info("\nFinal Evaluation----------------------------------------------");
        log.info("========App Detection========");
        log.info("Accuracy : " + appIndicators[0]);
        log.info("Precision : " + appIndicators[1]);
        log.info("Recall" + appIndicators[2]);
        log.info("F1 : " + appIndicators[3]);
        log.info("==================================");
        log.info("========Behavior Detection========");
        log.info("Accuracy : " + behaviorAccuracy/count);
        log.info("Precision : " + behaviorPrecision/count);
        log.info("Recall" + behaviorRecall/count);
        log.info("F1 : " + behaviorF1/count);
        log.info("==================================");
    }

    public static void main(String[] args) {
        List<Pair<Integer, LinkedList<Integer>>> predicted;
        List<Pair<Integer, LinkedList<Integer>>> actual;




    }


}
