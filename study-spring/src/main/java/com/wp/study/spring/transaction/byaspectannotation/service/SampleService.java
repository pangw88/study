package com.wp.study.spring.transaction.byaspectannotation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wp.study.jdbc.mysql.dao.SampleDao;
import com.wp.study.jdbc.mysql.pojo.Sample;

/**
 * SampleService没有实现接口，故spring通过cglib为该类织入事务
 * 
 * @author wp
 *
 */
@Service("sampleService")
public class SampleService {

	private SampleDao sampleDao;

	@Autowired // 默认以匹配类型注入
	public void setSampleDao(SampleDao sampleDao) {
		this.sampleDao = sampleDao;
	}
	
	@Transactional
	public void addSample(Sample sample) {
		sampleDao.addSample(sample);
		System.out.println("add " + sample);
		//throw new RuntimeException("测试回滚");
	}

}
