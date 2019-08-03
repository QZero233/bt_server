package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.UpdateRecordDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.UpdateRecordEntity;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

public class SessionProcessor implements DataProcessor {

    private SessionDao sessionDao;
    private UpdateRecordDao updateRecordDao;

    private void createSession(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionTypeStr=params.get("session_type");
        String uidDst=params.get("uid_dst");
        String paramsStr=params.get("params");


        int sessionType=Integer.parseInt(sessionTypeStr);
        if(sessionType == SessionEntity.TYPE_NORMAL){
            String sessionIdExists=sessionDao.getExistNormalSession(thread.getCurrentUser().getId(),uidDst);
            if(sessionIdExists!=null){
                thread.reportActionStatus(true,datagram.getIdentifier(),sessionIdExists,null);
                return;
            }
        }

        String sessionId= UUIDUtils.getRandomUUID();
        SessionEntity session=new SessionEntity(sessionId,sessionType,thread.getCurrentUser().getId(),uidDst,paramsStr);
        if(sessionDao.addSession(session)){
            thread.reportActionStatus(true,datagram.getIdentifier(),sessionId,null);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
        }

        thread.remind(uidDst);
    }

    private void getSessionsId(ClientThread thread){
        String id=sessionDao.getSessionIndexes(thread.getCurrentUser().getId());


        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_SESSIONS_INDEX,new ParamBuilder().putParam("session_id",id).build());
        thread.writeDatagram(datagramReturn);
    }

    private SessionEntity getSession(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionId=params.get("session_id");

        SessionEntity session=sessionDao.getSession(sessionId);

        if(session==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
            return null;
        }

        return session;
    }

    private void getSessionDetail(Datagram datagram,ClientThread thread){
        SessionEntity session=getSession(datagram,thread);
        if(session==null)
            return;

        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_SESSION_DETAIL,new ParamBuilder().putParam("session", JSON.toJSONString(session)).build());
        thread.writeDatagram(datagramReturn);
    }

    private void updateSession(Datagram datagram, ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionId=params.get("session_id");
        String newParams=params.get("params");

        SessionEntity sessionEntity=sessionDao.getSession(sessionId);
        if(sessionEntity==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
            return;
        }

        sessionEntity.setParams(newParams);
        if(!sessionDao.updateSession(sessionEntity))
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
        else{
            thread.reportActionStatus(true,datagram.getIdentifier(),null,null);
            String srcUid=thread.getCurrentUser().getId();
            String dstUid=sessionEntity.getIdOfOther(srcUid);

            UpdateRecordEntity updateRecordEntity=new UpdateRecordEntity(sessionId,UpdateRecordEntity.TYPE_SESSION,UpdateRecordEntity.STATUS_ALIVE,System.currentTimeMillis());
            updateRecordDao.addOrUpdateUpdateRecord(updateRecordEntity);
            thread.remind(dstUid);
        }
    }

    private void deleteSession(Datagram datagram, ClientThread thread){
        SessionEntity sessionEntity=getSession(datagram,thread);
        if(sessionEntity==null)
            return;

        String sessionId=sessionEntity.getSessionId();

        String uidDst=sessionEntity.getIdOfOther(thread.getCurrentUser().getId());

        if(sessionDao.deleteSession(sessionId)){
            thread.reportActionStatus(true,datagram.getIdentifier(),null,null);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
        }

        UpdateRecordEntity updateRecordEntity=new UpdateRecordEntity(sessionId,UpdateRecordEntity.TYPE_SESSION,UpdateRecordEntity.STATUS_DELETED,System.currentTimeMillis());
        updateRecordDao.addOrUpdateUpdateRecord(updateRecordEntity);
        thread.remind(uidDst);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        sessionDao=thread.getSessionDao();
        updateRecordDao=new UpdateRecordDao();

        String identifier=datagram.getIdentifier();
        if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_CREATE_SESSION)){
            createSession(datagram,thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SESSIONS_INDEX)){
            getSessionsId(thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SESSION_DETAIL)){
            getSessionDetail(datagram,thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_SESSION)){
            deleteSession(datagram,thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_SESSION)){
            updateSession(datagram,thread);
            return;
        }
    }
}
