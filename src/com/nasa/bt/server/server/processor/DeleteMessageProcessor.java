package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

public class DeleteMessageProcessor implements DataProcessor {

    private TempMessageDao tempMessageDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        tempMessageDao=thread.getTempMessageDao();

        Map<String,String> params=datagram.getParamsAsString();
        String msgId=params.get("msg_id");

        if(datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_MARK_READ)){
            //标记消息已读
            String srcUid=params.get("src_uid");

            TempMessageEntity msgReadMark=new TempMessageEntity(UUIDUtils.getRandomUUID(),"system",srcUid,"",System.currentTimeMillis(),msgId);
            tempMessageDao.addTempMessage(msgReadMark);
            thread.remind(srcUid);

            thread.reportActionStatus(true,datagram.getIdentifier(),null,msgId);

            return;
        }

        if(tempMessageDao.deleteMessage(msgId)){
            thread.reportActionStatus(true,datagram.getIdentifier(),null,msgId);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),null,msgId);
        }
    }
}
