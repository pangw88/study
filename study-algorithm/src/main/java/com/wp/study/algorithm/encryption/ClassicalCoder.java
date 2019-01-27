package com.wp.study.algorithm.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 古典安全编码组件
 * 
 * @version 1.0
 */
public class ClassicalCoder {

	private static final Logger LOG = LoggerFactory.getLogger(ClassicalCoder.class);

	/**
	 * 移位加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String transpositionEncrypt(String data) {
		String res = null;
		if (data != null) {
			int length = data.length();
			int group = length / 5 + (length % 5 == 0 ? 0 : 1);
			char[] dataArr = data.toCharArray();
			for (int i = 0; i < group; i++) {
				for (int j = 0; j < 3; j++) {
					int index = i * 5 + j;
					if (index + 2 < length) {
						char temp = dataArr[index + 2];
						dataArr[index + 2] = dataArr[index];
						dataArr[index] = temp;
					}
				}
			}
			res = new String(dataArr);
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}

	/**
	 * 移位解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String transpositionDecrypt(String data) {
		String res = null;
		if (data != null) {
			int length = data.length();
			int group = length / 5 + (length % 5 == 0 ? 0 : 1);
			char[] dataArr = data.toCharArray();
			for (int i = 0; i < group; i++) {
				for (int j = 4; j > 1; j--) {
					int index = i * 5 + j;
					if (index + 1 > length) {
						continue;
					}
					char temp = dataArr[index];
					dataArr[index] = dataArr[index - 2];
					dataArr[index - 2] = temp;
				}
			}
			res = new String(dataArr);
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}

	/**
	 * 替代加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String substitutionEncrypt(String data) {
		String res = null;
		if (data != null) {
			int length = data.length();
			char[] dataArr = data.toCharArray();
			for (int i = 0; i < length; i++) {
				if (dataArr[i] >= 32 && dataArr[i] <= 126) {
					dataArr[i] = (char) ((dataArr[i] + 3 - 32) % 95 + 32);
				}
			}
			res = new String(dataArr);
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}

	/**
	 * 替代解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String substitutionDecrypt(String data) {
		String res = null;
		if (data != null) {
			int length = data.length();
			char[] dataArr = data.toCharArray();
			for (int i = 0; i < length; i++) {
				if (dataArr[i] >= 32 && dataArr[i] <= 126) {
					dataArr[i] = (char) ((dataArr[i] - 32 + 95 - 3) % 95 + 32);
				}
			}
			res = new String(dataArr);
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}

	/**
	 * 替代加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String substitutionEncrypt(String data, String key) {
		String res = null;
		if (data != null) {
			int sec = 0;
			Integer value = Integer.valueOf(key);
			while (value != 0) {
				sec += value % 10;
				value /= 10;
			}
			if (sec != 0) {
				int length = data.length();
				char[] dataArr = data.toCharArray();
				for (int i = 0; i < length; i++) {
					if (dataArr[i] >= 32 && dataArr[i] <= 126) {
						dataArr[i] = (char) (dataArr[i] * sec % 95 + 32);
					}
				}
				res = new String(dataArr);
			}
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}

	/**
	 * 替代解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String substitutionDecrypt(String data, String key) {
		String res = null;
		if (data != null) {
			int sec = 0;
			Integer value = Integer.valueOf(key);
			while (value != 0) {
				sec += value % 10;
				value /= 10;
			}
			if (sec != 0) {
				int length = data.length();
				char[] dataArr = data.toCharArray();
				for (int i = 0; i < length; i++) {
					if (dataArr[i] >= 32 && dataArr[i] <= 126) {
						int temp = dataArr[i] - 32;
						for (int j = 32; j <= 126; j++) {
							if (temp == j * sec % 95) {
								dataArr[i] = (char) j;
							}

						}
					}
				}
				res = new String(dataArr);
			}
		} else {
			LOG.warn("data is null!");
		}
		return res;
	}
}
