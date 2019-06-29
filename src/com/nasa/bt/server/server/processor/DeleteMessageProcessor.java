package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

@DatagramProcessor(identifier=Datagram.IDENTIFIER_DELETE_MESSAGE)
public class DeleteMessageProcessor implements DataProcessor {

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();
        String msgId=params.get("msg_id");

        if(datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_MARK_READ)){
            //标记消息已读
            String srcUid=params.get("src_uid");

            Msg msgReadMark=new Msg(UUIDUtils.getRandomUUID(),"system",srcUid,null,null,System.currentTimeMillis());
            thread.getDataUtils().addMsg(msgReadMark);
            thread.getDataUtils().writeLocalMsgContent(msgReadMark.getMsgId(),msgId);
            thread.remind(srcUid);

            return;
        }

        if(!thread.getDataUtils().checkMsgPermission(msgId,thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"权限错误",msgId);
            return;
        }



        if(thread.getDataUtils().deleteMessage(msgId)){
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"",msgId);
        }
    }
}
