package com.bc.wechat.robot.handler.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jmessage.api.JMessageClient;
import cn.jmessage.api.common.model.message.MessageBody;
import cn.jmessage.api.common.model.message.MessagePayload;
import cn.jmessage.api.message.MessageType;
import com.alibaba.fastjson.JSON;
import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.entity.Message;
import com.bc.wechat.robot.handler.MessageHandler;
import com.bc.wechat.robot.service.ElasticSearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件索引消息处理类
 *
 * @author zhou
 */
@Service("fileItemMessageHandler")
public class FileItemMessageHandler implements MessageHandler {

    @Resource
    private JMessageClient jMessageClient;

    @Resource
    private ElasticSearchService elasticSearchService;

    private static final Logger logger = LogManager.getLogger(FileItemMessageHandler.class);

    @Override
    public void handleMessage(Message message) {

        Map bodyMap = JSON.parseObject(message.getMessage_body(), Map.class);
        String text = (String) bodyMap.get("text");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(text)) {
            //must
            MultiMatchQueryBuilder keywordMmqb = QueryBuilders.multiMatchQuery(text,
                    "fileName", "filePath");
            boolQuery = boolQuery.must(keywordMmqb);
        }
        List<String> indexList = new ArrayList<>();
        List<String> typeList = elasticSearchService.getTypeList(Constant.ES_INDEX_FILE_ITEM);
        indexList.add(Constant.ES_INDEX_FILE_ITEM);

        List<SearchHit> searchHitList = elasticSearchService.executeSearchAndGetHit(indexList,
                typeList, boolQuery, null, 0, 10);


        String result;
        if (CollectionUtils.isEmpty(searchHitList)) {
            result = "not found";
        } else {
            result = searchHitList.get(0).getSourceAsString();
        }

        MessageBody replyMessageBody = new MessageBody.Builder()
                .setText(result)
                .addExtras(new HashMap<>(Constant.DEFAULT_HASH_MAP_CAPACITY))
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
