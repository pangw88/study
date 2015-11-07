package com.wp.study.base.pojo;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Entity implements Serializable {
	
	// serialVersionUID
	private static final long serialVersionUID = -3271936173707284595L;
	
	public static final int OPERATE_INDEX = 16;
	// id
	private Integer id;
	// site
	private String site;
	// name
	private String name;
	// key1 
	private String key1;
	// key2 
	private String key2;
	// code
	private String code;
	// application code
	private String appCode;
	// pay code
	private String payCode;
	// security code
	private String secCode;
	// message code
	private String mesCode;
	// compress code
	private String compCode;
	// priority
	private String priority;
	// email
	private String email;
	// contact
	private String contact;
	// description
	private String description;
	// create time
	private Date createTime;
	// update time
	private Timestamp updateTime;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey1() {
		return key1;
	}
	
	public void setKey1(String key1) {
		this.key1 = key1;
	}
	
	public String getKey2() {
		return key2;
	}
	
	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public String getPayCode() {
		return payCode;
	}

	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getMesCode() {
		return mesCode;
	}

	public void setMesCode(String mesCode) {
		this.mesCode = mesCode;
	}

	public String getCompCode() {
		return compCode;
	}

	public void setCompCode(String compCode) {
		this.compCode = compCode;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
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

	/**
	 * 有序获取实例中所有列名
	 * @return
	 */
	public static List<String> getAllColumns() {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 7062626003441927001L;
			{
				add("id");
				add("site");
				add("name");
				add("key1");
				add("key2");
				add("code");
				add("appCode");
				add("payCode");
				add("secCode");
				add("mesCode");
				add("compCode");
				add("priority");
				add("email");
				add("contact");
				add("description");
				add("updateTime");
				add("operate");
			}
		};
	}
	
	/**
	 * 有序获取实例中数据信息列名
	 * @return
	 */
	public static List<String> getDataColumns() {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 7062626003441927001L;
			{
				add("id");
				add("site");
				add("name");
				add("key1");
				add("key2");
				add("code");
				add("appCode");
				add("payCode");
				add("secCode");
				add("mesCode");
				add("compCode");
				add("priority");
				add("email");
				add("contact");
				add("description");
				add("updateTime");
			}
		};
	}
	
	/**
	 * 有序获取实例中基本信息列名
	 * @return
	 */
	public static List<String> getBasicColumns() {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 7062626003441927001L;
			{
				add("site");
				add("name");
			}
		};
	}

	/**
	 * 有序获取实例中详细信息列名
	 * @return
	 */
	public static List<String> getDetailColumns() {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 7062626003441927001L;
			{
				add("code");
				add("appCode");
				add("payCode");
				add("secCode");
				add("mesCode");
				add("compCode");
				add("email");
				add("contact");
			}
		};
	}
}
