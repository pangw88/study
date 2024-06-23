package com.wp.study.praxis.constant;


import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * 文件类型枚举类
 *
 * @see {https://www.iana.org/assignments/media-types/media-types.xhtml}
 *
 */
public enum FileTypeEnum {

    JPG("image/jpeg", ".jpg", MimeTypeEnum.IMAGE),

    MOV("video/quicktime", ".mov", MimeTypeEnum.VIDEO),
    MP4("video/mp4", ".mp4", MimeTypeEnum.VIDEO),
    ;

    FileTypeEnum(String template, String extension, MimeTypeEnum mimeType) {
        this.template = template;
        this.extension = extension;
        this.mimeType = mimeType;
    }

    private static final Map<String, FileTypeEnum> ENUM_MAP =
            Maps.uniqueIndex(Arrays.asList(values()), FileTypeEnum::getTemplate);

    /**
     * 以template获取枚举值
     *
     * @param template
     * @return
     */
    public static FileTypeEnum getEnum(String template) {
        return ENUM_MAP.get(template.toLowerCase());
    }

    /**
     * 文件类型模版
     */
    private final String template;

    /**
     * 文件扩展名
     */
    private final String extension;

    private final MimeTypeEnum mimeType;

    public String getTemplate() {
        return template;
    }

    public String getExtension() {
        return extension;
    }

    public MimeTypeEnum getMimeType() {
        return mimeType;
    }
}
