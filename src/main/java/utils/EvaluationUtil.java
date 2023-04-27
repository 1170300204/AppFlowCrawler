package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.collections.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EvaluationUtil {

    public static final Logger log = LoggerFactory.getLogger(EvaluationUtil.class);

    public static double[] BC_Result_4_Evaluation(int tp, int fp, int tn, int fn) {
        double accuracy;
        double precision;
        double recall;
        double f1;
        accuracy = (double) (tp + tn) / (tp + fn + tn + fp);
        precision = (double) tp / (tp + fp);
        recall = (double) tp / (tp + fn);
        f1 = 2 * precision * recall / (precision + recall);
//        log.info("==========App Detection==========");
//        log.info("Accuracy : " + accuracy);
//        log.info("Precision : " + precision);
//        log.info("Recall" + recall);
//        log.info("F1 : " + f1);
//        log.info("==================================");
        return new double[]{accuracy,precision,recall,f1};
    }

    public static double[] BC_Result_3_Evaluation(int tp, int fp, int fn) {
        double accuracy;
        double precision;
        double recall;
        double f1;
        accuracy = (double) tp / (tp + fn + fp);
        precision = (double) tp / (tp + fp);
        recall = (double) tp / (tp + fn);
        f1 = 2 * precision * recall / (precision + recall);
//        log.info("========Behavior Detection========");
//        log.info("Accuracy : " + accuracy);
//        log.info("Precision : " + precision);
//        log.info("Recall" + recall);
//        log.info("F1 : " + f1);
//        log.info("==================================");
        return new double[]{accuracy,precision,recall,f1};
    }

    public static void MC_Result_Evaluation() {

    }


}
