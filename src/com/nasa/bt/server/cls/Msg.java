package com.nasa.bt.server.cls;

public class Msg {
    private String msgId;
    private String srcUid;
    private String dstUid;
    private String content;
    private long time;

    public Msg() {
    }

    public Msg(String msgId, String srcUid, String dstUid, String content, long time) {
        this.msgId = msgId;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.content = content;
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

    @Override
    public String toString() {
        return "Msg{" +
                "msgId='" + msgId + '\'' +
                ", srcUid='" + srcUid + '\'' +
                ", dstUid='" + dstUid + '\'' +
                ", content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
