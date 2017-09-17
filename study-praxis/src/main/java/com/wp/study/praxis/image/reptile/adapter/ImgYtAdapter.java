package com.wp.study.praxis.image.reptile.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtil;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;
import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class ImgYtAdapter {
	
	/**
	 * 获取下载对象
	 * 
	 * @param aUrl
	 * @return
	 */
	public static DownloadDO getImageUrl(String albumName, String aUrl) {
		// 加载要下载的页面地址
		DownloadDO download = null;
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("imgContinue", "Continue to image ... ");
			String pageContent = HttpUtil.doPost(aUrl, params, String.class);
			if (StringUtils.isBlank(pageContent)) {
				System.out.println("page content is blank aUrl=" + aUrl);
				return download;
			}
			
			// 获取所有img标签
			List<String> imgEles = XmlFilter.getElements(pageContent, "img");
			if (null != imgEles && !imgEles.isEmpty()) {
				for(String img : imgEles) {
					List<String> classValues = XmlFilter.getAttributeValues(img, "class");
					if (null == classValues || classValues.size() == 0) {
						continue;
					}
					for(String str : classValues) {
						if("centred".equals(str)) {
							List<String> altValues = XmlFilter.getAttributeValues(img, "alt");
							String picName = altValues.get(0);
							if(!picName.toLowerCase().endsWith(".jpg")) {
								picName += ".jpg";
							} else {
								picName = picName.substring(0, picName.length() - 4) + ".jpg";
							}
							List<String> srcValues = XmlFilter.getAttributeValues(img, "src");
							String downUrl = srcValues.get(0);
							download = new DownloadDO(albumName, picName, aUrl, downUrl);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return download;
	}
	
}
