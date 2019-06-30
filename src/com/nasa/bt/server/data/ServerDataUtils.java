package com.nasa.bt.server.data;

import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.cls.ServerProperties;
import com.nasa.bt.server.cls.Session;
import com.nasa.bt.server.cls.UserInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;

public class ServerDataUtils {

    private static final Logger log=Logger.getLogger(ServerDataUtils.class);

    static{
        new File("data/msg/").mkdirs();
    }

    public static final String MSG_ROOT_PATH="data/msg/";
    public static final String PROPERTIES_FILE_PATH="server.properties";

    private MysqlDbHelper helper;

    public ServerDataUtils() {
        helper=new MysqlDbHelper();
    }

    public boolean checkAuth(String name,String codeHash){
        String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_AUTH_INFO+" WHERE name='"+name+"' and codeHash='"+codeHash+"'";
        ResultSet resultSet=helper.execSQLQuery(sql);
        try{
            if(!resultSet.first())
                return false;
            return true;
        }catch (Exception e){
            log.error("在验证身份读取数据集时错误",e);
            return false;
        }
    }

    /**
     * 根据uid查找用户信息
     * @param uid uid
     * @return 用户信息对象，失败返回null
     */
    public UserInfo getUserInfoByUid(String uid){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.TAB_NAME_USER_INFO +" WHERE id='"+uid+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            log.error("根据UID查找用户时错误，uid="+uid,e);
            return null;
        }
    }

    public UserInfo getUserInfoByName(String name){
        try{
            ResultSet resultSet=helper.execSQLQuery("SELECT * FROM "+MysqlDbHelper.TAB_NAME_USER_INFO +" WHERE name='"+name+"'");
            return getUserInfoFromResultSet(resultSet);
        }catch (Exception e){
            log.error("根据用户名称查找用户时错误，name="+name,e);
            return null;
        }
    }

    public UserInfo getUserInfoFromResultSet(ResultSet resultSet){
        try {
            if(!resultSet.first())
                return null;

            String id=resultSet.getString(resultSet.findColumn("id"));
            String name=resultSet.getString(resultSet.findColumn("name"));

            return new UserInfo(id,name);
        }catch (Exception e){
            log.error("在读取结果集并转为用户对象时错误",e);
            return null;
        }
    }


    /**
     * 把消息写入本地文件
     * @param index 索引
     * @param content 内容
     * @return 是否写入成功
     */
    public boolean writeLocalMsgContent(String index,String content){
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
    public String readLocalMsgContent(String index){
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
    public boolean addMsg(Msg msg){
        String sql="INSERT INTO "+MysqlDbHelper.TAB_NAME_MSG +" (msgId,srcUid,dstUid,sessionId,time) VALUES ('"+msg.getMsgId()+"','"+msg.getSrcUid()+"','"+msg.getDstUid()+"','"+
                msg.getSessionId()+"',"+msg.getTime()+")";

        if(helper.execSQL(sql)!=1)
            return false;
        return true;
    }

    /**
     * 获取某用户的未读消息
     * @return 消息索引，一个字符串，每个索引36个字符，失败或不存在返回空字符串
     */
    public String getMessageIndex(String uid){
        try {
            String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_MSG +" WHERE dstUid='"+uid+"'";
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
    public Msg getMessageDetail(String msgId){
        try {
            String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_MSG +" WHERE msgId='"+msgId+"'";
            ResultSet resultSet=helper.execSQLQuery(sql);
            if(!resultSet.first())
                return null;

            String srcUid=resultSet.getString(resultSet.findColumn("srcUid"));
            String dstUid=resultSet.getString(resultSet.findColumn("dstUid"));
            String sessionId=resultSet.getString(resultSet.findColumn("sessionId"));
            long time=resultSet.getLong(resultSet.findColumn("time"));

            String content=readLocalMsgContent(msgId);
            if(content==null)
                return null;

            Msg msg=new Msg(msgId,srcUid,dstUid,sessionId,content,time);
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
    public boolean deleteMessage(String msgId){
        Msg msg=getMessageDetail(msgId);
        if(msg==null)
            return false;

        File file=new File(MSG_ROOT_PATH,msgId);
        file.delete();

        if(helper.execSQL("DELETE FROM "+MysqlDbHelper.TAB_NAME_MSG +" WHERE msgId='"+msgId+"'")>=1)
            return true;
        return false;
    }

    /**
     * 读取服务器配置信息
     * @return 服务器配置信息的对象
     */
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

    /**
     * 向服务器数据库中插入一个对话记录
     * @param session 对话对象
     * @return 是否成功
     */
    public boolean insertSessionInfo(Session session){
        String sql="INSERT INTO "+MysqlDbHelper.TAB_NAME_SESSIONS+"(sessionId,sessionType,uidSrc,uidDst,params) VALUES ('"+session.getSessionId()
                +"',"+session.getSessionType()+",'"+session.getUidSrc()+"','"+session.getUidDst()+"','"+session.getParams()+"')";
        if(helper.execSQL(sql)==-1)
            return false;
        return true;
    }

    /**
     * 在数据库中查找一个对话
     * @param sessionId 对话id
     * @return 对话对象
     */
    public Session querySessionInfo(String sessionId){
        String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_SESSIONS+" WHERE sessionId='"+sessionId+"'";
        ResultSet resultSet=helper.execSQLQuery(sql);
        try{
            if(!resultSet.first())
                return null;

            int sessionType=resultSet.getInt(resultSet.findColumn("sessionType"));
            String uidSrc=resultSet.getString(resultSet.findColumn("uidSrc"));
            String uidDst=resultSet.getString(resultSet.findColumn("uidDst"));
            String params=resultSet.getString(resultSet.findColumn("params"));
            return new Session(sessionId,sessionType,uidSrc,uidDst,params);
        }catch (Exception e){
            log.error("在查询对话读取结果集时失败，对话id "+sessionId,e);
            return null;
        }
    }

    /**
     * 获取指定用户所有的会话id
     * @param uid 用户id
     * @return 会话id
     */
    public String querySessionsId(String uid){
        String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_SESSIONS+" WHERE uidSrc='"+uid+"' or uidDst='"+uid+"'";
        ResultSet resultSet=helper.execSQLQuery(sql);
        try{
            if(!resultSet.first())
                return null;

            String result="";
            do{
                String id=resultSet.getString(resultSet.findColumn("sessionId"));
                result+=id;
            }while (resultSet.next());

            return result;
        }catch (Exception e){
            log.error("在查询对话ID读取结果集时失败，用户ID "+uid,e);
            return null;
        }
    }

    /**
     * 检查一个普通的会话是否存在
     * @param uidAlpha UID1
     * @param uidBeta UID2
     * @return 存在返回ID，不存在返回null
     */
    public String checkNormalSessionExist(String uidAlpha,String uidBeta){
        String sql="SELECT * FROM "+MysqlDbHelper.TAB_NAME_SESSIONS+" WHERE uidDst='"+uidAlpha+"' and uidSrc='"+uidBeta+"' and sessionType="+Session.TYPE_NORMAL+" " +
                "or uidDst='"+uidBeta+"' and uidSrc='"+uidAlpha+"' and sessionType="+Session.TYPE_NORMAL;
        ResultSet resultSet=helper.execSQLQuery(sql);
        try{
            if(!resultSet.first())
                return null;
            return resultSet.getString(resultSet.findColumn("sessionId"));
        }catch (Exception e){
            log.error("检查一个普通的会话是否存在读取结果集时错误",e);
            return null;
        }
    }

    /**
     * 删除一个会话
     * @param sessionId 会话id
     * @return 是否成功
     */
    public boolean deleteSession(String sessionId){
        String sql="DELETE FROM "+MysqlDbHelper.TAB_NAME_SESSIONS+" WHERE sessionId='"+sessionId+"'";
        if(helper.execSQL(sql)!=-1)
            return true;
        return false;
    }

    public boolean checkMsgPermission(String msgId,String uid){
        Msg msg=getMessageDetail(msgId);
        if(msg==null)
            return false;

        return msg.getDstUid().equals(uid);
    }

}
