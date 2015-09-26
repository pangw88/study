package com.wp.study.spring.property.byannotation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("propertyService")
public class PropertyService {

	@Value("#{'test.property1'}")
	private String property1;
	
	@Value("#{'test.property2'}")
	private String property2;
	
	public void printProperty() {
		System.out.println("property1=" + getProperty1());
		System.out.println("property2=" + getProperty2());
	}

	public String getProperty1() {
		return property1;
	}

	public String getProperty2() {
		return property2;
	}
	
}
