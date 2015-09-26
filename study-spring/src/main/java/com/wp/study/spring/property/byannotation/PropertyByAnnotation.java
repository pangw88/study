package com.wp.study.spring.property.byannotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wp.study.spring.property.byannotation.service.PropertyService;

public class PropertyByAnnotation {

	public static void main(String[] args) {
		try {
			String configLocation = "com/wp/study/spring/property/byannotation/beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			PropertyService propertyService = (PropertyService) ctx.getBean("propertyService");
			propertyService.printProperty();
			
			// ApplicationContext接口没有定义close方法，需要调用实现类的close方法
			((ClassPathXmlApplicationContext)ctx).close();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
}
