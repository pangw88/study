package com.wp.study.spring.transaction.byspringaop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wp.study.jdbc.mysql.dao.SampleDao;
import com.wp.study.jdbc.mysql.pojo.Sample;

@Service("sampleService")
public class SampleService {

	private SampleDao sampleDao;

	@Autowired // 默认以匹配类型注入
	public void setSampleDao(SampleDao sampleDao) {
		this.sampleDao = sampleDao;
	}
	
	public void addSample(Sample sample) {
		sampleDao.addSample(sample);
		System.out.println("add " + sample);
		// throw new RuntimeException("测试回滚");
	}

}
