package utils;

import flow.BasicFlow;
import flow.FlowFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBUtil {
    public static Logger log = LoggerFactory.getLogger(DBUtil.class);

    private static final String URL = "jdbc:mysql:///appflowcrawler";
    private static final String username = "root";
    private static final String password = "111111";

    public static final String DATABASE = "appflowcrawler";
    public static final String APP_TABLE = DATABASE + ".apps";
    public static final String DEPTH_TABLE = DATABASE + ".depth";
    public static final String CONTEXT_TABLE = DATABASE + ".context";
    public static final String FLOWRELATION_TABLE = DATABASE + ".flowrelation";
    public static final String FLOWS_TABLE = DATABASE + ".flows";


    private static Connection connection = null;

    public static void initialize() {
        try {
            connection = DriverManager.getConnection(URL, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Fail to initialize MySQL Connection");
        }

    }

    public static ResultSet doQuery(String sql) {
        Connection con = getConnection();
        ResultSet res = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            res = statement.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to execute sql : " + sql);
        }
        return res;
    }

    public static int doUpdate(String sql) {
        Connection con = getConnection();
        int res = -1;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            res = statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to execute sql : " + sql);
        }
        return res;
    }

    public static Connection getConnection() {
        if (connection!=null)
            return connection;
        try {
            connection = DriverManager.getConnection(URL, username, password);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Fail to get MySQL Connection");
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        DBUtil.initialize();
        String sql = "Select * from test;";
        ResultSet resultSet = DBUtil.doQuery(sql);
        while(resultSet.next()){
            System.out.println(resultSet.getInt("id") + " : " + resultSet.getString("text"));
        }
    }

    public static BasicFlow getFLowById(int flowId) throws SQLException {
        String flow_query_sql = "SELECT * FROM " + DBUtil.FLOWS_TABLE + " WHERE `flowId` = " + flowId;
        ResultSet flow_query_rs = DBUtil.doQuery(flow_query_sql);
        BasicFlow flow = null;
        if (flow_query_rs.next()) {
            flow = new BasicFlow();
            flow.setId(flow_query_rs.getInt("flowId"));
            flow.setServerHost(flow_query_rs.getString("hostName"));
            flow.setDstPort(flow_query_rs.getInt("port"));
            flow.setTimestamp(flow_query_rs.getTimestamp("timestamp"));
            FlowFeature feature = new FlowFeature();
            feature.setTotalPktMaxLength(flow_query_rs.getDouble("totalPktMaxLength"));
            feature.setTotalPktMinLength(flow_query_rs.getDouble("totalPktMinLength"));
            feature.setTotalPktMeanLength(flow_query_rs.getDouble("totalPktMeanLength"));
            feature.setTotalPktStdLength(flow_query_rs.getDouble("totalPktStdLength"));
            feature.setFwdPktCount(flow_query_rs.getLong("fwdPktCount"));
            feature.setBwdPktCount(flow_query_rs.getLong("bwdPktCount"));
            feature.setFwdPktTotalLength(flow_query_rs.getDouble("fwdPktTotalLength"));
            feature.setFwdPktMaxLength(flow_query_rs.getDouble("fwdPktMaxLength"));
            feature.setFwdPktMinLength(flow_query_rs.getDouble("fwdPktMinLength"));
            feature.setFwdPktMeanLength(flow_query_rs.getDouble("fwdPktMeanLength"));
            feature.setFwdPktStdLength(flow_query_rs.getDouble("fwdPktStdLength"));
            feature.setBwdPktTotalLength(flow_query_rs.getDouble("bwdPktTotalLength"));
            feature.setBwdPktMaxLength(flow_query_rs.getDouble("bwdPktMaxLength"));
            feature.setBwdPktMinLength(flow_query_rs.getDouble("bwdPktMinLength"));
            feature.setBwdPktMeanLength(flow_query_rs.getDouble("bwdPktMeanLength"));
            feature.setBwdPktStdLength(flow_query_rs.getDouble("bwdPktStdLength"));
            flow.setFeature(feature);
        }
        return flow;
    }

    public static Set<String> getSNIFromDB(int appId) {
        Set<String> snis = null;
        String sni_query_sql = "SELECT DISTINCT hostName FROM " + DBUtil.FLOWS_TABLE;
        ResultSet sni_query_rs = DBUtil.doQuery(sni_query_sql);
        try {
            snis = new HashSet<>();
            while(sni_query_rs.next()) {
                snis.add(sni_query_rs.getString("hostName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Fail to get SNI from DB.");
        }

        return snis;
    }

    public static List<Integer> getAppsFromDB() throws SQLException {
        String app_query_sql = "SELECT * FROM " + DBUtil.APP_TABLE + ";";
        List<Integer> appIds = new ArrayList<>();
        ResultSet app_query_rs = DBUtil.doQuery(app_query_sql);
        while (app_query_rs.next()) {
            appIds.add(app_query_rs.getInt("appId"));
        }
        return appIds;
    }

}
