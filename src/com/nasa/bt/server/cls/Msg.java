package com.nasa.bt.server.cls;

public class Msg {
    private String msgId;
    private String srcUid;
    private String dstUid;
    private String content;
    private String msgType;
    private long time;

    public static final String MSG_TYPE_NORMAL="NORMAL";
    public static final String MSG_TYPE_SECRET_1="SECRET_1";
    public static final String MSG_TYPE_SECRET_6="SECRET_6";
    public static final String MSG_TYPE_GROUP="GROUP";

    public Msg() {
    }

    public Msg(String msgId, String srcUid, String dstUid, String content, String msgType, long time) {
        this.msgId = msgId;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.content = content;
        this.msgType = msgType;
        this.time = time;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "msgId='" + msgId + '\'' +
                ", srcUid='" + srcUid + '\'' +
                ", dstUid='" + dstUid + '\'' +
                ", content='" + content + '\'' +
                ", msgType='" + msgType + '\'' +
                ", time=" + time +
                '}';
    }
}
