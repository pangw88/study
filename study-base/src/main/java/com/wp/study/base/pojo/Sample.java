package com.wp.study.base.pojo;

import java.io.Serializable;

public class Sample implements Serializable {

	private static final long serialVersionUID = 335914662934819062L;

	private Integer id;
	
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
