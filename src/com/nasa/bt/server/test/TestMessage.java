package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;
import com.nasa.bt.server.utils.UUIDUtils;
import org.junit.Test;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TestMessage {

    @Test
    public void sendMsgTest() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        TestUtils.doLogin(helper);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    System.out.println(helper.readIs());
                }

            }
        }.start();

        Map<String,byte[]> loginParams=new HashMap<>();

        loginParams.put("msg_id", UUIDUtils.getRandomUUID().getBytes());
        loginParams.put("dst_uid","wdnmd".getBytes());
        loginParams.put("msg_content","消灭人类暴政，世界属于三体".getBytes());

        Datagram datagram=new Datagram("MESG",loginParams);
        helper.writeOs(datagram);


        Thread.sleep(5000);
    }

    @Test
    public void getMsgTest() throws Exception{
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

        Datagram datagram=new Datagram("MEGI",null);
        helper.writeOs(datagram);

        Map<String,byte[]> detailParam=new HashMap<>();
        detailParam.put("msg_id","5e124e0a-0d9a-47c7-af0d-f8152ce00aeb".getBytes());

        Datagram datagram1=new Datagram("MEGD",detailParam);
        helper.writeOs(datagram1);

        Thread.sleep(5000);
    }

    @Test
    public void deleteMsgTest() throws Exception{
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

        Map<String,byte[]> detailParam=new HashMap<>();
        detailParam.put("msg_id","5e124e0a-0d9a-47c7-af0d-f8152ce00aeb".getBytes());

        Datagram datagram1=new Datagram("MEDE",detailParam);
        helper.writeOs(datagram1);

        Thread.sleep(5000);
    }

}
