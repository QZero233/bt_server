package com.nasa.bt.server.data;

import com.nasa.bt.server.data.entity.SessionEntity;
import com.nasa.bt.server.data.entity.TempMessageEntity;
import com.nasa.bt.server.data.entity.UserInfoEntity;

public class PermissionChecker {

    private UserInfoEntity currentUser;

    public void setCurrentUser(UserInfoEntity currentUser) {
        this.currentUser = currentUser;
    }

    private boolean preCheck(){
        if(currentUser==null)
            return false;
        return true;
    }

    public boolean checkMessageReadAndWrite(TempMessageEntity messageEntity){
        try {
            if(!preCheck() || messageEntity==null)
                return false;

            if(messageEntity.getDstUid().equals(currentUser.getId()))
                return true;
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public boolean checkSessionReadAndWrite(SessionEntity sessionEntity){
        try {
            if(!preCheck() || sessionEntity==null)
                return false;

            String currentUid=currentUser.getId();
            if(sessionEntity.getDstUid().equals(currentUid) || sessionEntity.getSrcUid().equals(currentUid))
                return true;
            return false;
        }catch (Exception e){
            return false;
        }
    }



}
