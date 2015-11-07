package com.wp.study.spring.transaction.byaspectannotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wp.study.base.pojo.Sample;
import com.wp.study.spring.transaction.byaspectannotation.service.SampleService;

public class ByAspectAnnotation {
	
	public static void main(String[] args) {
		try {
			String configLocation = "com/wp/study/spring/transaction/byaspectannotation/beans.xml";
			// 加载应用上下文
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
			
			SampleService sampleService = (SampleService) ctx.getBean("sampleService");
			Sample sample = new Sample();
			sample.setName("test1");
			sampleService.addSample(sample);
			
			// ApplicationContext接口没有定义close方法，需要调用实现类的close方法
			((ClassPathXmlApplicationContext)ctx).close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
