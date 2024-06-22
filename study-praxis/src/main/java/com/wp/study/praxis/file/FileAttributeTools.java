package com.wp.study.praxis.file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.wp.study.praxis.constant.MimeTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public class FileAttributeTools {

    private static Logger logger = LoggerFactory.getLogger(FileAttributeTools.class);

    /**
     * 提取文件MIME类型
     *
     * @param file
     * @return
     */
    public static MimeTypeEnum extractMimeType(File file) {
        MimeTypeEnum mimeType = null;
        try {
            // 判断文件是否是一个目录
            if (file.isDirectory()) {
                logger.error("extractMimeType is directory, file={}", file);
                return null;
            }
            // 将文件路径转换为Path对象
            Path path = Paths.get(file.getPath());
            // 获取文件的MIME类型，这里假设是通过文件扩展名判断，实际应用中可能需要依赖文件内容或者其他信息
            String contentType = Files.probeContentType(path);
            if (StringUtils.isBlank(contentType)) {
                logger.error("extractMimeType contentType blank, file={}", file);
                return null;
            }
            String[] arr = contentType.split("/");
            mimeType = MimeTypeEnum.getEnum(arr[0]);
        } catch (Throwable e) {
            logger.error("extractMimeType fail, file={}, error:", file, e);
        }
        return mimeType;
    }

    /**
     * 提取文件创建时间
     *
     * @param file
     * @return
     */
    public static Date extractCreateTime(File file) {
        Date date = null;
        try {
            MimeTypeEnum mimeType = extractMimeType(file);
            if (null == mimeType) {
                logger.error("extractCreateTime mimeType null, file={}", file);
                return null;
            }
            switch (mimeType) {
                case VIDEO:
                    Path path = Paths.get(file.getPath());
                    // 获取文件的基本属性
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    FileTime creationTime = attributes.lastModifiedTime();
                    date = new Date(creationTime.toMillis());
                    break;
                case IMAGE:
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    break;
            }
        } catch (Throwable e) {
            logger.error("extractCreateTime fail, file={}, error:", file, e);
        }
        return date;
    }

    public static void main(String[] args) {
        extractCreateTime(new File("E:\\DCIM\\2024.part1_\\张家界_0511_075101.mov"));
    }

}
