package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.List;

public class RefreshProcessor implements DataProcessor {

    private TempMessageDao tempMessageDao;


    @Override
    public void process(Datagram datagram, ClientThread thread) {
        tempMessageDao=thread.getTempMessageDao();

        List<TempMessageEntity> unread=tempMessageDao.getAllUnreadMessage(thread.getCurrentUser().getId());
        sendUnreadMessage(thread,unread);


        thread.reportActionStatus(true,datagram.getIdentifier(),null,null);
    }

    private void sendUnreadMessage(ClientThread thread,List<TempMessageEntity> unread){
        if(unread==null || unread.isEmpty())
            return;
        for(TempMessageEntity messageEntity:unread){
            Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_MESSAGE_DETAIL,new ParamBuilder().putParam("msg", JSON.toJSONString(messageEntity)).build());
            thread.writeDatagram(returnDatagram);
        }
    }
}
