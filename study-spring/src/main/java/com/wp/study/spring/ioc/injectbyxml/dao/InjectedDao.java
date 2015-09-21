package com.wp.study.spring.ioc.injectbyxml.dao;

import org.springframework.stereotype.Repository;

@Repository("injectedDao")
public class InjectedDao {
	
	public void testPrint() {
		// TODO Auto-generated method stub
		System.out.println("this is a injected dao!");
	}

}
