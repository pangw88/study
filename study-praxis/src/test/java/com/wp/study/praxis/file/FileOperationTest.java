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
			/*rename(new File("D:/Course"));
			Long time1 = new Date().getTime();
			FileOperation.copy(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a1"));
			Long time2 = new Date().getTime();
			FileOperation.copy0(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a2"));
			Long time3 = new Date().getTime();
			System.out.println(time2 - time1);
			System.out.println(time3 - time2);*/
			/*File file = new File("F:/ftp/karen nishino");
			File info = new File("F:/1.txt");
			FileOperation.checkExist(file, info);*/
			String path = "F:/Downloads/aa";
			FileOperation.rename(new File(path));
//			File f1 = new File("H:/Album/Beauty/Koharu Nishino"); 
//			File f2 = new File("F:/Downloads/aa");
//			File[] files = {f1, f2};
//			FileOperation.getMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov|m4v)$", false, files);
			/*File winrar = new File("C:/Program Files/WinRAR/WinRAR.exe");
			File dir = new File("F:/temp");
			FileOperation.compress(winrar, dir, "90890219", 1);*/
			/*File f = new File("K:/Album/Beauty/Mayumi Yamanaka");
			FileOperation.getMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov)$", true, f);*/
			/*File f1 = new File("E:/IMBD-048.mkv.001");
			File f2 = new File("E:/IMBD-048.mkv.002");
			File f3 = new File("E:/IMBD-048.mkv.003");
			File f4 = new File("E:/IMBD-048.mkv.004");
			File f5 = new File("E:/IMBD-048.mkv.005");
			File f6 = new File("E:/IMBD-048.mkv.006");
			File f7 = new File("E:/IMBD-048.mkv.007");
			File f8 = new File("E:/IMBD-048.mkv.008");
			File f9 = new File("E:/IMBD-048.mkv.009");
			FileOperation.merge(new File("E:/1/test.mkv"), f1, f2, f3, f4, f5, f6, f7, f8, f9);*/
			// 任务结束
			LOG.info("Task end!");
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
