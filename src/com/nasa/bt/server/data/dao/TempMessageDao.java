package com.nasa.bt.server.data.dao;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

public class TempMessageDao {

    private static final Logger log=Logger.getLogger(TempMessageDao.class);
    private Session session;
    private UserInfoEntity currentUser;

    public TempMessageDao() {
        session= ConfigurationInstance.openSession();
    }

    /**
     * 在服务器上保存一条消息
     * @param message 信息实体类对象
     * @return 是否成功
     */
    public boolean addTempMessage(TempMessageEntity message){
        session.beginTransaction();
        session.save(message);
        session.getTransaction().commit();
        if(session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED) && ServerDataUtils.writeLocalMsgContent(message.getMsgId(),message.getContent()))
            return true;
        return false;
    }

    public String getUnreadMessageIndexes(String uid){
        Query query=session.createQuery("select msgId from TempMessageEntity where dstUid=?1");
        query.setParameter(1,uid);
        List resultList=query.list();
        if(resultList==null)
            return "";

        String result="";
        for(Object obj:resultList){
            result+=obj;
        }

        return result;
    }


    public TempMessageEntity getMessage(String msgId){
        Query query=session.createQuery("from TempMessageEntity where msgId=?1");
        query.setParameter(1,msgId);
        Object obj=query.uniqueResult();
        if(obj==null)
            return null;

        TempMessageEntity result= (TempMessageEntity) obj;

        if(!checkMessagePermission(result))
            return null;

        String content=ServerDataUtils.readLocalMsgContent(result.getMsgId());
        if(content==null)
            return null;

        result.setContent(content);
        return result;
    }

    public boolean deleteMessage(String msgId){
        TempMessageEntity deleteEntity=session.load(TempMessageEntity.class,msgId);
        if(!checkMessagePermission(deleteEntity))
            return false;

        session.beginTransaction();
        session.delete(deleteEntity);
        session.getTransaction().commit();
        if(session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED) && ServerDataUtils.deleteLocalMsgContent(msgId))
            return true;
        return false;
    }

    public List<TempMessageEntity> getAllUnreadMessage(String uid){
        String id=getUnreadMessageIndexes(uid);
        List<TempMessageEntity> result=new ArrayList<>();
        for(int i=0;i<id.length()/36;i++){
            result.add(getMessage(id.substring(i*36,(i+1)*36)));
        }
        return result;
    }

    private boolean checkMessagePermission(TempMessageEntity messageEntity){
        if(messageEntity==null || currentUser==null)
            return false;

        if(messageEntity.getDstUid().equals(currentUser.getId()))
            return true;
        return false;
    }

    public void setCurrentUser(UserInfoEntity currentUser) {
        this.currentUser = currentUser;
    }
}
