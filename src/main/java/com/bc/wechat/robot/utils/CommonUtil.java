package com.bc.wechat.robot.utils;

import cn.jmessage.api.common.model.message.MessageBody;
import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.entity.MsgBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用工具类
 *
 * @author zhou
 */
public class CommonUtil {
    private static Map<String, String> handleTypeMap = new HashMap<>();

    static {
        handleTypeMap.put("c8803e8993be442f9efcdf9021055fcc", Constant.HANDLE_TYPE_FILE_ITEM);
    }

    public static String getHandleType(String targetId) {
        return handleTypeMap.get(targetId);
    }

    /**
     * 将IM的message bean转为极光的message bean
     *
     * @param msgBody     IM的message bean
     * @param messageType 消息类型
     * @return 极光的message bean
     */
    public static MessageBody generateMessageBody(MsgBody msgBody, String messageType) {
        if (Constant.MSG_TYPE_TEXT.equals(messageType)) {
            return new MessageBody.Builder()
                    .setText(msgBody.getText())
                    .addExtras(msgBody.getExtras())
                    .build();
        } else if (Constant.MSG_TYPE_IMAGE.equals(messageType)) {
            return new MessageBody.Builder()
                    .setMediaId(msgBody.getMediaId())
                    .setMediaCrc32(msgBody.getMediaCrc32())
                    .setWidth(msgBody.getWidth())
                    .setHeight(msgBody.getHeight())
                    .setFormat(msgBody.getFormat())
                    .setFsize(msgBody.getFsize())
                    .build();
        } else {
            // 默认文字
            return new MessageBody.Builder()
                    .setText(msgBody.getText())
                    .addExtras(msgBody.getExtras())
                    .build();
        }
    }
}
