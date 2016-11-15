package com.wp.study.praxis.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class FileTest {
	
	private static List<File> picList = new ArrayList<File>();

	public static void procDownload(File dic, final String logReg, final String picReg) {
		if (null == dic || !dic.exists() || !dic.isDirectory()) {
			return;
		}
		File[] logs = dic.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().matches(logReg);
			}
		});
		File[] pics = dic.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().matches(picReg);
			}
		});
		if (null == logs || logs.length == 0 || null == pics || pics.length == 0) {
			return;
		}
		for (File pic : pics) {
			picList.add(pic);
		}
		for (File log : logs) {
			movePic(log);
		}
	}

	public static void movePic(File log) {
		if (null == log || !log.exists() || !log.isFile()) {
			return;
		}
		String picDicName = log.getName().substring(0, log.getName().lastIndexOf(".")).toLowerCase();
		File picDic = new File(log.getParentFile(), picDicName);
		if (!picDic.exists()) {
			picDic.mkdirs();
		}
		StringBuilder sb = new StringBuilder();
		List<File> delList = new ArrayList<File>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(log));
			String line = null;
			int index = 100;
			while (StringUtils.isNotBlank((line = br.readLine()))) {
				try {
					Iterator<File> iterator = picList.iterator();
					boolean exists = false;
					while (iterator.hasNext()) {
						File pic = iterator.next();
						String picName = pic.getName();
						String shortName = line.substring(line.lastIndexOf("/") + 1);
						if (pic.getName().equals(shortName)) {
							exists = true;
							pic.renameTo(new File(picDic, index + picName.substring(picName.lastIndexOf("."))));
							iterator.remove();
							if (pic.exists()) {
								delList.add(pic);
							}
						}
					}
					if(!exists) {
						sb.append(line).append("\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				index++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null != br) {
				try {
					br.close();
				} catch(Exception e) {
				}
			}
		}
		if(sb.length() > 0) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(new File(log.getParentFile(), picDicName + ".none"));
				fw.write(sb.toString());
				fw.flush();
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(null != fw) {
					try {
						fw.close();
					} catch(Exception e) {
					}
				}
			}
		}
		if(sb.length() == 0) {
			FileOperation.rename(picDic);
			for(File del : delList) {
				del.delete();
			}
		}
	}

	public static void main(String[] args) {
		File dic = new File("F:/photo");
		procDownload(dic, "^[\\s\\S]*\\.pic-log$", "^[\\s\\S]*\\.jp[e]*g$");
	}


}
