package com.bc.wechat.robot.handler;

import com.bc.wechat.robot.entity.Message;

public interface MessageHandler {

    void handleMessage(Message message);
}
