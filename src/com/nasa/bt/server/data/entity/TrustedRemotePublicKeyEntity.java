package com.nasa.bt.server.data.entity;


import com.nasa.bt.server.crypt.SHA256Utils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bt_trusted_remote_key", schema = "bt", catalog = "")
public class TrustedRemotePublicKeyEntity {

    @Id
    private String name;
    private String publicKey;
    private String publicKeyHash;

    public TrustedRemotePublicKeyEntity() {
    }

    public TrustedRemotePublicKeyEntity(String name, String publicKey) {
        this.name = name;
        this.publicKey = publicKey;
        this.publicKeyHash = SHA256Utils.getSHA256InHex(publicKey);
    }

    @Basic
    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(length = 2000)
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Basic
    @Column
    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrustedRemotePublicKeyEntity that = (TrustedRemotePublicKeyEntity) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(publicKey, that.publicKey) &&
                Objects.equals(publicKeyHash, that.publicKeyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, publicKey, publicKeyHash);
    }

    @Override
    public String toString() {
        return "TrustedRemotePublicKeyEntity{" +
                "name='" + name + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", publicKeyHash='" + publicKeyHash + '\'' +
                '}';
    }
}
