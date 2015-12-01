package com.wp.study.mq.jms.activemq.spring.receiver2;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleReceiver {

	public static void test() {
		try {
			String configLocation = "com/wp/study/mq/jms/activemq/spring/receiver2-beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			
			// ApplicationContext接口没有定义close方法，需要调用实现类的close方法
			//((ClassPathXmlApplicationContext)ctx).close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		test();
	}
}
