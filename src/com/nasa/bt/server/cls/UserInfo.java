package com.nasa.bt.server.cls;

public class UserInfo {

    private String name;
    private String id;
    private String codeHash;

    public UserInfo() {
    }

    public UserInfo(String name, String id, String codeHash) {
        this.name = name;
        this.id = id;
        this.codeHash = codeHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", codeHash='" + codeHash + '\'' +
                '}';
    }
}
