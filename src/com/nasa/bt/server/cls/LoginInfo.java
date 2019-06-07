package com.nasa.bt.server.cls;

/**
 * 用户信息类
 * @author QZero
 */
public class LoginInfo {

    /**
     * 字符串类型的id 36个字符组成，由 UUIDUtils生成
     */
    private String id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户密码的hash值（十六进制编码）
     */
    private String codeHash;

    public LoginInfo() {
    }

    public LoginInfo(String id, String name, String codeHash) {
        this.id = id;
        this.name = name;
        this.codeHash = codeHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", codeHash='" + codeHash + '\'' +
                '}';
    }
}
