package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

public class SendMessageProcessor implements DataProcessor {

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();

        String msgId=params.get("msg_id");
        String dstUid=params.get("dst_uid");
        String msg_content=params.get("msg_content");

        if(ServerDataUtils.getLoginInfoByUid(dstUid)==null){
            System.err.println("发送消息失败-对方id "+dstUid+" 不存在");
            thread.reportActionStatus(false,datagram.getIdentifier(),"对方id不存在",msgId);
            return;
        }

        if(!ServerDataUtils.writeLocalMsgContent(msgId,msg_content)){
            System.err.println("本地文件写入失败");
            thread.reportActionStatus(false,datagram.getIdentifier(),"本地文件写入失败",msgId);
            return;
        }

        Msg msg=new Msg(msgId,thread.user.getId(),dstUid,null,System.currentTimeMillis());
        if(ServerDataUtils.addMsg(msg)){
            System.out.println("消息 "+msgId+" 添加成功");
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
            thread.parent.remind(dstUid);
        }else{
            System.err.println("写入数据库失败");
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",msgId);
        }

    }
}
