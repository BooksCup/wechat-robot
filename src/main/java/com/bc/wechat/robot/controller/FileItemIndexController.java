package com.bc.wechat.robot.controller;

import com.bc.wechat.robot.cons.Constant;
import com.bc.wechat.robot.service.ElasticSearchService;
import com.bc.wechat.robot.utils.FileUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件索引控制器
 *
 * @author zhou
 */
@RestController
@RequestMapping("/fileItemIndex")
public class FileItemIndexController {

    @Resource
    private ElasticSearchService elasticSearchService;

    private static final Logger logger = LogManager.getLogger(FileItemIndexController.class);


    private static final int THREAD_NUM = 10;

    @ApiOperation(value = "初始化fileItem索引", notes = "初始化fileItem索引")
    @PostMapping(value = "")
    public String initFileItemIndex(@RequestParam String path,
                                    @RequestParam String diskName,
                                    @RequestParam(value = "threadNum", required = false) Integer threadNum) {
        long startTimeStamp = System.currentTimeMillis();

        threadNum = (null == threadNum || threadNum <= 0) ? THREAD_NUM : threadNum;

        File pathfile = new File(path);
        if (!pathfile.isDirectory()) {
            return "path应该是个文件路径";
        }

        StringBuffer resultBuffer = new StringBuffer();

        // 清除之前的索引
        List<SearchHit> docList = new ArrayList<>();
        // 如果查找不到会报no such index错误
        try {
            int from = 0;
            List<SearchHit> tmpList = elasticSearchService.executeSearchAndGetHit(Constant.ES_INDEX_FILE_ITEM,
                    diskName, QueryBuilders.boolQuery(), null, from, 10);
            while (!CollectionUtils.isEmpty(tmpList)) {
                from++;
                docList.addAll(tmpList);
                tmpList = elasticSearchService.executeSearchAndGetHit(Constant.ES_INDEX_FILE_ITEM,
                        diskName, QueryBuilders.boolQuery(), null, from, 10);
            }
            logger.info("docList's size: " + docList.size());

        } catch (Exception e) {
            logger.error("search docList error: " + e.getMessage());
            e.printStackTrace();
            docList = null;
        }

        try {
            if (null == docList) {
                Map<String, String> initMessMap = new HashMap<>(Constant.DEFAULT_HASH_MAP_CAPACITY);
                elasticSearchService.initMapping(Constant.ES_INDEX_FILE_ITEM, diskName,
                        initMessMap, null, null, null);
            } else {
                for (SearchHit doc : docList) {
                    elasticSearchService.deleteDocumentById(Constant.ES_INDEX_FILE_ITEM, diskName, doc.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<File> fileList = new ArrayList<>();
        try {
            FileUtil.resetFileList();
            fileList = FileUtil.getFileList(path);
            logger.info("fileList's size: " + fileList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 开启10个线程处理
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);

        List<List<File>> littleFileList = FileUtil.splitFileList(fileList, threadNum);
        CountDownLatch countDownLatch = new CountDownLatch(littleFileList.size());
        for (List<File> littleUnit : littleFileList) {
            fixedThreadPool.execute(() -> {
                for (File file : littleUnit) {
                    Map<String, String> fileItemMap = new HashMap<>(Constant.DEFAULT_HASH_MAP_CAPACITY);
                    fileItemMap.put("fileName", file.getName());
                    fileItemMap.put("filePath", file.getPath());
                    fileItemMap.put("diskName", diskName);
                    fileItemMap.put("fileSize", FileUtil.getShowSize(file.length()));
                    elasticSearchService.createDocument(Constant.ES_INDEX_FILE_ITEM,
                            diskName, UUID.randomUUID().toString().replaceAll("-", ""), fileItemMap);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }


        logger.info("all finished.");
        long endTimeStamp = System.currentTimeMillis();
        resultBuffer.append("fileItem索引重建成功, 共耗时:")
                .append(endTimeStamp - startTimeStamp).append("ms.");
        return resultBuffer.toString();
    }


    @ApiOperation(value = "清除fileItem索引", notes = "清除fileItem索引")
    @DeleteMapping(value = "")
    public String clearFileItemIndex() {
        long startTimeStamp = System.currentTimeMillis();
        StringBuffer resultBuffer = new StringBuffer();

        elasticSearchService.deleteIndex(Constant.ES_INDEX_FILE_ITEM);

        long endTimeStamp = System.currentTimeMillis();
        resultBuffer.append("fileItem清除成功, 共耗时:")
                .append(endTimeStamp - startTimeStamp).append("ms.");
        return resultBuffer.toString();
    }
}
