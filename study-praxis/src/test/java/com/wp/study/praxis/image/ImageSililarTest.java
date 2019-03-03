package com.wp.study.praxis.image;

import com.wp.study.praxis.image.similar.FingerPrint;
import com.wp.study.praxis.image.similar.ImagePHash;
import com.wp.study.praxis.image.similar.PhotoDigest;

public class ImageSililarTest {

	public static void main(String[] args) {
		String imagePath0 = "D:\\QMDownload\\tt\\fuuka-n02\\gh_fuuka-n041.jpg";
		String imagePath1 = "D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_fuuka_st1_05\\p_fuuka_st1_05_001.jpg";

		long begin0 = System.currentTimeMillis();
		float percent = PhotoDigest.getSimilarity(imagePath0, imagePath1);
		System.out.println("waste=" + (System.currentTimeMillis() - begin0) + "     " + percent);

		long begin1 = System.currentTimeMillis();
		int distance = ImagePHash.getSimilarity(imagePath0, imagePath1);
		System.out.println("waste=" + (System.currentTimeMillis() - begin1) + "     " + distance);

		long begin2 = System.currentTimeMillis();
		float compare = FingerPrint.getSimilarity(imagePath0, imagePath1);
		System.out.println("waste=" + (System.currentTimeMillis() - begin2) + "     " + compare);
	}

}
