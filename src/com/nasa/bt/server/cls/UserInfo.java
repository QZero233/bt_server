package com.nasa.bt.server.cls;

public class UserInfo {

    private String name;
    private String id;

    public UserInfo() {
    }

    public UserInfo(String name, String id) {
        this.name = name;
        this.id = id;
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

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
