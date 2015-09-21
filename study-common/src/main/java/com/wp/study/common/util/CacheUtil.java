package com.wp.study.common.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CacheUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CacheUtil.class);

	private static Map<String, Object> cacheMap = new HashMap<String, Object>();

	/**
	 * 添加缓存数据
	 * 
	 * @param key
	 * @param value
	 */
	public static void setCache(String key, Object value) {
		synchronized (cacheMap) {
			cacheMap.put(key, value);
		}
	}

	/**
	 * 获取指定类型缓存数据
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static <T> T getCache(String key, Class<T> clazz) {
		T t = null;
		try {
			t = (T) cacheMap.get(key);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return t;
	}
	
	/**
	 * 判断指定key值的对象是否存在
	 * 
	 * @param key
	 * @return
	 */
	public static boolean exists(String key) {
		return cacheMap.containsKey(key);
	}
}

