package com.nasa.bt.server.server;

import com.nasa.bt.server.data.MysqlDbHelper;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 主服务器管理类
 * @author QZero
 */
public class ServerManager {

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
        //初始化sql数据库连接
        MysqlDbHelper.getInstance();
        try {
            ss=new ServerSocket(port);
            status=STATUS_RUNNING;
            return true;
        }catch (Exception e){
            System.err.println("在启动服务器时发生错误");
            e.printStackTrace();
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
                        System.err.println("在接受客户端时发送错误");
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    public void addClient(ClientThread thread,String uid){
        clients.put(uid,thread);
    }

    public void removeClient(String uid){
        clients.remove(uid);
    }

    public static ServerManager getInstance(){
        if(instance==null)
            instance=new ServerManager();
        return instance;
    }



}
