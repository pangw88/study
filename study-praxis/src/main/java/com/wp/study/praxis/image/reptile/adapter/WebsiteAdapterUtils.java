package com.wp.study.praxis.image.reptile.adapter;

import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class WebsiteAdapterUtils {
	
	public static DownloadDO getPicUrl(String aUrl) {
		DownloadDO download = null;
		try {
			if (aUrl.contains("img.yt")) {
				download = ImgYtAdapter.getPicUrl(aUrl);
			} else if(aUrl.contains("imgcandy.net")) {
				System.out.println("not support download url, url=" + aUrl);
			} else if(aUrl.contains("chronos.to")) {
				System.out.println("not support download url, url=" + aUrl);
			} else if(aUrl.contains("pic-maniac.com")) {
				System.out.println("not support download url, url=" + aUrl);
			} else if(aUrl.contains("imagetwist.com")) {
				System.out.println("not support download url, url=" + aUrl);
			} else if(aUrl.contains("imgcandy.net")) {
				System.out.println("not support download url, url=" + aUrl);
			} else {
				System.out.println("not support download url, url=" + aUrl);
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return download;
	}
}
