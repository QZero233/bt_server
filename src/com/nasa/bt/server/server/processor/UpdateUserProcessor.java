package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;

import java.util.Map;

public class UpdateUserProcessor implements DataProcessor {
    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();

        String key=params.get("key");
        String uid=thread.user.getId();
        if(ServerDataUtils.updateUserInfo(key,uid)){
            thread.reportActionStatus(true,datagram.getIdentifier(),"",null);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"",null);
        }

    }
}
