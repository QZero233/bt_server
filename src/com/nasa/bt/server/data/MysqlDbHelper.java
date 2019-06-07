package com.nasa.bt.server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MysqlDbHelper {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/bt?serverTimezone=GMT%2B8";
    private static final String username = "bt";
    private static final String password = "bt";

    private Connection mConnection;

    private static MysqlDbHelper instance;

    public static final String USERS_TAB_NAME = "bt_users";
    public static final String SID_TAB_NAME = "bt_sid";
    public static final String MSG_TAB_NAME = "tempMessage";

    private MysqlDbHelper(){
        try{
            mConnection = (Connection) DriverManager.getConnection(url, username, password);
        }catch (Exception e){
            System.err.println("在打开mysql数据库连接时发生错误");
            e.printStackTrace();

        }
    }

    public static MysqlDbHelper getInstance(){
        if(instance==null)
            instance=new MysqlDbHelper();

        return instance;
    }

    /**
     * 执行sql安全检查
     * @param origin 原sql语句
     * @return sql安全处理后的sql语句
     */
    public static String sqlSecurity(String origin) {
        return origin.replaceAll("'", "");
    }

    public Connection getConnection() {
        return mConnection;
    }

    /**
     * 执行sql语句
     * @param sql sql语句
     * @return 被影响的行数，失败返回-1
     */
    public synchronized int execSQL(String sql) {
        //sql=sqlSecurity(sql);
        try {
            Statement statement=mConnection.createStatement();
            statement.execute(sql);
            return statement.getUpdateCount();
        }catch (Exception e) {
            System.err.println("执行sql语句时失败 "+sql);
            e.printStackTrace();
            return -1;
        }
    }


    public synchronized ResultSet execSQLQuery(String sql) {
      //  sql=sqlSecurity(sql);
        try {
            Statement statement=mConnection.createStatement();
            return statement.executeQuery(sql);
        }catch (Exception e) {
            System.err.println("执行sql查询语句时失败 "+sql);
            e.printStackTrace();
            return null;
        }
    }

}
