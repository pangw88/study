package com.wp.study.spring.aop.autoproxy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wp.study.spring.aop.autoproxy.service.SampleService;

public class AutoProxy {
	
	public static void test() {
		try {
			String configLocation = "com/wp/study/spring/aop/autoproxy/beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			
			SampleService sampleService = (SampleService) ctx.getBean("sampleService");
			sampleService.saySomething();
			
			// ApplicationContext接口没有定义close方法，需要调用实现类的close方法
			((ClassPathXmlApplicationContext)ctx).close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		test();
	}
}
