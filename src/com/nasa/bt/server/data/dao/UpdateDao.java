package com.nasa.bt.server.data.dao;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.data.entity.UpdateEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.List;

public class UpdateDao {

    private static final Logger log=Logger.getLogger(UpdateDao.class);
    private Session session;
    private UserInfoEntity currentUser;

    public UpdateDao() {
        session= ConfigurationInstance.openSession();
    }

    public void setCurrentUser(UserInfoEntity currentUser) {
        this.currentUser = currentUser;
    }

    public boolean addUpdate(UpdateEntity updateEntity){
        session.beginTransaction();
        session.save(updateEntity);
        session.getTransaction().commit();
        return session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED);
    }

    public String getUpdateIndexes(String uid){
        Query query=session.createQuery("select updateId from UpdateEntity where dstUid=?1");
        query.setParameter(1,uid);
        List list=query.list();
        if(list==null || list.isEmpty())
            return "";

        String result="";
        for(Object obj:list){
            result+=obj;
        }
        return result;
    }

    public UpdateEntity getUpdate(String updateId){
        Query query=session.createQuery("from UpdateEntity where updateId=?1");
        query.setParameter(1,updateId);
        UpdateEntity result=(UpdateEntity) query.uniqueResult();
        if(!checkUpdatePermission(result))
            return null;
        return result;
    }

    public boolean deleteUpdate(String updateId){
        UpdateEntity updateEntity=session.load(UpdateEntity.class,updateId);
        if(!checkUpdatePermission(updateEntity))
            return false;

        session.beginTransaction();
        session.delete(updateEntity);
        session.getTransaction().commit();
        return session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED);
    }

    public List<UpdateEntity> getAllUpdate(String uid){
        Query query=session.createQuery("from UpdateEntity where dstUid=?1");
        query.setParameter(1,uid);
        return query.list();
    }

    private boolean checkUpdatePermission(UpdateEntity updateEntity){
        if(updateEntity==null || currentUser==null)
            return false;
        if(updateEntity.getDstUid().equalsIgnoreCase(currentUser.getId()))
            return true;
        return false;
    }
}
