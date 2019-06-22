package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Msg;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.server.SocketIOHelper;

import java.util.HashMap;
import java.util.Map;

public class GetMessageProcessor implements DataProcessor {
    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,byte[]> returnParams=new HashMap<>();
        if(datagram.getIdentifier().equalsIgnoreCase(DataProcessorFactory.IDENTIFIER_GET_MESSAGE_INDEX)){
            //获取索引
            String index= ServerDataUtils.getMessageIndex(thread.getCurrentUser().getId());
            returnParams.put("index",index.getBytes());
            Datagram returnDatagram=new Datagram(DataProcessorFactory.IDENTIFIER_RETURN_MESSAGE_INDEX,returnParams);
            thread.writeDatagram(returnDatagram);
        }else{
            //获取具体消息
            Map<String,String> params=datagram.getParamsAsString();
            String msgId=params.get("msg_id");

            Msg msg=ServerDataUtils.getMessageDetail(msgId);
            if(msg==null){
                thread.reportActionStatus(false,datagram.getIdentifier(),"获取消息具体内容失败",msgId);
                return;
            }

            if(!msg.getDstUid().equals(thread.getCurrentUser().getId())){
                thread.reportActionStatus(false,datagram.getIdentifier(),"访问权限错误",msgId);
                return;
            }

            returnParams.put("msg_id",msgId.getBytes());
            returnParams.put("src_uid",msg.getSrcUid().getBytes());
            returnParams.put("msg_content",msg.getContent().getBytes());
            returnParams.put("time", SocketIOHelper.longToByteArray(msg.getTime()));

            Datagram returnDatagram=new Datagram(DataProcessorFactory.IDENTIFIER_RETURN_MESSAGE_DETAIL,returnParams);
            thread.writeDatagram(returnDatagram);
        }
    }
}
