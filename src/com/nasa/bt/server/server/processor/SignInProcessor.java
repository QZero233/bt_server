package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;
import com.nasa.bt.server.utils.UUIDUtils;

import java.util.Map;

public class SignInProcessor implements DataProcessor{

    private void onFailure(ClientThread thread,String more){
        thread.reportActionStatus(false,DataProcessorFactory.IDENTIFIER_SIGN_IN,more,null);
        System.out.println("处理用户登录失败 原因 "+more);
    }

    private void onSuccess(ClientThread thread,String more){
        thread.reportActionStatus(true,DataProcessorFactory.IDENTIFIER_SIGN_IN,more,null);
        System.out.println("处理用户登录成功 "+more);
    }

    @Override
    public void process(Datagram datagram, ClientThread thread) {
        Map<String,String> params=datagram.getParamsAsString();
        String useSid=params.get("use_sid");
        if(useSid.equals("yes")){
            //使用sid登录
            String sid=params.get("sid");
            String uid= ServerDataUtils.getUidBySid(sid);
            if(uid==null){
                onFailure(thread,"sid不存在");
                return;
            }

            UserInfo info=ServerDataUtils.getUserByUid(uid);
            if(info==null){
                onFailure(thread,"用户不存在");
                return;
            }

            thread.user=info;
            onSuccess(thread,"");
        }else{
            //使用账号密码登录
            String name=params.get("username");
            String codeHash=params.get("code_hash");

            UserInfo info=ServerDataUtils.getUserInfoByName(name);
            if(info==null){
                onFailure(thread,"用户不存在");
                return;
            }

            if(!info.getCodeHash().equals(codeHash)){
                onFailure(thread,"code错误");
                return;
            }

            thread.user=info;

            //发放sid，失败就...算了吧
            String sid= UUIDUtils.getRandomUUID();
            if(!ServerDataUtils.addSid(info.getId(),sid))
                onSuccess(thread,"");
            else
                onSuccess(thread,sid);

        }

    }
}
