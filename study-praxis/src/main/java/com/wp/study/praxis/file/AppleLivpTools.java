package com.wp.study.praxis.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class AppleLivpTools {

    private static final Logger LOG = LoggerFactory.getLogger(AppleLivpTools.class);

    /**
     * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
     *
     * @param parent
     */
    public static void livp2ZipAndUncompress(File parent) {
        if (parent == null || !parent.exists() || !parent.isDirectory()) {
            LOG.error("can not find directory <{}>", parent);
            return;
        }
        try {
            // 获取所有子文件
            List<File> subFiles = FileCommonTools.loadFiles(parent);
            if (null == subFiles || subFiles.isEmpty()) {
                return;
            }
            // 过滤有效文件
            for (File subFile : subFiles) {
                String path = subFile.getPath();
                if (path.toLowerCase().endsWith(".livp")) {
                    String name = subFile.getName();
                    String rename = name.replaceAll(".livp", ".zip");
                    rename = rename.replaceAll(" ", "-");
                    subFile.renameTo(new File(subFile.getParentFile(), rename));
                    WinRarTools.uncompress(new File(subFile.getParentFile(), rename), null);
                } else if (path.toLowerCase().endsWith(".zip")) {
                    WinRarTools.uncompress(subFile, null);
                }
            }
        } catch (Exception e) {
            LOG.error("livp2ZipAndUncompress fail, parent={}, error:", parent, e);
        }
    }

}
