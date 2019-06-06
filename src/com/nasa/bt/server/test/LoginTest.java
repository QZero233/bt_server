package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LoginTest {

    public static void main(String[] args) throws Exception {
        test();
    }


    public static void test() throws Exception{
        Map<String,byte[]> loginParams=new HashMap<>();

        loginParams.put("use_sid","no".getBytes());
        loginParams.put("username","qzero".getBytes());
        loginParams.put("code_hash","hash".getBytes());

        Datagram datagram=new Datagram("SIIN",loginParams);

        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        Thread.sleep(1000);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    System.out.println(helper.readIs());
                }

            }
        }.start();

        helper.writeOs(datagram);
        //Thread.sleep(1000);


        //while (true);

    }

}
