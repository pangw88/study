package com.wp.study.base.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

public class ByteUtils {

	public static String bytes2String(byte[] bytes) {
		if (null == bytes) {
			return null;
		}
		List<Byte> list = new ArrayList<Byte>();
		for (byte b : bytes) {
			list.add(b);
		}
		return JSON.toJSONString(list);
	}

	public static byte[] string2Bytes(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		List<Byte> list = JSON.parseArray(str, Byte.class);
		byte[] bytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			bytes[i] = list.get(i).byteValue();
		}
		return bytes;
	}

}
