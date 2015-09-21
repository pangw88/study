package com.wp.study.spring.aop.autoproxy.advice;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

public class SampleAfterAdvice implements AfterReturningAdvice {

	@Override
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("do some thing after method!");
	}

}
