package com.bc.wechat.robot.handler.impl;

import com.alibaba.fastjson.JSON;
import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.entity.Message;
import com.bc.wechat.robot.handler.MessageHandler;
import com.bc.wechat.robot.service.ElasticSearchService;
import com.bc.wechat.robot.service.MessageService;
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
    private ElasticSearchService elasticSearchService;

    @Resource
    private MessageService messageService;

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
            StringBuffer resultBuffer = new StringBuffer();
            for (SearchHit searchHit : searchHitList) {
                resultBuffer.append(searchHit.getSource().get("filePath"))
                        .append("\n")
                        .append("(")
                        .append(searchHit.getSource().get("fileSize"))
                        .append(")")
                        .append("\n");
            }
            resultBuffer.deleteCharAt(resultBuffer.length() - 1);
            result = resultBuffer.toString();
        }


        Map<String, Object> body = new HashMap<>(Constant.DEFAULT_HASH_MAP_CAPACITY);
        body.put("extras", new HashMap<>(Constant.DEFAULT_HASH_MAP_CAPACITY));
        body.put("text", result);

        messageService.sendMessage(message.getMessage_target_type(), message.getMessage_from_id(),
                message.getMessage_target_id(), Constant.MSG_TYPE_TEXT, JSON.toJSONString(body));

    }
}
