package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.data.dao.UpdateRecordDao;
import com.nasa.bt.server.data.entity.UpdateRecordEntity;
import com.nasa.bt.server.server.ClientThread;

public class UpdateRecordProcessor implements DataProcessor {

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        String entityId=datagram.getParamsAsString().get("entity_id");
        if(entityId==null)
            return;

        UpdateRecordDao updateRecordDao=new UpdateRecordDao();
        UpdateRecordEntity updateRecordEntity=updateRecordDao.getUpdateRecord(entityId);

        Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_UPDATE_RECORD,new ParamBuilder().putParam("update_record", JSON.toJSONString(updateRecordEntity)).build());
        thread.writeDatagram(datagramReturn);
    }
}
