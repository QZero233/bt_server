package com.nasa.bt.server.server;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.ca.CAObject;
import com.nasa.bt.server.ca.CAUtils;
import com.nasa.bt.server.cls.ActionReport;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.crypt.CryptModuleRSA;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.dao.UpdateDao;
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
    private UpdateDao updateDao=new UpdateDao();

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

    private ParamBuilder prepareHandShakeParam(String need){
        ParamBuilder result=new ParamBuilder();
        if(need.contains(SocketIOHelper.NEED_PUB_KEY)){
            result.putParam(SocketIOHelper.NEED_PUB_KEY,CryptModuleRSA.SERVER_PUB_KEY);
        }
        if(need.contains(SocketIOHelper.NEED_CA)){
            String caStr=CAUtils.readCAFile();
            result.putParam(SocketIOHelper.NEED_CA,caStr);
        }

        return result;
    }

    private boolean checkHandShakeParam(Map<String,String> params,String myNeed){
        /**
         * 如果有问题就返回false，没问题就跳过
         */
        String dstPubKey=params.get(SocketIOHelper.NEED_PUB_KEY);
        if(myNeed.contains(SocketIOHelper.NEED_PUB_KEY)){
            if(dstPubKey==null)
                return false;
            helper.initRSACryptModule(dstPubKey,CryptModuleRSA.SERVER_PRI_KEY);
        }
        if(myNeed.contains(SocketIOHelper.NEED_CA)){
            String ca=params.get(SocketIOHelper.NEED_CA);
            if(ca==null)
                return false;

            CAObject caObject=CAUtils.stringToCAObject(ca);
            if(!CAUtils.checkCA(caObject,dstPubKey))
                return false;
        }

        return true;
    }



    private boolean doHandShake(){
        String feedback=Datagram.HANDSHAKE_FEEDBACK_SUCCESS;
        //开始握手
        log.info("开始握手");
        /**
         * 1.发送需求
         * 2.获取需求
         * 3.发送对方需要的
         * 4.接收自己需要的
         * 5.反馈
         */

        //TODO 动态获取需求
        //String myNeed=SocketIOHelper.NEED_PUB_KEY+",";
        String myNeed=SocketIOHelper.NEED_PUB_KEY+","+SocketIOHelper.NEED_CA;
        if(!helper.sendNeed(myNeed)){
            log.error("发送需求失败");
            return false;
        }

        String dstNeed;
        if((dstNeed=helper.readNeed())==null){
            log.error("读取对方需求失败");
            return false;
        }

        ParamBuilder handShakeParam=prepareHandShakeParam(dstNeed);
        if(!helper.sendHandShakeParam(handShakeParam)){
            log.error("发送握手参数失败");
            return false;
        }

        Map<String,String> params;
        if((params=helper.readHandShakeParam())==null){
            log.error("读取对方握手参数失败");
            return false;
        }

        if(!checkHandShakeParam(params,myNeed)){
            log.error("参数检查失败");
            feedback=Datagram.HANDSHAKE_FEEDBACK_CA_WRONG;
            helper.sendFeedback(feedback);
            return false;
        }

        helper.sendFeedback(feedback);

        return true;
    }

    private boolean readHandShakeFeedback(){
        Datagram datagram=helper.readHandShakeFeedback();
        String feedback=datagram.getParamsAsString().get("feedback");
        if(feedback==null)
            return false;

        if(feedback.equalsIgnoreCase(Datagram.HANDSHAKE_FEEDBACK_SUCCESS)){
            return true;
        }else if(feedback.equalsIgnoreCase(Datagram.HANDSHAKE_FEEDBACK_CA_WRONG)){
            return false;
        }
        return false;
    }



    @Override
    public void run() {
        super.run();

        if(!doHandShake())
            return;
        if(!readHandShakeFeedback())
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

    public void setCurrentUser(UserInfoEntity currentUser) {
        this.currentUser = currentUser;
        parent.addClient(this,currentUser.getId());

        sessionDao.setCurrentUser(currentUser);
        tempMessageDao.setCurrentUser(currentUser);
        updateDao.setCurrentUser(currentUser);

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

    public UpdateDao getUpdateDao() {
        return updateDao;
    }
}
