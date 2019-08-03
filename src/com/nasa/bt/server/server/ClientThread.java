package com.nasa.bt.server.server;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.ActionReport;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.PermissionChecker;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.dao.UpdateRecordDao;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.server.processor.DataProcessor;
import com.nasa.bt.server.server.processor.DataProcessorFactory;
import org.apache.log4j.Logger;

import java.net.Socket;

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
    private String name;

    private SessionDao sessionDao=new SessionDao();
    private TempMessageDao tempMessageDao=new TempMessageDao();
    private UserInfoDao userInfoDao=new UserInfoDao();

    private PermissionChecker permissionChecker=new PermissionChecker();

    public ClientThread(Socket socket, ServerManager parent) {
        this.socket = socket;
        this.parent = parent;
        try {
            helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());
            log.info("新客户端连接成功");
        }catch (Exception e){
            log.error("启动客户端线程时错误",e);
        }

    }

    @Override
    public void run() {
        super.run();

        ServerHandShakeHelper serverHandShakeHelper=new ServerHandShakeHelper(helper,this);
        if(!serverHandShakeHelper.doHandShake())
            return;

        //握手完成
        log.info("握手完成");
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

        Datagram datagram=new Datagram(Datagram.IDENTIFIER_REPORT,new ParamBuilder().putParam("action_report",JSON.toJSONString(report)).build());
        helper.writeOs(datagram);
    }

    public boolean writeDatagram(Datagram datagram){
        return helper.writeOs(datagram);
    }

    public UserInfoEntity getCurrentUser(){
        return currentUser;
    }

    public void setHandShakeName(String name){
        this.name=name;
    }

    public void setCurrentUser(UserInfoEntity currentUser) {
        if(!currentUser.getName().equals(name)){
            throw new IllegalArgumentException("错误，登录用户与需求参数中的用户名不一致");
        }

        this.currentUser = currentUser;
        parent.addClient(this,currentUser.getId());

        permissionChecker.setCurrentUser(currentUser);
        tempMessageDao.setPermissionChecker(permissionChecker);
        sessionDao.setPermissionChecker(permissionChecker);

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
