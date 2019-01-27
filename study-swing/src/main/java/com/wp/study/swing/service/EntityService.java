package com.wp.study.swing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.encryption.ClassicalCoder;
import com.wp.study.algorithm.encryption.IDEACoder;
import com.wp.study.base.pojo.Entity;
import com.wp.study.base.util.IoUtil;
import com.wp.study.jdbc.derby.dao.EntityMapper;
import com.wp.study.swing.util.CommonUtil;

public class EntityService {
	
	private static final Logger LOG = LoggerFactory.getLogger(EntityService.class);
	
	/**
	 * 添加entity
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @return
	 */
	public int addEntity(Entity entity, String key, String key1) {
		int result = 0;
		SqlSession sqlSession = getSqlSession();
		try {
			result = entityEncrypt(entity, key, key1);
			// entity encrypt fail
			if(result == 0) {
				return result;
			}
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			Map<String, String> params = new HashMap<String, String>();
			params.put("site", entity.getSite());
			params.put("name", entity.getName());
			List<Entity> resList = entityMapper.queryEntityWithConditions(params);
			if(resList == null || resList.size() == 0) {
				result = entityMapper.addEntity(entity);
				sqlSession.commit();
			} else {
				// entity has existed
				result = -1;
			}
		} catch (Exception e) {
			// add entity fail
			LOG.error(e.getMessage());
        } finally {
        	IoUtil.closeQuietly(sqlSession);
        }
		return result;
	}
	
	/**
	 * 编辑entity
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @return
	 */
	public int editEntity(Entity entity, String key, String key1) {
		int result = 0;
		SqlSession sqlSession = getSqlSession();
		try {
			result = entityEncrypt(entity, key, key1);
			if(result == 0) {
				return result; // entity encrypt fail
			}
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			result = entityMapper.editEntity(entity);
			sqlSession.commit();
		} catch (Exception e) {
			// edit entity fail
			LOG.error(e.getMessage());
        } finally {
        	IoUtil.closeQuietly(sqlSession);
        }
		return result;
	}
	
	/**
	 * 查询entity
	 * 
	 * @param level
	 * @param site
	 * @param name
	 * @param key
	 * @param key1
	 * @return
	 */
	public List<Entity> queryEntity(String level, String site, String name, String key, String key1) {
		List<Entity> entityList = null;
		SqlSession sqlSession = getSqlSession();
		site = CommonUtil.strEncrypt(site, key1);
		name = CommonUtil.strEncrypt(name, key1);
		try {
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			Map<String, String> params = new HashMap<String, String>();
			params.put("level", level);
			params.put("site", site);
			params.put("name", name);
			entityList = entityMapper.queryEntityWithConditions(params);
			for(Entity entity : entityList) {
				entityDecrypt(entity, key, key1);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
        } finally {
        	IoUtil.closeQuietly(sqlSession);
        }
		return entityList;
	}
	
	/**
	 * 删除entity
	 * 
	 * @param id
	 * @return
	 */
	public int deleteEntity(Integer id) {
		int result = 0;
		SqlSession sqlSession = getSqlSession();
		try {
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			result = entityMapper.deleteEntityById(id);
			sqlSession.commit();
		} catch (Exception e) {
			// delete entity fail
			LOG.error(e.getMessage());
        } finally {
        	IoUtil.closeQuietly(sqlSession);
        }
		return result;
	}
	
	/**
	 * 获取sqlSession
	 * 
	 * @return
	 */
	private SqlSession getSqlSession() {
		String resource = "com/wp/study/swing/service/mybatis-config.xml";
		SqlSession sqlSession = null;
		try {
			sqlSession = new SqlSessionFactoryBuilder().build(Resources  
	            .getResourceAsReader(resource)).openSession();  
        } catch (Exception e) {  
        	LOG.error(e.getMessage());
        }
		return sqlSession;
	}
	
	/**
	 * 对entity进行加密
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @param algo
	 * @return
	 */
	private static int entityEncrypt(Entity entity, String key, String key1, String algo) {
		int result = 0;
		List<String> basicCols = Entity.getBasicColumns();
		List<String> usedCols = Entity.getDetailColumns();
		try {
			Integer prior = CommonUtil.calLevel(entity.getPriority());
			for (String column : basicCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					value = ClassicalCoder.substitutionEncrypt(
							Base64.encodeBase64String(value.getBytes()), key1);
					CommonUtil.setField(entity, column, value);
				}
			}
			for (String column : usedCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					if (prior == 0) {
						value = ClassicalCoder.transpositionEncrypt(value);
					} else {
						switch (algo) {
						case "IDEA":
							
							break;

						default:
							break;
						}
						
						value = ClassicalCoder.substitutionEncrypt(Base64
								.encodeBase64String(IDEACoder.encrypt(
										value.getBytes(),
										Base64.decodeBase64(key))), key1);
					}
					CommonUtil.setField(entity, column, value);
				}
			}
			result = 1;
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return result;
	}
	
	/**
	 * 对entity进行加密
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @return
	 */
	private static int entityEncrypt(Entity entity, String key, String key1) {
		int result = 0;
		List<String> basicCols = Entity.getBasicColumns();
		List<String> usedCols = Entity.getDetailColumns();
		try {
			Integer prior = CommonUtil.calLevel(entity.getPriority());
			for (String column : basicCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					value = ClassicalCoder.substitutionEncrypt(
							Base64.encodeBase64String(value.getBytes()), key1);
					CommonUtil.setField(entity, column, value);
				}
			}
			for (String column : usedCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					if (prior == 0) {
						value = ClassicalCoder.transpositionEncrypt(value);
					} else {
						value = ClassicalCoder.substitutionEncrypt(Base64
								.encodeBase64String(IDEACoder.encrypt(
										value.getBytes(),
										Base64.decodeBase64(key))), key1);
					}
					CommonUtil.setField(entity, column, value);
				}
			}
			result = 1;
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return result;
	}

	/**
	 * 对entity进行解密
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @return
	 */
	private static int entityDecrypt(Entity entity, String key, String key1) {
		int result = 0;
		List<String> basicCols = Entity.getBasicColumns();
		List<String> usedCols = Entity.getDetailColumns();
		try {
			Integer prior = CommonUtil.calLevel(entity.getPriority());
			for (String column : basicCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					value = new String(Base64.decodeBase64(ClassicalCoder
							.substitutionDecrypt(value, key1)));
					CommonUtil.setField(entity, column, value);
				}
			}
			for (String column : usedCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					if (prior == 0) {
						value = ClassicalCoder.transpositionDecrypt(value);
					} else {
						value = new String(IDEACoder.decrypt(Base64
								.decodeBase64(ClassicalCoder
										.substitutionDecrypt(value, key1)),
								Base64.decodeBase64(key)));
					}
					CommonUtil.setField(entity, column, value);
				}
			}
			result = 1;
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return result;
	}
}
