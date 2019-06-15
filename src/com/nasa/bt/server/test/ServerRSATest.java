package com.nasa.bt.server.test;

import com.nasa.bt.server.cls.Datagram;
import com.nasa.bt.server.server.SocketIOHelper;
import org.junit.Test;

import java.net.Socket;

public class ServerRSATest {

    public static final String TEST_PUB="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAokkg5lrAeyesHUl9j8EWGKwLTDfBN5oFTxJPkE96yvCj/DngseWEh0MbW7ijetPYLUFrNfcpB1t0H+udIQ+JkWmUN4xcgL48MHbfDXeYbk2MxWaniyQaSBkHyj9z0mhE4HrrYlvxd7OVFL/Pkn4cNqjWbptYtfen5nmjeJICqs3YnjbhVUqH9b5woRxjDOAb7FwC9FIoOam62+RH5fQ41//uDgnzuPGqKE2ZLPFHdf/SYCgGDJjkhv5zK2BiybGL9XUp7GCygssuzWWxt36MvOn7EIRsTBbBnZmKgy7l1Gy2slWR/kkqpxRI1zV14/8TMKvB1lOPHKKDwlfLdkQS3wIDAQAB";
    public static final String TEST_PRI="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCiSSDmWsB7J6wdSX2PwRYYrAtMN8E3mgVPEk+QT3rK8KP8OeCx5YSHQxtbuKN609gtQWs19ykHW3Qf650hD4mRaZQ3jFyAvjwwdt8Nd5huTYzFZqeLJBpIGQfKP3PSaETgeutiW/F3s5UUv8+Sfhw2qNZum1i196fmeaN4kgKqzdieNuFVSof1vnChHGMM4BvsXAL0Uig5qbrb5Efl9DjX/+4OCfO48aooTZks8Ud1/9JgKAYMmOSG/nMrYGLJsYv1dSnsYLKCyy7NZbG3foy86fsQhGxMFsGdmYqDLuXUbLayVZH+SSqnFEjXNXXj/xMwq8HWU48cooPCV8t2RBLfAgMBAAECggEAcy/+xyJBHukA1GTWV5PQkECFYFrurP2IqSAxExgm6ya07pFSOFLVp4BtScQbXVJCTZTyeQJuv8cc/I8rH0088taCmo7pFYRximDFX3S04A3xTifX4f4FHYmOqvIwznat9LuqmUzzbmgr9+dReQREGFPsp+Nhqv8W0JMkAa95byu8vQPRPivpiqjj6/QD1/nXsoboabhYTW4X3ppYE1MM9lZGA2BZSGzJALrerOyVbHuMfKXefj6I5NBj932ouPkcWFsHHuwY3+Det2jcWnKF2M+Ka9qphsb2P4ezkcFQmRoN1fa52dEeIJlcmMAvoT7aPlwQD0CnT8ECVDI3wCuQsQKBgQDXWD4VqSr5J4vlahZ1cIzbQEWCjBh4ie0sa7BgriiYWFRUl/k2CTA1RDO7RusjNz/YsRBaP+0Xq6ViDo5rB42Jw4DDBi2m0vrCkD0R5W+/LhquAkny/UJMT2APixcsfkoHyKthjNeOAE1ClJ/ro2H5+YTSS/BM3Hk6wmdlH8OVRwKBgQDA7IGQVGNF1rVxkfM5LWmBKJqLTOLN9jMPBqyv8xGW8r/3KdSvQ3L4hITxiopGg6K7Cb9XYJRRDRz/s1mrKLgCQMuTS428mRppimw5MpZnfDCTwrjHi/3auW+T73P5lJhXC8Kzxb6X4bGasjtqNDTYd5mttSUbL31kgowiXoTBqQKBgDb+yTQ89MWWrOqU8lFhwgdIXbgeUS2Pg+uhQPUf5Swq+rZz7Wg4ZoBj/5YnYWun831CkvuJceJmqqDlOMfqsOHq9NpZnJULMpE3xvDoGGbJvnSMDxioRyM37j9p9SwewIdGX0ZVcsNIHnMPg9vo6N+vMpWGYUMv1yIDfeZce1WHAoGBAL+UHLVul4ZqyJOXE6jb58FSyOU73J4atmldf27aOcVkGi0fZQ0GNc2EC2Cw6s9LOPXzoAbs6VvzYbCqMYk+Tjwui/ohNQbsIsZbp0zFZ4YsVTDmVQgThGsHaj3Fs61MWltWWmqbG8FHZk7wXwAAB9pOKlXNZKOybRq6aN5YbgzBAoGAJxvFBgXFbhyeBqqPrKthYRe3qreNnG5QflKNIIw9A5DpCmXHDRn7K9x0FN0oVaSYzJdUr21yFrW4tMIx9HdDkGVJBqhsMHwpk417fNAUKgiKvKscZufKOsMbtG2ztgLHUIeAAZDp1ghp3F6KHB2zCGjVBJT8IYus7izaFxkeTW8=";

    @Test
    public void testSererRSA() throws Exception{
        Socket socket=new Socket("127.0.0.1",8848);
        SocketIOHelper helper=new SocketIOHelper(socket.getInputStream(),socket.getOutputStream());

        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    System.out.println(helper.readIs());
                }
            }
        }.start();

        helper.setPrivateKey(TEST_PRI);
        helper.sendPublicKey(TEST_PUB);

        Thread.sleep(5000);

        Datagram datagramTest=new Datagram("AAAA",null);
        helper.writeOs(datagramTest);

        Thread.sleep(500000);
    }

}
