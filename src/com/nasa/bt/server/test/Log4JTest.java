package com.nasa.bt.server.test;

import org.apache.log4j.Logger;

public class Log4JTest {

    private static final Logger logger=Logger.getLogger(Log4JTest.class);

    public static void main(String[] args) {
        logger.debug("这是调试消息");
        logger.error("这是错误消息");
    }
}
