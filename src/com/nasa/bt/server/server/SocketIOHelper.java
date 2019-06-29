package com.nasa.bt.server.server;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.crypt.CryptModule;
import com.nasa.bt.server.crypt.CryptModuleFactory;
import com.nasa.bt.server.crypt.CryptModuleRSA;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * socket流协助类
 * @author QZero
 */
public class SocketIOHelper {

    private static Logger log=Logger.getLogger(SocketIOHelper.class);

    /**
     * 输入流
     */
    private InputStream is;

    /**
     * 输出流
     */
    private OutputStream os;

    /**
     * 当前使用的加密模块s
     */
    private CryptModule cryptModule;

    /**
     *  上次调用输出流写数据的时间，规定写数据必须间隔100ms
     */
    private static long lastWriteTime=0;

    /**
     * 初始化helper类
     * @throws IllegalArgumentException 当配置的加密模块不存在时抛出异常
     */
    public SocketIOHelper(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
        cryptModule= CryptModuleFactory.getCryptModule(CryptModuleFactory.CURRENT_CRYPT_MODULE);
        if(cryptModule==null)
            throw new IllegalArgumentException("加密模块不存在");
    }

    /**
     * 将byte数组转为long
     * @param buf byte数组
     * @return long数据
     */
    public static long byteArrayToLong(byte[] buf){
        ByteBuffer buffer=ByteBuffer.wrap(buf);
        return buffer.getLong();
    }

    /**
     * long转byte数组
     * @param l long数据
     * @return byte数组
     */
    public static byte[] longToByteArray(long l){
        ByteBuffer buffer=ByteBuffer.allocate(8);
        buffer.putLong(l);
        return buffer.array();
    }

    /**
     * 将byte数组转为int
     * @param buf byte数组
     * @return longint
     **/
    public static int byteArrayToInt(byte[] buf){
        ByteBuffer buffer=ByteBuffer.wrap(buf);
        return buffer.getInt();
    }

    /**
     * int转byte数组
     * @param i int数据
     * @return byte数组
     */
    public static byte[] intToByteArray(int i){
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }

    /**
     * 在输入流中读取指定长度的数据包
     * @param is 输入流
     * @param length 长度
     * @return 数据包
     * @throws Exception 读取时的异常
     */
    private byte[] readBufFromIs(InputStream is,int length) throws Exception{
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream(length);
        byte[] buf=new byte[length];
        int len;
        while(true){
            len=is.read(buf,0,length);
            length-=len;
            outputStream.write(buf,0,len);
            if(length==0)
                break;
        }

        return outputStream.toByteArray();
    }

    /**
     * 从输入流中读取数据并转为数据包对象
     * @return 读取到的数据
     * @throws RuntimeException 当读取输入流错误时，抛出异常
     */
    public Datagram readIs() throws RuntimeException{
        synchronized (is){
            try {

                //数据包总长度
                byte[] intTmpBuf=readBufFromIs(is,4);
                int dataLength=byteArrayToInt(intTmpBuf);

                if(dataLength<=0)
                    return null;

                //明文信息总长度
                intTmpBuf=readBufFromIs(is,4);;
                int contentLength=byteArrayToInt(intTmpBuf);

                byte[] buf=readBufFromIs(is,dataLength);

                //全部内容读取完成，开始解密数据包
                byte[] decrypted=cryptModule.doDecrypt(buf,null,null);
                //解密失败返回空数据包
                if(decrypted==null){
                    return new Datagram(Datagram.IDENTIFIER_NONE,null);
                }
                ByteArrayInputStream inputBuf=new ByteArrayInputStream(decrypted,0,contentLength);

                //读取标识符
                byte[] identifierBuf=new byte[4];
                inputBuf.read(identifierBuf);

                //读取版本号
                int verCode=inputBuf.read();

                //读取时间戳
                byte[] timeBuf=new byte[8];
                inputBuf.read(timeBuf);

                //读取参数数量
                int paramsCount=inputBuf.read();
                Map<String,byte[]> params=new HashMap<>();

                //读取具体参数
                for(int i=0;i<paramsCount;i++){
                    //读取参数总长度以及参数名长度
                    inputBuf.read(intTmpBuf);
                    int paramLength=byteArrayToInt(intTmpBuf);
                    int paramNameLength=inputBuf.read();
                    paramLength-=paramNameLength;

                    //读取参数名
                    byte[] paramName=new byte[paramNameLength];
                    inputBuf.read(paramName);

                    byte[] paramBuf=new byte[paramLength];
                    inputBuf.read(paramBuf);

                    params.put(new String(paramName),paramBuf);
                }

                //读取完成，开始封装
                String identifier=new String(identifierBuf);
                Datagram datagram=new Datagram(identifier,verCode,byteArrayToLong(timeBuf),params);

                log.debug("收到数据包 "+datagram);

                return datagram;
            }catch (Exception e) {
                log.error("读取输入流错误，断开连接",e);
                throw new RuntimeException("读取输入流错误，断开连接");
                //一旦发生读取错误就断开与客户端的连接
            }
        }
    }

    /**
     * 根据数据包对象写输出流
     * @param datagram 数据包对象
     * @return 是否写入成功
     * @throws IllegalArgumentException 当标识符不合法时抛出异常
     */
    public boolean writeOs(Datagram datagram) throws IllegalArgumentException{
        if(datagram==null)
            return false;

        if(datagram.getIdentifier().length()!=4)
            throw new IllegalArgumentException("标识符不合法");

        synchronized (os){
            try {

                //数据包正文内容缓冲区
                ByteArrayOutputStream tmpBuf=new ByteArrayOutputStream();

                //填充缓冲区
                tmpBuf.write(datagram.getIdentifier().getBytes());
                tmpBuf.write(datagram.getVerCode());
                tmpBuf.write(longToByteArray(datagram.getTime()));

                Map<String,byte[]> params=datagram.getParams();
                tmpBuf.write(params.size());

                Set<String> keys=params.keySet();
                for(String key:keys){
                    byte[] paramNameBuf=key.getBytes();
                    byte[] paramContentBuf=params.get(key);

                    tmpBuf.write(intToByteArray(paramNameBuf.length+paramContentBuf.length));
                    tmpBuf.write(paramNameBuf.length);
                    tmpBuf.write(paramNameBuf);
                    tmpBuf.write(paramContentBuf);
                }

                byte[] encryptedBuf=cryptModule.doEncrypt(tmpBuf.toByteArray(),null,null);
                //加密失败
                if(encryptedBuf==null)
                    return false;
                os.write(intToByteArray(encryptedBuf.length),0,4);
                os.write(intToByteArray(tmpBuf.size()),0,4);
                os.write(encryptedBuf);

                return true;
            }catch (Exception e){
                //e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 向服务器发送自己的公钥
     * @param key 公钥
     * @return 是否成功
     */
    public boolean sendPublicKey(String key){
        if(key==null || key.equals(""))
            return false;

        try {
            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            outputStream.write(Datagram.IDENTIFIER_CHANGE_KEY.getBytes());
            outputStream.write(key.getBytes());

            os.write(intToByteArray(outputStream.size()));
            os.write(intToByteArray(outputStream.size()));
            os.write(outputStream.toByteArray());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置私钥
     * @param key 私钥
     */
    public void setPrivateKey(String key){
        ((CryptModuleRSA)cryptModule).setMyPrivateKey(key);
    }
}
