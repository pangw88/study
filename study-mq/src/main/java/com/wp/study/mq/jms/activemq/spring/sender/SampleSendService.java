package com.wp.study.mq.jms.activemq.spring.sender;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.wp.study.mq.jms.activemq.spring.message.SampleMessage;

@Service("sampleSendService")
public class SampleSendService {
	
	@Autowired
	private JmsTemplate jmsTemplate;

	public void sendMessage() {
		// TODO Auto-generated method stub
		final SampleMessage sm = new SampleMessage();
		sm.setText("send message test!");
		jmsTemplate.send("sample.queue", new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(sm);
			}
		});
		System.out.print("send message is : " + sm.getText());
	}

}
