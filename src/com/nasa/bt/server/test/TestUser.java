package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;
import com.nasa.bt.server.server.processor.DataProcessorFactory;
import org.junit.Test;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TestUser {

    @Test
    public void testGetUserIndex() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        TestUtils.doLogin(helper);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    Datagram datagram=helper.readIs();
                    System.out.println(datagram);
                    //System.out.println(datagram.getParamsAsString().get("index"));
                }

            }
        }.start();

        Datagram datagram=new Datagram("USID",null);
        helper.writeOs(datagram);
        Thread.sleep(5000);
    }

    @Test
    public void testGetUserInfo() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        TestUtils.doLogin(helper);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    Datagram datagram=helper.readIs();
                    System.out.println(datagram);
                    //System.out.println(datagram.getParamsAsString().get("index"));
                }

            }
        }.start();

        Map<String,byte[]> params=new HashMap<>();
        params.put("name","qz".getBytes());

        Datagram datagram=new Datagram("USIF",params);
        helper.writeOs(datagram);
        Thread.sleep(5000);
    }

    @Test
    public void testUpdateUserInfo() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        TestUtils.doLogin(helper);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    Datagram datagram=helper.readIs();
                    System.out.println(datagram);
                    //System.out.println(datagram.getParamsAsString().get("index"));
                }

            }
        }.start();

        Map<String,byte[]> params=new HashMap<>();
        params.put("key","qz".getBytes());

        Datagram datagram=new Datagram("IFUP",params);
        helper.writeOs(datagram);
        Thread.sleep(5000);
    }

}
