package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.UpdateDao;
import com.nasa.bt.server.data.entity.UpdateEntity;
import com.nasa.bt.server.server.ClientThread;

import java.util.Map;

public class UpdateProcessor implements DataProcessor {

    private UpdateDao updateDao;

    private void getIndex(ClientThread thread){
        String index=updateDao.getUpdateIndexes(thread.getCurrentUser().getId());
        Datagram datagram=new Datagram(Datagram.IDENTIFIER_UPDATE_INDEX,new ParamBuilder().putParam("update_id",index).build());
        thread.writeDatagram(datagram);
    }

    private void getDetail(Datagram datagram, ClientThread thread){
        Map<String,String> param=datagram.getParamsAsString();
        String updateId=param.get("update_id");

        UpdateEntity updateEntity=updateDao.getUpdate(updateId);
        if(updateEntity==null){
            thread.reportActionStatus(false,datagram.getIdentifier(),null,null);
            return;
        }

        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_UPDATE_DETAIL,new ParamBuilder().putParam("update", JSON.toJSONString(updateEntity)).build());
        thread.writeDatagram(datagramReturn);
    }

    private void deleteUpdate(Datagram datagram, ClientThread thread){
        Map<String,String> param=datagram.getParamsAsString();
        String updateId=param.get("update_id");
        thread.reportActionStatus(updateDao.deleteUpdate(updateId),datagram.getIdentifier(),null,updateId);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        updateDao=thread.getUpdateDao();

        String identifier=datagram.getIdentifier();
        if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_UPDATE)){
            deleteUpdate(datagram,thread);
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_INDEX)){
            getIndex(thread);
        }else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_DETAIL)){
            getDetail(datagram,thread);
        }
    }
}
