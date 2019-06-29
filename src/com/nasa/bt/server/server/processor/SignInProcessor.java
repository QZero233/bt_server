package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.server.ClientThread;
import org.apache.log4j.Logger;

import java.util.Map;

public class SignInProcessor implements DataProcessor {

    private static final Logger log=Logger.getLogger(SignInProcessor.class);

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String, String> params = datagram.getParamsAsString();

        String name = params.get("username");
        String codeHash = params.get("code_hash");

        if(!thread.getDataUtils().checkAuth(name,codeHash)){
            thread.reportActionStatus(false, Datagram.IDENTIFIER_SIGN_IN, "", null);
            log.debug("处理用户登录失败 name="+name+" codeHash="+codeHash);
            return;
        }

        UserInfo user = thread.getDataUtils().getUserInfoByName(name);
        if (user == null) {
            thread.reportActionStatus(false, Datagram.IDENTIFIER_SIGN_IN, "", null);
            log.debug("处理用户登录失败 获取用户错误");
            return;
        }

        thread.setCurrentUser(user);
        thread.reportActionStatus(true, Datagram.IDENTIFIER_SIGN_IN, user.getId(), null);
    }
}
