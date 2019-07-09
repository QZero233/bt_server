package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.HashMap;
import java.util.Map;

public class UserProcessor implements DataProcessor {

    private UserInfoDao userInfoDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        userInfoDao=thread.getUserInfoDao();
        Map<String,String> params=datagram.getParamsAsString();
        String uid=params.get("uid");
        String name=params.get("name");
        if(uid==null && name==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),"信息不能为空",null);
            return;
        }

        UserInfoEntity userInfoEntity;
        if(uid!=null){
            userInfoEntity=userInfoDao.getUserInfoByUid(uid);
        }else{
            userInfoEntity=userInfoDao.getUserInfoByName(name);
        }

        Map<String,byte[]> returnParams=new HashMap<>();
        if(userInfoEntity==null){
            returnParams.put("exist","0".getBytes());
        }else{
            returnParams.put("uid",userInfoEntity.getId().getBytes());
            returnParams.put("name",userInfoEntity.getName().getBytes());
            returnParams.put("exist","1".getBytes());
        }

        Datagram returnDatagram=new Datagram(Datagram.IDENTIFIER_RETURN_USER_INFO,returnParams);
        thread.writeDatagram(returnDatagram);
    }
}
