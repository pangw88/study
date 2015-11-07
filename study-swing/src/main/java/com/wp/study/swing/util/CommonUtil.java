package com.wp.study.swing.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.encryption.ClassicalCoder;

public class CommonUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);

	/**
	 * 获取level等级
	 * 
	 * @param level
	 * @return
	 * @throws Exception
	 */
	public static int calLevel(String level) throws Exception {
		if (StringUtils.isEmpty(level) || level.length() != 1)
			throw new Exception();
		return level.charAt(0) - 'a';
	}

	/**
	 * 获取实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param returnType
	 * @return
	 * @throws Exception
	 */
	public static <E, T> T getField(E e, String name, Class<T> returnType)
			throws Exception {
		String methodName = "get" + Character.toUpperCase(name.charAt(0))
				+ name.substring(1);
		Object value = e.getClass().getMethod(methodName).invoke(e);
		return returnType.cast(value);
	}

	/**
	 * 获取实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static <E> Object getField(E e, String name) throws Exception {
		String methodName = "get" + Character.toUpperCase(name.charAt(0))
				+ name.substring(1);
		Method method = e.getClass().getMethod(methodName);
		if (method != null) {
			Class<?> returnType = method.getReturnType();
			return returnType.cast(method.invoke(e));
		} else {
			throw new NoSuchMethodException();
		}
	}

	/**
	 * 设置实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param value
	 * @param parameterType
	 * @throws Exception
	 */
	public static <E, T> void setField(E e, String name, T value,
			Class<T> parameterType) throws Exception {
		if(value != null) {
			String methodName = "set" + Character.toUpperCase(name.charAt(0))
					+ name.substring(1);
			e.getClass().getMethod(methodName, parameterType).invoke(e, value);
		}
	}

	/**
	 * 设置实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static <E> void setField(E e, String name, Object value)
			throws Exception {
		if(value != null) {
			String methodName = "set" + Character.toUpperCase(name.charAt(0))
					+ name.substring(1);
			Method[] methods = e.getClass().getMethods();
			if (methods != null && methods.length > 0) {
				for (Method method : methods) {
					if (method.getName().equals(methodName)) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes != null && parameterTypes.length == 1) {
							method.invoke(e, parameterTypes[0].cast(value));
							break;
						}
					}
				}
			} else {
				throw new NoSuchMethodException();
			}
		}
	}

	/**
	 * 对str进行替代加密
	 * 
	 * @param str
	 * @param key1
	 * @return
	 */
	public static String strEncrypt(String str, String key) {
		try {
			if (StringUtils.isNotEmpty(str)) {
				str = ClassicalCoder.substitutionEncrypt(
						Base64.encodeBase64String(str.getBytes()), key);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return str;
	}

	/**
	 * 以key值对map进行排序
	 * 
	 * @param oriMap
	 * @return
	 */
	public Map<String, String> sortMapByKey(Map<String, String> oriMap) {
		if (oriMap == null || oriMap.isEmpty()) {
			return null;
		}
		Map<String, String> sortedMap = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String key1, String key2) {
				return key1.compareTo(key2);
			}
		});
		sortedMap.putAll(oriMap);
		return sortedMap;
	}

	/**
	 * 以key值对map进行排序
	 * 
	 * @param oriMap
	 * @return
	 */
	public Map<String, String> sortMapByValue(Map<String, String> oriMap) {
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(
					oriMap.entrySet());
			Collections.sort(entryList,
					new Comparator<Map.Entry<String, String>>() {
						public int compare(Entry<String, String> entry1,
								Entry<String, String> entry2) {
							String value1 = entry1.getValue();
							String value2 = entry2.getValue();
							if (value1 == null) {
								return -1;
							} else if (value2 == null) {
								return 1;
							}
							return value1.compareTo(value2);
						}
					});
			Iterator<Map.Entry<String, String>> iter = entryList.iterator();
			Map.Entry<String, String> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return sortedMap;
	}

	public static void main(String[] args) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec secret = new SecretKeySpec("accessKey".getBytes("UTF-8"), mac.getAlgorithm());
			mac.init(secret);
			byte[] bs = mac.doFinal("POST/accessKey=accessKey&format=json&method=alipay.ebuckler.mobile.rain.score.get&mobileList=1358828899&signatureType=HMAC-SHA1&timestamp=2015-06-28T19%3A33%3A47Z&version=1.0".getBytes());
			System.out.println(Base64.encodeBase64String(bs));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
