package com.wp.study.praxis.image;

import java.io.File;

import com.wp.study.praxis.image.similar.FingerPrint;
import com.wp.study.praxis.image.similar.ImagePHash;
import com.wp.study.praxis.image.similar.PhotoDigest;

public class ImageSililarTest {

	public static void main(String[] args) {
		String image0 = "D:\\origin_pic\\033s.jpg";
		String image1 = "D:\\希捷数据救护\\done\\Asami Kondou\\p_dvd20_asami-ko02\\p_dvd20_asami-ko02_033.jpg";

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
