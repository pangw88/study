package com.wp.study.spring.aop.proxybycglib.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ModelMethodInterceptor implements MethodInterceptor {

	/**
	 * 
	 * @param obj
	 * 	是对被代理类的增强对象，即被代理类的子类对象
	 * @param method
	 * 	被代理类对象的方法
	 * @param args
	 * 	调用方法的参数列表
	 * @param proxy
	 * 	代理对象对被代理对象方法的方法代理
	 * 
	 */
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		// 可以在方法调用之前和之后动态添加其它业务逻辑
				System.out.println("do some thing before method invoke");
				// 使用方法代理来调用被代理对象的方法
				proxy.invokeSuper(obj, args);
				System.out.println("do some thing after method invoke");
				return null;
	}

}
