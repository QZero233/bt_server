package com.nasa.bt.server.data.dao;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.data.PermissionChecker;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.List;

public class SessionDao {

    private static final Logger log=Logger.getLogger(SessionDao.class);
    private Session session;
    private PermissionChecker permissionChecker;

    public SessionDao() {
        session= ConfigurationInstance.openSession();
    }

    public boolean addSession(SessionEntity sessionEntity){
        session.beginTransaction();
        session.save(sessionEntity);
        session.getTransaction().commit();
        if(session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED))
            return true;
        log.error("添加Session时事务处理失败 "+session.getTransaction().getStatus());
        return false;
    }

    public SessionEntity getSession(String sessionId){
        session.clear();
        Query query=session.createQuery("from SessionEntity where sessionId=?1");
        query.setCacheMode(CacheMode.IGNORE);
        query.setParameter(1,sessionId);

        SessionEntity sessionEntity= (SessionEntity) query.uniqueResult();

        if(!permissionChecker.checkSessionReadAndWrite(sessionEntity))
            return null;

        return sessionEntity;
    }

    public String getSessionIndexes(String uid){
        Query query=session.createQuery("select sessionId from SessionEntity where srcUid=?1 or dstUid=?2");
        query.setParameter(1,uid);
        query.setParameter(2,uid);

        List resultList=query.list();
        if(resultList==null)
            return "";

        String result="";
        for(Object obj:resultList){
            result+=obj;
        }

        return result;
    }

    public boolean deleteSession(String sessionId){
        SessionEntity sessionEntity=session.load(SessionEntity.class,sessionId);
        if(!permissionChecker.checkSessionReadAndWrite(sessionEntity))
            return false;

        session.beginTransaction();
        session.delete(sessionEntity);
        session.getTransaction().commit();
        if(session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED))
            return true;
        return false;
    }

    public String getExistNormalSession(String uid,String uidDst){
        Query query=session.createQuery("select sessionId from SessionEntity where dstUid=?1 and srcUid=?2 and sessionType=?3  or dstUid=?2 and srcUid=?1 and sessionType=?3");
        query.setParameter(1,uid);
        query.setParameter(2,uidDst);
        query.setParameter(3,SessionEntity.TYPE_NORMAL);
        Object obj=query.uniqueResult();
        if(obj==null)
            return null;
        return (String) obj;
    }

    public boolean updateSession(SessionEntity sessionEntity){
        if(!permissionChecker.checkSessionReadAndWrite(sessionEntity))
            return false;

        session.beginTransaction();
        session.update(sessionEntity);
        session.getTransaction().commit();
        return session.getTransaction().getStatus().equals(TransactionStatus.COMMITTED);
    }


    public List<SessionEntity> getAllSession(String uid){
        Query query=session.createQuery("from SessionEntity where srcUid=?1 or dstUid=?1");
        query.setParameter(1,uid);
        return query.list();
    }

    public List<SessionEntity> getAllSessionExcept(String sessionIds,String uid){
        String hql="from SessionEntity where (srcUid=?1 or dstUid=?1) ";
        for(int i=0;i<sessionIds.length()/36;i++){
            String subId=sessionIds.substring(i*36,(i+1)*36);
            hql+="and sessionId!='"+subId+"'";
        }
        Query query=session.createQuery(hql);
        query.setParameter(1,uid);

        return query.list();
    }

    public void setPermissionChecker(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }
}
