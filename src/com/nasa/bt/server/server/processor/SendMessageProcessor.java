package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.cls.SecretChat;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import org.apache.log4j.Logger;

import java.util.Map;

public class SendMessageProcessor implements DataProcessor {

    private static final Logger log=Logger.getLogger(SendMessageProcessor.class);

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();
        Msg msg= JSON.parseObject(params.get("msg"),Msg.class);

        String msg_type=msg.getMsgType();
        if(msg_type.equalsIgnoreCase(Msg.MSG_TYPE_NORMAL)){
            log.info("收到普通消息，开始处理");
            processNormal(datagram,thread);
        }else if(msg_type.equalsIgnoreCase(Msg.MSG_TYPE_SECRET_1)){
            log.info("收到1级私密消息，开始处理");
            processSecret1(datagram,thread);
        }
    }

    private void processSecret1(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        Msg msg= JSON.parseObject(params.get("msg"),Msg.class);

        SecretChat secretChat=ServerDataUtils.getSecretChat(msg.getDstUid());
        if(secretChat==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"对方id不存在",msg.getMsgId());
            return;
        }

        String dstUid=(secretChat.getSrcUid().equals(thread.getCurrentUser().getId()))?secretChat.getDstUid():secretChat.getSrcUid();
        msg.setDstUid(dstUid);
        msg.setSrcUid(msg.getDstUid());
        msg.setTime(System.currentTimeMillis());
        processMsg(msg,datagram,thread);
    }

    private void processNormal(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        Msg msg= JSON.parseObject(params.get("msg"),Msg.class);
        processMsg(msg,datagram,thread);
    }

    private void processMsg(Msg msg,Datagram datagram,ClientThread thread){
        String msgId=msg.getMsgId();
        String dstUid=msg.getDstUid();

        if(dstUid.equalsIgnoreCase(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不能给自己发消息",msgId);
            return;
        }

        if(ServerDataUtils.getUserInfoByUid(dstUid)==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"对方id不存在",msgId);
            return;
        }

        if(!ServerDataUtils.writeLocalMsgContent(msgId,msg.getContent())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"本地文件写入失败",msgId);
            return;
        }

        msg=new Msg(msgId,thread.getCurrentUser().getId(),dstUid,null,msg.getMsgType(),System.currentTimeMillis());
        if(ServerDataUtils.addMsg(msg)){
            log.info("消息 "+msgId+" 添加成功");
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
            thread.remind(dstUid);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",msgId);
        }
    }

}
