package com.nasa.bt.server.data.dao;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.data.entity.UpdateRecordEntity;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;


import java.util.List;

public class UpdateRecordDao {

    private static final Logger log=Logger.getLogger(UserInfoDao.class);
    private Session session;

    public UpdateRecordDao() {
        session= ConfigurationInstance.openSession();
    }

    private String getQueryHqlWhere(String ids,long time){
        //String where="where (entityId='' or entityId='') and lastEditTime>=";
        String where="where (entityId='"+ids.substring(0,36)+"' ";
        for(int i=1;i<ids.length()/36;i++){
            String id=ids.substring(i*36,(i+1)*36);
            where+="or entityId='"+id+"' ";
        }
        where+=") and lastEditTime>="+time;
        return where;
    }

    public List<UpdateRecordEntity> getUpdateRecords(String id,long time){
        Query query=session.createQuery("from UpdateRecordEntity "+getQueryHqlWhere(id,time));
        return query.list();
    }

    public boolean addOrUpdateUpdateRecord(UpdateRecordEntity updateRecordEntity){
        session.beginTransaction();
        session.saveOrUpdate(updateRecordEntity);
        session.getTransaction().commit();
        return session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED);
    }

    public UpdateRecordEntity getUpdateRecord(String entityId){
        Query query=session.createQuery("from UpdateRecordEntity where entityId=?1");
        query.setParameter(1,entityId);
        return (UpdateRecordEntity) query.uniqueResult();
    }

}
