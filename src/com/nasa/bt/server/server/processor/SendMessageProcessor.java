package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.server.ClientThread;
import org.apache.log4j.Logger;

import java.util.Map;

public class SendMessageProcessor implements DataProcessor {

    private static final Logger log=Logger.getLogger(SendMessageProcessor.class);

    private SessionDao sessionDao;
    private TempMessageDao tempMessageDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        sessionDao=thread.getSessionDao();
        tempMessageDao=thread.getTempMessageDao();

        Map<String,String> params=datagram.getParamsAsString();
        TempMessageEntity msg= JSON.parseObject(params.get("msg"),TempMessageEntity.class);
        msg.setSrcUid(thread.getCurrentUser().getId());

        String msgId=msg.getMsgId();

        SessionEntity session=sessionDao.getSession(msg.getSessionId());

        if(session==null)
            return;
        if(!session.checkInSession(thread.getCurrentUser().getId()) || !session.checkInSession(msg.getDstUid())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不在会话中",msgId);
            return;
        }

        String dstUid=session.getIdOfOther(thread.getCurrentUser().getId());

        if(dstUid.equals(thread.getCurrentUser().getId())){
            thread.reportActionStatus(false,datagram.getIdentifier(),"不能给自己发消息",msgId);
            return;
        }


        TempMessageEntity messageEntityWrite=new TempMessageEntity(msgId,thread.getCurrentUser().getId(),dstUid,msg.getSessionId(),System.currentTimeMillis(),msg.getContent());

        if(tempMessageDao.addTempMessage(messageEntityWrite)){
            log.debug("消息 "+msg+" 添加成功");
            thread.reportActionStatus(true,datagram.getIdentifier(),"",msgId);
            thread.remind(dstUid);
        }else{
            thread.reportActionStatus(false,datagram.getIdentifier(),"写入数据库失败",msgId);
        }
    }



}
