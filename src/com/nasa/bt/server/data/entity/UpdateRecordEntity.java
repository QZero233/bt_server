package com.nasa.bt.server.data.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bt_update_record", schema = "bt", catalog = "")
public class UpdateRecordEntity {

    @Id
    private String entityId;
    private int entityType;
    private int entityStatus;
    private long lastEditTime;

    public static final int TYPE_SESSION=0;
    public static final int TYPE_USER=1;

    public static final int STATUS_ALIVE=1;
    public static final int STATUS_DELETED=0;

    public UpdateRecordEntity() {
    }

    public UpdateRecordEntity(String entityId, int entityType, int entityStatus, long lastEditTime) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.entityStatus = entityStatus;
        this.lastEditTime = lastEditTime;
    }

    @Basic
    @Column
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Basic
    @Column
    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    @Basic
    @Column
    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    @Basic
    @Column
    public int getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(int entityStatus) {
        this.entityStatus = entityStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateRecordEntity that = (UpdateRecordEntity) o;
        return entityType == that.entityType &&
                entityStatus == that.entityStatus &&
                lastEditTime == that.lastEditTime &&
                Objects.equals(entityId, that.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, entityType, entityStatus, lastEditTime);
    }

    @Override
    public String toString() {
        return "UpdateRecordEntity{" +
                "entityId='" + entityId + '\'' +
                ", entityType=" + entityType +
                ", entityStatus=" + entityStatus +
                ", lastEditTime=" + lastEditTime +
                '}';
    }
}
