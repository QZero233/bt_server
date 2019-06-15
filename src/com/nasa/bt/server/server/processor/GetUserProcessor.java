package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;

import java.util.HashMap;
import java.util.Map;

public class GetUserProcessor implements DataProcessor {

    private void getIndex(ClientThread thread){
        String index=ServerDataUtils.getUserIndex();
        Map<String,byte[]> returnParams=new HashMap<>();
        returnParams.put("index",index.getBytes());

        Datagram returnDatagram=new Datagram(DataProcessorFactory.IDENTIFIER_RETURN_USERS_INDEX,returnParams);
        thread.helper.writeOs(returnDatagram);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        if(datagram.getIdentifier().equalsIgnoreCase(DataProcessorFactory.IDENTIFIER_GET_USERS_INDEX)){
            getIndex(thread);
            return;
        }
        Map<String,String> params=datagram.getParamsAsString();
        String uid=params.get("uid");
        String name=params.get("name");
        if(uid==null && name==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"信息不能为空",null);
            return;
        }

        UserInfo info;
        if(uid!=null){
            info= ServerDataUtils.getUserInfoByUid(uid);
        }else{
            info=ServerDataUtils.getUserInfoByName(name);
        }

        Map<String,byte[]> returnParams=new HashMap<>();
        if(info==null){
            returnParams.put("exist","0".getBytes());
        }else{
            returnParams.put("uid",info.getId().getBytes());
            returnParams.put("name",info.getName().getBytes());
            returnParams.put("exist","1".getBytes());
        }

        Datagram returnDatagram=new Datagram(DataProcessorFactory.IDENTIFIER_RETURN_USER_INFO,returnParams);
        thread.helper.writeOs(returnDatagram);
    }
}
