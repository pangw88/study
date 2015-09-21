package com.wp.study.spring.ioc.injectbyannotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wp.study.spring.ioc.injectbyannotation.service.InjectedService;

public class InjectByAnnotation {

	public static void test() {
		try {
			String configLocation = "com/wp/study/spring/ioc/injectbyannotation/beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			
			InjectedService injectedService = (InjectedService) ctx.getBean("injectedService");
			injectedService.testPrint();
			
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
