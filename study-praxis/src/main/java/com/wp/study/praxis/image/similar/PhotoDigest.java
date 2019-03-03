package com.wp.study.praxis.image.similar;

import javax.imageio.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

public class PhotoDigest {

	public static void main(String[] args) throws Exception {
		float percent = compare(getData("D:\\QMDownload\\fuuka-n02\\gh_fuuka-n041.jpg"),
				getData("D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_fuuka_st1_05\\p_fuuka_st1_05_001.jpg"));
		if (percent == 0) {
			System.out.println("无法比较");
		} else {
			System.out.println("两张图片的相似度为：" + percent + "%");
		}
	}

	public static float getSimilarity(String imagePath0, String imagePath1) {
		return compare(getData(imagePath0), getData(imagePath1));
	}

	public static int[] getData(String name) {
		try {
			BufferedImage img = ImageIO.read(new File(name));
			BufferedImage slt = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			slt.getGraphics().drawImage(img, 0, 0, 100, 100, null);
			// ImageIO.write(slt,"jpeg",new File("slt.jpg"));
			int[] data = new int[256];
			for (int x = 0; x < slt.getWidth(); x++) {
				for (int y = 0; y < slt.getHeight(); y++) {
					int rgb = slt.getRGB(x, y);
					Color myColor = new Color(rgb);
					int r = myColor.getRed();
					int g = myColor.getGreen();
					int b = myColor.getBlue();
					data[(r + g + b) / 3]++;
				}
			}
			// data 就是所谓图形学当中的直方图的概念
			return data;
		} catch (Exception exception) {
			System.out.println("有文件没有找到,请检查文件是否存在或路径是否正确");
			return null;
		}
	}

	public static float compare(int[] s, int[] t) {
		try {
			float result = 0F;
			for (int i = 0; i < 256; i++) {
				int abs = Math.abs(s[i] - t[i]);
				int max = Math.max(s[i], t[i]);
				result += (1 - ((float) abs / (max == 0 ? 1 : max)));
			}
			return (result / 256) * 100;
		} catch (Exception exception) {
			return 0;
		}
	}
}
