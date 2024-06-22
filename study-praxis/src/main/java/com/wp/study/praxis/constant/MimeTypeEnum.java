package com.wp.study.praxis.constant;


import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * mime类型枚举类
 *
 * @see {https://www.iana.org/assignments/media-types/media-types.xhtml}
 */
public enum MimeTypeEnum {

    VIDEO("video"),
    IMAGE("image"),
    ;

    MimeTypeEnum(String code) {
        this.code = code;
    }

    private static final Map<String, MimeTypeEnum> ENUM_MAP =
            Maps.uniqueIndex(Arrays.asList(values()), MimeTypeEnum::getCode);

    /**
     * 以code获取枚举值
     *
     * @param code
     * @return
     */
    public static MimeTypeEnum getEnum(String code) {
        return ENUM_MAP.get(code);
    }

    private final String code;

    public String getCode() {
        return code;
    }
}
