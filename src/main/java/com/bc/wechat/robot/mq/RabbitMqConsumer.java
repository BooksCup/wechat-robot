package com.bc.wechat.robot.mq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author zhou
 */
@Component
public class RabbitMqConsumer {
    private static final Logger logger = LogManager.getLogger(RabbitMqConsumer.class);


    @RabbitListener(queues = "sync.queue.im")
    public void process(String content) {
        // 处理消息
        logger.info("接收消息, 消息是: " + content);

    }
}
