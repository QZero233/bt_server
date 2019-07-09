package com.nasa.bt.server.server;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.ActionReport;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.crypt.CryptModuleRSA;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.server.processor.DataProcessor;
import com.nasa.bt.server.server.processor.DataProcessorFactory;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端线程，在服务器接受了客户端之后所有事务都将交给这个类处理
 * @author QZero
 */
public class ClientThread extends Thread {

    private static final Logger log=Logger.getLogger(ClientThread.class);

    private Socket socket;
    private SocketIOHelper helper;
    private ServerManager parent;

    private UserInfoEntity currentUser=null;

    private SessionDao sessionDao=new SessionDao();
    private TempMessageDao tempMessageDao=new TempMessageDao();
    private UserInfoDao userInfoDao=new UserInfoDao();

    public ClientThread(Socket socket, ServerManager parent) {
        this.socket = socket;
        this.parent = parent;
        try {
            helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());
            helper.setPrivateKey(CryptModuleRSA.SERVER_PRI_KEY);
            log.info("新客户端连接成功");
        }catch (Exception e){
            log.error("启动客户端线程时错误",e);
        }

    }

    @Override
    public void run() {
        super.run();

        new Thread(){
            @Override
            public void run() {
                super.run();
                //启动新线程来发送公钥信息
                while(!helper.sendPublicKey(CryptModuleRSA.SERVER_PUB_KEY))
                    log.info("发送公钥失败，继续尝试");
            }
        }.start();

        //只要连接没断就一直读取数据包
        while(!socket.isClosed()){
            try {
                Datagram datagram=helper.readIs();
                if(datagram==null)
                    break;

                String identifier=datagram.getIdentifier();

                if(currentUser==null && !identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SIGN_IN)){
                    reportActionStatus(false,identifier,"在未登录前不能进行其他操作",null);
                    continue;
                }

                DataProcessor processor= DataProcessorFactory.getProcessor(identifier);
                if(processor==null){
                    log.info("收到了未知的数据包 "+datagram);
                    continue;
                }
                processor.process(datagram,this);
            }catch (Exception e){
                log.error("读取数据包失败，当前用户 "+currentUser,e);
                break;
            }
        }

        if(currentUser!=null)
            parent.removeClient(currentUser.getId());
    }

    public void reportActionStatus(boolean status,String identifier,String more,String replyId){
        String statusStr= ActionReport.STATUS_SUCCESS;
        if(!status)
            statusStr=ActionReport.STATUS_FAILURE;

        ActionReport report=new ActionReport(statusStr,identifier,replyId,more);

        Map<String,String> params=new HashMap<>();
        params.put("action_report", JSON.toJSONString(report));

        Datagram datagram=new Datagram(Datagram.IDENTIFIER_REPORT,params,"");
        helper.writeOs(datagram);
    }

    public boolean writeDatagram(Datagram datagram){
        return helper.writeOs(datagram);
    }

    public UserInfoEntity getCurrentUser(){
        return currentUser;
    }

    public void setCurrentUser(UserInfoEntity currentUser) {
        this.currentUser = currentUser;
        parent.addClient(this,currentUser.getId());

        sessionDao.setCurrentUser(currentUser);
        tempMessageDao.setCurrentUser(currentUser);

        log.info("用户 "+currentUser.getId()+" 登录成功");
    }

    public void remind(String uid){
        parent.remind(uid);
    }

    public void terminate(){
        try {
            socket.close();
        }catch (Exception e){
            log.error("在关闭连接时异常",e);;
        }
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    public TempMessageDao getTempMessageDao() {
        return tempMessageDao;
    }

    public UserInfoDao getUserInfoDao() {
        return userInfoDao;
    }
}
