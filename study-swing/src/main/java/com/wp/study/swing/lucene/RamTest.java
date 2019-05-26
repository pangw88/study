package com.wp.study.swing.lucene;

import java.awt.image.BufferedImage;

import org.apache.lucene.util.RamUsageEstimator;

public class RamTest {
	
	public static void main(String[] args) {
		BufferedImage br = null;
		try {
			br = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
			long size = RamUsageEstimator.sizeOf(br);
			System.out.println(size);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(null != br) {
				br.flush();
			}
		}
	}

}
