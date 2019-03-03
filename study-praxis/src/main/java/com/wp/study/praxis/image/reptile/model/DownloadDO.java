package com.wp.study.praxis.image.reptile.model;

public class DownloadDO {
	
	private String albumName;
	private String imageName;
	private String aUrl;
	private String imgUrl;
	private String downUrl;
	private Integer status;
	private Long wasteTime;
	private boolean hasDown = false;
	private int tryTimes = 0;
	
	public DownloadDO() {
	}
	
	public DownloadDO(String aUrl, String downUrl) {
		this.aUrl = aUrl;
		this.downUrl = downUrl;
	}
	
	public DownloadDO(String albumName, String imageName, String aUrl, String downUrl) {
		this.albumName = albumName;
		this.imageName = imageName;
		this.aUrl = aUrl;
		this.downUrl = downUrl;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getaUrl() {
		return aUrl;
	}

	public void setaUrl(String aUrl) {
		this.aUrl = aUrl;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getWasteTime() {
		return wasteTime;
	}

	public void setWasteTime(Long wasteTime) {
		this.wasteTime = wasteTime;
	}

	public boolean isHasDown() {
		return hasDown;
	}

	public void setHasDown(boolean hasDown) {
		this.hasDown = hasDown;
	}

	public int getTryTimes() {
		return tryTimes;
	}

	public void setTryTimes(int tryTimes) {
		this.tryTimes = tryTimes;
	}
	
	public int addTryTimes() {
		return ++tryTimes;
	}
	
}
