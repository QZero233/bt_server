package com.nasa.bt.server.server.processor;

/**
 * 数据包处理类的工厂类
 * @author QZero
 */
public class DataProcessorFactory {

    public static final String IDENTIFIER_SIGN_IN="SIIN";
    public static final String IDENTIFIER_REPORT="REPO";

    /**
     * 根据标识符获取处理器
     * @param identifier 标识符
     * @return 处理器，不存在则返回null
     */
    public static DataProcessor getProcessor(String identifier){
        if(identifier.equalsIgnoreCase(IDENTIFIER_SIGN_IN))
            return new SignInProcessor();

        return null;
    }
}
