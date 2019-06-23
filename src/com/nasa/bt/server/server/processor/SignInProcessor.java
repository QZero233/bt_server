package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import org.apache.log4j.Logger;

import java.util.Map;

public class SignInProcessor implements DataProcessor {

    private static final Logger log=Logger.getLogger(SignInProcessor.class);

    private void onFailure(ClientThread thread, String more) {
        thread.reportActionStatus(false, Datagram.IDENTIFIER_SIGN_IN, more, null);
        log.info("处理用户登录失败 原因 " + more);
    }

    private void onSuccess(ClientThread thread, String more) {
        thread.reportActionStatus(true, Datagram.IDENTIFIER_SIGN_IN, more, null);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String, String> params = datagram.getParamsAsString();

        String name = params.get("username");
        String codeHash = params.get("code_hash");

        UserInfo user = ServerDataUtils.getUserInfoByName(name);
        if (user == null) {
            onFailure(thread, "用户不存在");
            return;
        }

        if (!user.getCodeHash().equals(codeHash)) {
            onFailure(thread, "code错误");
            return;
        }

        thread.setCurrentUser(user);
        onSuccess(thread, user.getId());
    }
}
