package com.nasa.bt.server.test;

import com.nasa.bt.server.crypt.RSAUtils;
import com.nasa.bt.server.crypt.SHA256Utils;
import org.junit.Test;

import java.util.Base64;

public class RSATest {

    @Test
    public void testRSA() throws  Exception{
        RSAUtils utils=new RSAUtils();
        System.out.println(utils.getPub());
        System.out.println(utils.getPri());
    }

    @Test
    public void testBase64(){
        Base64.getDecoder().decode("HvVYxZKSoEdJGEcRgHRkF95bKiKtv2TMQOvOXI8cUpL4i1L5FNcLHLqtcyNSnxZ/FyqrQeiu7DI4\n" +
                "LAeGR9eAOUub+DDwLMByKDAOmnujq3rqIBhtI3PucRejVsWKlDvTqBuTpzpA8h+yzTdA+/mHyTf2\n" +
                "Ar/7Gvp+ttAK0BRXLib6OMxll/va5LYjwFUYPRUAWrsvjTeMWdNsBeQ74EqF7mFU2nq1/aSiC+pW\n" +
                "MTPYF1N1Bb/zoYQ/0vOV6Mvl5sw9/qPJ9rL/FUNsJJddVMwhAKq2TuirMgbTiRrve8GhLSNAiBG8\n" +
                "O6P0v+lrxjS+7yax4plstbA8ShxWOHvVkrEa1Q==");

    }

    @Test
    public void sha256Test(){
        System.out.println(SHA256Utils.getSHA256InHex("439-2384-3"));
    }

}
