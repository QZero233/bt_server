package com.nasa.bt.server.data.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bt_temp_message", schema = "bt", catalog = "")
public class TempMessageEntity {

    @Id
    private String msgId;
    private String srcUid;
    private String dstUid;
    private String sessionId;
    private Long time;

    @Transient
    private String content;

    public TempMessageEntity(String msgId, String srcUid, String dstUid, String sessionId, Long time, String content) {
        this.msgId = msgId;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.sessionId = sessionId;
        this.time = time;
        this.content = content;
    }

    public TempMessageEntity(String msgId, String srcUid, String dstUid, String sessionId, Long time) {
        this.msgId = msgId;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.sessionId = sessionId;
        this.time = time;
    }

    public TempMessageEntity() {
    }

    @Basic
    @Column(name = "msgId")
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Basic
    @Column(name = "srcUid")
    public String getSrcUid() {
        return srcUid;
    }

    public void setSrcUid(String srcUid) {
        this.srcUid = srcUid;
    }

    @Basic
    @Column(name = "dstUid")
    public String getDstUid() {
        return dstUid;
    }

    public void setDstUid(String dstUid) {
        this.dstUid = dstUid;
    }

    @Basic
    @Column(name = "sessionId")
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Basic
    @Column(name = "time")
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempMessageEntity that = (TempMessageEntity) o;
        return Objects.equals(msgId, that.msgId) &&
                Objects.equals(srcUid, that.srcUid) &&
                Objects.equals(dstUid, that.dstUid) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId, srcUid, dstUid, sessionId, time);
    }

    @Override
    public String toString() {
        return "TempMessageEntity{" +
                "msgId='" + msgId + '\'' +
                ", srcUid='" + srcUid + '\'' +
                ", dstUid='" + dstUid + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
