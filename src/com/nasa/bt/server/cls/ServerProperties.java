package com.nasa.bt.server.cls;

public class ServerProperties {
    private int serverPort;

    public ServerProperties(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public String toString() {
        return "ServerProperties{" +
                "serverPort=" + serverPort +
                '}';
    }
}
