package com.wp.study.mq.amqp.rabbitmq.spring.sender;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleSender {

	public static void main(final String... args) throws Exception {

	    AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("com/wp/study/mq/amqp/rabbitmq/spring/beans.xml");
	    RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
	    //template.setRoutingKey("foo.bar");
	    template.convertAndSend("{\"declareBody\":{},\"declareType\":\"nanShaDeclare\"}");
	    Thread.sleep(1000);
	    ctx.destroy();
	}
	
}
