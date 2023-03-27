package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

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


}
