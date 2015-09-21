package com.wp.study.praxis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LevenshteinDistance {
	
	private static List<String> oldTradeInfo = new ArrayList<String>();
	
	public static void CalculateDistance(String oldFilePath, 
			String inFile, String outFilePath) {
		BufferedReader br = null;
		FileWriter fw = null;
		StringBuilder sb = null;
		
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		try {
			loadOldTrade(oldFilePath);
			
			File input = new File(inFile);
			if(!input.exists()) {
				throw new FileNotFoundException(inFile + " don't exist!");
			}
			
			sb = new StringBuilder(outFilePath);
			sb.append(year).append(month).append(day).append("_out.txt");
			File output = new File(sb.toString());
			if(!output.exists()) {
				output.mkdirs();
			}
			
			fw = new FileWriter(output, true);
			
			br = new BufferedReader(new FileReader(inFile));
			String line = null;
			while(null != (line = br.readLine())) {
				for(String oldAddr : oldTradeInfo) {
					
				}
			}
			
			br.close();
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}  finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if(fw != null) {
				try {
					fw.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private static void loadOldTrade(String oldFilePath) throws FileNotFoundException {
		File oldFile = null;
		Calendar now = Calendar.getInstance();
		int year = 0;
		int month = 0;
		int day = 0;
		BufferedReader br = null;
		StringBuilder sb = null;
		try {
			for(int i = 0; i < 7; i++) {
				now.add(Calendar.DATE, i - 7);
				year = now.get(Calendar.YEAR);
				month = now.get(Calendar.MONTH) + 1;
				day = now.get(Calendar.DAY_OF_MONTH);
				sb = new StringBuilder(oldFilePath);
				sb.append(year).append(month).append(day).append("_old.txt");
				oldFile = new File(sb.toString());
				if(!oldFile.exists()) {
					throw new FileNotFoundException(oldFilePath + " don't exist!");
				}
				
				br = new BufferedReader(new FileReader(oldFile));
				String line = null;
				while(null != (line = br.readLine())) {
					oldTradeInfo.add(line);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private static float calculateSimilarity(String addr1, String addr2) {
		//计算两个字符串的长度。
		int len1 = addr1.length();
		int len2 = addr2.length();
		//建立上面说的数组，比字符长度大一个空间
		int[][] dif = new int[len1 + 1][len2 + 1];
		//赋初值，步骤B。
		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}
		//计算两个字符是否一样，计算左上的值
		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (addr1.charAt(i - 1) == addr2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				//取三个值中最小的
				dif[i][j] = Math.min(Math.min(
					dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1), dif[i - 1][j] + 1);
			}
		}
		return (1 - (float) dif[len1][len2] / Math.max(addr1.length(), addr2.length()));
	}

	public static void main(String[] args) {
		CalculateDistance("D:/calculate/old/", 
				"D:/calculate/in/in.txt", "D:/calculate/out/");
	}

}
