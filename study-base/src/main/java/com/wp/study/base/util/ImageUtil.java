package com.wp.study.base.util;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageUtil {
	
	public static boolean isValidImage(URL imgUrl, int minSize) {
		boolean valid = false;
		if(null == imgUrl) {
			return valid;
		}
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(imgUrl);
			// 图片大小不符合定义
			if(bi.getHeight() < minSize || bi.getWidth() < minSize) {
				return valid;
			}
			int [] data = new int[bi.getWidth()];
			// 基于高去取图片rgb（正常基于宽），且取最后一行像素点
			for(int i = 0; i < bi.getWidth(); i++){
                data[i] = bi.getRGB(i, bi.getHeight() - 1);
            }
			// 校验图片是否灰图
			valid = !isGreyImage(data);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(null != bi) {
				bi.flush();
			}
		}
		return valid;
	}

	public static boolean isGreyImage(int [] imgData) {
		boolean isGrey = false;
		if(null == imgData || imgData.length == 0) {
			return isGrey;
		}
		// 倒序检查是否灰度图
		for(int i = imgData.length - 1; i >= 0; i--){
			 int pixel = imgData[i];
             int r = (pixel & 0xff0000) >> 16;  
             int g = (pixel & 0xff00) >> 8;  
             int b = (pixel & 0xff);
             isGrey = Math.abs(r + g + b - 128 * 3) <= 5 * 3;
             if(!isGrey) {
            	 break;
             }
        }
		return isGrey;
	}
	
	public static String getShortUrl(String imageUrl) {
		String shortUrl = null;
		if(null == imageUrl) {
			return shortUrl;
		}
		try {
			shortUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return shortUrl;
	}
	
}
