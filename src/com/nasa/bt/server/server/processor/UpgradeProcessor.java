package com.nasa.bt.server.server.processor;

import com.alibaba.fastjson.JSON;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.ParamBuilder;
import com.nasa.bt.server.cls.UpgradeStatus;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.ClientThread;

public class UpgradeProcessor implements DataProcessor {
    @Override
    public void process(Datagram datagram, ClientThread thread) {
        UpgradeStatus upgradeStatus= ServerDataUtils.readUpgradeStatus();

        if(datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_UPGRADE_VER_CODE)){
            int verCode=0;
            if(upgradeStatus!=null){
                verCode=upgradeStatus.getNewestVerCode();
            }

            Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_UPGRADE_VER_CODE,new ParamBuilder().putParam("ver_code",verCode+"").build());
            thread.writeDatagram(datagramReturn);
        }else if(datagram.getIdentifier().equalsIgnoreCase(Datagram.IDENTIFIER_UPGRADE_DETAIL)){
            Datagram datagramReturn=new Datagram(Datagram.IDENTIFIER_UPGRADE_DETAIL,new ParamBuilder().putParam("upgrade_status", JSON.toJSONString(upgradeStatus)).build());
            thread.writeDatagram(datagramReturn);
        }
    }
}
