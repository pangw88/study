package com.wp.study.jdbc.derby.pojo;

import java.sql.Date;
import java.sql.Timestamp;

public class Config {

	// config key
	private String confKey;
	// config value
	private String confValue;
	// description
	private String description;
	// create time
	private Date createTime;
	// update time
	private Timestamp updateTime;
	
	public String getConfKey() {
		return confKey;
	}
	
	public void setConfKey(String confKey) {
		this.confKey = confKey;
	}
	
	public String getConfValue() {
		return confValue;
	}
	
	public void setConfValue(String confValue) {
		this.confValue = confValue;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
