package com.wp.study.praxis.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileOperationTest.class);

	public FileOperationTest() {
		super();
	}

	public static void main(String[] args) {
		try {
			// 任务开始
			long startTime = System.currentTimeMillis();
			LOG.info("task begin");
			/*
			Long time1 = new Date().getTime();
			FileOperation.copy(new File("E:/r2_kaneko_m03.mp4"), new File("D:/a1"));
			Long time2 = new Date().getTime();
			System.out.println(time2 - time1);*/
			/*File file = new File("F:/ftp/karen nishino");
			File info = new File("F:/1.txt");
			FileOperation.checkExist(file, info);*/
			String path = "D:\\图集\\Fuuka Nishihama\\p_fuuka_st2_08";
			FileOperation.rename(new File(path));
			/*File path = new File("G:/Album/Beauty/Koharu Nishino");
			FileOperation.checkSubValidAndCut(path);*/
            /*File f1 = new File("H:/Album/Beauty/Koharu Nishino"); 
			File f2 = new File("F:/Downloads/aa");
			File[] files = {f1, f2};
			FileOperation.getMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov|m4v)$", false, files);*/
			/*File winrar = new File("C:/Program Files/WinRAR/WinRAR.exe");
			File dir = new File("F:/temp");
			FileOperation.compress(winrar, dir, "90890219", 1);*/
			/*File f = new File("K:/Album/Beauty/Mayumi Yamanaka");*
			FileOperation.getMD5("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov)$", true, f);*/
            /*File f1 = new File("C:/Users/wp/Downloads/200GANA-953.7z.001");
			File f2 = new File("C:/Users/wp/Downloads/200GANA-953.7z.002");
			File f3 = new File("C:/Users/wp/Downloads/200GANA-953.7z.003");
			File f4 = new File("C:/Users/wp/Downloads/200GANA-953.7z.004");
			FileOperation.merge(new File("F:/Downloads/abs/200GANA-953.7z"), f1, f2, f3, f4);*/
//			File dir = new File("G:\\Album\\Beauty\\Momo Shiina");
//			compress(dir, "rar", "90890219");
//			File baseDir = new File("F:\\photo\\invalid\\SEJIN 세진\\2015.10.21 [세진] 소파 위에 미녀 [44P]");
//			File targetDir = new File("F:\\photo\\invalid\\SEJIN 세진\\2016.05.28 [세진] 소파 위에 미녀 [44P]");
//			FileOperation.replaceName(baseDir, targetDir);
			// 任务结束
			LOG.info("wasteTime=" + (System.currentTimeMillis() - startTime));
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	public static void compress(File dir, String compressModel, String password) {
		if(null == dir || !dir.exists()) {
			return;
		}
		File[] files = dir.listFiles();
		Map<String, List<File>> map = new HashMap<String, List<File>>();
		for(File f : files) {
			String name = f.getName();
			String compressName = "";
			if(!name.startsWith("[")) {
				if(name.lastIndexOf(".") > -1) {
					name = name.substring(0, name.lastIndexOf("."));
				}
				if (name.matches("^[\\s\\S]*\\d{3}$")) {
					compressName = name.substring(0, name.length() - 3);
					if(compressName.endsWith("_")) {
						compressName = compressName.substring(0, compressName.length() - 1);
					} else {
						compressName = name.substring(0, name.length() - 2);
					}
				} else if (name.matches("^[\\s\\S]*\\d{2}$")) {
					if(name.charAt(name.length() - 3) == '_') {
						compressName = name.substring(0, name.length() - 3);
					} else if(name.matches("^[\\s\\S]*[a-z]\\d{2}$")) {
						if(name.charAt(name.length() - 4) == '_') {
							compressName = name.substring(0, name.length() - 4);
						} else {
							compressName = name.substring(0, name.length() - 2);
						}
					}
				} else if (name.matches("^[\\s\\S]*_\\d{1}$")) {
					compressName = name.substring(0, name.length() - 4);
					if(compressName.endsWith("_")) {
						compressName = compressName.substring(0, compressName.length() - 1);
					}
				}
				if(StringUtils.isBlank(compressName)) {
					System.out.println(name);
				} else {
					if(compressName.startsWith("p_")) {
						compressName = compressName.substring(2);
					}
					List<File> list = map.get(compressName);
					if(null == list) {
						list = new ArrayList<File>();
						map.put(compressName, list);
					}
					list.add(f);
				}
			}
		}
		for(Map.Entry<String, List<File>> entry : map.entrySet()) {
			FileOperation.compress(entry.getKey(), compressModel, password, entry.getValue().toArray(new File[0]));
		}
	}
}
