package com.wp.study.praxis.image;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.wp.study.praxis.image.similar.FingerPrint;

public class SimilarImageInspectTest {

	public static void main(String[] args) {
		String originDirPath = "D:\\QMDownload\\tt";
		String targetDirPath = "D:\\希捷数据救护\\done\\Fuuka Nishihama--done";
		firstImageInspect(originDirPath, targetDirPath);
	}

	public static void firstImageInspect(String originDirPath, String targetDirPath) {
		File originDir = new File(originDirPath);
		File targetDir = new File(targetDirPath);
		if (!originDir.exists() || !originDir.isDirectory() || !targetDir.exists() || !targetDir.isDirectory()) {
			return;
		}
		File[] subOriginDirs = originDir.listFiles();
		for (File subOriginDir : subOriginDirs) {
			if (subOriginDir.isFile()) {
				continue;
			}
			computeSimilarity(subOriginDir.listFiles()[0], targetDir);
		}

	}

	public static void computeSimilarity(File originFile, File targetDir) {
		float similarity = 0.0f;
		FileWriter fw = null;
		try {
			// 重命名详情输出
			fw = new FileWriter(new File("E:/similarity.txt")) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			File[] subTargetDirs = targetDir.listFiles();
			for (File subTargetDir : subTargetDirs) {
				if (subTargetDir.isFile()) {
					continue;
				}
				File firstTargetFile = subTargetDir.listFiles()[0];
				similarity = FingerPrint.getSimilarity(originFile, firstTargetFile);
				if (similarity > 0.90f) {
					System.out.print("similarity=" + similarity + "     " + originFile + "=" + firstTargetFile);
					fw.write(originFile + "=" + firstTargetFile);
					if (similarity > 0.95f) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
