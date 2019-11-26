package com.bc.wechat.robot.enums;

/**
 * @author zhou
 */
public enum ResponseMsg {
    /**
     * Search模块接口返回信息
     */
    ES_BATCH_INSERT_DOCUMENT_SUCCESS("批量新增索引成功"),
    ES_BATCH_INSERT_DOCUMENT_ERROR("批量新增索引失败"),
    ES_BATCH_INSERT_DOCUMENT_PARAM_DATA_ERROR("批量新增索引数据格式(dataJson)不对"),

    ES_DELETE_INDEX_SUCCESS("删除索引成功"),
    ES_DELETE_INDEX_ERROR("删除索引失败"),

    ES_DELETE_DOCUMENT_BY_ID_SUCCESS("根据ID删除文档成功"),
    ES_DELETE_DOCUMENT_BY_ID_ERROR("根据ID删除文档失败"),

    ES_CREATE_MESS_INDEX_PATH_ILLEGAL("path应该是个文件路径"),

    ;

    private final String reasonPhrase;

    ResponseMsg(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String value() {
        return reasonPhrase;
    }
}
