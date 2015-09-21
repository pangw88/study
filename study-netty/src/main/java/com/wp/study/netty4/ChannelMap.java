package com.wp.study.netty4;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMap {

	private static final Map<String, Object> channelMap = new ConcurrentHashMap<String, Object>();
	
	public Object get(String name) {
		return channelMap.get(name);
	}
}
