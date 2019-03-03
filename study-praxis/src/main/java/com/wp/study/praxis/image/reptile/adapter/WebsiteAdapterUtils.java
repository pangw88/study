package com.wp.study.praxis.image.reptile.adapter;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class WebsiteAdapterUtils {

	public static DownloadDO getImageUrl(String albumName, String aUrl, String imgUrl) {
		DownloadDO download = null;
		try {
			if (aUrl.contains("img.yt")) {
				download = ImgYtAdapter.getImageUrl(albumName, aUrl);
			} else if (aUrl.contains("chronos.to")) {
				download = ChronosAdapter.getImageUrl(albumName, aUrl);
			} else if (aUrl.contains("imagetwist.com")) {
				download = ImagetwistAdapter.getImageUrl(albumName, aUrl);
			} else if (aUrl.contains("imagetwist.com")) {
				download = ImagetwistAdapter.getImageUrl(albumName, aUrl);
			} else if (aUrl.contains("imx.to")) {
				if(StringUtils.isBlank(imgUrl)) {
					System.out.println("not support download url, url=" + aUrl);
				} else {
					download = ImxToAdapter.getImageUrl(albumName, aUrl, imgUrl);
				}
			} else if (aUrl.contains("imgcandy.net")) {
				System.out.println("not support download url, url=" + aUrl);
			} else {
				System.out.println("not support download url, url=" + aUrl);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return download;
	}

}
