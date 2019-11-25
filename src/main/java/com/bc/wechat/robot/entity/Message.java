package com.bc.wechat.robot.entity;

/**
 * 消息
 *
 * @author zhou
 */
public class Message {
    private String message_id;
    private String message_from_id;
    private String message_target_id;
    private String message_msg_type;
    private String message_body;
    private String message_from_type;
    private String message_target_type;
    private String message_create_time;
    private String message_jim_id;
    private String message_jim_ctime;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_from_id() {
        return message_from_id;
    }

    public void setMessage_from_id(String message_from_id) {
        this.message_from_id = message_from_id;
    }

    public String getMessage_target_id() {
        return message_target_id;
    }

    public void setMessage_target_id(String message_target_id) {
        this.message_target_id = message_target_id;
    }

    public String getMessage_msg_type() {
        return message_msg_type;
    }

    public void setMessage_msg_type(String message_msg_type) {
        this.message_msg_type = message_msg_type;
    }

    public String getMessage_body() {
        return message_body;
    }

    public void setMessage_body(String message_body) {
        this.message_body = message_body;
    }

    public String getMessage_from_type() {
        return message_from_type;
    }

    public void setMessage_from_type(String message_from_type) {
        this.message_from_type = message_from_type;
    }

    public String getMessage_target_type() {
        return message_target_type;
    }

    public void setMessage_target_type(String message_target_type) {
        this.message_target_type = message_target_type;
    }

    public String getMessage_create_time() {
        return message_create_time;
    }

    public void setMessage_create_time(String message_create_time) {
        this.message_create_time = message_create_time;
    }

    public String getMessage_jim_id() {
        return message_jim_id;
    }

    public void setMessage_jim_id(String message_jim_id) {
        this.message_jim_id = message_jim_id;
    }

    public String getMessage_jim_ctime() {
        return message_jim_ctime;
    }

    public void setMessage_jim_ctime(String message_jim_ctime) {
        this.message_jim_ctime = message_jim_ctime;
    }
}
