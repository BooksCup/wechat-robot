package com.bc.wechat.robot.handler.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jmessage.api.JMessageClient;
import cn.jmessage.api.common.model.message.MessageBody;
import cn.jmessage.api.common.model.message.MessagePayload;
import cn.jmessage.api.message.MessageType;
import com.alibaba.fastjson.JSON;
import com.bc.wechat.robot.entity.Message;
import com.bc.wechat.robot.handler.MessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("fileItemMessageHandler")
public class FileItemMessageHandler implements MessageHandler {

    @Resource
    private JMessageClient jMessageClient;

    private static final Logger logger = LogManager.getLogger(FileItemMessageHandler.class);

    @Override
    public void handleMessage(Message message) {

        Map bodyMap = JSON.parseObject(message.getMessage_body(), Map.class);
        String text = (String) bodyMap.get("text");

        MessageBody replyMessageBody = new MessageBody.Builder()
                .setText(text)
                .addExtras(new HashMap<>())
                .build();

        MessagePayload payload = MessagePayload.newBuilder().setVersion(1)
                .setTargetType(message.getMessage_target_type()).setTargetId(message.getMessage_from_id()).setFromType("admin")
                .setFromId(message.getMessage_target_id()).setMessageType(MessageType.TEXT)
                .setMessageBody(replyMessageBody)
                // App不接收通知
                .setNoNotification(true)
                .build();

        // 文字消息
        try {
            jMessageClient.sendMessage(payload);
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
    }
}
