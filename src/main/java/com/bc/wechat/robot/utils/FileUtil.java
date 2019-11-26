package com.bc.wechat.robot.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 *
 * @author zhou
 */
public class FileUtil {
    static List<File> fileList = new ArrayList<>();

    public synchronized static List<File> getFileList(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (null != files) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileList(file.getAbsolutePath());
                } else {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    public static void resetFileList() {
        fileList.clear();
    }

    /**
     * 根据字节数计算可显示流量(如:B、KB、MB、GB等)
     *
     * @param size long类型的字节数
     * @return 可显示流量
     */
    public static String getShowSize(long size) {
        // 定义GB的计算常量
        int GB = 1024 * 1024 * 1024;
        // 定义MB的计算常量
        int MB = 1024 * 1024;
        // 定义KB的计算常量
        int KB = 1024;
        // 格式化小数
        DecimalFormat df = new DecimalFormat("0.00");
        String resultSize = "";
        if (size / GB >= 1) {
            // 如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            // 如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            // 如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }

    public static List<List<File>> splitFileList(List<File> fileList, int splitNum) {
        List<List<File>> littleFileList = new ArrayList<>();
        if (splitNum >= fileList.size()) {
            littleFileList.add(fileList);
            return littleFileList;
        }
        //每个小单元的容量
        int littleSize = fileList.size() / splitNum;
        int i = 1;
        List<File> littleUnit = new ArrayList<>();
        for (File file : fileList) {
            littleUnit.add(file);
            if (i % littleSize == 0 && i != fileList.size()) {
                littleFileList.add(littleUnit);
                littleUnit = new ArrayList<>();
            }
            if (i == fileList.size()) {
                littleFileList.add(littleUnit);
                return littleFileList;
            }
            i++;
        }
        return littleFileList;
    }

}
