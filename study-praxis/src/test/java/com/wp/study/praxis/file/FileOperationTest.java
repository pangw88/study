package com.wp.study.praxis.file;

import java.io.File;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileOperationTest.class);
	
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
			FileOperation.copyFile(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a1"));
			Long time2 = new Date().getTime();
			FileOperation.copyFile0(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a2"));
			Long time3 = new Date().getTime();
			System.out.println(time2 - time1);
			System.out.println(time3 - time2);*/
			/*File file = new File("F:/ftp/karen nishino");
			File info = new File("F:/1.txt");
			FileOperation.checkFileExist(file, info);*/
			String path = "F:/u15";
			FileOperation.rename(new File(path));
			/*File f1 = new File("F:\\Downloads\\Mayumi Yamanaka");
			File f2 = new File("F:\\Downloads\\pics");
			File[] files = {f1, f2};
			FileOperation.getFileMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov)$", true, files);*/
			/*File f = new File("K:/Album/Beauty/Mayumi Yamanaka");
			FileOperation.getFileMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov)$", true, f);*/
			// 任务结束
			LOG.info("Task end!");
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
