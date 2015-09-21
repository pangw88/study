package com.wp.study.common.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertyUtil.class);
	
	private static Properties props;
	
	private static void init() {
		if(props == null) {
			synchronized(PropertyUtil.class) {
				props = new Properties();
				InputStream is = null;
				try {
					is = PropertyUtil.class.getClassLoader()
							.getResourceAsStream("resource/account.txt");
					props.load(is);
				} catch(Exception e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}
	
	public static <T> T getProperty(String key, Class<T> requiredType) {
		init();
		return requiredType.cast(props.get(key));
	}

	public static void main(String[] args) {
		System.out.println(PropertyUtil.getProperty("password1", String.class));
	}
}
