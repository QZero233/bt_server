package com.nasa.bt.server.server.processor;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * 数据包处理类的工厂类
 * @author QZero
 */
public class DataProcessorFactory {

    public static final String IDENTIFIER_SIGN_IN="SIIN";
    public static final String IDENTIFIER_REPORT="REPO";
    public static final String IDENTIFIER_SEND_MESSAGE ="MESG";
    public static final String IDENTIFIER_GET_MESSAGE_INDEX="MEGI";
    public static final String IDENTIFIER_GET_MESSAGE_DETAIL="MEGD";
    public static final String IDENTIFIER_DELETE_MESSAGE="MEDE";
    public static final String IDENTIFIER_GET_USER_INFO="USIF";
    public static final String IDENTIFIER_GET_USERS_INDEX="USID";
    public static final String IDENTIFIER_MARK_READ="MKRD";


    public static final String IDENTIFIER_RETURN_MESSAGE_INDEX="MERI";
    public static final String IDENTIFIER_RETURN_MESSAGE_DETAIL="MERD";
    public static final String IDENTIFIER_RETURN_USER_INFO="USRF";
    public static final String IDENTIFIER_RETURN_USERS_INDEX="USRI";


    /**
     * 根据标识符获取处理器
     * @param identifier 标识符
     * @return 处理器，不存在则返回null
     */
    public static DataProcessor getProcessor(String identifier){

        if(identifier.equalsIgnoreCase(IDENTIFIER_SIGN_IN))
            return new SignInProcessor();
        else if(identifier.equalsIgnoreCase(IDENTIFIER_SEND_MESSAGE))
            return new SendMessageProcessor();
        else if(identifier.equalsIgnoreCase(IDENTIFIER_GET_MESSAGE_INDEX) || identifier.equalsIgnoreCase(IDENTIFIER_GET_MESSAGE_DETAIL))
            return new GetMessageProcessor();
        else if(identifier.equalsIgnoreCase(IDENTIFIER_DELETE_MESSAGE) || identifier.equalsIgnoreCase(IDENTIFIER_MARK_READ))
            return new DeleteMessageProcessor();
        else if(identifier.equalsIgnoreCase(IDENTIFIER_GET_USER_INFO) || identifier.equalsIgnoreCase(IDENTIFIER_GET_USERS_INDEX))
            return new GetUserProcessor();


        return null;
    }
}
