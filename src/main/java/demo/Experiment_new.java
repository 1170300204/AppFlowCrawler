package demo;

import flow.BasicFlow;
import utils.ParseUtil;

import java.io.File;
import java.util.List;

public class Experiment_new {
    public static void build(String tag, int fromDep, int toDep, String[] csvFileNames) {
        for(String csvFIle : csvFileNames) {
            List<BasicFlow> validFlowsFromCsv = ParseUtil.getValidFlowsFromCsv(new File(csvFIle));



        }





    }


}
