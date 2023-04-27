package utils;

import com.csvreader.CsvReader;
import flow.BasicFlow;
import flow.FLowRelation;
import flow.FlowFeature;
import jnet.PcapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.collections.Pair;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {

    public static Logger log = LoggerFactory.getLogger(ParseUtil.class);

    public static final String PACKAGE_NAME = "com.vkontakte.android";

    public static final String PROJECT_PATH = System.getProperty("user.dir") + File.separator;
    public static final String EDITCAP_PATH = "\"C:\\Program Files\\Wireshark\\editcap.exe\"";
    public static final String CICFLOWMETER_PATH = "\"" + PROJECT_PATH + "CICFlowMeter-4.0\\bin\\cfm.bat\"";
    public static final String CICFLOWMETER_EXECUTE_PATH =  PROJECT_PATH + "CICFlowMeter-4.0\\bin";
    public static final String SNI_PY_PATH = "\"" + PROJECT_PATH + "dnsPyTools\\dns.py\"";

    public static final Map<String, String> SNI = new HashMap<>();

    public static final double MULTIFLOW_SIMILARITY_THRESHOLD = 0.84;


    public static final int VALID_PACKET_COUNT_THRESHOLD = 10;

    public static List<File> getAllFile(String filePath) {
        File dirFile = new File(filePath);
        // 如果文件夹不存在或着不是文件夹，则返回 null
        if (!dirFile.exists() || dirFile.isFile())
            return null;

        File[] childrenFiles = dirFile.listFiles();
        if (Objects.isNull(childrenFiles) || childrenFiles.length == 0)
            return null;

        List<File> files = new ArrayList<>();
        for (File childFile : childrenFiles) {
            if (childFile.isFile()) {
                files.add(childFile);
            }
//            else {
//                List<File> cFiles = getAllFile(childFile);
//                if (Objects.isNull(cFiles) || cFiles.isEmpty()) continue;
//                files.addAll(cFiles);
//            }
        }
        return files;
    }

    public static List<BasicFlow> getFlowsFromCsv(File csvFile) {
        List<BasicFlow> flows = null;
        CsvReader csvReader = null;
        try {
            flows = new ArrayList<>();
             csvReader = new CsvReader(csvFile.getAbsolutePath(), ',', Charset.forName("GBK"));
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
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
                LocalDateTime time = LocalDateTime.from(format.parse(flowData[6]));
                flow.setTimestamp(Timestamp.valueOf(time));
                flow.setServerHost(SNI.get(flow.getDstIp().trim())==null?"":SNI.get(flow.getDstIp().trim()));
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
            log.error("Fail to read CSV File : " + csvFile.getName());
        } finally {
            if (null!=csvReader)
                csvReader.close();
        }
        return flows;
    }

    public static List<BasicFlow> getValidFlowsFromCsv(File csvFile) {
        List<BasicFlow> flows = getFlowsFromCsv(csvFile);
        List<BasicFlow> invalidFlows = new ArrayList<>();
        for (BasicFlow flow : flows) {
            if ((flow.getFeature().getBwdPktCount() + flow.getFeature().getFwdPktCount()) <= VALID_PACKET_COUNT_THRESHOLD) {
                invalidFlows.add(flow);
            }
        }
        flows.removeAll(invalidFlows);
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

    public static void buildMultiFlow(String path) {
//        String [] csvFiles = {"D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic.pcap_Flow.csv",
//                "D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic2.pcap_Flow.csv",
//                "D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic3.pcap_Flow.csv",
//                "D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic4_onlyUpLoad.pcap_Flow.csv","" +
//                "D:\\Workspace\\IDEA_workspace\\AppFlowCrawler\\csv\\uploadPic5.pcap_Flow.csv"};
        List<File> csvFiles = getAllFile(path);
        if (null == csvFiles)   return;

        Set<BasicFlow> multiFlows = new HashSet<>();
        Map<BasicFlow, Map<BasicFlow,Integer>> pianXu = new HashMap<>();
        Map<BasicFlow, Integer> count = new HashMap<>();
        int index = 1;
        for (File csvFile : csvFiles) {
            List<BasicFlow> flows = ParseUtil.getValidFlowsFromCsv(csvFile);
            if (multiFlows.size() == 0) {
                multiFlows.addAll(flows);
                for (BasicFlow ff : flows) {
                    ff.setId(index);
                    index++;
                    count.put(ff, 1);
                    pianXu.put(ff, new HashMap<>());
                }
                for (int j = 0; j < flows.size(); j++) {
                    for (int k = 0; k < flows.size(); k++) {
                        if (j == k) continue;
                        Map<BasicFlow, Integer> rel = pianXu.get(flows.get(j));
                        if (flows.get(j).getTimestamp().before(flows.get(k).getTimestamp())) {
                            if (null == rel.get(flows.get(k))) {
                                rel.put(flows.get(k), 1);
                            } else {
                                rel.put(flows.get(k), rel.get(flows.get(k)) + 1);
                            }
                        } else {
                            if (null == rel.get(flows.get(k))) {
                                rel.put(flows.get(k), -1);
                            } else {
                                rel.put(flows.get(k), rel.get(flows.get(k)) - 1);
                            }
                        }
                    }
                }
            } else {
                Set<BasicFlow> updateMultiFlow = new HashSet<>();
                Set<BasicFlow> updateRelFlows = new HashSet<>();
                for (BasicFlow flow : flows) {
                    boolean flag = false;
                    for (BasicFlow valFlow : multiFlows) {
                        if (getFlowFeatureCosineSimilarity(flow.getFeature(), valFlow.getFeature()) > 0.9) {
//                            count.put(valFlow,count.get(valFlow)+1);
                            updateMultiFlow.add(valFlow);
                            updateRelFlows.add(valFlow);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        flow.setId(index);
                        index++;
                        multiFlows.add(flow);
                        count.put(flow, 1);
                        updateRelFlows.add(flow);
                        pianXu.put(flow, new HashMap<>());
                    }
                }
                for (BasicFlow updateFlow : updateMultiFlow) {
                    count.put(updateFlow, count.get(updateFlow) + 1);
                }
                for (BasicFlow relFlow : updateRelFlows) {
                    for (BasicFlow multiFlow : updateRelFlows) {
                        if (relFlow.equals(multiFlow)) continue;
                        Map<BasicFlow, Integer> rel = pianXu.get(relFlow);
                        if (relFlow.getTimestamp().before(multiFlow.getTimestamp())) {
                            if (null == rel.get(multiFlow)) {
                                rel.put(multiFlow, 1);
                            } else {
                                rel.put(multiFlow, rel.get(multiFlow) + 1);
                            }
                        } else {
                            if (null == rel.get(multiFlow)) {
                                rel.put(multiFlow, -1);
                            } else {
                                rel.put(multiFlow, rel.get(multiFlow) - 1);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("------------------------------");
        for (BasicFlow f :
                count.keySet()) {
            System.out.println(f);
            System.out.println(count.get(f));
        }
        System.out.println("------------------------------");
        for (BasicFlow flow : pianXu.keySet()) {
            Map<BasicFlow, Integer> rels = pianXu.get(flow);
            System.out.println(flow);
            for (BasicFlow flow1 : rels.keySet()) {
                System.out.println(rels.get(flow1) + " : " + flow1);
            }
            System.out.println("===============================");
        }
    }

    public static ArrayList<String[]> getTimeStamps(String timestampFile) {
        File file = new File(timestampFile);
        BufferedReader reader = null;
        String temp;
        ArrayList<String[]> timestamps = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((temp=reader.readLine())!=null) {
                String[] cols = temp.split("\t");
                timestamps.add(cols);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return timestamps;
    }

    public static ArrayList<String> splitPcap(String timestampFile, String pcapFile) {
        ArrayList<String[]> timeStamps = getTimeStamps(timestampFile);
        ArrayList<String> outputFiles = new ArrayList<>();
        for (String[] cols : timeStamps) {
            File pcap = new File(pcapFile);
            String outputFile;
            if (pcap.exists()) {
                String fileName = cols[2] + "_" + cols[3] + "_" + cols[4];
                Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
                Matcher matcher = pattern.matcher(fileName);
                fileName= matcher.replaceAll("_");
                outputFile = pcap.getParentFile().getAbsolutePath() + File.separator + fileName + ".pcap";
            } else {
                outputFile = cols[2] + "_" + cols[3] + "_" + cols[4] + ".pcap";
            }
            String cmd = EDITCAP_PATH + " -F libpcap -T ether -A \"" + cols[0] + "\" -B \"" + cols[1]+ "\" \"" + pcapFile + "\" \"" + outputFile + "\"";
            // -F libpcap -T ether
            outputFiles.add(outputFile);
//            System.out.println(cmd);
            CommandUtil.executeCmdNormal(cmd);
            log.info("Split pcap from " +  cols[0] + " to " + cols[1] + " , Output : " + outputFile);
        }
//        System.out.println(outputFiles);
        return outputFiles;
    }

    public static String cicFlowMeter(String pcapFIle, String csvFIlePath) {
        String cmd = CICFLOWMETER_PATH + " \"" + pcapFIle + "\" \"" + csvFIlePath + "\"";
        log.info(cmd);
        CommandUtil.executeWithPath(cmd, CICFLOWMETER_EXECUTE_PATH);
//        int i = pcapFIle.lastIndexOf("\\");
//        return csvFIlePath + pcapFIle.substring(i) + "_Flow.csv";
        return csvFIlePath + File.separator + new File(pcapFIle).getName() + "_Flow.csv";
    }

    public static void setSNI(String pcapFile) {
//        String cmd = "python " + SNI_PY_PATH + " \"" + pcapFile + "\" \"" + sniPath + "\"";
//        CommandUtil.executeCmdNormal(cmd);
//        BufferedReader reader = null;
//        String temp = null;
//        try {
//            File sniFile = new File(sniPath);
//            reader = new BufferedReader(new FileReader(sniFile));
//            while ((temp=reader.readLine())!=null) {
//                String[] cols = temp.split("\t");
//                SNI.put(cols[0].trim(), cols[1].trim());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Map<String, String> sniFromPcap = PcapUtil.getSNIFromPcap(pcapFile);
        if (null != sniFromPcap)
            SNI.putAll(sniFromPcap);
    }

    public static void storeDB(List<BasicFlow> flows, int fromDep, int toDep, String tag) throws SQLException {
        //处理应用ID
        int appId = -1;
        ResultSet rs_app = DBUtil.doQuery("SELECT * FROM " + DBUtil.APP_TABLE + " WHERE packageName = \"" + PACKAGE_NAME + "\"");
        if(!rs_app.next()) {
            String sql = "INSERT INTO " + DBUtil.APP_TABLE + " (`packageName`) VALUES ('" + PACKAGE_NAME + "');";
            DBUtil.doUpdate(sql);
            log.info("New Package Create : " + PACKAGE_NAME);
            rs_app = DBUtil.doQuery("SELECT * FROM " + DBUtil.APP_TABLE + " WHERE packageName = \"" + PACKAGE_NAME + "\"");
            if (rs_app.next()){
                appId = rs_app.getInt("appId");
                log.info("SUCCESS");
            } else
                log.error("Fail");
        } else {
            log.info("Package : " + PACKAGE_NAME + " already exists int DB. Update data.");
            appId = rs_app.getInt("appId");
        }
        if (appId < 0) {
            log.error("Fail to check App ID, Return ...");
            return;
        }
        log.info("App ID in DB is " + appId);

        //处理深度信息 如果没有相关的深度 则新建
        String fromDep_sql = "INSERT INTO " + DBUtil.DEPTH_TABLE + " (`depth`,`appId`) SELECT " + fromDep + "," + appId
                + " FROM DUAL WHERE NOT EXISTS (SELECT * FROM " + DBUtil.DEPTH_TABLE + " where depth = " + fromDep + " and appId = " + appId + ")";
        String toDep_sql = "INSERT INTO " + DBUtil.DEPTH_TABLE + " (`depth`,`appId`) SELECT " + toDep + "," + appId
                + " FROM DUAL WHERE NOT EXISTS (SELECT * FROM " + DBUtil.DEPTH_TABLE + " where depth = " + toDep + " and appId = " + appId + ")";
        if(DBUtil.doUpdate(fromDep_sql)>=0) {
            log.info("FromDep : " + fromDep);
        } else {
            log.error("Fail to Habdle FromDep, Return ...");
            return;
        }
        if(DBUtil.doUpdate(toDep_sql)>=0) {
            log.info("ToDep : " + toDep);
        } else {
            log.error("Fail to Habdle ToDep, Return ...");
            return;
        }

        //处理多流表
        int contextId = -1;
        //如果库中已经存在了该context则不会操作 如果不存在则会新建一个
        String context_insert_sql = "INSERT INTO " + DBUtil.CONTEXT_TABLE + " (`contextId`,`depthFrom`, `depthTo`, `tag`, `appId`) SELECT null," + fromDep + "," + toDep +",'" + tag +"'," + appId
                + "  FROM DUAL WHERE NOT EXISTS (SELECT * FROM " + DBUtil.CONTEXT_TABLE + " WHERE depthFrom=" + fromDep + " and depthTo=" + toDep + " and tag='" + tag + "' and appId=" + appId + ")";
        if (DBUtil.doUpdate(context_insert_sql) > 0) {
            //新建成功 获取新建的context的ID
            String context_query_sql = "SELECT LAST_INSERT_ID() FROM " + DBUtil.CONTEXT_TABLE;
            ResultSet context_query_rs = DBUtil.doQuery(context_query_sql);
            if (context_query_rs.next()){
                contextId = context_query_rs.getInt(1);
            }
            log.info("New Context Create :" + tag);
        } else {
            //已经存在了 获取已经存在该context的ID
            String context_query_sql = "SELECT `contextId` FROM " + DBUtil.CONTEXT_TABLE + " WHERE depthFrom=" + fromDep + " and depthTo=" + toDep + " and tag='" + tag + "' and appId=" + appId;
            ResultSet context_query_rs = DBUtil.doQuery(context_query_sql);
            if (context_query_rs.next()){
                contextId = context_query_rs.getInt(1);
            }
            log.info("Context : " + tag + " already exists int DB. Update data.");
        }
        if (contextId < 0) {
            log.error("Fail to check Context ID, Return ...");
            return;
        }
        log.info("Context ID in DB is " + contextId);

        //获取该多流对应的一些单流
        String flows_query_sql = "SELECT * FROM " + DBUtil.FLOWS_TABLE + " WHERE multiFlowId=" + contextId;
        ResultSet flows_query_rs = DBUtil.doQuery(flows_query_sql);
        List<BasicFlow> extFlows = new ArrayList<>( );
        while(flows_query_rs.next()) {
            BasicFlow flow = DBUtil.getFLowById(flows_query_rs.getInt("flowId"));
            extFlows.add(flow);
        }
        if (extFlows.size()==0) {
            //之前没有存储过该多流 即多流对应的单流集为空 则新建
            //新建流
            for (BasicFlow flow : flows) {
                flow.setInevitable(true);
                int flowId = DBUtil.storeFlow(contextId, flow);
                if (flowId > 0) {
                    flow.setId(flowId);
                }
            }

            //新建流关系
            int length = flows.size();
            for (int i = 0; i < length; i++) {
                BasicFlow flowI = flows.get(i);
                for (int j = i+1; j < length; j++) {
                    BasicFlow flowJ = flows.get(j);
                    DBUtil.storeFlowRel(flowI, flowJ, contextId);
                }
            }

        } else {

            //已有多流 则更新多流信息
            Map<BasicFlow, BasicFlow> matches = new HashMap<>();//记录匹配到的流和库中的流的对应关系 key是匹配流 value是库流
            List<BasicFlow> mflows = new ArrayList<>();//存储匹配到的流
            for (BasicFlow flow : flows) {
                for (BasicFlow bf: extFlows) {
                    if (flow.getServerHost().trim().equals(bf.getServerHost().trim()) && flow.getDstPort()==bf.getDstPort()
                            && getFlowFeatureCosineSimilarity(flow.getFeature(),bf.getFeature())>=0.8
//                            && !matches.containsValue(bf)
                    ) {
                        flow.setId(bf.getId());
                        matches.put(flow,bf);
                        mflows.add(flow);
                        break;
                    }
                }
            }

            ////////更新主流
            List<BasicFlow> inevitableFlows = DBUtil.getInevitableFlows(contextId);
            if (!inevitableFlows.isEmpty()) {
                for (BasicFlow iFlow : inevitableFlows) {
                    boolean hit = false;
                    for (BasicFlow flow : flows) {
                        if (flow.getId() == iFlow.getId()) {
                            //主流出现
                            hit = true;
                            break;
                        }
                    }
                    if (!hit) {
                        DBUtil.setInevitableFlow(iFlow.getId(), false);
                    }
                }
            }

            ////////

            //更新匹配到的流之间的关系
            for (BasicFlow mflow : matches.values()) {
                //更新count计数
                String flow_rel_update_sql1 = "UPDATE " + DBUtil.FLOWRELATION_TABLE + " SET `flow1count` = `flow1count` + 1 WHERE `multiflowId` = " + contextId + " and `flowId1` = " + mflow.getId();
                String flow_rel_update_sql2 = "UPDATE " + DBUtil.FLOWRELATION_TABLE + " SET `flow2count` = `flow2count` + 1 WHERE `multiflowId` = " + contextId + " and `flowId2` = " + mflow.getId();
                boolean flag = true;
                if (DBUtil.doUpdate(flow_rel_update_sql1)<0){
                    log.error("Fail to update flow relation flow1count, flowId : " + mflow.getId());
                    flag = false;
                }
                if (DBUtil.doUpdate(flow_rel_update_sql2)<0){
                    log.error("Fail to update flow relation flow2count, flowId : " + mflow.getId());
                    flag = false;
                }
                if (flag) {
                    log.info("Success to update flow[id=" + mflow.getId() + "] relations count.");
                }
            }
            for (int i = 0; i < mflows.size(); i++) {
                for (int j = i+1; j < mflows.size(); j++) {
                    boolean new_potype = mflows.get(i).getTimestamp().before(mflows.get(j).getTimestamp());
                    boolean potype = matches.get(mflows.get(i)).getTimestamp().before(matches.get(mflows.get(j)).getTimestamp());
                    if (new_potype!=potype) {
                        String flow_rel_po_update_sql = "UPDATE " + DBUtil.FLOWRELATION_TABLE + " SET `isPO` = '0' WHERE ((`flowId1` = " + matches.get(mflows.get(i)).getId() + " and `flowId2` = " + matches.get(mflows.get(j)).getId() + ") or (`flowId1` = " + matches.get(mflows.get(j)).getId() + " and `flowId2` = " + matches.get(mflows.get(i)).getId() + ")) and `multiflowId`=" + contextId + ";";
                        DBUtil.doUpdate(flow_rel_po_update_sql);
                        log.info("Flow POType Change : flow[id=" + matches.get(mflows.get(i)).getId() + "] and flow[id=" + matches.get(mflows.get(j)).getId() + "]");
                    }
                }
            }
            //对于未在库中匹配到对应流的 为其新建流添加到库中并更新流关系

            List<BasicFlow> nflows = new ArrayList<>(flows);
            nflows.removeAll(mflows);
            //直接设为1-1 匹配的时候忽略1-1的边()
            for (BasicFlow nflow : nflows) {
                nflow.setInevitable(true);
                int flowId = DBUtil.storeFlow(contextId, nflow);
                if (flowId > 0) {
                    nflow.setId(flowId);
                }
            }
            for (int i = 0; i < nflows.size() - 1; i++) {
                BasicFlow flowI = nflows.get(i);
                for (int j = i+1; j < nflows.size(); j++) {
                    BasicFlow flowJ = nflows.get(j);
                    DBUtil.storeFlowRel(flowI, flowJ, contextId);
                }
            }
            List<BasicFlow> dbflows = new ArrayList<>(matches.values());
            for (BasicFlow flow : nflows) {
                for (BasicFlow  flow1: dbflows){
                    if (flow.getId()==flow1.getId()) {
                        continue;
                    }
                    DBUtil.storeFlowRel(flow, flow1, contextId);
                }
            }

        }
    }

    public static void extract(String timestampFile, String pcapFIle, String csvPath) {
        //按照时间戳分割pcap
        ArrayList<String> pcaps = splitPcap(timestampFile, pcapFIle);
        File csvPathFIle = new File(csvPath);
        if (!csvPathFIle.exists()) {
            csvPathFIle.mkdirs();
        }
        //提取pcap获得csv流特征文件
        ArrayList<String> csvs = new ArrayList<>();
        for (String pcap : pcaps) {
            String csv = cicFlowMeter(pcap, csvPath);
            csvs.add(csv);
            log.info("Extract csv : " + csv);
        }
        //处理每个csv
        for (String csvString : csvs) {
            List<BasicFlow> flows;
            File csv = new File(csvString);
            if (csv.exists() && csv.canRead()) {
                //从csv中获取并构建流
                flows = getValidFlowsFromCsv(csv);
                if (flows.size() == 0) {
                    continue;
                }
                int fromDep;
                int toDep;
                Pattern pattern = Pattern.compile("(\\d+)_(\\d+).pcap_Flow.csv");
                Matcher matcher = pattern.matcher(csvString);
                if (matcher.find()) {
                    fromDep =Integer.parseInt(matcher.group(1));
                    toDep = Integer.parseInt(matcher.group(2));
                } else {
                    log.error("CSV File name wrong, can not find fromDepth and toDepth : " + csvString);
                    continue;
                }
                String tag = csv.getName().split("\\.pcap")[0];
                log.info("From " + fromDep + " To " + toDep + " : " +flows);
                log.info("==============================");
                try {
                    storeDB(flows, fromDep, toDep, tag);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                log.error("Fail to read CSV file, Please check if it exit :" + csvString);
            }
        }
    }

    public static Map<Integer, Integer> matchApp(String pcapFilePath) throws SQLException {
        Map<String, String> sniFromPcap = PcapUtil.getSNIFromPcap(pcapFilePath);
        if (sniFromPcap == null || sniFromPcap.isEmpty()) {
            log.info("No available snap information in PCAP.");
            return null;
        }
        List<Integer> appsFromDB = DBUtil.getAppsFromDB();
        if (appsFromDB.size() == 0) {
            log.info("No App in DB");
            return null;
        }
        Map<Integer, Integer> appSniMatchMap = new HashMap<>();
        for (int appId : appsFromDB) {
            Set<String> sniFromDB = DBUtil.getSNIFromDB(appId);
            int count = 0;
            for (String sni : sniFromPcap.values()) {
                if (sniFromDB.contains(sni)) {
                    count ++;
                }
            }
            if (count != 0) {
                appSniMatchMap.put(appId, count);
            }
        }
        return appSniMatchMap;
    }

    //多流匹配
    //匹配成功返回对应多流的toDepth 否则返回-1
    //传入的流需要预先处理好host(SNI)信息以进行后续流的匹配
    public static Pair<Integer,Integer> matchFlow(List<BasicFlow> matchFlows, int fromDepth, int appId, boolean flag) throws SQLException {
        String multiFlow_query_sql = "SELECT * FROM " + DBUtil.CONTEXT_TABLE + " WHERE `depthFrom` = " + fromDepth + " and `appId` = " + appId + " ORDER BY contextId DESC;";
        ResultSet multiFlow_query_rs = DBUtil.doQuery(multiFlow_query_sql);
        Map<Integer, Integer> multiFlows = new HashMap<>();
        Map<Integer, String> multiFlowTags = new HashMap<>();//for debug
        while (multiFlow_query_rs.next()) {
            int contextId = multiFlow_query_rs.getInt("contextId");
            int toDepth = multiFlow_query_rs.getInt("depthTo");
            String tag = multiFlow_query_rs.getString("tag");
            multiFlows.put(contextId,toDepth);
            multiFlowTags.put(contextId,tag);
        }
        if (multiFlows.size()==0)   return new Pair<>(-1,-1);
        double maxFS = 0;
        int maxMF = -1;
        for (int mulFLowId : multiFlows.keySet()) {
//            double multiFLowSimilarity = getMultiFLowSimilarity(matchFlows, mulFLowId, flag);
            double multiFLowSimilarity = getMultiFLowSimilarity_new(matchFlows, mulFLowId, flag);
            log.info("Temp multiFLowSimilarity[" + mulFLowId + ":" + multiFlowTags.get(mulFLowId) + "] : " + multiFLowSimilarity);
            if (multiFLowSimilarity >= maxFS) {
                maxFS = multiFLowSimilarity;
                maxMF = mulFLowId;
            }
        }
        if(maxFS >= MULTIFLOW_SIMILARITY_THRESHOLD) {
            log.info("Success to match multiflow[" + maxMF + ":" + multiFlowTags.get(maxMF) + "], multiFLowSimilarity = " + maxFS + ", Jump to depth " + multiFlows.get(maxMF));
            return new Pair<>(multiFlows.get(maxMF),maxMF);
        }
        return new Pair<>(-1,-1);
    }

    public static double getMultiFLowSimilarity(List<BasicFlow> matchFlows, int multiFlowId, boolean flag) throws SQLException {
        String flow_rel_query_sql = "SELECT * FROM " + DBUtil.FLOWRELATION_TABLE + " WHERE `multiflowId` = " + multiFlowId;
        ResultSet flow_rel_query_rs = DBUtil.doQuery(flow_rel_query_sql);
        List<FLowRelation> frs = new ArrayList<>();
        while(flow_rel_query_rs.next()){
            FLowRelation fr = new FLowRelation();
            fr.setRelId(flow_rel_query_rs.getInt("relId"));
            fr.setMultiflowId(flow_rel_query_rs.getInt("multiflowId"));
            fr.setFlowId1(flow_rel_query_rs.getInt("flowId1"));
            fr.setFlowId2(flow_rel_query_rs.getInt("flowId2"));
            fr.setPO(flow_rel_query_rs.getBoolean("isPO"));
            fr.setPOtype(flow_rel_query_rs.getBoolean("POtype"));
            fr.setFlowcount1(flow_rel_query_rs.getInt("flow1count"));
            fr.setFlowcount2(flow_rel_query_rs.getInt("flow2count"));
            if (flag || fr.getFlowcount1() != 1 && fr.getFlowcount2() != 1) {
                frs.add(fr);
            }
        }
        if (frs.isEmpty())  {
            String flow_query_sql = "SELECT * FROM " + DBUtil.FLOWS_TABLE + " WHERE `multiFlowId` = " + multiFlowId;
            ResultSet flow_query_rs = DBUtil.doQuery(flow_query_sql);
            List<BasicFlow> flows = new ArrayList<>();
            while (flow_query_rs.next()) {
                flows.add(DBUtil.getFLowById(flow_query_rs.getInt("flowId")));
            }

            /////////////////////////////////////////////////
            if (Math.abs(matchFlows.size()-flows.size())>=3)    return 0;

            ////////////////////////////////////////////////

            List<BasicFlow> rm = new ArrayList<>();
            flows.forEach(f->{if (f.getServerHost().length()<1)   rm.add(f);});
            flows.removeAll(rm);
            if (flows.isEmpty())    return 0;
            int count = 0;
            double res = 0;
            for (BasicFlow flow : matchFlows) {
                for (BasicFlow f : flows) {
                    if (flow.getServerHost().equals(f.getServerHost())) {
                        count++;
                        res += getFlowFeatureCosineSimilarity(flow.getFeature(), f.getFeature());
                        break;
                    }
                }
            }
            if (count==0) {
                return 0;
            } else {
                return res/count;
            }

//            if (matchFlows.size()==1 || flag) {
//                String flow_query_sql = "SELECT * FROM " + DBUtil.FLOWS_TABLE + " WHERE `multiFlowId` = " + multiFlowId;
//                ResultSet flow_query_rs = DBUtil.doQuery(flow_query_sql);
//                BasicFlow flow = null;
//                if(flow_query_rs.next()) {
//                    flow = DBUtil.getFLowById(flow_query_rs.getInt("flowId"));
//                }
//                if (flow==null) return 0;
//                double max = 0;
//                for (BasicFlow mflow : matchFlows) {
//                    double cs = getFlowFeatureCosineSimilarity(flow.getFeature(), mflow.getFeature());
//                    if (cs > max)
//                        max = cs;
//                }
//                return max;
//            } else {
////                log.info("No flow relation in DB.");
//                return 0;
//            }
        }

        double res = 0;
        int relCount = 0;

        for (FLowRelation fr : frs) {
            double temp;
            int flowId1 = fr.getFlowId1();
            int flowId2 = fr.getFlowId2();
            BasicFlow flow1 = DBUtil.getFLowById(flowId1);
            BasicFlow flow2 = DBUtil.getFLowById(flowId2);
            if (null == flow1 || null == flow2) {
                continue;
            }

            BasicFlow mflow1 = null;
            Optional<BasicFlow> mflow1_ops = matchFlows.stream().filter(item -> (item.getServerHost().equals(flow1.getServerHost()) || item.getDstIp().equals(flow1.getDstIp())) && item.getDstPort() == flow1.getDstPort()).findFirst();
            if (mflow1_ops.isPresent()) {
                mflow1 = mflow1_ops.get();
            }
            BasicFlow mflow2 = null;
            Optional<BasicFlow> mflow2_ops = matchFlows.stream().filter(item -> (item.getServerHost().equals(flow2.getServerHost()) || item.getDstIp().equals(flow1.getDstIp())) && item.getDstPort() == flow2.getDstPort()).findFirst();
            if (mflow2_ops.isPresent()) {
                mflow2 = mflow2_ops.get();
            }

            if (mflow1 == null) {
                double max = 0;
                BasicFlow tFlow = null;
                for (BasicFlow flow : matchFlows) {
                    double fcs = getFlowFeatureCosineSimilarity(flow1.getFeature(), flow.getFeature());
                    if (fcs>max) {
                        max = fcs;
                        tFlow = flow;
                    }
                }
                mflow1 = tFlow;
            }
            if (mflow2 == null) {
                double max = 0;
                BasicFlow tFlow = null;
                for (BasicFlow flow : matchFlows) {
                    double fcs = getFlowFeatureCosineSimilarity(flow2.getFeature(), flow.getFeature());
                    if (fcs>max) {
                        max = fcs;
                        tFlow = flow;
                    }
                }
                mflow2 = tFlow;
            }

            if (null== mflow1 || null == mflow2) {
                continue;
            }
//            log.info("Match flow relation[multiflowId = " + fr.getMultiflowId() + "] : flow1[" + flow1.getId() + "]  flow2[" + flow2.getId() + "]");
            relCount++;
            double cs1 = getFlowFeatureCosineSimilarity(flow1.getFeature(), mflow1.getFeature());
            double cs2 = getFlowFeatureCosineSimilarity(flow2.getFeature(), mflow2.getFeature());
            if (fr.isPO) {
                if (fr.POtype == mflow1.getTimestamp().before(mflow2.getTimestamp())) {
                    res += cs1*cs2;
                } else {
                    res += 0;
                }
            } else {
                res += cs1 * (fr.flowcount1> fr.flowcount2? (double)fr.flowcount2/fr.flowcount1 : (double)fr.flowcount1/fr.flowcount2) * cs2;
            }
        }
        return relCount==0?0:res/relCount;
    }


    public static double getMultiFLowSimilarity_new(List<BasicFlow> matchFlows, int multiFlowId, boolean flag) throws SQLException {
//        double alpha = 0.85;
//        double beta = 0.075;
//        double gamma = 0.075;
        // hello thank you i am a dog

        double alpha = 0.9;
        double beta = 0.1;
        double gamma = 0;

        double phi = 0.6;
        double omega = 0.4;

        double inevitableDegree = 0;
        double contextDegree;
        double pointDegree;
        double sizeDegree;

        //sizeDegree
        String flow_query_sql = "SELECT * FROM " + DBUtil.FLOWS_TABLE + " WHERE `multiFlowId` = " + multiFlowId;
        ResultSet flow_query_rs = DBUtil.doQuery(flow_query_sql);
        List<BasicFlow> flows = new ArrayList<>();
        while (flow_query_rs.next()) {
            flows.add(DBUtil.getFLowById(flow_query_rs.getInt("flowId")));
        }
        if (flows.size()==0)    return 0;
        if (matchFlows.size()==0)   return 0;
        sizeDegree = matchFlows.size() < flows.size() ? (double) matchFlows.size()/flows.size() : (double) flows.size()/matchFlows.size();
        //pointDegree
        int matchedPointCount = 0;
        for (BasicFlow mFlow : matchFlows) {
            for (BasicFlow flow : flows) {
                if (
                        (mFlow.getServerHost().equals(flow.getServerHost()) || mFlow.getDstIp().equals(flow.getDstIp())) || getFlowFeatureCosineSimilarity(flow.getFeature(),mFlow.getFeature()) >= 0.95
                ) {
                    matchedPointCount++;
                    break;
                }
            }
        }
        pointDegree = (double) matchedPointCount / matchFlows.size();

        //calculate inevitableDegree
        List<BasicFlow> inevitableFlows = DBUtil.getInevitableFlows(multiFlowId);
        if (!inevitableFlows.isEmpty()) {
            //如果没有主流是否要认为该多流是不稳定的
            int inevitableCount = 0;
            for (BasicFlow invitableFLow : inevitableFlows) {
                for (BasicFlow mFlow : matchFlows) {
                    if ( (invitableFLow.getServerHost().equals(mFlow.getServerHost()) && invitableFLow.getServerHost().length()>1)
                    || getFlowFeatureCosineSimilarity(invitableFLow.getFeature(), mFlow.getFeature())>=0.9){
                        inevitableDegree += getFlowFeatureCosineSimilarity(invitableFLow.getFeature(), mFlow.getFeature());
                        inevitableCount++;
                        break;
                    }
                }
            }
            log.info("For context[" + multiFlowId + "] match " + inevitableCount + " of all " + inevitableFlows.size());
            inevitableDegree /= inevitableFlows.size();
        } else {
            inevitableDegree = 0;
        }

        //contextDegree
        String flow_rel_query_sql = "SELECT * FROM " + DBUtil.FLOWRELATION_TABLE + " WHERE `multiflowId` = " + multiFlowId;
        ResultSet flow_rel_query_rs = DBUtil.doQuery(flow_rel_query_sql);
        List<FLowRelation> frs = new ArrayList<>();
        while(flow_rel_query_rs.next()){
            FLowRelation fr = new FLowRelation();
            fr.setRelId(flow_rel_query_rs.getInt("relId"));
            fr.setMultiflowId(flow_rel_query_rs.getInt("multiflowId"));
            fr.setFlowId1(flow_rel_query_rs.getInt("flowId1"));
            fr.setFlowId2(flow_rel_query_rs.getInt("flowId2"));
            fr.setPO(flow_rel_query_rs.getBoolean("isPO"));
            fr.setPOtype(flow_rel_query_rs.getBoolean("POtype"));
            fr.setFlowcount1(flow_rel_query_rs.getInt("flow1count"));
            fr.setFlowcount2(flow_rel_query_rs.getInt("flow2count"));
            if (flag || fr.getFlowcount1() != 1 && fr.getFlowcount2() != 1) {
                frs.add(fr);
            }
        }
        if (frs.isEmpty()) {
            //frs为空代表只有一条流或未记录流关系
            int count = 0;
            double res = 0;
            for (BasicFlow flow : matchFlows) {
                for (BasicFlow f : flows) {
                    if (flow.getServerHost().equals(f.getServerHost()) || getFlowFeatureCosineSimilarity(flow.getFeature(),f.getFeature()) >= 0.95) {
//                    if (flow.getServerHost().equals(f.getServerHost())) {
                        count++;
                        res += getFlowFeatureCosineSimilarity(flow.getFeature(), f.getFeature());
                        break;
                    }
                }
            }
            if (count==0) {
                contextDegree = 0;
            } else {
                contextDegree = res/count;
            }
        } else {
            int relCount = 0;
            double res = 0;
            for (FLowRelation fr : frs) {
                int flowId1 = fr.getFlowId1();
                int flowId2 = fr.getFlowId2();
                BasicFlow flow1 = DBUtil.getFLowById(flowId1);
                BasicFlow flow2 = DBUtil.getFLowById(flowId2);
                if (null == flow1 || null == flow2) {
                    continue;
                }

                BasicFlow mflow1 = null;
                Optional<BasicFlow> mflow1_ops = matchFlows.stream().filter(item -> (item.getServerHost().equals(flow1.getServerHost()) || item.getDstIp().equals(flow1.getDstIp())) && item.getDstPort() == flow1.getDstPort()).findFirst();
                if (mflow1_ops.isPresent()) {
                    mflow1 = mflow1_ops.get();
                }
                BasicFlow mflow2 = null;
                Optional<BasicFlow> mflow2_ops = matchFlows.stream().filter(item -> (item.getServerHost().equals(flow2.getServerHost()) || item.getDstIp().equals(flow1.getDstIp())) && item.getDstPort() == flow2.getDstPort()).findFirst();
                if (mflow2_ops.isPresent()) {
                    mflow2 = mflow2_ops.get();
                }

                if (mflow1 == null) {
                    double max = 0;
                    BasicFlow tFlow = null;
                    for (BasicFlow flow : matchFlows) {
                        double fcs = getFlowFeatureCosineSimilarity(flow1.getFeature(), flow.getFeature());
                        if (fcs>max) {
                            max = fcs;
                            tFlow = flow;
                        }
                    }
                    mflow1 = tFlow;
                }
                if (mflow2 == null) {
                    double max = 0;
                    BasicFlow tFlow = null;
                    for (BasicFlow flow : matchFlows) {
                        double fcs = getFlowFeatureCosineSimilarity(flow2.getFeature(), flow.getFeature());
                        if (fcs>max) {
                            max = fcs;
                            tFlow = flow;
                        }
                    }
                    mflow2 = tFlow;
                }

                if (null== mflow1 || null == mflow2) {
                    continue;
                }
                relCount++;
                double cs1 = getFlowFeatureCosineSimilarity(flow1.getFeature(), mflow1.getFeature());
                double cs2 = getFlowFeatureCosineSimilarity(flow2.getFeature(), mflow2.getFeature());
                if (fr.isPO) {
                    if (fr.POtype == mflow1.getTimestamp().before(mflow2.getTimestamp())) {
                        res += cs1*cs2;
                    } else {
                        res += 0;
                    }
                } else {
                    res += cs1 * (fr.flowcount1> fr.flowcount2? (double)fr.flowcount2/fr.flowcount1 : (double)fr.flowcount1/fr.flowcount2) * cs2;
                }
            }
            if (relCount!=0) {
                contextDegree = res/relCount;
            } else {
                contextDegree = 0;
            }
        }
        log.info("inevitableDegree:" + inevitableDegree + ",contextDegree:"+contextDegree + ",pointDegree:"+pointDegree+",sizeDegree:"+sizeDegree);
        return alpha * (omega * inevitableDegree +  phi * contextDegree) + beta * pointDegree + gamma * sizeDegree;
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

    public static void main(String[] args) throws Exception {
//        ParseUtil.test();
//        ParseUtil.buildMultiFlow(System.getProperty("user.dir") + File.separator + "csv" + File.separator);
        String timestampFile = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\pcaps\\timestamp.txt";
        String pcapFIle = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\pcaps\\com.vkontakte.android.pcap";
        String csvPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\csvs";
//        String sniPath = "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\sni.txt";
        setSNI(pcapFIle);
        //        extract(timestampFile, pcapFIle, csvPath);

        //del
        File csv = new File("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\output\\com.vkontakte.android-2023-03-21_17-15-33\\csvs\\00000000-0000-001e-ffff-ffff000001f4_3_1.pcap_Flow.csv");
        List<BasicFlow> flows = getValidFlowsFromCsv(csv);
        flows.forEach(System.out::println);

        int fromDep = Integer.parseInt(String.valueOf(csv.toString().charAt(csv.toString().length()-17)));
        int toDep = Integer.parseInt(String.valueOf(csv.toString().charAt(csv.toString().length()-15)));
        String tag = csv.getName().split("\\.")[0];
        storeDB(flows, fromDep, toDep, tag);


        //del

    }

}
