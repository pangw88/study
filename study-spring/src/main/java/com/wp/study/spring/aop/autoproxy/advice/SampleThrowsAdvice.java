package com.wp.study.spring.aop.autoproxy.advice;

import org.springframework.aop.ThrowsAdvice;

public class SampleThrowsAdvice implements ThrowsAdvice {
	
	/**
	 * ThrowsAdvice是一个标识接口，没有定义方法。实现ThrowsAdvice增强类中的方法，方法名称必须为afterThrowing。
	 * 必须以void afterThrowing([Method method, Object[] args, Object target], Throwable t);签名形式定义方法。
	 * 前三个参数如果定义就必须全部定义，否则只保留第四个参数
	 * 
	 * @param e
	 */
	public void afterThrowing(RuntimeException e) {
		System.out.println("do some thing after method throw runtime exception!");
	}

}
