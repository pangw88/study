package com.wp.study.praxis.image.reptile.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class XmlFilter {

	public static List<String> getElements(String input, String tagName) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isBlank(input) || StringUtils.isBlank(tagName)) {
			throw new RuntimeException("the input or key is blank!");
		}
		try {
			String startStr = "<" + tagName;
			String endStr = "</" + tagName + ">";
			for (int i = 0; i < input.length(); i++) {
				if (input.startsWith(startStr, i)) {
					int startP = i;
					int endP = 0;
					// 坐标位移
					int offset = i + startStr.length();
					// 以'/>'结尾标签
					while (offset < input.length()) {
						if (input.charAt(offset) == '>') {
							if (input.charAt(offset - 1) == '/') {
								endP = offset + 1;
							}
							break;
						}
						offset++;
					}
					// 以'</key>'结尾
					if (endP == 0) {
						while (offset < input.length()) {
							if (input.startsWith(endStr, offset)) {
								endP = offset + endStr.length();
								break;
							}
							offset++;
						}
					}
					if (endP != 0) {
						result.add(input.substring(startP, endP));
						i = endP;
					} else {
						i += startStr.length();
					}
					i--; // 消除for循环中i++偏移
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> getAttributeValues(String input, String attributeName) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isBlank(input) || StringUtils.isBlank(attributeName)) {
			throw new RuntimeException("the input or key is blank!");
		}
		try {
			String startStr = " " + attributeName;
			for (int i = 0; i < input.length(); i++) {
				if (input.startsWith(startStr, i)) {
					// 坐标位移
					int offset = i + startStr.length();
					while (offset < input.length()) {
						char nextCh = input.charAt(offset);
						if (nextCh == '=') {
							offset++;
							char startCh = 0;
							int startP = 0;
							int endP = 0;
							// 获取初始坐标
							while (offset < input.length()) {
								nextCh = input.charAt(offset);
								if (nextCh == '\'' || nextCh == '"') {
									startCh = nextCh;
									offset++; // 空格，坐标移动一位
									startP = offset;
									break;
								} else if (nextCh == ' ') {
									offset++; // 空格，坐标移动一位
								} else {
									break;
								}
							}
							if (startP != 0) {
								while (offset < input.length()) {
									nextCh = input.charAt(offset);
									if (nextCh == startCh) {
										endP = offset;
										break;
									}
									offset++; // 坐标移动一位
								}
							}
							if (endP != 0) {
								result.add(input.substring(startP, endP));
								i = endP;
							} else {
								i += startStr.length();
							}
						} else if (nextCh == ' ') {
							offset++; // 空格，坐标移动一位
						} else {
							i = offset;
							break;
						}
					}
					i--; // 消除for循环中i++偏移
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String input = "<a bbb =\"sss\" bbb='aaa'>";
		String key = "bbb";
		List<String> result = getAttributeValues(input, key);
		System.out.println(result);
	}
}
