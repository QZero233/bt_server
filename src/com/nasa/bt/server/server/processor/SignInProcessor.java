package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.dao.UserInfoDao;
import com.nasa.bt.server.data.entity.UserInfoEntity;
import com.nasa.bt.server.server.ClientThread;
import org.apache.log4j.Logger;

import java.util.Map;

public class SignInProcessor implements DataProcessor {

    private static final Logger log=Logger.getLogger(SignInProcessor.class);
    private UserInfoDao userInfoDao;

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        userInfoDao=thread.getUserInfoDao();
        Map<String, String> params = datagram.getParamsAsString();

        String name = params.get("username");
        String codeHash = params.get("code_hash");

        UserInfoEntity userInfoEntity=userInfoDao.checkAuth(name,codeHash);
        if(userInfoEntity==null){
            thread.reportActionStatus(false, Datagram.IDENTIFIER_SIGN_IN, "", null);
            log.debug("处理用户登录失败 name="+name+" codeHash="+codeHash);
            return;
        }

        thread.setCurrentUser(userInfoEntity);
        thread.reportActionStatus(true, Datagram.IDENTIFIER_SIGN_IN, userInfoEntity.getId(), null);
    }
}
