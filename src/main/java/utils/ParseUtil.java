package utils;

import com.csvreader.CsvReader;
import flow.BasicFlow;
import flow.FlowFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParseUtil {
    public static Logger log = LoggerFactory.getLogger(ParseUtil.class);

    public static List<BasicFlow> getFlowsFromCsv(String csvFileName) {
        List<BasicFlow> flows = null;
        CsvReader csvReader = null;
        try {
            flows = new ArrayList<>();
             csvReader = new CsvReader(csvFileName, ',', Charset.forName("GBK"));
            String []flowData;
            csvReader.readRecord();
            while (csvReader.readRecord()) {
                flowData = csvReader.getValues();
                BasicFlow flow = new BasicFlow();
                flow.setSrcIp(flowData[1]);
                flow.setSrcPort(Integer.parseInt(flowData[2]));
                flow.setDstIp(flowData[3]);
                flow.setDstPort(Integer.parseInt(flowData[4]));
                flow.setProtocol(Integer.parseInt(flowData[5]));
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
                flow.setTimestamp(format.parse(flowData[6]));
                FlowFeature feature = new FlowFeature();
                feature.setTotalPktMaxLength(Double.parseDouble(flowData[45]));
                feature.setTotalPktMinLength(Double.parseDouble(flowData[44]));
                feature.setTotalPktMeanLength(Double.parseDouble(flowData[46]));
                feature.setTotalPktStdLength(Double.parseDouble(flowData[47]));

                feature.setFwdPktCount(Long.parseLong(flowData[8]));
                feature.setFwdPktTotalLength(Double.parseDouble(flowData[10]));
                feature.setFwdPktMaxLength(Double.parseDouble(flowData[12]));
                feature.setFwdPktMinLength(Double.parseDouble(flowData[13]));
                feature.setFwdPktMeanLength(Double.parseDouble(flowData[14]));
                feature.setFwdPktStdLength(Double.parseDouble(flowData[15]));

                feature.setBwdPktCount(Long.parseLong(flowData[9]));
                feature.setBwdPktTotalLength(Double.parseDouble(flowData[11]));
                feature.setBwdPktMaxLength(Double.parseDouble(flowData[16]));
                feature.setBwdPktMinLength(Double.parseDouble(flowData[17]));
                feature.setBwdPktMeanLength(Double.parseDouble(flowData[18]));
                feature.setBwdPktStdLength(Double.parseDouble(flowData[19]));

                flow.setFeature(feature);
                flows.add(flow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to read CSV File : " + csvFileName);
        } finally {
            if (null!=csvReader)
                csvReader.close();
        }
        return flows;
    }

    public static double getFlowFeatureCosineSimilarity(FlowFeature feature1, FlowFeature feature2){
        return cosineSimilarity(feature1.getFeatureList(),feature2.getFeatureList());
    }

    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static void test() {
        FlowFeature feature1 = new FlowFeature(2840,0,560.255639097744,702.029586423657,65,67,68557,5957,1460,2840,0,0,1054.72307692307,88.9104477611939,584.501831361874,428.158450255923);
        System.out.println(feature1);
        FlowFeature feature2 = new FlowFeature(2840,0,636.409638554217,716.562307965641,122,126,151891,6575,1460,2840,0,0,1245.00819672131,52.1825396825396,471.016914575852,308.472660721656);
        System.out.println(feature2);
        FlowFeature feature4 = new FlowFeature(2521,0,614.878125,695.686761221902,158,161,191055,5706,1460,2521,0,0,1209.20886075949,35.4409937888198,465.622575538303,251.343436474888);
        System.out.println(feature4);
        FlowFeature feature5 = new FlowFeature(1460,0,639.835820895522,710.114214520008,132,135,165722,5754,1460,1394,0,0,1255.46969696969,42.6222222222222,471.358430567527,227.656420447885);
        System.out.println(feature5);
        FlowFeature feature6 = new FlowFeature(1460,0,629.514195583596,691.532409489097,158,158,196552,1664,1460,1420,0,0,1244,10.5316455696202,425.123990905373,114.029925164158);
        System.out.println(feature6);
        FlowFeature feature3 = new FlowFeature(7100,0,977.578125,1419.41078547634,30,33,886,61679,270,7100,0,0,29.5333333333333,1869.0606060606,68.5674766239411,1506.71077888628);
        System.out.println(feature3);

        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature1,feature2));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature1,feature4));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature4,feature2));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature1,feature5));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature2,feature5));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature4,feature5));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature1,feature6));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature2,feature6));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature4,feature6));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature5,feature6));

        System.out.println();
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature1,feature3));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature2,feature3));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature4,feature3));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature5,feature3));
        System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(feature6,feature3));
    }

    public static void main(String[] args) {
//        ParseUtil.test();
        List<BasicFlow> flows = ParseUtil.getFlowsFromCsv("D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic.pcap_Flow.csv");
        FlowFeature feature2 = new FlowFeature(2840,0,636.409638554217,716.562307965641,122,126,151891,6575,1460,2840,0,0,1245.00819672131,52.1825396825396,471.016914575852,308.472660721656);
        for (BasicFlow flow: flows) {
            System.out.println(flow.toString());
            System.out.println(ParseUtil.getFlowFeatureCosineSimilarity(flow.getFeature(),feature2));
        }
    }

}
