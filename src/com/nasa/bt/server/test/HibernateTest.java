package com.nasa.bt.server.test;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.crypt.SHA256Utils;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.UserAuthInfoEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.utils.UUIDUtils;
import jdk.security.jarsigner.JarSigner;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class HibernateTest {

    private static Session session;

    @Before
    public void init(){
        Configuration configuration=new Configuration();
        //configuration.setProperty("connection.username","bt");
        //configuration.setProperty("connection.password","bt");
        configuration.configure();
        SessionFactory sessionFactory=configuration.buildSessionFactory();
        session=sessionFactory.openSession();
    }

    @Test
    public void testRead(){
        Query query=session.createQuery("from UserInfoEntity");
        List<UserInfoEntity> userInfoList=query.list();
        System.out.println(userInfoList);
    }

    @Test
    public void testWrite(){
        UserInfoEntity user=new UserInfoEntity(UUIDUtils.getRandomUUID(),"test-hibernate");
        UserAuthInfoEntity authInfo=new UserAuthInfoEntity("test-hibernate", SHA256Utils.getSHA256InHex("code"));

        Transaction transaction=session.beginTransaction();
        session.save(user);
        session.save(authInfo);
        transaction.commit();
    }

    @Test
    public void entityJsonTest(){
        SessionEntity sessionEntity= session.load(SessionEntity.class,"4d448809-0874-42b5-b253-b58592ef66c8");
        String json= JSON.toJSONString(sessionEntity);
        System.out.println(json);
    }

}
