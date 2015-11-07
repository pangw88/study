package com.wp.study.jdbc.derby.dao;

import java.util.List;
import java.util.Map;

import com.wp.study.base.pojo.Entity;

public interface EntityMapper {

	/**
	 * 添加entity
	 * 
	 * @param entity
	 * @return
	 */
	int addEntity(Entity entity);
	
	/**
	 * 编辑entity
	 * 
	 * @param entity
	 * @return
	 */
	int editEntity(Entity entity);
	
	/**
	 * 查询所有entity
	 * 
	 * @return
	 */
	List<Entity> queryAllEntity();
	
	/**
	 * 查询满足条件entity
	 * 
	 * @param params
	 * @return
	 */
	List<Entity> queryEntityWithConditions(Map<String, String> params);
	
	/**
	 * 以id删除entity
	 * 
	 * @param id
	 * @return
	 */
	int deleteEntityById(Integer id);
}
