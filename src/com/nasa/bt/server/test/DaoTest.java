package com.nasa.bt.server.test;

import com.nasa.bt.server.data.dao.SessionDao;
import com.nasa.bt.server.data.dao.TempMessageDao;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.utils.UUIDUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class DaoTest {

    private SessionDao sessionDao;
    private TempMessageDao tempMessageDao;
    private UserInfoDao userInfoDao;
    private static final Logger log=Logger.getLogger(DaoTest.class);

    @Before
    public void init(){
        sessionDao=new SessionDao();
        //tempMessageDao=new TempMessageDao();
        //userInfoDao=new UserInfoDao();
    }

    @Test
    public void testUserInfoDao(){
        UserInfoEntity userInfoUid=userInfoDao.getUserInfoByUid("testtest-1111-4360-b26e-7777454eaafc");
        UserInfoEntity userInfoName=userInfoDao.getUserInfoByName("test2");
        UserInfoEntity userInfoAuth=userInfoDao.checkAuth("test3","d0:4b:98:f4:8e:8f:8b:cc:15:c6:ae:5a:c0:50:80:1c:d6:dc:fd:42:8f:b5:f9:e6:5c:4e:16:e7:80:73:40:fa");
        UserInfoEntity userInfoAuth2=userInfoDao.checkAuth("test3","ff:4b:98:f4:8e:8f:8b:cc:15:c6:ae:5a:c0:50:80:1c:d6:dc:fd:42:8f:b5:f9:e6:5c:4e:16:e7:80:73:40:fa");

        log.info("ByUid "+userInfoUid);
        log.info("ByName "+userInfoName);
        log.info("ByAuth "+userInfoAuth);
        log.info("ByAuthWrong "+userInfoAuth2);

        //UserInfoEntity userAdd=new UserInfoEntity(UUIDUtils.getRandomUUID(),"test4");
        //boolean addResult=userInfoDao.addUser(userAdd,"code");
        //log.info("addResult "+addResult);

        boolean deleteResult=userInfoDao.deleteUser("dbd2e085-7af4-4a3c-b735-2ef773ac0c1f");
        log.info("deleteResult "+deleteResult);
    }

    @Test
    public void testTempMessage(){
        //TempMessageEntity messageEntityAdd=new TempMessageEntity("msgIdTestAdd","srcUid","dstUid","sessionId",System.currentTimeMillis(),"content233");
        //boolean addResult=tempMessageDao.addTempMessage(messageEntityAdd);
        //log.info("addResult "+addResult);

        TempMessageEntity messageEntityById=tempMessageDao.getMessage("msgIdTestAdd");
        log.info("byId "+messageEntityById);

        String indexes=tempMessageDao.getUnreadMessageIndexes("dstUid");
        log.info("unread "+indexes);

        boolean deleteResult=tempMessageDao.deleteMessage("msgIdTestAdd");
        log.info("deleteResult "+deleteResult);
    }

    @Test
    public void testSession(){
        //SessionEntity sessionEntityAdd=new SessionEntity("sessionIdAdd",SessionEntity.TYPE_NORMAL,"srcUid","dstUid",null);
        //boolean addResult=sessionDao.addSession(sessionEntityAdd);
        //log.info("addResult "+addResult);

        String sessionIndexes=sessionDao.getSessionIndexes("srcUid");
        log.info("indexes "+sessionIndexes);

        SessionEntity sessionEntityById=sessionDao.getSession(sessionIndexes);
        log.info("byId "+sessionEntityById);

        boolean deleteResult=sessionDao.deleteSession(sessionIndexes);
        log.info("deleteResult "+deleteResult);
    }

}
