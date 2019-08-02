package com.nasa.bt.server.data.dao;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.data.entity.TrustedRemotePublicKeyEntity;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;


public class TrustedRemoteKeyDao {
    private static final Logger log=Logger.getLogger(TempMessageDao.class);
    private Session session;

    public TrustedRemoteKeyDao(){
        session= ConfigurationInstance.openSession();
    }

    public boolean addTrustedKey(TrustedRemotePublicKeyEntity keyEntity){
        session.beginTransaction();
        session.saveOrUpdate(keyEntity);
        session.getTransaction().commit();
        return session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED);
    }

    public TrustedRemotePublicKeyEntity getTrustedKey(String name){
        Query query=session.createQuery("from TrustedRemotePublicKeyEntity where name=?1");
        query.setParameter(1,name);
        return (TrustedRemotePublicKeyEntity) query.uniqueResult();
    }

}
