package com.bc.wechat.robot.service;


import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhou
 */
public interface ElasticSearchService {

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
    void initMapping(String index, String type, Map<String, String> description,
                     List<String> ikFieldList, List<String> suggestFieldList, List<String> pinyinFieldList);

    /**
     * 插入文档
     *
     * @param index   索引
     * @param type    类型
     * @param id      文档主键
     * @param dataMap 数据map
     * @return true:插入成功 false:插入失败
     */
    boolean createDocument(String index, String type, String id,
                           Map<String, String> dataMap);

    /**
     * 修改文档
     *
     * @param index   索引
     * @param type    类型
     * @param id      文档主键
     * @param dataMap 数据map
     * @return true:插入成功 false:插入失败
     */
    boolean updateDocument(String index, String type, String id,
                           Map<String, String> dataMap);

    /**
     * 批量插document
     *
     * @param index          索引
     * @param type           类型
     * @param dataList       数据列表
     * @param primaryKeyName document的主键,一般为业务表的主键
     * @param <T>            泛型
     */
    <T> void batchInsertDocument(String index, String type, List<T> dataList, String primaryKeyName);

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
    <T> void batchInsertDocument(String index, String type, List<T> dataList, String primaryKeyName,
                                 List<String> suggestFieldList, List<String> pinyinFieldList);

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
    ResponseEntity<String> batchInsertDocumentByDataMap(String index, String type, List<Map> dataMapList,
                                                        String primaryKeyName);

    /**
     * 删除索引
     *
     * @param index 索引
     * @return ResponseEntity
     */
    ResponseEntity<String> deleteIndex(String index);

    /**
     * 根据ID删除索引下的文档
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档ID
     * @return ResponseEntity
     */
    ResponseEntity<String> deleteDocumentById(String index, String type, String id);

    /**
     * 通过查询条件删除文档
     *
     * @param index  索引
     * @param filter 查询条件
     * @return 成功删除的文档数
     */
    long deleteByQuery(String index, QueryBuilder filter);

    /**
     * 查询
     *
     * @param index        索引
     * @param type         type
     * @param queryBuilder 查询
     * @param postFilter   过滤
     * @param from         搜索开端
     * @param size         搜索文档数量
     * @param isHighlight  是否需要高亮 "00":不需要 "01":需要
     * @return 查询出来的文档, List<Json>格式
     */
    List<String> executeSearch(String index, String type, QueryBuilder queryBuilder,
                               QueryBuilder postFilter, Integer from, Integer size, String isHighlight);

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
    List<String> executeSearch(List<String> indexList, List<String> typeList, QueryBuilder queryBuilder,
                               QueryBuilder postFilter, Integer from, Integer size, String isHighlight);

    List<SearchHit> executeSearchAndGetHit(String index, String type, QueryBuilder queryBuilder,
                                           QueryBuilder postFilter, Integer from, Integer size);


    List<SearchHit> executeSearchAndGetHit(List<String> indexList, List<String> typeList, QueryBuilder queryBuilder,
                                           QueryBuilder postFilter, Integer from, Integer size);
}
