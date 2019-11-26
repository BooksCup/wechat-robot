package com.bc.wechat.robot.utils;

import com.bc.wechat.robot.cons.Constant;

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
}
