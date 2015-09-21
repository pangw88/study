package com.wp.study.spring.aop.proxybyjdk.model;

public class ProxyModelIntfImpl implements ProxyModelIntf {

	/**
	 * jdk代理是对接口的代理，故代理的方法必须是公有方法
	 * 
	 */
	@Override
	public void print(String str) {
		// TODO Auto-generated method stub
		System.out.println(str);
	}

}
