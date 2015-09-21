package com.wp.study.spring.aop.autoproxy.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class SampleMethodInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("do some thing surround method 0!");
		Object obj = arg0.proceed();
		System.out.println("do some thing surround method 1!");
		return obj;
	}

}
