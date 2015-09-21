package com.wp.study.spring.jms.message;

import java.io.Serializable;

public class SampleMessage implements Serializable {
	
	private static final long serialVersionUID = 9114280136432896812L;
	
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
