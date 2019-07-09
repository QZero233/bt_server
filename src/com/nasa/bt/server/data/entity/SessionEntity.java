package com.nasa.bt.server.data.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bt_session", schema = "bt", catalog = "")
public class SessionEntity {

    @Id
    private String sessionId;
    private Integer sessionType;
    private String srcUid;
    private String dstUid;
    private String params;

    public static final int TYPE_NORMAL=0;
    public static final int TYPE_SECRET_CHAT=1;

    public SessionEntity(String sessionId, Integer sessionType, String srcUid, String dstUid, String params) {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.srcUid = srcUid;
        this.dstUid = dstUid;
        this.params = params;
    }

    public SessionEntity() {
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
    @Column(name = "sessionType")
    public Integer getSessionType() {
        return sessionType;
    }

    public void setSessionType(Integer sessionType) {
        this.sessionType = sessionType;
    }

    @Basic
    @Column(name = "srcUid")
    public String getSrcUid() {
        return srcUid;
    }

    public void setSrcUid(String uidSrc) {
        this.srcUid = uidSrc;
    }

    @Basic
    @Column(name = "dstUid")
    public String getDstUid() {
        return dstUid;
    }

    public void setDstUid(String uidDst) {
        this.dstUid = uidDst;
    }

    @Basic
    @Column(name = "params")
    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionEntity that = (SessionEntity) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(sessionType, that.sessionType) &&
                Objects.equals(srcUid, that.srcUid) &&
                Objects.equals(dstUid, that.dstUid) &&
                Objects.equals(params, that.params);
    }

    @Override
    public String toString() {
        return "SessionEntity{" +
                "sessionId='" + sessionId + '\'' +
                ", sessionType=" + sessionType +
                ", srcUid='" + srcUid + '\'' +
                ", dstUid='" + dstUid + '\'' +
                ", params='" + params + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, sessionType, srcUid, dstUid, params);
    }

    public boolean checkInSession(String uid){
        if(srcUid.equals(uid) || dstUid.equals(uid))
            return true;
        return false;
    }

    @Transient
    public String getIdOfOther(String uidMine){
        if(srcUid.equals(uidMine))
            return dstUid;
        return srcUid;
    }
}
