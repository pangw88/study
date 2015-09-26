package com.wp.study.spring.property.byxml.service;

public class PropertyService {

	private String property1;
	
	private String property2;
	
	public void printProperty() {
		System.out.println("property1=" + getProperty1());
		System.out.println("property2=" + getProperty2());
	}

	public String getProperty1() {
		return property1;
	}

	public void setProperty1(String property1) {
		this.property1 = property1;
	}

	public String getProperty2() {
		return property2;
	}

	public void setProperty2(String property2) {
		this.property2 = property2;
	}
	
}
