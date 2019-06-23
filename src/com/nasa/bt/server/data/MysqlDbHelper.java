package com.nasa.bt.server.data;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MysqlDbHelper {

    private static final Logger log=Logger.getLogger(MysqlDbHelper.class);

    private static final String url = "jdbc:mysql://127.0.0.1:3306/bt?serverTimezone=GMT%2B8";
    private static final String username = "bt";
    private static final String password = "bt";

    private Connection mConnection;

    public static final String MSG_TAB_NAME = "temp_message";
    public static final String USER_INFO_TAB_NAME = "bt_user_info";

    private static MysqlDbHelper instance;

    private MysqlDbHelper(){
        try{
            mConnection =  DriverManager.getConnection(url, username, password);
        }catch (Exception e){
            log.error("在打开mysql数据库连接时发生错误",e);
        }
    }

    public static MysqlDbHelper getInstance(){
        if(instance==null)
            instance=new MysqlDbHelper();
        instance.checkConnectionStatus();
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

    /**
     * 检查当前数据库连接状态，若断开则重连
     */
    public synchronized void checkConnectionStatus(){
        try {
            if(mConnection.isClosed())
                mConnection =  DriverManager.getConnection(url, username, password);
        }catch (Exception e){
            log.error("在检测连接是否关闭时异常",e);
        }
    }

    /**
     * 执行sql语句
     * @param sql sql语句
     * @return 被影响的行数，失败返回-1
     */
    public synchronized int execSQL(String sql) {
        try {
            checkConnectionStatus();
            Statement statement=mConnection.createStatement();
            statement.execute(sql);
            return statement.getUpdateCount();
        }catch (Exception e) {
            log.error("执行sql语句时失败 "+sql,e);
            return -1;
        }
    }


    public synchronized ResultSet execSQLQuery(String sql) {
        try {
            checkConnectionStatus();
            Statement statement=mConnection.createStatement();
            return statement.executeQuery(sql);
        }catch (Exception e) {
            log.error("执行sql查询语句时失败 "+sql,e);
            return null;
        }
    }

}
