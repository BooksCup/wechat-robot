package com.bc.wechat.robot.cons;

/**
 * 常量类
 *
 * @author zhou
 */
public class Constant {

    /**
     * 消息处理类型-文件索引
     */
    public static final String HANDLE_TYPE_FILE_ITEM = "0";

    /**
     * 索引-文件
     */
    public static final String ES_INDEX_FILE_ITEM = "file_item";

    /**
     * 初始化hashmap容量
     */
    public static final int DEFAULT_HASH_MAP_CAPACITY = 16;

    /**
     * 默认搜索开端
     */
    public static final Integer DEFAULT_SEARCH_FROM = 0;

    /**
     * 默认搜索单页数量
     */
    public static final Integer DEFAULT_SEARCH_SIZE = 10;


    /**
     * 是否需要高亮
     */
    /**
     * 不需要
     */
    public static final String IS_HIGHLIGHT_NO = "00";

    /**
     * 需要
     */
    public static final String IS_HIGHLIGHT_YES = "01";


    // 各种mapping后缀
    /**
     * suggest
     */
    public static final String MAPPING_SUFFIX_SUGGEST = "Suggest";

    /**
     * 拼音
     */
    public static final String MAPPING_SUFFIX_PINYIN = "Pinyin";

    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_IMAGE = "image";
    public static final String MSG_TYPE_VOICE = "voice";
    public static final String MSG_TYPE_CUSTOM = "custom";
}
