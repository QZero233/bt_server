package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static void doLogin(SocketIOHelper helper) throws Exception{
        Map<String,byte[]> loginParams=new HashMap<>();

        loginParams.put("use_sid","no".getBytes());
        loginParams.put("username","qzero".getBytes());
        loginParams.put("code_hash","hash".getBytes());

        Datagram datagram=new Datagram("SIIN",loginParams);
        helper.writeOs(datagram);

    }

}
