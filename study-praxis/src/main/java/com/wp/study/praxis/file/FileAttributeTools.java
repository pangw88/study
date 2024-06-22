package com.wp.study.praxis.file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.wp.study.praxis.constant.MimeTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
//            String contentType = new MimetypesFileTypeMap().getContentType(file);
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
        BufferedReader reader = null;
        try {
            MimeTypeEnum mimeType = extractMimeType(file);
            if (null == mimeType) {
                logger.error("extractCreateTime mimeType null, file={}", file);
                return null;
            }

            switch (mimeType) {
                case VIDEO:
                    Process process = Runtime.getRuntime().exec("ffmpeg -i " + file.getPath());
                    reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 创建时间格式-> "creation_time   : 2024-06-14T10:02:42.000000Z"
                        if (line.contains("creation_time")) {
                            String timeStr = line.substring(line.indexOf(":") + 1);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                            // 解析创建时间
                            date = dateFormat.parse(timeStr.trim());
                            break;
                        }
                    }
                    break;
                case IMAGE:
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    // 设置为东八区时间
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getTimeZone("GMT+8"));
                    break;
                default:
                    Path path = Paths.get(file.getPath());
                    // 获取文件的基本属性
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    FileTime creationTime = attributes.lastModifiedTime();
                    date = new Date(creationTime.toMillis());
                    break;
            }
        } catch (Throwable e) {
            logger.error("extractCreateTime fail, file={}, error:", file, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return date;
    }

}
