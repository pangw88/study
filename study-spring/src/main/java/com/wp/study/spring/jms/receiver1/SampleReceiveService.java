package com.wp.study.spring.jms.receiver1;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.wp.study.spring.jms.message.SampleMessage;

@Service("sampleReceiveService")
public class SampleReceiveService {
	
	@Autowired
	private JmsTemplate jmsTemplate;

	public void receiveMessage() {
		// TODO Auto-generated method stub
		try {
			// receive方法是阻塞的
			ObjectMessage receiveMessage = (ObjectMessage)jmsTemplate.receive("sample.queue");
			SampleMessage sm = (SampleMessage)receiveMessage.getObject();
			System.out.print("receive message is : " + sm.getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
