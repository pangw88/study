package com.wp.study.spring.jms.sender;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleSender {

	public static void test() {
		try {
			String configLocation = "com/wp/study/spring/jms/sender/beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			
			SampleSendService sampleSendService = (SampleSendService) ctx.getBean("sampleSendService");
			sampleSendService.sendMessage();
			
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
