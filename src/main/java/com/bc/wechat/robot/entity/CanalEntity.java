package com.bc.wechat.robot.entity;

import java.io.Serializable;

/**
 * canal实体类
 *
 * @author zhou
 */
public class CanalEntity implements Serializable {

    /**
     * 序列号
     */
    private static final long serialVersionUID = -2464794278427415065L;

    private String id;

    private String before;
    private String binlog;
    private String db;
    private String eventType;
    private String table;
    private String time;
    private String after;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBefore() {
        return before;
    }

    public String getBinlog() {
        return binlog;
    }

    public String getDb() {
        return db;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTable() {
        return table;
    }

    public String getTime() {
        return time;
    }

    public String getAfter() {
        return after;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public void setBinlog(String binlog) {
        this.binlog = binlog;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setAfter(String after) {
        this.after = after;
    }

}

