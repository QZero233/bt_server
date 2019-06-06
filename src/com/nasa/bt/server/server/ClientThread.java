package com.nasa.bt.server.server;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.UserInfo;
import com.nasa.bt.server.server.processor.DataProcessor;
import com.nasa.bt.server.server.processor.DataProcessorFactory;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端线程，在服务器接受了客户端之后所有事务都将交给这个类处理
 * @author QZero
 */
public class ClientThread extends Thread {
    //TODO P.S. 这里作者懒，直接用的public，日后有时间改了
    public Socket socket;
    public SocketIOHelper helper;
    public ServerManager parent;

    public UserInfo user=new UserInfo(null,null,null);

    public ClientThread(Socket socket, ServerManager parent) {
        this.socket = socket;
        this.parent = parent;
        try {
            helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("启动客户端线程时错误");
        }
    }

    @Override
    public void run() {
        super.run();

        //只要连接没断就一直读取数据包
        while(!socket.isClosed()){
            try {
                Datagram datagram=helper.readIs();
                String identifier=datagram.getIdentifier();
                DataProcessor processor= DataProcessorFactory.getProcessor(identifier);
                if(processor==null){
                    System.err.println("收到了未知的数据包 "+datagram);
                    continue;
                }
                processor.process(datagram,this);
            }catch (Exception e){
                e.printStackTrace();
                System.err.println("读取数据包失败，当前用户 "+user);
                break;
            }
        }
    }

    public void reportActionStatus(boolean status,String identifier,String more,String replyId){
        String statusStr="1";
        if(!status)
            statusStr="0";

        Map<String,byte[]> params=new HashMap<>();
        params.put("action_status",statusStr.getBytes());
        params.put("action_identifier",identifier.getBytes());
        params.put("mroe",more.getBytes());
        if(replyId!=null)
            params.put("reply_id",replyId.getBytes());

        Datagram datagram=new Datagram(DataProcessorFactory.IDENTIFIER_REPORT,params);
        helper.writeOs(datagram);
    }
}
