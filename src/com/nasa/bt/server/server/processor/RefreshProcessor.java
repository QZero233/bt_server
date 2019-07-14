package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.dao.UpdateDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.data.entity.UpdateEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.List;

public class RefreshProcessor implements DataProcessor {

    private TempMessageDao tempMessageDao;
    private SessionDao sessionDao;
    private UpdateDao updateDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        tempMessageDao=thread.getTempMessageDao();
        sessionDao=thread.getSessionDao();
        updateDao=thread.getUpdateDao();

        List<TempMessageEntity> unread=tempMessageDao.getAllUnreadMessage(thread.getCurrentUser().getId());
        sendUnreadMessage(thread,unread);

        List<SessionEntity> sessionEntityList=sessionDao.getAllSession(thread.getCurrentUser().getId());
        sendSessions(thread,sessionEntityList);

        List<UpdateEntity> updateEntityList=updateDao.getAllUpdate(thread.getCurrentUser().getId());
        sendUpdates(thread,updateEntityList);

        thread.reportActionStatus(true,datagram.getIdentifier(),null,null);
    }

    private void sendUpdates(ClientThread thread,List<UpdateEntity> updateEntityList){
        if(updateEntityList==null || updateEntityList.isEmpty())
            return;
        for(UpdateEntity updateEntity:updateEntityList){
            Datagram datagram=new Datagram(Datagram.IDENTIFIER_UPDATE_DETAIL,new ParamBuilder().putParam("update",JSON.toJSONString(updateEntity)).build());
            thread.writeDatagram(datagram);
        }
    }

    private void sendUnreadMessage(ClientThread thread,List<TempMessageEntity> unread){
        if(unread==null || unread.isEmpty())
            return;
        for(TempMessageEntity messageEntity:unread){
            Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_MESSAGE_DETAIL,new ParamBuilder().putParam("msg", JSON.toJSONString(messageEntity)).build());
            thread.writeDatagram(returnDatagram);
        }
    }

    private void sendSessions(ClientThread thread, List<SessionEntity> sessions){
        if(sessions==null || sessions.isEmpty())
            return;
        for(SessionEntity sessionEntity:sessions){
            Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_SESSION_DETAIL,new ParamBuilder().putParam("session", JSON.toJSONString(sessionEntity)).build());
            thread.writeDatagram(datagramReturn);
        }
    }

}
