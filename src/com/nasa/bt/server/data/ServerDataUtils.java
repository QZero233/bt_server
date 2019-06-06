package com.nasa.bt.server.data;

import com.nasa.bt.server.cls.UserInfo;

import java.sql.ResultSet;

public class ServerDataUtils {

    private static MysqlDbHelper helper=null;
    static{
        if(helper==null)
            helper=MysqlDbHelper.getInstance();
    }

    /**
     * 根据sid查找uid
     * @param sid sid
     * @return uid，失败返回null
     */
    public static String getUidBySid(String sid){
        try {
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.SID_TAB_NAME+" WHERE sid='"+sid+"'");
            if(!resultSet.first())
                return null;
            String uid=resultSet.getString(resultSet.findColumn("id"));
            return uid;
        }catch (Exception e){
            System.err.println("在根据SID查找UID时错误，sid="+sid);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据uid查找用户信息
     * @param uid uid
     * @return 用户信息对象，失败返回null
     */
    public static UserInfo getUserByUid(String uid){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.USERS_TAB_NAME+" WHERE id='"+uid+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            System.err.println("根据UID查找用户时错误，uid="+uid);
            e.printStackTrace();
            return null;
        }
    }

    public static UserInfo getUserInfoByName(String name){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.USERS_TAB_NAME+" WHERE name='"+name+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            System.err.println("根据用户名称查找用户时错误，name="+name);
            e.printStackTrace();
            return null;
        }
    }

    public static UserInfo getUserInfoFromResultSet(ResultSet resultSet){
        try {
            if(!resultSet.first())
                return null;

            String id=resultSet.getString(resultSet.findColumn("id"));
            String name=resultSet.getString(resultSet.findColumn("name"));
            String codeHash=resultSet.getString(resultSet.findColumn("codeHash"));

            UserInfo userInfo=new UserInfo(id,name,codeHash);
            return userInfo;
        }catch (Exception e){
            System.err.println("在读取结果集并转为用户对象时错误");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加sid
     * @param uid uid
     * @param sid sid
     * @return 是否成功
     */
    public static boolean addSid(String uid,String sid){
        if(helper.execSQL("INSERT INTO "+MysqlDbHelper.SID_TAB_NAME+" (id,sid) VALUES ('"+uid+"','"+sid+"')")!=1)
            return false;
        return true;
    }
}
