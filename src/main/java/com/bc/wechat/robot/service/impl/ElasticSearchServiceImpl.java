package com.bc.wechat.robot.service.impl;

import com.alibaba.fastjson.JSON;
import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.enums.ResponseMsg;
import com.bc.wechat.robot.service.ElasticSearchService;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkIndexByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zhou
 */
@Service("elasticSearchService")
public class ElasticSearchServiceImpl implements ElasticSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

    @Autowired
    private TransportClient client;

    /**
     * 初始化mapping
     *
     * @param index            索引
     * @param type             类型
     * @param description      description
     * @param ikFieldList      ik分词field
     * @param suggestFieldList 推荐field
     * @param pinyinFieldList  拼音field
     */
    @Override
    public void initMapping(String index, String type, Map<String, String> description,
                            List<String> ikFieldList, List<String> suggestFieldList, List<String> pinyinFieldList) {
        try {
            client.admin().indices().prepareCreate(index).execute().actionGet();
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject().startObject(
                    type).startObject("properties");
            for (Map.Entry<String, String> entry : description.entrySet()) {
                String fieldName = entry.getKey();
                if (!CollectionUtils.isEmpty(ikFieldList) && ikFieldList.contains(fieldName)) {
                    xContentBuilder.startObject(entry.getKey());
                    xContentBuilder.field("type", "string");
                    xContentBuilder.field("analyzer", "ik_max_word");
                    xContentBuilder.startObject("fields");
                    xContentBuilder.startObject("keyword");
                    xContentBuilder.field("type", "keyword");
                    xContentBuilder.field("ignore_above", 256);
                    xContentBuilder.endObject().endObject().endObject();
                } else {
                    xContentBuilder.startObject(entry.getKey());
                    xContentBuilder.field("type", "text");
                    xContentBuilder.startObject("fields");
                    xContentBuilder.startObject("keyword");
                    xContentBuilder.field("type", "keyword");
                    xContentBuilder.field("ignore_above", 256);
                    xContentBuilder.endObject().endObject().endObject();
                }

                if (!CollectionUtils.isEmpty(suggestFieldList) && suggestFieldList.contains(fieldName)) {
                    xContentBuilder.startObject(entry.getKey() + Constant.MAPPING_SUFFIX_SUGGEST);
                    xContentBuilder.field("type", "completion");
                    xContentBuilder.field("analyzer", "standard");
                    xContentBuilder.field("preserve_separators", false);
                    xContentBuilder.field("preserve_position_increments", false);
                    xContentBuilder.field("max_input_length", 50);
                    xContentBuilder.endObject();
                }

                if (!CollectionUtils.isEmpty(pinyinFieldList) && pinyinFieldList.contains(fieldName)) {
                    xContentBuilder.startObject(entry.getKey() + Constant.MAPPING_SUFFIX_PINYIN);
                    xContentBuilder.field("type", "text");
                    xContentBuilder.field("analyzer", "pinyin");
                    xContentBuilder.endObject();
                }
            }
            xContentBuilder.endObject().endObject().endObject();
            PutMappingRequest mapping = Requests.putMappingRequest(index).type(type).source(
                    xContentBuilder);
            client.admin().indices().putMapping(mapping).actionGet();

            logger.info("initIndex success!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("initIndex error! errorMsg: " + e.getMessage());
        }
    }


    /**
     * 插入文档
     *
     * @param index   索引
     * @param type    类型
     * @param id      文档主键
     * @param dataMap 数据map
     * @return true:插入成功 false:插入失败
     */
    @Override
    public boolean createDocument(String index, String type, String id,
                                  Map<String, String> dataMap) {
        boolean result;
        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();

            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                String fieldName = entry.getKey();
                xContentBuilder.field(fieldName, entry.getValue());

            }
            xContentBuilder.endObject();
            client.prepareIndex(index, type, id).setSource(xContentBuilder).get();
            result = true;
        } catch (Exception e) {
            logger.error(
                    "createDoucument error! errorMsg: " + e.getMessage() + ", dataMap:" + dataMap);
            result = false;
        }
        return result;
    }

    /**
     * 修改文档
     *
     * @param index   索引
     * @param type    类型
     * @param id      文档主键
     * @param dataMap 数据map
     * @return true:插入成功 false:插入失败
     */
    @Override
    public boolean updateDocument(String index, String type, String id,
                                  Map<String, String> dataMap) {
        boolean result;
        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                String fieldName = entry.getKey();
                xContentBuilder.field(fieldName, entry.getValue());
            }
            xContentBuilder.endObject();

            UpdateResponse response = client.prepareUpdate(index, type, id).setDoc(
                    xContentBuilder).get();
            logger.info("updateDocument result: " + response.getResult());
            result = true;
        } catch (Exception e) {
            logger.error("updateDocument error! errorMsg: " + e.getMessage());
            result = false;
        }
        return result;
    }


    /**
     * 批量插document
     *
     * @param index          索引
     * @param type           类型
     * @param dataList       数据列表
     * @param primaryKeyName document的主键,一般为业务表的主键
     * @param <T>            泛型
     */
    @Override
    public <T> void batchInsertDocument(String index, String type, List<T> dataList, String primaryKeyName) {
        batchInsertDocument(index, type, dataList, primaryKeyName, null, null);
    }

    /**
     * 批量插document
     *
     * @param index            索引
     * @param type             类型
     * @param dataList         数据列表
     * @param primaryKeyName   document的主键,一般为业务表的主键
     * @param suggestFieldList 推荐field
     * @param pinyinFieldList  拼音field
     * @param <T>              泛型
     */
    @Override
    public <T> void batchInsertDocument(String index, String type, List<T> dataList, String primaryKeyName,
                                        List<String> suggestFieldList, List<String> pinyinFieldList) {
        try {
            if (StringUtils.isEmpty(primaryKeyName)) {
                primaryKeyName = "id";
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (Object data : dataList) {
                Map<String, String> dataMap = BeanUtils.describe(data);
                // 移除 class
                dataMap.remove("class");

                // 加入suggest field
                if (!CollectionUtils.isEmpty(suggestFieldList)) {
                    for (String suggestField : suggestFieldList) {
                        // suggest null value 过滤
                        if (null != dataMap.get(suggestField)) {
                            dataMap.put(suggestField + Constant.MAPPING_SUFFIX_SUGGEST, dataMap.get(suggestField));
                        }
                    }
                }

                // 加入pinyin field
                if (!CollectionUtils.isEmpty(pinyinFieldList)) {
                    for (String pinyinField : pinyinFieldList) {
                        // pinyin null value 过滤
                        if (null != dataMap.get(pinyinField)) {
                            dataMap.put(pinyinField + Constant.MAPPING_SUFFIX_PINYIN, dataMap.get(pinyinField));
                        }
                    }
                }

                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    xContentBuilder.field(entry.getKey(), entry.getValue());
                }
                xContentBuilder.endObject();
                bulkRequest.add(client.prepareIndex(index, type, dataMap.get(primaryKeyName)).
                        setSource(xContentBuilder));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                logger.error("batch create index error. msg: " + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * 批量插document
     * 数据格式:List<Map>, 主要用于外部调用
     *
     * @param index          索引
     * @param type           类型
     * @param dataMapList    数据列表
     * @param primaryKeyName document的主键,一般为业务表的主键
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<String> batchInsertDocumentByDataMap(String index, String type, List<Map> dataMapList,
                                                               String primaryKeyName) {
        ResponseEntity<String> responseEntity;
        try {
            if (StringUtils.isEmpty(primaryKeyName)) {
                primaryKeyName = "id";
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (Map dataMap : dataMapList) {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
                for (Object entry : dataMap.entrySet()) {
                    xContentBuilder.field(((Map.Entry) entry).getKey().toString(),
                            ((Map.Entry) entry).getValue() == null ? null : ((Map.Entry) entry).getValue().toString());
                }
                xContentBuilder.endObject();
                String indexId = dataMap.get(primaryKeyName) == null ?
                        UUID.randomUUID().toString().replaceAll("-", "") : dataMap.get(primaryKeyName).toString();
                bulkRequest.add(client.prepareIndex(index, type, indexId).
                        setSource(xContentBuilder));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                logger.error("batch insert document error.");
                responseEntity = new ResponseEntity<>(ResponseMsg.ES_BATCH_INSERT_DOCUMENT_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                responseEntity = new ResponseEntity<>(ResponseMsg.ES_BATCH_INSERT_DOCUMENT_SUCCESS.value(),
                        HttpStatus.OK);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            responseEntity = new ResponseEntity<>(ResponseMsg.ES_BATCH_INSERT_DOCUMENT_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    /**
     * 删除索引
     *
     * @param index 索引
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<String> deleteIndex(String index) {
        ResponseEntity<String> responseEntity;
        try {
            DeleteIndexResponse response = client.admin().indices().prepareDelete(
                    index).execute().actionGet();

            if (response.isAcknowledged()) {
                logger.info("delete index:[" + index + "] success!");
                responseEntity = new ResponseEntity<>(ResponseMsg.ES_DELETE_INDEX_SUCCESS.value(),
                        HttpStatus.OK);
            } else {
                logger.error("delete index:[" + index + "] error!");
                responseEntity = new ResponseEntity<>(ResponseMsg.ES_DELETE_INDEX_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("deleteIndex error. index: " + index + ",errorMsg: " + e.getMessage());
            responseEntity = new ResponseEntity<>(ResponseMsg.ES_DELETE_INDEX_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    /**
     * 根据ID删除索引下的文档
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档ID
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<String> deleteDocumentById(String index, String type, String id) {
        ResponseEntity<String> responseEntity;
        try {
            client.prepareDelete(index, type, id).get();
            responseEntity = new ResponseEntity<>(ResponseMsg.ES_DELETE_DOCUMENT_BY_ID_SUCCESS.value(),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("deleteDocById error. index: " + index
                    + ",errorMsg: " + e.getMessage());
            responseEntity = new ResponseEntity<>(ResponseMsg.ES_DELETE_DOCUMENT_BY_ID_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    /**
     * 通过查询条件删除文档
     *
     * @param index  索引
     * @param filter 查询条件
     * @return 成功删除的文档数
     */
    @Override
    public long deleteByQuery(String index, QueryBuilder filter) {
        try {
            BulkIndexByScrollResponse response =
                    DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                            .filter(filter)
                            .source(index).get();
            // 删除文档数量
            long deleted = response.getDeleted();
            logger.info("delete: " + deleted + " docs. index: " + index);
            return deleted;
        } catch (Exception e) {
            logger.error("deleteByQuery error. index: " + index + ", errorMsg: " + e.getMessage());
            return 0;
        }
    }

    // 查询

    /**
     * @param index        索引
     * @param type         type
     * @param queryBuilder 查询
     * @param postFilter   过滤
     * @param from         搜索开端
     * @param size         搜索文档数量
     * @param isHighlight  是否需要高亮 "00":不需要 "01":需要
     * @return 查询出来的文档, List<Json>格式
     */
    @Override
    public List<String> executeSearch(String index, String type, QueryBuilder queryBuilder,
                                      QueryBuilder postFilter, Integer from, Integer size, String isHighlight) {
        List<String> indexList = new ArrayList<>();
        indexList.add(index);
        List<String> typeList = new ArrayList<>();
        typeList.add(type);
        return executeSearch(indexList, typeList, queryBuilder, postFilter, from, size, isHighlight);
    }

    /**
     * 查询
     * DSL查询语言中存在两种：查询DSL（query DSL）和过滤DSL（filter DSL）。
     * 查询和过滤的场景不一样
     * 查询(query):这个文档匹不匹配这个查询，它的相关度高么？
     * 一些query的场景：
     * 与full text search的匹配度最高
     * 包含run单词，如果包含这些单词：runs、running、jog、sprint，也被视为包含run单词
     * 包含quick、brown、fox。这些词越接近，这份文档的相关性就越高
     * 过滤(filter):这个文档匹不匹配？
     * 答案很简单，是或者不是。它不会去计算任何分值，也不会关心返回的排序问题，因此效率会高一点。
     * 一些过滤的情况：
     * 创建日期是否在2013-2014年间？
     * status字段是否为published？
     * lat_lon字段是否在某个坐标的10公里范围内？
     *
     * @param indexList    索引
     * @param typeList     type
     * @param queryBuilder 查询
     * @param postFilter   过滤
     * @param from         搜索开端
     * @param size         搜索文档数量
     * @param isHighlight  是否需要高亮 "00":不需要 "01":需要
     * @return 查询出来的文档, List<Json>格式
     */
    @Override
    public List<String> executeSearch(List<String> indexList, List<String> typeList, QueryBuilder queryBuilder,
                                      QueryBuilder postFilter, Integer from, Integer size, String isHighlight) {
        List<String> resultList = new ArrayList<>();
        String[] indexs;
        String[] types;
        if (CollectionUtils.isEmpty(indexList)) {
            throw new RuntimeException("indexs can't be null!");
        } else {
            String[] indexArray = new String[indexList.size()];
            indexs = indexList.toArray(indexArray);
        }

        if (CollectionUtils.isEmpty(typeList)) {
            throw new RuntimeException("types can't be null!");
        } else {
            String[] typeArray = new String[typeList.size()];
            types = typeList.toArray(typeArray);
        }

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexs).setTypes(types);
        if (null == queryBuilder) {
            throw new RuntimeException("QueryBuilder can't be null!");
        }
        searchRequestBuilder.setQuery(queryBuilder);
        if (null != postFilter) {
            searchRequestBuilder.setPostFilter(postFilter);
        }
        if (null == from) {
            searchRequestBuilder.setFrom(0);
        }

        size = (null == size || 0 == size) ? Constant.DEFAULT_SEARCH_SIZE : size;
        searchRequestBuilder.setFrom(
                (null == from || 0 == from) ? Constant.DEFAULT_SEARCH_FROM : size * (from - 1));
        searchRequestBuilder.setSize(size);

        // 需要高亮
        if (Constant.IS_HIGHLIGHT_YES.equals(isHighlight)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<font color='red'>");
            highlightBuilder.postTags("</font>");
            highlightBuilder.field("*");
            searchRequestBuilder.highlighter(highlightBuilder);
        }

        SearchResponse response = searchRequestBuilder.get();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceMap = searchHit.getSource();
            // 需要高亮
            if (Constant.IS_HIGHLIGHT_YES.equals(isHighlight)) {
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
                    // 高亮字段拼接
                    Text[] text = entry.getValue().getFragments();
                    sourceMap.put(entry.getKey() + "Hl", text[0].string());
                }
            }
            resultList.add(JSON.toJSONString(sourceMap));
        }
        return resultList;
    }

    @Override
    public List<SearchHit> executeSearchAndGetHit(String index, String type, QueryBuilder queryBuilder,
                                                  QueryBuilder postFilter, Integer from, Integer size) {
        List<String> indexList = new ArrayList<>();
        List<String> typeList = new ArrayList<>();
        indexList.add(index);
        typeList.add(type);
        return executeSearchAndGetHit(indexList, typeList, queryBuilder, postFilter, from, size);
    }

    @Override
    public List<SearchHit> executeSearchAndGetHit(List<String> indexList, List<String> typeList, QueryBuilder queryBuilder,
                                                  QueryBuilder postFilter, Integer from, Integer size) {
        List<SearchHit> resultList = new ArrayList<>();
        String[] indexs;
        String[] types;
        if (CollectionUtils.isEmpty(indexList)) {
            throw new RuntimeException("indexs can't be null!");
        } else {
            String[] indexArray = new String[indexList.size()];
            indexs = indexList.toArray(indexArray);
        }

        if (CollectionUtils.isEmpty(typeList)) {
            throw new RuntimeException("types can't be null!");
        } else {
            String[] typeArray = new String[typeList.size()];
            types = typeList.toArray(typeArray);
        }

        SearchRequestBuilder requestBuilder = client.prepareSearch(indexs).setTypes(types);

        if (null == queryBuilder) {
            throw new RuntimeException("QueryBuilder can't be null!");
        }
        requestBuilder.setQuery(queryBuilder);
        if (null != postFilter) {
            requestBuilder.setPostFilter(postFilter);
        }
        if (null == from) {
            requestBuilder.setFrom(0);
        }
        size = (null == size || 0 == size) ? Constant.DEFAULT_SEARCH_SIZE : size;
        requestBuilder.setFrom(
                (null == from || 0 == from) ? Constant.DEFAULT_SEARCH_FROM : size * from);
        requestBuilder.setSize(size);

        SearchResponse response = requestBuilder.get();

        SearchHit[] searchHits = response.getHits().hits();
        for (SearchHit searchHit : searchHits) {
            logger.info(searchHit.getSourceAsString());
            resultList.add(searchHit);
        }
        return resultList;
    }

}
