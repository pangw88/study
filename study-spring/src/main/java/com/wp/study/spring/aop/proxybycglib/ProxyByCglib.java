package com.wp.study.spring.aop.proxybycglib;


import com.wp.study.spring.aop.proxybycglib.interceptor.ModelMethodInterceptor;
import com.wp.study.spring.aop.proxybycglib.model.ProxyModel;

import net.sf.cglib.proxy.Enhancer;

public class ProxyByCglib {

	/**
	 * spring将委托给spring容器管理的类，使用代理方式生成实例，
	 * 就可以动态给被代理类中的方法添加权限、日志、事物等等逻辑
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// 方法拦截器
		ModelMethodInterceptor smi = new ModelMethodInterceptor();

		Enhancer enhancer = new Enhancer();
		// 设置被代理的类为父类
		enhancer.setSuperclass(ProxyModel.class);
		// 设置方法拦截器
		enhancer.setCallback(smi);
		
		// 生成ProxyModel类的代理对象，该对象是其子类实例对象
		ProxyModel proxyModel = (ProxyModel)enhancer.create();
		
		proxyModel.print(proxyModel.getClass() + 
				" : class proxy use cglib enhancer and method interceptor");
		
	}
}
