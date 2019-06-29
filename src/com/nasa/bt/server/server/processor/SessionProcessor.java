package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.Session;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.HashMap;
import java.util.Map;

public class SessionProcessor implements DataProcessor {

    private void createSession(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionTypeStr=params.get("session_type");
        String uidDst=params.get("uid_dst");
        String paramsStr=params.get("params");

        String sessionId= UUIDUtils.getRandomUUID();
        int sessionType=Integer.parseInt(sessionTypeStr);

        Session session=new Session(sessionId,sessionType,thread.getCurrentUser().getId(),uidDst,params);
        if(thread.getDataUtils().insertSessionInfo(session)){
            thread.reportActionStatus(true,datagram.getIdentifier(),sessionId,null);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
        }
    }

    private void getSessionsId(ClientThread thread){
        String id=thread.getDataUtils().querySessionsId(thread.getCurrentUser().getId());

        Map<String,String> returnValue=new HashMap<>();
        returnValue.put("session_id",id);
        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_RETURN_SESSIONS_INDEX,returnValue,null);
        thread.writeDatagram(datagramReturn);
    }

    private void getSessionDetail(Datagram datagram,ClientThread thread){
        Map<String,String> params=datagram.getParamsAsString();
        String sessionId=params.get("session_id");

        Session session=thread.getDataUtils().querySessionInfo(sessionId);
        if(session==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
            return;
        }

        Map<String,String> returnValue=new HashMap<>();
        returnValue.put("session", JSON.toJSONString(returnValue));
        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_RETURN_SESSION_DETAIL,returnValue,null);
        thread.writeDatagram(datagramReturn);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        String identifier=datagram.getIdentifier();
        if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_CREATE_SESSION)){
            createSession(datagram,thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_GET_SESSIONS_INDEX)){
            getSessionsId(thread);
            return;
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_GET_SESSION_DETAIL)){
            getSessionDetail(datagram,thread);
            return;
        }
    }
}
