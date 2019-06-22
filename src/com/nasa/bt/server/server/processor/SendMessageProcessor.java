package com.nasa.bt.server.server.processor;

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

        String msgId=params.get("msg_id");
        String dstUid=params.get("dst_uid");
        String msg_content=params.get("msg_content");

        if(dstUid.equalsIgnoreCase(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不能给自己发消息",msgId);
            return;
        }

        if(ServerDataUtils.getUserInfoByUid(dstUid)==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"对方id不存在",msgId);
            return;
        }

        if(!ServerDataUtils.writeLocalMsgContent(msgId,msg_content)){
            thread.reportActionStatus(false,datagram.getIdentifier(),"本地文件写入失败",msgId);
            return;
        }

        Msg msg=new Msg(msgId,thread.getCurrentUser().getId(),dstUid,null,System.currentTimeMillis());
        if(ServerDataUtils.addMsg(msg)){
            log.info("消息 "+msgId+" 添加成功");
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
            thread.remind(dstUid);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",msgId);
        }

    }
}
