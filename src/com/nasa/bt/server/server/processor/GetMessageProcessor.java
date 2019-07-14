package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.HashMap;
import java.util.Map;

public class GetMessageProcessor implements DataProcessor {

    private TempMessageDao tempMessageDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        tempMessageDao=thread.getTempMessageDao();


        if(datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_MESSAGE_INDEX)){
            //获取索引
            String index= tempMessageDao.getUnreadMessageIndexes(thread.getCurrentUser().getId());
            Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_MESSAGE_INDEX,new ParamBuilder().putParam("index",index).build());
            thread.writeDatagram(returnDatagram);
        }else{
            //获取具体消息
            Map<String,String> params=datagram.getParamsAsString();
            String msgId=params.get("msg_id");

            TempMessageEntity msg=tempMessageDao.getMessage(msgId);

            if(msg==null){
                thread.reportActionStatus(false,datagram.getIdentifier(),"获取消息具体内容失败",msgId);
                return;
            }

            if(!msg.getDstUid().equals(thread.getCurrentUser().getId())){
                thread.reportActionStatus(false,datagram.getIdentifier(),"访问权限错误",msgId);
                return;
            }

            Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_MESSAGE_DETAIL,new ParamBuilder().putParam("msg", JSON.toJSONString(msg)).build());
            thread.writeDatagram(returnDatagram);
        }
    }
}
