package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.cls.UpgradeStatus;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.UpdateRecordDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.UpdateRecordEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.List;

public class SyncProcessor implements DataProcessor {


    @Override
    public void process(Datagram datagram, ClientThread thread) {
        SessionDao sessionDao=thread.getSessionDao();
        UpdateRecordDao updateRecordDao=new UpdateRecordDao();

        String sessionIds=datagram.getParamsAsString().get("session_id");
        String lastSyncTime=datagram.getParamsAsString().get("last_sync_time");
        List<SessionEntity> result=sessionDao.getAllSessionExcept(sessionIds,thread.getCurrentUser().getId());
        List<UpdateRecordEntity> updateRecordEntities=null;

        try {
            updateRecordEntities=updateRecordDao.getUpdateRecords(sessionIds,Long.parseLong(lastSyncTime));
        }catch (Exception e){

        }


        sendVerCode(thread);
        sendUserInfoOfMine(thread);
        sendSessions(thread,result);
        sendUpdateRecords(thread,updateRecordEntities);


        thread.reportActionStatus(true,datagram.getIdentifier(),System.currentTimeMillis()+"",null);
    }

    private void sendUserInfoOfMine(ClientThread thread){
        ParamBuilder paramBuilder=new ParamBuilder();
        UserInfoEntity userInfoEntity=thread.getCurrentUser();
        paramBuilder.putParam("user_info",JSON.toJSONString(userInfoEntity));
        Datagram datagram=new Datagram(Datagram.IDENTIFIER_USER_INFO_MINE,paramBuilder.build());
        thread.writeDatagram(datagram);
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

    private void sendUpdateRecords(ClientThread thread, List<UpdateRecordEntity> updateRecordEntities){
        if(updateRecordEntities==null || updateRecordEntities.isEmpty())
            return;

        for(UpdateRecordEntity updateRecordEntity:updateRecordEntities){
            Datagram datagram=new Datagram(Datagram.IDENTIFIER_UPDATE_RECORD,new ParamBuilder().putParam("update_record",JSON.toJSONString(updateRecordEntity)).build());
            thread.writeDatagram(datagram);
        }
    }
}
