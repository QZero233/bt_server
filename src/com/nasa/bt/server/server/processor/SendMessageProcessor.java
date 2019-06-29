package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
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
        msg.setSrcUid(thread.getCurrentUser().getId());
        processMsg(msg,datagram,thread);


    }


    private void processMsg(Msg msg,Datagram datagram,ClientThread thread){
        String msgId=msg.getMsgId();
        String dstUid=msg.getDstUid();

        if(dstUid.equalsIgnoreCase(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不能给自己发消息",msgId);
            return;
        }

        if(thread.getDataUtils().getUserInfoByUid(dstUid)==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"对方id不存在",msgId);
            return;
        }

        if(!thread.getDataUtils().writeLocalMsgContent(msgId,msg.getContent())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"本地文件写入失败",msgId);
            return;
        }

        msg=new Msg(msgId,msg.getSrcUid(),dstUid,msg.getSessionId(),null,System.currentTimeMillis());
        if(thread.getDataUtils().addMsg(msg)){
            log.debug("消息 "+msg+" 添加成功");
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
            thread.remind(dstUid);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",msgId);
        }
    }

}
