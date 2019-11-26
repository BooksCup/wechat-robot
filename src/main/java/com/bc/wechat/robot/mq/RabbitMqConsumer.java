package com.bc.wechat.robot.mq;

import com.alibaba.fastjson.JSON;
import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.entity.CanalEntity;
import com.bc.wechat.robot.entity.Message;
import com.bc.wechat.robot.handler.MessageHandler;
import com.bc.wechat.robot.utils.CommonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhou
 */
@Component
public class RabbitMqConsumer {
    private static final Logger logger = LogManager.getLogger(RabbitMqConsumer.class);

    private static final String INSERT = "INSERT";

    private static final String TABLE_MESSAGE = "t_message";

    @Resource
    private MessageHandler fileItemMessageHandler;


    @RabbitListener(queues = "sync.queue.im")
    public void process(String content) {
        // 处理消息
        logger.info("接收消息, 消息是: " + content);
        CanalEntity canalEntity = JSON.parseObject(content, CanalEntity.class);
        // 消息相关
        if (TABLE_MESSAGE.equals(canalEntity.getTable())) {
            // 新增消息
            if (INSERT.equals(canalEntity.getEventType())) {
                Message message = JSON.parseObject(canalEntity.getAfter(), Message.class);
                logger.info("body: " + message.getMessage_body());
                String handleType = CommonUtil.getHandleType(message.getMessage_target_id());
                if (Constant.HANDLE_TYPE_FILE_ITEM.equals(handleType)) {
                    fileItemMessageHandler.handleMessage(message);
                }
            }
        }

    }
}
