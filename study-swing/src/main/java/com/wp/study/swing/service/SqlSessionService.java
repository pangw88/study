package com.wp.study.swing.service;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlSessionService {
	
	public static SqlSession getSqlSession() {
		String resource = "com/wp/study/swing/service/mybatis-config.xml";
		try {
			return new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(resource)).openSession();
		} catch (Exception e) {
			throw new RuntimeException("getSqlSession fail", e);
		}
	}
	
	public static void commitAndClose(SqlSession sqlSession) {
		if(null == sqlSession) {
			return;
		}
		try {
			sqlSession.commit();
			sqlSession.close();
		} catch (Exception e) {
			throw new RuntimeException("commitAndClose fail", e);
		}
	}

}
