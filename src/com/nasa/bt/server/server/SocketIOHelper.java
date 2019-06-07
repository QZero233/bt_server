package com.nasa.bt.server.server;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.crypt.CryptModule;
import com.nasa.bt.server.crypt.CryptModuleFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * socket流协助类
 * @author QZero
 */
public class SocketIOHelper {

    /**
     * 输入流
     */
    private InputStream is;

    /**
     * 输出流
     */
    private OutputStream os;

    /**
     * 当前使用的加密模块
     */
    private CryptModule cryptModule;

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
     * 从输入流中读取数据并转为数据包对象
     * @return 读取到的数据
     * @throws RuntimeException 当读取输入流错误时，抛出异常
     */
    public Datagram readIs() throws RuntimeException{
        synchronized (is){
            try {
                //读取标识符
                byte[] identifierBuf=new byte[4];
                is.read(identifierBuf);

                //读取版本号
                int verCode=is.read();

                //读取时间戳
                byte[] timeBuf=new byte[8];
                is.read(timeBuf);

                //读取参数数量
                int paramsCount=is.read();//FIXME 在无参数时，会堵塞在此处，目前的解决方案是没参数就加个参数
                Map<String,byte[]> params=new HashMap<>();

                //读取具体参数
                for(int i=0;i<paramsCount;i++){
                    //读取参数总长度以及参数名长度
                    int paramLength=is.read();
                    int paramNameLength=is.read();
                    paramLength-=paramNameLength;

                    //读取参数名
                    byte[] paramName=new byte[paramNameLength];
                    is.read(paramName);

                    //读取参数内容，循环读取直到读完
                    ByteArrayOutputStream tmpStream=new ByteArrayOutputStream(paramLength);
                    while(tmpStream.size()<paramLength){
                        byte[] tmpBuf=new byte[paramLength];
                        int len=is.read(tmpBuf);
                        tmpStream.write(tmpBuf,0,len);
                    }
                    byte[] paramContent=tmpStream.toByteArray();
                    params.put(new String(paramName),paramContent);
                }

                //读取完成，开始封装
                String identifier=new String(identifierBuf);
                Datagram datagram=new Datagram(identifier,verCode,byteArrayToLong(timeBuf),params);
                return datagram;
            }catch (Exception e) {
                //e.printStackTrace();
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
                os.write(datagram.getIdentifier().getBytes());
                os.write(datagram.getVerCode());
                os.write(longToByteArray(datagram.getTime()));

                Map<String,byte[]> params=datagram.getParams();
                if(params.isEmpty())
                    os.write(0);
                else
                    os.write(params.size());

                Set<String> keys=params.keySet();
                for(String key:keys){
                    byte[] paramNameBuf=key.getBytes();
                    byte[] paramContentBuf=params.get(key);

                    os.write(paramNameBuf.length+paramContentBuf.length);
                    os.write(paramNameBuf.length);
                    os.write(paramNameBuf);
                    os.write(paramContentBuf);
                }
                return true;
            }catch (Exception e){
                //e.printStackTrace();
                return false;
            }
        }
    }
}
