package com.nasa.bt.server.server;

import com.nasa.bt.server.ca.CAObject;
import com.nasa.bt.server.ca.CAUtils;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.crypt.CryptModuleRSA;
import com.nasa.bt.server.crypt.SHA256Utils;
import com.nasa.bt.server.data.dao.TrustedRemoteKeyDao;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.TrustedRemotePublicKeyEntity;
import org.apache.log4j.Logger;

import java.util.Map;

public class ServerHandShakeHelper {

    private SocketIOHelper helper;
    private ClientThread thread;
    private UserInfoDao userInfoDao;

    private static final Logger log=Logger.getLogger(ServerHandShakeHelper.class);

    public ServerHandShakeHelper(SocketIOHelper helper, ClientThread thread) {
        this.helper = helper;
        this.thread = thread;
        userInfoDao=thread.getUserInfoDao();
    }

    private ParamBuilder prepareHandShakeParam(String need){
        ParamBuilder result=new ParamBuilder();
        if(need.contains(SocketIOHelper.NEED_PUB_KEY)){
            result.putParam(SocketIOHelper.NEED_PUB_KEY, CryptModuleRSA.SERVER_PUB_KEY);
        }
        if(need.contains(SocketIOHelper.NEED_CA)){
            String caStr= CAUtils.readCAFile();
            result.putParam(SocketIOHelper.NEED_CA,caStr);
        }

        return result;
    }

    private boolean checkHandShakeParam(Map<String,String> params, String myNeed){
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

    private String getNeed(Map<String,String> needParam){
        String name=needParam.get("name");
        String pubKeyHash=needParam.get("keyHash");
        String need="";

        if(pubKeyHash==null){
            need+=SocketIOHelper.NEED_PUB_KEY+",";
        }else{
            TrustedRemoteKeyDao keyDao=new TrustedRemoteKeyDao();
            TrustedRemotePublicKeyEntity keyEntity=keyDao.getTrustedKey(name);
            if(keyEntity!=null && keyEntity.getPublicKeyHash().equals(pubKeyHash)){
                helper.initRSACryptModule(keyEntity.getPublicKey(),CryptModuleRSA.SERVER_PRI_KEY);
                return "";
            }else
                need+=SocketIOHelper.NEED_PUB_KEY+",";

        }

        if(userInfoDao.checkIfForceCA(name))
            need+=SocketIOHelper.NEED_CA;
        return need;
    }

    public boolean doHandShake(){
        String feedback= Datagram.HANDSHAKE_FEEDBACK_SUCCESS;
        //开始握手
        log.info("开始握手");
        /**
         * 0.发送需求参数
         * 1.发送需求
         * 2.获取需求
         * 3.发送对方需要的
         * 4.接收自己需要的
         * 5.反馈
         */

        ParamBuilder needParamSend=new ParamBuilder();
        needParamSend.putParam("keyHash", SHA256Utils.getSHA256InHex(CryptModuleRSA.SERVER_PUB_KEY));
        if(!sendNeedParam(needParamSend)){
            log.error("发送需求参数失败");
            return false;
        }

        Map<String,String> needParam=readNeedParam();
        if(needParam==null){
            log.error("读取客户端需求参数失败");
            return false;
        }

        String name=needParam.get("name");
        thread.setHandShakeName(name);

        String myNeed=getNeed(needParam);

        if(!sendNeed(myNeed)){
            log.error("发送需求失败");
            return false;
        }

        String dstNeed;
        if((dstNeed=readNeed())==null){
            log.error("读取对方需求失败");
            return false;
        }

        ParamBuilder handShakeParam=prepareHandShakeParam(dstNeed);
        if(!sendHandShakeParam(handShakeParam)){
            log.error("发送握手参数失败");
            return false;
        }

        Map<String,String> params;
        if((params=readHandShakeParam())==null){
            log.error("读取对方握手参数失败");
            return false;
        }

        if(!checkHandShakeParam(params,myNeed)){
            log.error("参数检查失败");
            feedback=Datagram.HANDSHAKE_FEEDBACK_CA_WRONG;
            sendFeedback(feedback);
            return false;
        }else{
            TrustedRemoteKeyDao keyDao=new TrustedRemoteKeyDao();
            keyDao.addTrustedKey(new TrustedRemotePublicKeyEntity(name,helper.getRSACryptModuleDstKey()));
        }

        if(!sendFeedback(feedback)){
            log.error("发送反馈失败");
            return false;
        }

        return readHandShakeFeedback();
    }

    private boolean readHandShakeFeedback(){
        Datagram datagram=helper.readIsNotEncrypted();
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

    public boolean sendNeed(String need){
        ParamBuilder paramBuilder=new ParamBuilder().putParam("need",need);
        Datagram datagram=new Datagram(Datagram.IDENTIFIER_NONE,paramBuilder.build());
        return helper.writeOsNotEncrypt(datagram);
    }

    public String readNeed(){
        Datagram datagram=helper.readIsNotEncrypted();
        if(datagram==null || !datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_NONE))
            return null;

        String need=datagram.getParamsAsString().get("need");
        return need;
    }

    public Map<String,String> readHandShakeParam(){
        Datagram datagram=helper.readIsNotEncrypted();
        if(datagram==null || !datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_NONE))
            return null;

        return datagram.getParamsAsString();
    }

    public boolean sendHandShakeParam(ParamBuilder param){
        if(param==null)
            return false;

        Datagram datagram=new Datagram(Datagram.IDENTIFIER_NONE,param.build());
        return helper.writeOsNotEncrypt(datagram);
    }

    public boolean sendFeedback(String feedback){
        Datagram datagram=new Datagram(Datagram.IDENTIFIER_NONE,new ParamBuilder().putParam("feedback",feedback).build());
        return helper.writeOsNotEncrypt(datagram);
    }

    public boolean sendNeedParam(ParamBuilder needParam){
        Datagram datagram=new Datagram(Datagram.IDENTIFIER_NONE,needParam.build());
        return helper.writeOsNotEncrypt(datagram);
    }

    public Map<String,String> readNeedParam(){
        return helper.readIsNotEncrypted().getParamsAsString();
    }




}
