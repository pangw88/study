package com.wp.study.praxis.file;

import java.io.File;

public class ReadLibFile {

	public static void main(String[] args) {
		File dir = new File("D:/opencv/opencv3.2-cuda/install/x64/vc14/lib");
		File[] fileList = dir.listFiles();
		for(File f : fileList) {
			if(!f.isFile()) {
				continue;
			}
			String fileName = f.getName();
			if(fileName.endsWith(".lib")) {
				System.out.println(fileName);
			}
		}
	}
}
