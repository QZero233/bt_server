package com.nasa.bt.server.crypt;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    //私钥
    private String pri = null;
    //公钥
    private String pub = null;

    //声明非对称加密算法
    public static final String RSA = "RSA";
    //密钥长度
    public static final int KEY_SIZE = 2048;

    public static final int CLEAR_MAX_SIZE = (KEY_SIZE/8)-11;
    public static final int CIPHER_MAX_SIZE = 256;

    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";//TODO 加密填充方式，android必须用这个，因为协议和java不同

    //密钥工厂对象
    private KeyFactory keyFactory;

    // 公钥对象
    private RSAPublicKey publicKey = null;

    // 声明私钥对象
    private RSAPrivateKey privateKey = null;

    public RSAUtils(String pub, String pri) throws Exception {
        this.pri = pri;
        this.pub = pub;

        keyFactory = KeyFactory.getInstance(RSA);

        if(pri==null || pub==null)
            return;

        //加载公钥私钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pub));
        publicKey = (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);

        PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pri));
        privateKey = (RSAPrivateKey) keyFactory.generatePrivate(pKCS8EncodedKeySpec);
    }

    public RSAUtils() throws Exception {
        keyFactory = KeyFactory.getInstance(RSA);
        genRSAKeyPair();
    }

    public RSAUtils(String pub) throws Exception{
        keyFactory = KeyFactory.getInstance(RSA);
        this.pub=pub;
        //加载公钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pub));
        publicKey = (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
    }

    public void loadPublicKey(String pubKey) throws Exception{
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKey));
        publicKey = (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
    }

    public void loadPrivateKey(String priKey) throws Exception{
        PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKey));
        privateKey = (RSAPrivateKey) keyFactory.generatePrivate(pKCS8EncodedKeySpec);
    }

    public String getPri() {
        return pri;
    }

    public String getPub() {
        return pub;
    }

    private void genRSAKeyPair() throws Exception {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
        // 初始化密钥对生成器，设置秘钥长度
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());

        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 得到公钥
        publicKey = (RSAPublicKey) keyPair.getPublic();

        // 将公钥转换为 String 类型
        pub = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        // 得到私钥
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // 将私钥转换为 String 类型
        pri = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

    }

    /**
     * 公钥加密过程, 明文长度小于 (公钥长度 / 8) - 11
     *
     * @param clearText 明文数据
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public byte[] publicEncryptMini(byte[] clearText) throws Exception {
        //TODO android 上要换成 ECB_PKCS1_PADDING
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] output = cipher.doFinal(clearText);

        return output;
    }

    /**
     * 私钥加密过程, 明文长度小于 (私钥长度 / 8) - 11
     *
     * @param clearText 明文数据
     * @return 密文，base64编码
     * @throws Exception 加密过程中的异常信息
     */
    public byte[] privateEncryptMini(byte[] clearText) throws Exception {
        //TODO android 上要换成 ECB_PKCS1_PADDING
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        byte[] output = cipher.doFinal(clearText);

        return output;


    }

    /**
     * 私钥解密过程
     *
     * @param cipherText 密文
     * @return 明文
     * @throws Exception
     */
    public byte[] privateDecryptMini(byte[] cipherText) throws Exception {
        //TODO android 上要换成 ECB_PKCS1_PADDING
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] output = cipher.doFinal(cipherText);
        return output;
    }

    /**
     * 公钥解密过程
     *
     * @param cipherText 密文
     * @return 明文
     * @throws Exception
     */
    public byte[] publicDecryptMini(byte[] cipherText) throws Exception {
        //TODO android 上要换成 ECB_PKCS1_PADDING
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(cipherText);
        return output;
    }

    /**
     * 公钥加密 适用于明文过长
     *
     * @param clearText 明文数据
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public String publicEncrypt(String clearText) throws Exception {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

        byte[] buf=clearText.getBytes();
        ByteArrayInputStream inputStream=new ByteArrayInputStream(buf);

        int currentIndex = 0;
        int bufLength = buf.length;
        while (currentIndex < bufLength) {
            int endIndex = (currentIndex + CLEAR_MAX_SIZE) > bufLength ? bufLength : currentIndex + CLEAR_MAX_SIZE;

            byte[] tmpBuf=new byte[endIndex-currentIndex];
            inputStream.read(tmpBuf);

            outputStream.write(publicEncryptMini(tmpBuf));
            currentIndex += CLEAR_MAX_SIZE;
        }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 私钥加密 适用于明文过长
     *
     * @param clearText 明文数据
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public String privateEncrypt(String clearText) throws Exception {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

        byte[] buf=clearText.getBytes();
        ByteArrayInputStream inputStream=new ByteArrayInputStream(buf);

        int currentIndex = 0;
        int bufLength = buf.length;
        while (currentIndex < bufLength) {
            int endIndex = (currentIndex + CLEAR_MAX_SIZE) > bufLength ? bufLength : currentIndex + CLEAR_MAX_SIZE;

            byte[] tmpBuf=new byte[endIndex-currentIndex];
            inputStream.read(tmpBuf);

            outputStream.write(privateEncryptMini(tmpBuf));
            currentIndex += CLEAR_MAX_SIZE;
        }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 私钥解密 适用于密文过长
     *
     * @param cipherText 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public String privateDecrypt(String cipherText) throws Exception {
        byte[] buf=Base64.getDecoder().decode(cipherText);
        ByteArrayInputStream inputStream=new ByteArrayInputStream(buf);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

        int currentIndex = 0;
        int bufLength = buf.length;
        while (currentIndex < bufLength) {
            int endIndex = (currentIndex + CIPHER_MAX_SIZE) > bufLength ? bufLength : currentIndex + CIPHER_MAX_SIZE;

            byte[] tmpBuf=new byte[endIndex-currentIndex];
            inputStream.read(tmpBuf);

            outputStream.write(privateDecryptMini(tmpBuf));
            currentIndex += CIPHER_MAX_SIZE;
        }

        return new String(outputStream.toByteArray());
    }

    /**
     * 公钥解密 适用于密文过长
     *
     * @param cipherText 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public String publicDecrypt(String cipherText) throws Exception {
        byte[] buf=Base64.getDecoder().decode(cipherText);
        ByteArrayInputStream inputStream=new ByteArrayInputStream(buf);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

        int currentIndex = 0;
        int bufLength = buf.length;
        while (currentIndex < bufLength) {
            int endIndex = (currentIndex + CIPHER_MAX_SIZE) > bufLength ? bufLength : currentIndex + CIPHER_MAX_SIZE;

            byte[] tmpBuf=new byte[endIndex-currentIndex];
            inputStream.read(tmpBuf);

            outputStream.write(publicDecryptMini(tmpBuf));
            currentIndex += CIPHER_MAX_SIZE;
        }

        return new String(outputStream.toByteArray());
    }

}
