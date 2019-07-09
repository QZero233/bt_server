package com.nasa.bt.server.data.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bt_user_auth_info", schema = "bt", catalog = "")
public class UserAuthInfoEntity {

    @Id
    private String name;
    private String codeHash;

    public UserAuthInfoEntity(String name, String codeHash) {
        this.name = name;
        this.codeHash = codeHash;
    }

    public UserAuthInfoEntity() {
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "codeHash")
    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthInfoEntity that = (UserAuthInfoEntity) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(codeHash, that.codeHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, codeHash);
    }

    @Override
    public String toString() {
        return "UserAuthInfoEntity{" +
                "name='" + name + '\'' +
                ", codeHash='" + codeHash + '\'' +
                '}';
    }
}
