package com.nasa.bt.server.crypt;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.cls.RSAKeySet;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

public class CryptModuleRSA implements CryptModule {

    private static final Logger log=Logger.getLogger(CryptModuleRSA.class);

    public static final String SERVER_PUB_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl25SnsKTpQxsJCWpS9eKO2aAlgcfUXc3YK3S5QHNwptxM5GUvYilUjrLvcoaaQsfoxuc5JeBhAKAkRhtAsIis6/4sSsLJuOKMCE8wotkkgF6QJRW8SUnYS/MdFfgdPg11Hc+wZnUSycv4GBfykuW89tKxFK8xYKhLSaJHWPAJbGEvtR0G2ixOGrfSKFNIX8tytCfIzTO31ZCfdMyMp5dnbEwbLC/SRqCdJ4T2stVRjJ/C545NHdKsmAhvuMEffrk6vJRbpqqw65QTK6pHxwcM9YPPqmQ9lBUzI6d6aNxBqiUcTiRwIqltStooDI6VTZx6zUQ66Dhdl0O+l2R2hf/lQIDAQAB";
    public static final String SERVER_PRI_KEY="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXblKewpOlDGwkJalL14o7ZoCWBx9RdzdgrdLlAc3Cm3EzkZS9iKVSOsu9yhppCx+jG5zkl4GEAoCRGG0CwiKzr/ixKwsm44owITzCi2SSAXpAlFbxJSdhL8x0V+B0+DXUdz7BmdRLJy/gYF/KS5bz20rEUrzFgqEtJokdY8AlsYS+1HQbaLE4at9IoU0hfy3K0J8jNM7fVkJ90zIynl2dsTBssL9JGoJ0nhPay1VGMn8Lnjk0d0qyYCG+4wR9+uTq8lFumqrDrlBMrqkfHBwz1g8+qZD2UFTMjp3po3EGqJRxOJHAiqW1K2igMjpVNnHrNRDroOF2XQ76XZHaF/+VAgMBAAECggEBAJZe0OAhq2c7vK7wTvqm3/c4Q/u2uty0m2L/eOo7Lk1O+cwRhjfnVvMeu26NIEiq6RGYu+UqpUunCHAMZ0NT8A2qcjPL4FkPngtJFgLBVG6/1NwH8YWoNUCGrGnjUVoVl66RctngnTS7hwkx6i0BfrZMTiRBXH5sxwwEtidfBFDoOed1pG7x53r1A1gdtWJ4kGJthfU0MSZNdgJT2Z7ODrBZMnxj4IB8Xm9zDEPYnximCE+eCChic/pw2ePpRWmbPEErg3Vca2bpINCkKlufP6QfO897O9qHdxmilwx5zM9DOny0Uc7xbk8ykhGDG/+9YDoLERM/0XMzCcCZlrSkKAECgYEA4DxFBTkxAlglJer2FVqBxKf/ehNzKv0lAzMiJbloepbeKFOcawWWJ27x6qWfnjyV11Z3sn6Gda2n0ruebKg7vaJxc5jIVKbuHBiOEKJhlmqGRuNCd6VLHECwnCJDe5U+m1TPAFAHC5SgUXf6h2YEL/o6xenq6v2jPRDLA7xEcJUCgYEArOHaXw9SCZm5A1La0W+3UuQG5tm61VozVrjSN94w3o5rvHYshKW3KwH0Bg9lPwWgZXDyKUBRyh5TSfNzIcWls+OmqtW0P4UALvDxaxW2nYhs0mCdYRerTAmjVB2dZhPckF8YDxoRtRwwa61YsnV/J4xW86kglogQxoeR4Zr2kwECgYBXAWuyhCdtBnCbjtifHngdqyXo4yUbuA+ldzC3vII6ltFqik62A/voHxdE+bC+gDmPzWAyZP+dJ51nmh71gC11/lLRDKYmmoLnaeutrqP47ipd1Q5USFGli8A7RNw0VottT/Hl6Cdjqe6uzcPqx322c/xcENBzD6lIJ3zuXR2dJQKBgH+D2wDwmO8R3oAQFewSvaI7JMgTYpZJgMJ/cjbZASy22cpZU77HSFJFj4sEwgX5DXSlGUjJ80nkkkRid9I6KV2FFYK+O8P6Qsdc2oTEsDyDythlgM+DmMpt9XVunpETJ0WpjmJdNSBj2WcipeCDf7VY8y2n2qyNUepNRpGkLikBAoGARUeg6BpzlXJRpj+wCyViJ8klPJEMhwFD7PMVjy2Hn+OVdjBFZMqrb8BWTe6psayHJA8PdRPT6P/krI9SMM9KiVYmYlex64zBwPi7OcWLTo58gRYh8oGpuWYTBlSypKHAFsfE3eAg/bgRMC88h92tPHLvT4bqlJbN2pOLA8XQMHg=";
    public static final RSAKeySet SERVER_DEFAULT_KEY_SET=new RSAKeySet(SERVER_PUB_KEY,SERVER_PRI_KEY);

    private String dstPubKey =null;
    private String myPrivateKey=null;

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
}
