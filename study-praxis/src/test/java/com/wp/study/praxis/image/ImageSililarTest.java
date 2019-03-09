package com.wp.study.praxis.image;

import java.io.File;

import com.wp.study.praxis.image.similar.FingerPrint;
import com.wp.study.praxis.image.similar.ImagePHash;
import com.wp.study.praxis.image.similar.PhotoDigest;

public class ImageSililarTest {

	public static void main(String[] args) {
		String image0 = "D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_dvd7_fuuka03\\p_dvd7_fuuka03_013.jpg";
		String image1 = "D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_dvd7_fuuka03\\p_dvd7_fuuka03_014.jpg";

		long begin0 = System.currentTimeMillis();
		float percent = PhotoDigest.getSimilarity(image0, image1);
		System.out.println("waste=" + (System.currentTimeMillis() - begin0) + "     " + percent);

		long begin1 = System.currentTimeMillis();
		int distance = ImagePHash.getSimilarity(image0, image1);
		System.out.println("waste=" + (System.currentTimeMillis() - begin1) + "     " + distance);

		long begin2 = System.currentTimeMillis();
		float compare = FingerPrint.getSimilarity(new File(image0), new File(image1));
		System.out.println("waste=" + (System.currentTimeMillis() - begin2) + "     " + compare);
		
	}

}
