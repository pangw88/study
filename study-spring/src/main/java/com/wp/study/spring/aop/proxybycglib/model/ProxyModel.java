package com.wp.study.spring.aop.proxybycglib.model;

public class ProxyModel {

	// cglib不能代理私有方法，但可以代理protected方法
	/*protected void print(String str) {
		// TODO Auto-generated method stub
		System.out.println("cglib:" + str);
	}*/
	
	public void print(String str) {
		// TODO
		System.out.println("cglib:" + str);
	}
	
}
