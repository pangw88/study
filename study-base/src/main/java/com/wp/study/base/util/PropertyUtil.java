package com.wp.study.base.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertyUtil.class);
	
	private static Properties props;
	
	/**
	 * synchronized static是锁.class，阻止多个线程访问这个类的synchronized方法，
	 * synchronized是锁类的实例，阻止多个线程访问该实例的synchronized的方法。
	 * 
	 */
	static {
		synchronized (PropertyUtil.class) {
			if(props == null) {
				props = new Properties();
				InputStream is = null;
				try {
					is = PropertyUtil.class.getClassLoader()
							.getResourceAsStream("resource/account.txt");
					props.load(is);
				} catch(Exception e) {
					LOG.error(e.getMessage());
				} finally {
					IoUtil.closeQuietly(is);
				}
			} else {
				LOG.warn("props is null, need init.");
			}
		}
	}
	
	public static <T> T getProperty(String key, Class<T> requiredType) {
		T t = null;
		try {
			t = requiredType.cast(props.get(key));
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
		return t;
	}

}
