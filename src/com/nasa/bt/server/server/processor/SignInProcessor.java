package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.LoginInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

public class SignInProcessor implements DataProcessor {

    private void onFailure(ClientThread thread, String more) {
        thread.reportActionStatus(false, DataProcessorFactory.IDENTIFIER_SIGN_IN, more, null);
        System.out.println("处理用户登录失败 原因 " + more);
    }

    private void onSuccess(ClientThread thread, String more) {
        thread.reportActionStatus(true, DataProcessorFactory.IDENTIFIER_SIGN_IN, more, null);
        thread.parent.addClient(thread, thread.user.getId());
        System.out.println("处理用户登录成功 " + more);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String, String> params = datagram.getParamsAsString();

        String name = params.get("username");
        String codeHash = params.get("code_hash");

        LoginInfo info = ServerDataUtils.getLoginInfoByName(name);
        if (info == null) {
            onFailure(thread, "用户不存在");
            return;
        }

        if (!info.getCodeHash().equals(codeHash)) {
            onFailure(thread, "code错误");
            return;
        }

        thread.user = info;
        onSuccess(thread, "");
    }
}
