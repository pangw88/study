package com.wp.study.praxis.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileOperationTest.class);
	
	public static void main(String[] args) {
		try {
			// 任务开始
			LOG.info("Task start!");
			/*
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
			String path = "F:/photo";
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
/*			File f1 = new File("C:/Users/wp/Downloads/200GANA-953.7z.001");
			File f2 = new File("C:/Users/wp/Downloads/200GANA-953.7z.002");
			File f3 = new File("C:/Users/wp/Downloads/200GANA-953.7z.003");
			File f4 = new File("C:/Users/wp/Downloads/200GANA-953.7z.004");
			FileOperation.merge(new File("F:/Downloads/abs/200GANA-953.7z"), f1, f2, f3, f4);*/
			// 任务结束
			LOG.info("Task end!");
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
