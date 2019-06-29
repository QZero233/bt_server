package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;

import java.util.HashMap;
import java.util.Map;

public class UserProcessor implements DataProcessor {

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();
        String uid=params.get("uid");
        String name=params.get("name");
        if(uid==null && name==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"信息不能为空",null);
            return;
        }

        UserInfo info;
        if(uid!=null){
            info= thread.getDataUtils().getUserInfoByUid(uid);
        }else{
            info=thread.getDataUtils().getUserInfoByName(name);
        }

        Map<String,byte[]> returnParams=new HashMap<>();
        if(info==null){
            returnParams.put("exist","0".getBytes());
        }else{
            returnParams.put("uid",info.getId().getBytes());
            returnParams.put("name",info.getName().getBytes());
            returnParams.put("exist","1".getBytes());
        }

        Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_RETURN_USER_INFO,returnParams);
        thread.writeDatagram(returnDatagram);
    }
}
