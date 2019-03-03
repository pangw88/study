package com.wp.study.praxis.image.similar;

public class ImageSililarTest {

	public static void main(String[] args) {
		String imagePath0 = "D:\\QMDownload\\fuuka-n02\\gh_fuuka-n041.jpg";
		String imagePath1 = "D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_fuuka_st1_05\\p_fuuka_st1_05_002.jpg";

		float percent = PhotoDigest.getSimilarity(imagePath0, imagePath1);
		System.out.println(percent);

		int distance = ImagePHash.getSimilarity(imagePath0, imagePath1);
		System.out.println(distance);

		float compare = FingerPrint.getSimilarity(imagePath0, imagePath1);
		System.out.println(compare);
	}

}
