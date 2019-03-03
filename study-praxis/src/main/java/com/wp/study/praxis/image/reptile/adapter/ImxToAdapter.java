package com.wp.study.praxis.image.reptile.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtils;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;
import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class ImxToAdapter {

	/**
	 * 获取下载对象
	 * 
	 * @param aUrl
	 * @return
	 */
	public static DownloadDO getImageUrl(String albumName, String aUrl, String imgUrl) {
		// 加载要下载的页面地址
		DownloadDO download = null;
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("imgContinue", "Continue to image ... ");
			String pageContent = HttpUtils.doPost(aUrl, params, String.class);
			if (StringUtils.isBlank(pageContent)) {
				System.out.println("page content is blank aUrl=" + aUrl);
				return download;
			}

			// 获取所有img标签
			List<String> titleEles = XmlFilter.getElements(pageContent, "title");
			if (null != titleEles && !titleEles.isEmpty()) {
				String title = titleEles.get(0);
				String[] arr = title.split(" / ");
				String picName = arr[1].toLowerCase();
				picName = picName.substring(0, picName.indexOf("jpg") + 3);
				download = new DownloadDO(albumName, picName, aUrl, imgUrl.replace("/t/", "/i/"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return download;
	}
}
