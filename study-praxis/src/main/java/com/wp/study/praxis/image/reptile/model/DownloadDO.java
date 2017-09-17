package com.wp.study.praxis.image.reptile.model;

public class DownloadDO {
	
	private String albumName;
	private String imageName;
	private String aUrl;
	private String downUrl;
	private boolean hasDown = false;
	
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
