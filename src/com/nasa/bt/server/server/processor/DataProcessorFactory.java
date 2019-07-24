package com.nasa.bt.server.server.processor;

import com.nasa.bt.server.cls.Datagram;

/**
 * 数据包处理类的工厂类
 * @author QZero
 */
public class DataProcessorFactory {

    /**
     * 根据标识符获取处理器
     * @param identifier 标识符
     * @return 处理器，不存在则返回null
     */
    public static DataProcessor getProcessor(String identifier){

        if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SIGN_IN))
            return new SignInProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SEND_MESSAGE))
            return new SendMessageProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_MESSAGE_INDEX) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_MESSAGE_DETAIL))
            return new GetMessageProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_MESSAGE) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_MARK_READ))
            return new DeleteMessageProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_USER_INFO))
            return new UserProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_CREATE_SESSION) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SESSION_DETAIL) ||
        identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SESSIONS_INDEX) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_SESSION) ||
        identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_SESSION))
            return new SessionProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_REFRESH))
            return new RefreshProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_INDEX) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPDATE_DETAIL)
                || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_DELETE_UPDATE))
            return new UpdateProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_SYNC))
            return new SyncProcessor();
        else if(identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPGRADE_DETAIL) || identifier.equalsIgnoreCase(Datagram.IDENTIFIER_UPGRADE_VER_CODE))
            return new UpgradeProcessor();

        return null;
    }
}
