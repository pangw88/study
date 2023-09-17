package com.wp.study.praxis.file;

import com.wp.study.algorithm.digester.DigesterCoder;
import com.wp.study.base.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Md5Tools {

    private static final Logger LOG = LoggerFactory.getLogger(Md5Tools.class);

    /**
     * 获取文件（夹）及其子文件的md5值
     *
     * @param files
     * @param regex 匹配正则表达式的文件不计算md5值
     * @param isCut 是否剪切MD5值重复的文件
     * @return
     */
    public static void getMD5WithCut(String regex, boolean isCut, File... files) {
        if (files == null || files.length == 0) {
            LOG.error("files is null");
            return;
        }
        // 定义md5Map存储文件md5和文件path
        Map<String, String> md5Map = new HashMap<String, String>();
        FileWriter fw = null;
        try {
            // 获取所有子文件
            List<File> subFiles = FileTools.loadFiles(files);
            if (null == subFiles || subFiles.isEmpty()) {
                return;
            }

            // md5相同文件列表输出
            fw = new FileWriter(new File("E:/photo/md5.txt")) {
                public void write(String str) throws IOException {
                    super.write(str + "\r\n"); // 换行
                }
            };

            for (File subFile : subFiles) {
                String filePath = subFile.getPath();
                // 过滤匹配这则表达式的文件
                if (filePath.matches(regex)) {
                    LOG.info("<{}> is filtered", filePath);
                    continue;
                }
                String md5 = DigesterCoder.getFileDigest(subFile, "MD5");
                if (md5Map.containsKey(md5)) {
                    // 记录MD5值相同的文件
                    fw.write(md5Map.get(md5) + " === " + filePath + "，md5 = " + md5);
                    if (isCut) {
                        // 剪切重复文件到磁盘根目录的temp临时文件夹
                        int index = filePath.indexOf(File.separator);
                        File root = new File(index == -1 ? filePath : filePath.substring(0, index));
                        File temp = new File(root, "temp");
                        FileTools.cut(subFile, temp);
                    }
                } else {
                    md5Map.put(md5, subFile.getPath());
                }

            }
        } catch (Exception e) {
            LOG.error("getMD5WithCut fail, regex={}, isCut={}, error:", regex, isCut, e);
        } finally {
            IoUtils.closeQuietly(fw);
        }
    }

}
