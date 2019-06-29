package com.nasa.bt.server.cls;

import com.alibaba.fastjson.JSON;

import java.util.Map;


public class Session {

    public static final int TYPE_NORMAL=0;
    public static final int TYPE_SECRET_CHAT=1;

    private String sessionId;
    private int sessionType;

    private String uidSrc;
    private String uidDst;

    private String params;


    public Session() {
    }



    public Session(String sessionId, int sessionType, String uidSrc, String uidDst, Map<String,String> params) {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.uidSrc = uidSrc;
        this.uidDst = uidDst;
        this.params = JSON.toJSONString(params);
    }

    public Session(String sessionId, int sessionType, String uidSrc, String uidDst, String params) {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.uidSrc = uidSrc;
        this.uidDst = uidDst;
        this.params = params;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Map<String,String> getParamsInMap(){
        return (Map<String, String>) JSON.parse(params);
    }

    public void setParamsInMap(Map<String,String> params){
        this.params=JSON.toJSONString(params);
    }

    public String getUidSrc() {
        return uidSrc;
    }

    public void setUidSrc(String uidSrc) {
        this.uidSrc = uidSrc;
    }

    public String getUidDst() {
        return uidDst;
    }

    public void setUidDst(String uidDst) {
        this.uidDst = uidDst;
    }

    public String getIdOfOther(String uid){
        if(uid==null)
            return null;
        if(uid.equals(uidSrc))
            return uidDst;
        return uidSrc;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", sessionType=" + sessionType +
                ", uidSrc='" + uidSrc + '\'' +
                ", uidDst='" + uidDst + '\'' +
                ", params='" + params + '\'' +
                '}';
    }
}
