package com.nasa.bt.server.server;

import com.nasa.bt.server.ServerMain;
import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.data.ServerDataUtils;
import com.nasa.bt.server.server.processor.DataProcessorFactory;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 主服务器管理类
 * @author QZero
 */
public class ServerManager {

    private static final Logger log=Logger.getLogger(ServerManager.class);

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 当前服务器状态
     * 详情见代码中的常量
     */
    private int status=0;

    /**
     * 服务器状态 启动失败
     */
    public static final int STATUS_FAILED=-1;

    /**
     * 服务器状态 正在运行
     */
    public static final int STATUS_RUNNING=1;

    /**
     * 所有客户端的集合
     */
    private Map<String,ClientThread> clients=new HashMap<>();

    private ServerSocket ss;

    private static ServerManager instance;

    private ServerManager(){
        /*
        Q：程序员撒过的最多的谎是什么
        A：//TODO....
         */
        this.port=8848;//TODO 读取配置文件更改默认端口
        if(init())
            startListening();
    }

    /**
     * 初始化服务器
     */
    private boolean init(){
        try {
            ss=new ServerSocket(port);
            status=STATUS_RUNNING;
            log.info("服务器已启动......");
            return true;
        }catch (Exception e){
            log.error("在启动服务器时发生错误",e);
            status=STATUS_FAILED;
            return false;
        }
    }

    /**
     * 开始接受客户端连接
     */
    private void startListening(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    try {
                        Socket socket=ss.accept();
                        new ClientThread(socket,ServerManager.this).start();
                    }catch (Exception e){
                        log.error("在接受客户端时发送错误",e);
                    }
                }
            }
        }.start();
    }


    public void addClient(ClientThread thread,String uid){
        if(clients.get(uid)!=null){
            clients.get(uid).terminate();
        }
        clients.put(uid,thread);
        remind(uid);
    }

    public void removeClient(String uid){
        clients.get(uid).terminate();
        clients.remove(uid);
        log.info("用户 "+uid+" 已断开连接");
    }

    public static ServerManager getInstance(){
        if(instance==null)
            instance=new ServerManager();
        return instance;
    }

    public void remind(String uid){
        if(clients.get(uid)!=null){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Map<String,byte[]> returnParams=new HashMap<>();
                    String index= ServerDataUtils.getMessageIndex(clients.get(uid).getCurrentUser().getId());
                    returnParams.put("index",index.getBytes());
                    Datagram returnDatagram=new Datagram(DataProcessorFactory.IDENTIFIER_RETURN_MESSAGE_INDEX,returnParams);
                    clients.get(uid).writeDatagram(returnDatagram);
                }
            }.start();
        }
    }



}
