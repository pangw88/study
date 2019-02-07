package com.wp.study.swing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.encryption.ClassicalCoder;
import com.wp.study.algorithm.encryption.EncryptionFactory;
import com.wp.study.base.constant.CommonConstants;
import com.wp.study.base.pojo.Entity;
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
		return addEntity(entity, key, key1, CommonConstants.ENCRYPTION_ALGO_AES);
	}

	/**
	 * 添加entity
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @param encryption
	 * @return
	 */
	public int addEntity(Entity entity, String key, String key1, String encryption) {
		int result = 0;
		SqlSession sqlSession = null;
		try {
			sqlSession = SqlSessionService.getSqlSession();
			result = entityEncrypt(entity, key, key1, encryption);
			// entity encrypt fail
			if (result == 0) {
				return result;
			}
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			Map<String, String> params = new HashMap<String, String>();
			params.put("site", entity.getSite());
			params.put("name", entity.getName());
			List<Entity> resList = entityMapper.queryEntityWithConditions(params);
			if (resList == null || resList.size() == 0) {
				result = entityMapper.addEntity(entity);
			} else {
				// entity has existed
				result = -1;
			}
		} catch (Exception e) {
			LOG.error("addEntity fail, error:", e);
		} finally {
			SqlSessionService.commitAndClose(sqlSession);
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
		return editEntity(entity, key, key1, CommonConstants.ENCRYPTION_ALGO_AES);
	}

	/**
	 * 编辑entity
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @param encryption
	 * @return
	 */
	public int editEntity(Entity entity, String key, String key1, String encryption) {
		int result = 0;
		SqlSession sqlSession = null;
		try {
			sqlSession = SqlSessionService.getSqlSession();
			result = entityEncrypt(entity, key, key1, encryption);
			if (result == 0) {
				return result; // entity encrypt fail
			}
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			result = entityMapper.editEntity(entity);
		} catch (Exception e) {
			LOG.error("editEntity fail, error:", e);
		} finally {
			SqlSessionService.commitAndClose(sqlSession);
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
		site = CommonUtil.strEncrypt(site, key1);
		name = CommonUtil.strEncrypt(name, key1);
		SqlSession sqlSession = null;
		try {
			sqlSession = SqlSessionService.getSqlSession();
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			Map<String, String> params = new HashMap<String, String>();
			params.put("level", level);
			params.put("site", site);
			params.put("name", name);
			entityList = entityMapper.queryEntityWithConditions(params);
			for (Entity entity : entityList) {
				entityDecrypt(entity, key, key1, CommonConstants.ENCRYPTION_ALGO_AES);
			}
		} catch (Exception e) {
			LOG.error("queryEntity fail, error:", e);
		} finally {
			SqlSessionService.commitAndClose(sqlSession);
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
		SqlSession sqlSession = null;
		try {
			sqlSession = SqlSessionService.getSqlSession();
			EntityMapper entityMapper = sqlSession.getMapper(EntityMapper.class);
			result = entityMapper.deleteEntityById(id);
		} catch (Exception e) {
			LOG.error("deleteEntity fail, error:", e);
		} finally {
			SqlSessionService.commitAndClose(sqlSession);
		}
		return result;
	}

	/**
	 * 对entity进行加密
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @param encryption
	 * @return
	 */
	private static int entityEncrypt(Entity entity, String key, String key1, String encryption) {
		int result = 0;
		List<String> basicCols = Entity.getBasicColumns();
		List<String> usedCols = Entity.getDetailColumns();
		try {
			Integer prior = CommonUtil.calLevel(entity.getPriority());
			for (String column : basicCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					value = ClassicalCoder.substitutionEncrypt(Base64.encodeBase64String(value.getBytes(CommonConstants.UTF_8)), key1);
					CommonUtil.setField(entity, column, value);
				}
			}
			for (String column : usedCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					if (prior == 0) {
						value = ClassicalCoder.transpositionEncrypt(value);
					} else {
						byte[] temp = EncryptionFactory.encrypt(encryption, value.getBytes(CommonConstants.UTF_8), Base64.decodeBase64(key));
						value = ClassicalCoder.substitutionEncrypt(Base64.encodeBase64String(temp), key1);
					}
					CommonUtil.setField(entity, column, value);
				}
			}
			result = 1;
		} catch (Exception e) {
			LOG.error("entityEncrypt fail, error:", e);
		}
		return result;
	}

	/**
	 * 对entity进行解密
	 * 
	 * @param entity
	 * @param key
	 * @param key1
	 * @param encryption
	 * @return
	 */
	private static int entityDecrypt(Entity entity, String key, String key1, String encryption) {
		int result = 0;
		List<String> basicCols = Entity.getBasicColumns();
		List<String> usedCols = Entity.getDetailColumns();
		try {
			Integer prior = CommonUtil.calLevel(entity.getPriority());
			for (String column : basicCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					value = new String(Base64.decodeBase64(ClassicalCoder.substitutionDecrypt(value, key1)), CommonConstants.UTF_8);
					CommonUtil.setField(entity, column, value);
				}
			}
			for (String column : usedCols) {
				String value = (String) CommonUtil.getField(entity, column);
				if (StringUtils.isNotEmpty(value)) {
					String decryptValue = null;
					if (prior == 0) {
						decryptValue = ClassicalCoder.transpositionDecrypt(value);
					} else {
						byte[] temp = EncryptionFactory.decrypt(encryption,
								Base64.decodeBase64(ClassicalCoder.substitutionDecrypt(value, key1)),
								Base64.decodeBase64(key));
						decryptValue = new String(temp, CommonConstants.UTF_8);
					}
					CommonUtil.setField(entity, column, decryptValue);
				}
			}
			result = 1;
		} catch (Exception e) {
			LOG.error("entityDecrypt fail, error:", e);
		}
		return result;
	}

}
