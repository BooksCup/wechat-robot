package com.bc.wechat.robot.handler;

import com.bc.wechat.robot.entity.Message;

/**
 * 消息处理接口
 *
 * @author zhou
 */
public interface MessageHandler {

    /**
     * 处理消息
     *
     * @param message 消息
     */
    void handleMessage(Message message);
}
