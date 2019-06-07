package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;
import org.junit.Test;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LoginTest {
    @Test
    public void test() throws Exception{
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
        Thread.sleep(5000);


        //while (true);
    }

}
