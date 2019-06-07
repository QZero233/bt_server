package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IOTest {

    public static void main(String[] args) throws Exception {

        ServerSocket ss=new ServerSocket(8848);
        Socket socket=ss.accept();
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        while(true){
            Datagram datagram=helper.readIs();
            System.out.println(datagram);
            Collection<byte[]> values=datagram.getParams().values();
            for(byte[] value:values){
                System.out.println(Arrays.toString(value));
            }
        }
    }

    @org.junit.Test
    public void testSent() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        Map<String,byte[]> params=new HashMap<>();

        byte[] buf1={1,2,3,4,5,6,7};
        params.put("wdnmd",buf1);

        byte[] buf2={8,8,4,8,2,3,3};
        params.put("nmlgb",buf2);

        Datagram datagram=new Datagram("MDZZ",params);
        helper.writeOs(datagram);


        while (true)
            Thread.sleep(5000);
    }
}
