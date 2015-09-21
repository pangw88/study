package com.wp.study.jdbc.derby.dao;

import java.util.List;

import com.wp.study.jdbc.derby.pojo.Config;

public interface ConfigMapper {
	
	/**
	 * 查询所有配置
	 * 
	 * @return
	 */
	List<Config> queryAllConfig();
}
