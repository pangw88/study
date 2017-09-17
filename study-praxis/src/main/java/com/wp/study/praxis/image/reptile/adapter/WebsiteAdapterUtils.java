package com.wp.study.praxis.image.reptile.adapter;

import com.wp.study.praxis.image.reptile.ImageReptile;
import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class WebsiteAdapterUtils {
	
	public static DownloadDO getPicUrl(String aUrl, String albumName) {
		DownloadDO download = null;
		try {
			if (aUrl.contains("img.yt")) {
				download = ImgYtAdapter.getPicUrl(albumName, aUrl);
			} else if(aUrl.contains("imgcandy.net")) {
				ImageReptile.writeNoSupport("not support download url, url=" + aUrl);
			} else if(aUrl.contains("chronos.to")) {
				ImageReptile.writeNoSupport("not support download url, url=" + aUrl);
			} else if(aUrl.contains("pic-maniac.com")) {
				ImageReptile.writeNoSupport("not support download url, url=" + aUrl);
			} else if(aUrl.contains("imagetwist.com")) {
				download = ImagetwistAdapter.getPicUrl(albumName, aUrl);
			} else if(aUrl.contains("imgcandy.net")) {
				ImageReptile.writeNoSupport("not support download url, url=" + aUrl);
			} else {
				ImageReptile.writeNoSupport("not support download url, url=" + aUrl);
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return download;
	}
}
