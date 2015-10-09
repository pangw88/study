package com.wp.study.common.util;

import java.io.File;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileTest.class);
	
	public static void rename(File dir) {
		Stack<File> s = new Stack<File>();
		s.push(dir);
		while(!s.isEmpty()) {
			File popF = s.pop();
			File[] files = popF.listFiles();
			if(files == null || files.length == 0)
				continue;
			for(File f : files) {
				if(f.isDirectory()) {
					s.push(f);
				} else {
					String name = f.getName();
					String rename = name.replaceAll("\\(\\)_", "");
					if(!name.equalsIgnoreCase(rename)) {
						f.renameTo(new File(f.getParentFile(), 
								rename));
						System.out.println(name + " ==> " + rename);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			// 任务开始
			LOG.info("Task start!");
			//rename(new File("D:/Course"));
			/*Long time1 = new Date().getTime();
			FileUtil.copyFile(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a1"));
			Long time2 = new Date().getTime();
			FileUtil.copyFile0(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a2"));
			Long time3 = new Date().getTime();
			System.out.println(time2 - time1);
			System.out.println(time3 - time2);*/
			File file = new File("K:/Album/U15/Rei Kuromiya");
			File info = new File("C:/Users/wp/Desktop/Rei Kuromiya.txt");
			FileUtil.checkFileExist(file, info);
			/*String path = "F:\\Downloads\\temp\\st1_hitorijime2_kouzuki_a01";
			FileUtil.rename(new File(path));*/
			/*File f1 = new File("K:\\Album\\Beauty\\Momo Shiina");
			File f2 = new File("E:\\Downloads\\OIMO-675");
			File[] files = {f1, f2};
			FileUtil.getFileMD5(files, "^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov)$", false);*/
			// 任务结束
			LOG.info("Task end!");
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
