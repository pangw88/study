package com.wp.study.praxis.constant;


import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * 文件类型枚举类
 *
 */
public enum FileTypeEnum {

    JPG("jpg", MimeTypeEnum.IMAGE),
    JPEG("jpeg", MimeTypeEnum.IMAGE),
    MOV("mov", MimeTypeEnum.VIDEO),
    MP4("mp4", MimeTypeEnum.VIDEO),
    ;

    FileTypeEnum(String code, MimeTypeEnum mimeType) {
        this.code = code;
        this.mimeType = mimeType;
    }

    private static final Map<String, FileTypeEnum> ENUM_MAP =
            Maps.uniqueIndex(Arrays.asList(values()), FileTypeEnum::getCode);

    /**
     * 以code获取枚举值
     *
     * @param code
     * @return
     */
    public static FileTypeEnum getEnum(String code) {
        return ENUM_MAP.get(code.toLowerCase());
    }

    private final String code;

    private final MimeTypeEnum mimeType;

    public String getCode() {
        return code;
    }

    public MimeTypeEnum getMimeType() {
        return mimeType;
    }
}
