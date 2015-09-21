package com.wp.study.spring.ioc.injectbyxml.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wp.study.spring.ioc.injectbyxml.dao.InjectedDao;

@Service("injectedService")
public class InjectedService {

	private InjectedDao injectedDao;

	@Autowired // 默认以匹配类型注入
	public void setInjectedDao(InjectedDao injectedDao) {
		this.injectedDao = injectedDao;
	}
	
	/*@Resource // 默认以匹配名称注入
	public void setInjectedDao(InjectedDao injectedDao) {
		this.injectedDao = injectedDao;
	}*/
	
	public void testPrint() {
		// TODO Auto-generated method stub
		System.out.println("this is a injected service!");
		injectedDao.testPrint();
	}

}