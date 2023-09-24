package com.wp.study.praxis.text;

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;

public class AsciiTools {

    // 中文字符
    private static Set<UnicodeBlock> chineseUnicodeBlocks = new HashSet<UnicodeBlock>() {{
        add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        add(UnicodeBlock.GENERAL_PUNCTUATION);
        add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
        add(UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
    }};

    // 日语字符
    private static Set<UnicodeBlock> japaneseUnicodeBlocks = new HashSet<UnicodeBlock>() {{
        add(UnicodeBlock.HIRAGANA);
        add(UnicodeBlock.KATAKANA);
        add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
    }};

    // 韩语字符
    private static Set<UnicodeBlock> koreanUnicodeBlocks = new HashSet<UnicodeBlock>() {{
        add(UnicodeBlock.HANGUL_JAMO);
        add(UnicodeBlock.HANGUL_JAMO_EXTENDED_A);
        add(UnicodeBlock.HANGUL_JAMO_EXTENDED_B);
        add(UnicodeBlock.HANGUL_COMPATIBILITY_JAMO);
        add(UnicodeBlock.HANGUL_SYLLABLES);
    }};

    /** 判断单个字符是否为中文 */
    public static boolean isChineseChar(char c) {
        return chineseUnicodeBlocks.contains(UnicodeBlock.of(c));
    }

    /** 判断单个字符是否为日文 */
    public static boolean isJapaneseChar(char c) {
        return japaneseUnicodeBlocks.contains(UnicodeBlock.of(c));
    }

    /** 判断单个字符是否为韩文 */
    public static boolean isKoreanChar(char c) {
        return koreanUnicodeBlocks.contains(UnicodeBlock.of(c));
    }

}
