package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.ClientThread;

/**
 * 数据包处理类通用接口
 * @author QZero
 */
public interface DataProcessor {

    /**
     * 处理数据包
     * @param datagram 数据包对象
     * @param thread 线程对象
     */
    void process(Datagram datagram, ClientThread thread);
}
