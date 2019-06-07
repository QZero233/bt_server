package com.nasa.bt.server.crypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

public class CryptModuleAes implements CryptModule {
    @Override
    public byte[] doEncrypt(byte[] clearText, String pwd, Map<String, Object> params) {
        try{
            SecretKeySpec key = new SecretKeySpec(pwd.getBytes(), "AES/CBC/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(clearText);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] doDecrypt(byte[] cipherText, String pwd, Map<String, Object> params) {
        try{
            SecretKeySpec key = new SecretKeySpec(pwd.getBytes(), "AES/CBC/PKCS5PADDING");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(cipherText);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
