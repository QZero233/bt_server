package com.nasa.bt.server;

import com.nasa.bt.server.data.ConfigurationInstance;
import com.nasa.bt.server.server.ServerManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * 服务器运行类
 * @author QZero
 */
public class ServerMain {

    private static final Logger logger=Logger.getLogger(ServerMain.class);

    public static void main(String[] args) {

        /**
         * BT服务器开发步骤
         * 1.打开Intellij IDEA，新建项目
         * 2.新建包，新建main类
         * 3.写main函数
         * 4.思考许久...
         * 5.IDEA 关闭 bilibili 启动
         */
        logger.info("正在初始化数据库连接");
        Session session= ConfigurationInstance.openSession();
        session.close();
        logger.info("数据库连接初始化完成");

        ServerManager manager=ServerManager.getInstance();
    }
}
