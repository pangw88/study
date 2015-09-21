package com.wp.study.spring.aop.autoproxy.service;

import org.springframework.stereotype.Service;

@Service("sampleService")
public class SampleServiceImpl implements SampleService {

	@Override
	public void saySomething() {
		System.out.println("say some thing ");
		throw new RuntimeException();
	}

}
