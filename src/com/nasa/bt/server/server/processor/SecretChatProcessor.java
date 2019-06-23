package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.cls.SecretChat;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.HashMap;
import java.util.Map;

public class SecretChatProcessor implements DataProcessor {

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        String identifier=datagram.getIdentifier();
        if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_CREATE_SECRET_CHAT))
            createSecretChat(datagram,thread);
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_SECRET_CHAT))
            deleteSecretChat(datagram,thread);
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_GET_SECRET_CHAT))
            getSecretChat(datagram,thread);
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_GET_SECRET_CHAT_INDEX))
            getSecretChatIndex(datagram,thread);
    }

    private void getSecretChatIndex(Datagram datagram,ClientThread thread){
        String index=ServerDataUtils.getSecretChatIndex(thread.getCurrentUser().getId());

        Map<String,String> returnValue=new HashMap<>();
        returnValue.put("session_index",index);
        thread.writeDatagram(new Datagram(Datagram.IDENTIFIER_RETURN_SECRET_CHAT_INDEX,returnValue,null));
    }

    private void getSecretChat(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionId=params.get("session_id");

        SecretChat secretChat=ServerDataUtils.getSecretChat(sessionId);
        if(secretChat==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"会话不存在",sessionId);
            return;
        }
        if(!secretChat.getSrcUid().equals(thread.getCurrentUser().getId()) && !secretChat.getDstUid().equals(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"没有权限",sessionId);
            return;
        }

        Map<String,String> returnValue=new HashMap<>();
        returnValue.put("secret_chat",JSON.toJSONString(secretChat));
        thread.writeDatagram(new Datagram(Datagram.IDENTIFIER_RETURN_SECRET_CHAT,returnValue,null));
    }

    private void deleteSecretChat(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionId=params.get("session_id");

        SecretChat secretChat=ServerDataUtils.getSecretChat(sessionId);
        if(secretChat==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"会话不存在",sessionId);
            return;
        }
        if(!secretChat.getSrcUid().equals(thread.getCurrentUser().getId()) && !secretChat.getDstUid().equals(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"没有权限",sessionId);
            return;
        }

        if(ServerDataUtils.deleteSecretChat(sessionId)) {
            thread.reportActionStatus(true,datagram.getIdentifier(),"",sessionId);
            remindDstDelete(secretChat,thread);
        }else
            thread.reportActionStatus(false,datagram.getIdentifier(),"",sessionId);
    }

    private void createSecretChat(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();

        String dstName=params.get("dst_name");
        String keyHash=params.get("key_hash");

        UserInfo userInfo=ServerDataUtils.getUserInfoByName(dstName);
        if(userInfo==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"用户不存在",null);
            return;
        }

        String dstUid=userInfo.getId();

        if(dstUid==null || dstUid.equals(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不能和自己创建加密对话",null);
            return;
        }

        SecretChat secretChat=new SecretChat(UUIDUtils.getRandomUUID(),thread.getCurrentUser().getId(),dstUid,keyHash);
        if(ServerDataUtils.createSecretChat(secretChat)){
            Map<String,String> returnValue=new HashMap<>();
            returnValue.put("secret_chat", JSON.toJSONString(secretChat));
            thread.writeDatagram(new Datagram(Datagram.IDENTIFIER_RETURN_SECRET_CHAT,returnValue,null));
            thread.reportActionStatus(true,datagram.getIdentifier(),secretChat.getSessionId(),null);
            remindDst(secretChat,thread);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",null);
        }
    }

    private void remindDst(SecretChat secretChat,ClientThread thread){
        String dstUid=secretChat.getDstUid();
        String sessionId=secretChat.getSessionId();

        Msg msg=new Msg(UUIDUtils.getRandomUUID(),"secretChat",dstUid,sessionId,Msg.MSG_TYPE_NORMAL,System.currentTimeMillis());
        ServerDataUtils.addMsg(msg);
        thread.remind(dstUid);
    }

    private void remindDstDelete(SecretChat secretChat,ClientThread thread){
        String dstUid=secretChat.getDstUid();
        String sessionId=secretChat.getSessionId();

        Msg msg=new Msg(UUIDUtils.getRandomUUID(),"secretChatDelete",dstUid,sessionId,Msg.MSG_TYPE_NORMAL,System.currentTimeMillis());
        ServerDataUtils.addMsg(msg);
        thread.remind(dstUid);
    }
}
