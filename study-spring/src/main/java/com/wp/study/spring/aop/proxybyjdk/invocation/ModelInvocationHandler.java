package com.wp.study.spring.aop.proxybyjdk.invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ModelInvocationHandler implements InvocationHandler {
	
	private Object target;
	
	public ModelInvocationHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// 可以在调用method之前和之后动态添加其它业务逻辑
		System.out.println("do some thing before method invoke");
		method.invoke(target, args);
		System.out.println("do some thing after method invoke");
		return null;
	}

}
