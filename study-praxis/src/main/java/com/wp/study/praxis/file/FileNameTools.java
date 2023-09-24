package com.wp.study.praxis.file;

import com.wp.study.base.util.IoUtils;
import com.wp.study.praxis.text.AsciiTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileNameTools {

    private static final Logger LOG = LoggerFactory.getLogger(FileNameTools.class);

    /**
     * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
     *
     * @param parent
     */
    public static void rename(File parent) {
        if (parent == null || !parent.exists() || !parent.isDirectory()) {
            LOG.error("can not find directory <{}>", parent);
            return;
        }
        FileWriter fw = null;
        try {
            // 获取所有子文件
            List<File> subFiles = FileCommonTools.loadFiles(parent);
            if (null == subFiles || subFiles.isEmpty()) {
                return;
            }

            // 重命名详情输出
            fw = new FileWriter(new File("E:/photo/rename.txt")) {
                @Override
                public void write(String str) throws IOException {
                    super.write(str + "\r\n"); // 换行
                }
            };

            // 过滤有效文件
            List<File> files = new ArrayList<File>();
            for (File subFile : subFiles) {
                String path = subFile.getPath();
                if (path.matches("^[\\s\\S]*\\.ini$")) {
                    LOG.info("exist .ini file <{}>", path);
                    subFile.delete();
                } else if (path.matches("^[\\s\\S]*\\.db$")) { // \s匹配任意的空白符，\S则是匹配任意非空白符的字符
                    // 过滤数据库缓存文件
                    LOG.info("exist .db file <{}>", path);
                } else if (path.toLowerCase().matches("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov|m4v|zip|rar)$")) {
                    String name = subFile.getName();
                    // .*匹配除换行符外任意长度字符串
                    if (name.matches("^.*(_mov|_mk).*$")) {
                        String rename = name.replaceAll("_mov", "").replaceAll("_mk", "");
                        subFile.renameTo(new File(subFile.getParentFile(), rename));
                        fw.write(name + " rename to " + rename);
                    } else {
                        // 过滤视频文件
                        LOG.info("ignore video file <{}>", path);
                    }
                } else { // 非数据库、视频文件处理
                    files.add(new File(path) {
                        // 重写File类的compareTo方法
                        private static final long serialVersionUID = 122810055536327561L;

                        @Override
                        public int compareTo(File pathname) {
                            String name1 = this.getName();
                            String name2 = pathname.getName();
                            String pattern = "^.*\\(\\d{1,}\\).*$";
                            if (name1.matches(pattern) && name2.matches(pattern)) {
                                int i1 = Integer.valueOf(name1.split("\\(")[1].split("\\)")[0]);
                                int i2 = Integer.valueOf(name2.split("\\(")[1].split("\\)")[0]);
                                return i1 - i2;
                            }
                            return super.compareTo(pathname);
                        }
                    });
                }
            }

            // 文件按名称进行排序
            Collections.sort(files);
            Map<String, AtomicInteger> indexMap = new HashMap<String, AtomicInteger>();
            // 文件以文件夹为基础进行重命名
            for (File file : files) {
                AtomicInteger ai = indexMap.get(file.getParentFile().getName());
                if (null == ai) {
                    ai = new AtomicInteger(1);
                    indexMap.put(file.getParentFile().getName(), ai);
                }
                String name = file.getName();
                // 文件后缀，形如：'.jpg'
                String suffix = name.lastIndexOf('.') >= 0
                        ? name.substring(name.lastIndexOf('.')).toLowerCase().replace("jpeg", "jpg")
                        : "";
                // 当前文件的重命名序号
                String num = String.valueOf(ai.getAndIncrement());
                StringBuffer sb = new StringBuffer(file.getParentFile().getName() + "_");
                // 图片数量不超过1000，故最多补2个"0"
                for (int j = 0; j < 3 - num.length(); j++) {
                    sb.append("0");
                }
                String rename = sb.append(num).append(suffix).toString();
                if (!rename.equals(name)) {
                    file.renameTo(new File(file.getParentFile(), rename));
                    fw.write(name + " rename to " + rename);
                }
            }
            fw.flush();
        } catch (Exception e) {
            LOG.error("rename fail, parent={}, error:", parent, e);
        } finally {
            IoUtils.closeQuietly(fw);
        }
    }

    /**
     * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
     *
     * @param baseDir
     * @param targetDir
     *
     */
    public static void replaceName(File baseDir, File targetDir) {
        if (null == baseDir || null == targetDir || !baseDir.exists() || !targetDir.exists()) {
            LOG.error("can not find baseDir <{}>, targetDir <{}>", baseDir, targetDir);
            return;
        }
        if (!baseDir.isDirectory() || !targetDir.isDirectory() || baseDir.list().length != targetDir.list().length) {
            LOG.error("can not find baseDir length <{}>, targetDir length <{}>", baseDir.list().length,
                    targetDir.list().length);
            return;
        }
        try {
            // 过滤有效文件
            List<File> originFiles = new ArrayList<File>();
            // 获取所有子文件
            for (File file : baseDir.listFiles()) {
                originFiles.add(new File(file.getAbsolutePath()) {
                    // 重写File类的compareTo方法
                    private static final long serialVersionUID = 122810055536327561L;

                    @Override
                    public int compareTo(File pathname) {
                        String name1 = this.getName().substring(0, this.getName().indexOf("."));
                        String name2 = pathname.getName().substring(0, pathname.getName().indexOf("."));
                        int len = name1.length() > name2.length() ? name2.length() : name1.length();
                        int compare = name1.substring(0, len).compareTo(name2.substring(0, len));
                        if (compare == 0 && name1.length() != name2.length()) {
                            compare = name1.length() > name2.length() ? 1 : -1;
                        }
                        return compare;
                    }
                });
            }
            // 文件按名称进行排序
            Collections.sort(originFiles);
            // 过滤有效文件
            List<File> targetFiles = new ArrayList<File>();
            // 获取所有子文件
            for (File file : targetDir.listFiles()) {
                targetFiles.add(new File(file.getAbsolutePath()) {
                    private static final long serialVersionUID = 1L;

                    // 重写File类的compareTo方法
                    @Override
                    public int compareTo(File pathname) {
                        String name1 = this.getName();
                        String name2 = pathname.getName();
                        String pattern = "^.*\\(\\d{1,}\\).*$";
                        if (name1.matches(pattern) && name2.matches(pattern)) {
                            int i1 = Integer.valueOf(name1.split("\\(")[1].split("\\)")[0]);
                            int i2 = Integer.valueOf(name2.split("\\(")[1].split("\\)")[0]);
                            return i1 - i2;
                        }
                        return super.compareTo(pathname);
                    }
                });
            }
            // 文件按名称进行排序
            Collections.sort(targetFiles);

            // 文件以文件夹为基础进行重命名
            for (int i = 0; i < targetFiles.size(); i++) {
                File ori = originFiles.get(i);
                File tar = targetFiles.get(i);
                System.out.println(ori.getName() + "==" + tar.getName());
                tar.renameTo(new File(tar.getParentFile(), ori.getName()));
            }
        } catch (Exception e) {
            LOG.error("replaceName fail, originDir={}, targetDir={}, error:", baseDir, targetDir, e);
        }
    }

    /**
     * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
     *
     * @param dir
     * @param keyStr
     * @param replaceStr
     * @param fileType
     */
    public static void renameByReplaceStr(File dir, String keyStr, String replaceStr, String fileType) {
        renameByReplaceStr(dir, keyStr, replaceStr, fileType, true);
    }

    /**
     * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
     * readSystemFileTime 读取系统文件时间
     * @param dir
     * @param keyStr
     * @param replaceStr
     * @param fileType
     * @param readSystemFileTime
     */
    public static void renameByReplaceStr(File dir, String keyStr, String replaceStr, String fileType, boolean readSystemFileTime) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LOG.error("can not find directory <{}>", dir);
            return;
        }
        try {
            // 获取所有子文件
            List<File> subFiles = FileCommonTools.loadFiles(dir);
            if (null == subFiles || subFiles.isEmpty()) {
                return;
            }
            // 过滤有效文件
            for (File subFile : subFiles) {
                String path = subFile.getPath();
                Map<String,Object> attributes = Files.readAttributes(Paths.get(path), "*", LinkOption.NOFOLLOW_LINKS);
                FileTime lastModifiedTime = (FileTime) attributes.get("lastModifiedTime");

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(lastModifiedTime.toMillis()));

                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int hours = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                int seconds = cal.get(Calendar.SECOND);
                String monthDay = (month<10?"0":"") + (month*100 + day);
                String time = (hours<10?"0":"") + (hours*10000 + minutes*100 + seconds);
                if (path.indexOf(keyStr) >= 0) {
                    String name = subFile.getName();
                    String rename = null;
                    if (readSystemFileTime) {
                        rename = replaceStr + monthDay + "_" + time + (StringUtils.isBlank(fileType) ? "" : fileType);
                    } else {
                        rename = name.replaceAll(keyStr, replaceStr);
                    }
                    subFile.renameTo(new File(subFile.getParentFile(), rename));
                }
            }
        } catch (Exception e) {
            LOG.error("renameByReplaceStr fail, dir={}, error:", dir, e);
        }
    }

    /**
     * 通过文件名反转进行重命名，xxx_001.jpg -> gpj.100.xxx
     *
     * @param filePath
     */
    public static void renameByReverse(String filePath) {
        renameByReverse(filePath, null);
    }

    /**
     * 通过文件名反转进行重命名，测试_001.jpg -> gpj.100_试测
     *
     * @param filePath
     * @param limitedSeparators  插入的分隔串，仅针对中、日、韩文字符
     *                      如果分隔串不为空，!!，测试_001.jpg -> gpj.100_!!试!!测
     */
    public static void renameByReverse(String filePath, String... limitedSeparators) {
        if (StringUtils.isBlank(filePath)) {
            LOG.error("invalid filePath={}, limitedSeparators={}", filePath, limitedSeparators);
            return;
        }
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                LOG.error("invalid file={}, limitedSeparators={}", filePath, limitedSeparators);
                return;
            }
            String rename = new StringBuilder(file.getName()).reverse().toString();
            if (null != limitedSeparators && limitedSeparators.length > 0) {
                // 确认匹配的分隔串
                Set<String> matchedSeparatorSet = new HashSet<>();
                for (String limitedSeparator : limitedSeparators) {
                    if (rename.indexOf(limitedSeparator) >= 0) {
                        matchedSeparatorSet.add(limitedSeparator);
                    }
                    String limitedSeparatorReverse = new StringBuilder(limitedSeparator).reverse().toString();
                    if (rename.indexOf(limitedSeparatorReverse) >= 0) {
                        matchedSeparatorSet.add(limitedSeparatorReverse);
                    }
                }

                if (!matchedSeparatorSet.isEmpty()) {
                    for (String limitedSeparator : matchedSeparatorSet) {
                        // 名字中存在分隔串，则将所有分隔串转为“”
                        rename = rename.replaceAll(limitedSeparator, "");
                    }
                } else {
                    // 名字中无分隔串，则插入分隔串
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < rename.length(); i++) {
                        char ch = rename.charAt(i);
                        sb.append(ch);
                        if (AsciiTools.isChineseChar(ch) || AsciiTools.isJapaneseChar(ch) || AsciiTools.isKoreanChar(ch)) {
                            // 中文、日文、韩文字符添加分隔串
                            sb.append(limitedSeparators[i % limitedSeparators.length]);
                        }
                    }
                    rename = sb.toString();
                }
            }
            System.out.println(rename);
            file.renameTo(new File(file.getParentFile(), rename));
        } catch (Exception e) {
            LOG.error("renameByReverse fail, filePath={}, limitedSeparators={}, error:", filePath, limitedSeparators, e);
        }
    }

    public static void main(String[] args) {
        renameByReverse("E:\\" + "rar.]p0801[]5102[]版!!辑$!剪#!演!!导$!.梅!!寻$!血#!踏!![", "!!", "$!", "#!");
    }

}
