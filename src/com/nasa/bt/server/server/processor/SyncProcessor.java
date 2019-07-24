package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.cls.UpgradeStatus;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.List;

public class SyncProcessor implements DataProcessor {

    private SessionDao sessionDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        sessionDao=thread.getSessionDao();
        String sessionIds=datagram.getParamsAsString().get("session_id");
        List<SessionEntity> result=sessionDao.getAllSessionExcept(sessionIds,thread.getCurrentUser().getId());

        sendVerCode(thread);
        sendSessions(thread,result);

        thread.reportActionStatus(true,datagram.getIdentifier(),null,null);
    }

    private void sendVerCode(ClientThread clientThread){
        UpgradeStatus upgradeStatus= ServerDataUtils.readUpgradeStatus();
        int verCode=0;
        if(upgradeStatus!=null){
            verCode=upgradeStatus.getNewestVerCode();
        }

        Datagram datagram=new Datagram(Datagram.IDENTIFIER_UPGRADE_VER_CODE,new ParamBuilder().putParam("ver_code",verCode+"").build());
        clientThread.writeDatagram(datagram);
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
