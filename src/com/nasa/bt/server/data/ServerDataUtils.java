package com.nasa.bt.server.data;

import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.cls.ServerProperties;
import com.nasa.bt.server.cls.UserInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;

public class ServerDataUtils {

    private static final Logger log=Logger.getLogger(ServerDataUtils.class);

    static{
        helper=MysqlDbHelper.getInstance();
        new File("data/msg/").mkdirs();
    }

    public static final String MSG_ROOT_PATH="data/msg/";
    public static final String PROPERTIES_FILE_PATH="server.properties";

    private static MysqlDbHelper helper;


    /**
     * 根据uid查找用户信息
     * @param uid uid
     * @return 用户信息对象，失败返回null
     */
    public static UserInfo getUserInfoByUid(String uid){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.USER_INFO_TAB_NAME +" WHERE id='"+uid+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            log.error("根据UID查找用户时错误，uid="+uid,e);
            return null;
        }
    }

    public static UserInfo getUserInfoByName(String name){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.USER_INFO_TAB_NAME +" WHERE name='"+name+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            log.error("根据用户名称查找用户时错误，name="+name,e);
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


            return new UserInfo(name,id,codeHash);
        }catch (Exception e){
            log.error("在读取结果集并转为用户对象时错误",e);
            return null;
        }
    }

    public static String getUserIndex(){

        try {
            String sql="SELECT * FROM "+MysqlDbHelper.USER_INFO_TAB_NAME;
            ResultSet resultSet=helper.execSQLQuery(sql);
            if(!resultSet.first())
                return "";

            String result="";
            int index=resultSet.findColumn("id");
            do{
                result+=resultSet.getString(index);
            }while (resultSet.next());
            return result;
        }catch (Exception e){
            return "";
        }

    }


    /**
     * 把消息写入本地文件
     * @param index 索引
     * @param content 内容
     * @return 是否写入成功
     */
    public static boolean writeLocalMsgContent(String index,String content){
        File file=new File(MSG_ROOT_PATH,index);
        try{
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            return true;
        }catch (Exception e){
            log.error("在将消息内容写入文件时错误",e);
            return false;
        }
    }

    /**
     * 根据索引读出本地文件保存的消息内容
     * @param index 索引
     * @return 消息内容，错误返回null
     */
    public static String readLocalMsgContent(String index){
        File file=new File(MSG_ROOT_PATH,index);
        if(!file.exists())
            return null;

        try{
            FileInputStream fis=new FileInputStream(file);
            byte[] buf=new byte[(int) file.length()];
            fis.read(buf);
            fis.close();
            return new String(buf);
        }catch (Exception e){
            log.error("在读取消息文件时错误",e);
            return null;
        }
    }

    /**
     * 向数据库中添加一条信息
     * @param msg 信息对象
     * @return 是否成功
     */
    public static boolean addMsg(Msg msg){
        String sql="INSERT INTO "+MysqlDbHelper.MSG_TAB_NAME+" (msgId,srcUid,dstUid,time) VALUES ('"+msg.getMsgId()+"','"+msg.getSrcUid()+"','"+
                msg.getDstUid()+"',"+msg.getTime()+")";

        if(helper.execSQL(sql)!=1)
            return false;
        return true;
    }

    /**
     * 获取某用户的未读消息
     * @return 消息索引，一个字符串，每个索引36个字符，失败或不存在返回空字符串
     */
    public static String getMessageIndex(String uid){
        try {
            String sql="SELECT * FROM "+MysqlDbHelper.MSG_TAB_NAME+" WHERE dstUid='"+uid+"'";
            ResultSet resultSet=helper.execSQLQuery(sql);
            if(!resultSet.first())
                return "";
            String result="";
            int index=resultSet.findColumn("msgId");
            do{
                result+=resultSet.getString(index);
            }while (resultSet.next());
            return result;
        }catch (Exception e){
            log.error("在获取消息索引时失败 uid="+uid,e);
            return "";
        }
    }

    /**
     * 获取消息的具体内容
     * @param msgId 消息id
     * @return 消息具体内容，失败返回null
     */
    public static Msg getMessageDetail(String msgId){
        try {
            String sql="SELECT * FROM "+MysqlDbHelper.MSG_TAB_NAME+" WHERE msgId='"+msgId+"'";
            ResultSet resultSet=helper.execSQLQuery(sql);
            if(!resultSet.first())
                return null;

            String srcUid=resultSet.getString(resultSet.findColumn("srcUid"));
            String dstUid=resultSet.getString(resultSet.findColumn("dstUid"));
            String msgType=resultSet.getString(resultSet.findColumn("msgType"));
            long time=resultSet.getLong(resultSet.findColumn("time"));

            String content=readLocalMsgContent(msgId);
            if(content==null)
                return null;

            Msg msg=new Msg(msgId,srcUid,dstUid,content,msgType,time);
            return msg;
        }catch (Exception e){
            log.error("在获取消息具体内容时失败 msgId="+msgId,e);
            return null;
        }
    }

    /**
     * 删除信息
     * @param msgId 信息id
     * @return 是否成功
     */
    public static boolean deleteMessage(String msgId){
        Msg msg=getMessageDetail(msgId);
        if(msg==null)
            return false;

        File file=new File(MSG_ROOT_PATH,msgId);
        file.delete();

        if(helper.execSQL("DELETE FROM "+MysqlDbHelper.MSG_TAB_NAME+" WHERE msgId='"+msgId+"'")>=1)
            return true;
        return false;
    }

    public static ServerProperties readProperties(){
        ServerProperties defaultProperties=new ServerProperties(8848);

        File file=new File(PROPERTIES_FILE_PATH);
        if(!file.exists())
            return defaultProperties;

        /**
         * 配置文件格式
         * 第一行 服务器端口号
         */
        String propertiesStr;
        try{
            FileInputStream fis=new FileInputStream(file);
            byte[] buf=new byte[(int) file.length()];
            fis.read(buf);
            fis.close();
            propertiesStr=new String(buf);
        }catch (Exception e){
            log.error("在读取服务器配置文件时错误",e);
            return defaultProperties;
        }

        try {
            int port=Integer.parseInt(propertiesStr);
            return new ServerProperties(port);
        }catch (Exception e){
            log.error("在解析服务器配置文件时错误",e);
            return defaultProperties;
        }
    }

}
