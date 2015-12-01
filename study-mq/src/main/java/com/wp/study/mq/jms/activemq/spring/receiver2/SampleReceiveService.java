package com.wp.study.mq.jms.activemq.spring.receiver2;

import org.springframework.stereotype.Service;

import com.wp.study.mq.jms.activemq.spring.message.SampleMessage;

@Service("sampleReceiveService")
public class SampleReceiveService {

	/**
	 * 被监听器调用的方法，需要有一个消息对象参数
	 * 
	 * @param sm
	 */
	public void receiveMessage(SampleMessage sm) {
		// TODO Auto-generated method stub
		System.out.println("receive message is : " + sm);
	}

}
