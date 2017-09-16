package com.wp.study.praxis.image.reptile.model;

public class DownloadDO {
	
	private String albumName;
	private String picName;
	private String aUrl;
	private String downUrl;
	private boolean hasDown = false;
	
	public DownloadDO() {
	}
	
	public DownloadDO(String aUrl, String downUrl) {
		this.aUrl = aUrl;
		this.downUrl = downUrl;
	}
	
	public DownloadDO(String albumName, String picName, String aUrl, String downUrl) {
		this.albumName = albumName;
		this.picName = picName;
		this.aUrl = aUrl;
		this.downUrl = downUrl;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
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

	public boolean isHasDown() {
		return hasDown;
	}

	public void setHasDown(boolean hasDown) {
		this.hasDown = hasDown;
	}
	
}
