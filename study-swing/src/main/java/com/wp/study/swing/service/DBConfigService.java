package com.wp.study.swing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.pojo.Config;
import com.wp.study.base.util.IoUtils;
import com.wp.study.jdbc.derby.dao.ConfigMapper;

public class DBConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DBConfigService.class);
	
	private static Map<String, Object> dbCache;
	
	/**
	 * 初始化数据库配置表的配置值
	 * 
	 */
	private static synchronized void initDBConfig() {
		if(dbCache == null) {
			dbCache = new HashMap<String, Object>();
			String resource = "com/wp/study/swing/service/mybatis-config.xml";
			SqlSession sqlSession = null;
			try {
				sqlSession = new SqlSessionFactoryBuilder().build(Resources  
			        .getResourceAsReader(resource)).openSession();
		        ConfigMapper configMapper = sqlSession.getMapper(ConfigMapper.class);
		        List<Config> configs = configMapper.queryAllConfig();
		        for(Config conf : configs) {
		        	dbCache.put(conf.getConfKey(), conf.getConfValue());
		        }
	        } catch (Exception e) {  
	        	LOG.error(e.getMessage());
	        } finally {
	        	IoUtils.closeQuietly(sqlSession);
	        }
		}
	}
	
	/**
	 * 获取指定类型数据库缓存配置
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static <T> T getDBConfig(String key, Class<T> clazz) {
		T t = null;
		try {
			initDBConfig();
			t = clazz.cast(dbCache.get(key));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return t;
	}
}
