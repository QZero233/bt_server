package com.nasa.bt.server.test;

import com.nasa.bt.server.crypt.RSAUtils;
import com.nasa.bt.server.crypt.SHA256Utils;
import org.junit.Test;

public class RSATest {

    @Test
    public void testRSA() throws  Exception{
        RSAUtils utils=new RSAUtils();
        System.out.println(utils.getPub());
        System.out.println(utils.getPri());
    }

    @Test
    public void sha256Test(){
        System.out.println(SHA256Utils.getSHA256InHex("439-2384-3"));
    }

}
