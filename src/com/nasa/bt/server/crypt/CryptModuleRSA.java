package com.nasa.bt.server.crypt;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.RSAKeySet;
import com.nasa.bt.server.data.ServerDataUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

public class CryptModuleRSA implements CryptModule {

    private static final Logger log=Logger.getLogger(CryptModuleRSA.class);

    public static String SERVER_PUB_KEY;
    public static String SERVER_PRI_KEY;
    public static RSAKeySet SERVER_DEFAULT_KEY_SET;

    private String dstPubKey =null;
    private String myPrivateKey=null;

    static{
        SERVER_DEFAULT_KEY_SET= ServerDataUtils.readRSAKeySet();
        if(SERVER_DEFAULT_KEY_SET==null){
            SERVER_DEFAULT_KEY_SET=RSAUtils.genRSAKeySet();
            ServerDataUtils.saveRSAKeySet(SERVER_DEFAULT_KEY_SET);
        }

        SERVER_PUB_KEY=SERVER_DEFAULT_KEY_SET.getPub();
        SERVER_PRI_KEY=SERVER_DEFAULT_KEY_SET.getPri();
    }

    /**
     * 根据使用场景，加密使用公钥
     * @param clearText 明文（base64编码后再转为byte数组形式）
     * @param key 密钥 多余的
     * @param params 参数 用不着
     * @return 密文，base64编码后转为byte数组形式
     */
    @Override
    public byte[] doEncrypt(byte[] clearText, String key, Map<String, Object> params) {
        if(dstPubKey==null)
            return null;

        try {

            clearText= Base64.getEncoder().encode(clearText);

            RSAKeySet clientKey=new RSAKeySet(dstPubKey,null);
            RSAUtils rsaUtils=new RSAUtils(clientKey);

            String result=rsaUtils.publicEncrypt(new String(clearText));
            return result.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 根据使用场景，解密使用公私钥
     * @param cipherText 密文（base64编码后再转为byte数组形式）
     * @param key 密钥 多余的
     * @param params 参数 用不着
     * @return 明文，byte数组形式
     */
    @Override
    public byte[] doDecrypt(byte[] cipherText, String key, Map<String, Object> params) {
        try {
            if(dstPubKey==null)
                return null;


            if(myPrivateKey==null)
                return null;

            RSAUtils rsaUtils=new RSAUtils(SERVER_DEFAULT_KEY_SET);
            String result=rsaUtils.privateDecrypt(new String(cipherText));

            return Base64.getDecoder().decode(result.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initKeys(String dstPubKey,String myPriKey){
        this.dstPubKey=dstPubKey;
        this.myPrivateKey=myPriKey;
    }

    public String getDstPubKey() {
        return dstPubKey;
    }
}
