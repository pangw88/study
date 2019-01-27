package com.wp.study.base.util;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(IoUtils.class);
	
	public static void closeQuietly(Closeable... closeoObjs) {
		if(null == closeoObjs || closeoObjs.length == 0) {
			return;
		}
		for(Closeable closeoObj : closeoObjs) {
			if(null == closeoObj) {
				continue;
			}
			try {
				closeoObj.close();
			} catch(Exception e) {
				LOG.error("closeQuietly fail, closeoObj={}, error:", closeoObj, e);
			}
		}
	}
	
	public static void closeQuietly(AutoCloseable... closeoObjs) {
		if(null == closeoObjs || closeoObjs.length == 0) {
			return;
		}
		for(AutoCloseable closeoObj : closeoObjs) {
			if(null == closeoObj) {
				continue;
			}
			try {
				closeoObj.close();
			} catch(Exception e) {
				LOG.error("closeQuietly fail, closeoObj={}, error:", closeoObj, e);
			}
		}
	}

}
