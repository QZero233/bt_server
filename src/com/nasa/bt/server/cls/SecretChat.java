package com.nasa.bt.server.cls;

public class SecretChat {

    private String sessionId;
    private String srcUid;
    private String dstUid;
    private String keyHash;

    public SecretChat() {
    }

    public SecretChat(String sessionId, String srcUid, String dstUid, String keyHash) {
        this.sessionId = sessionId;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.keyHash = keyHash;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSrcUid() {
        return srcUid;
    }

    public void setSrcUid(String srcUid) {
        this.srcUid = srcUid;
    }

    public String getDstUid() {
        return dstUid;
    }

    public void setDstUid(String dstUid) {
        this.dstUid = dstUid;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    @Override
    public String toString() {
        return "SecretChat{" +
                "sessionId='" + sessionId + '\'' +
                ", srcUid='" + srcUid + '\'' +
                ", dstUid='" + dstUid + '\'' +
                ", keyHash='" + keyHash + '\'' +
                '}';
    }
}
