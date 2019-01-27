package com.wp.study.praxis;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.wp.study.base.util.IoUtils;

public class LevenshteinDistance {

	private static Map<String, String> tradeInfoMap = new HashMap<String, String>();
	private static List<String> newTradeIds = new ArrayList<String>();
	private static Set<String> similar = new HashSet<String>();

	public static void CalculateDistance(String oldFilePath, String inFile, String outFilePath, float threshold) {
		StringBuilder sb = null;
		FileOutputStream fos = null;

		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		try {
			loadOldTrade(oldFilePath);

			File input = new File(inFile);
			if (!input.exists()) {
				throw new FileNotFoundException(inFile + " don't exist!");
			}
			loadNewTrade(input, 1);

			sb = new StringBuilder(outFilePath);
			sb.append(year).append(month).append(day).append("-").append(input.getName());
			File output = new File(sb.toString());
			if (!output.exists()) {
				output.getParentFile().mkdirs();
				output.createNewFile();
			}
			
			//创建一个Excel(or new XSSFWorkbook())  
	        Workbook wb = new HSSFWorkbook(); 
	        //创建表格  
	        Sheet sheet = wb.createSheet(input.getName()); 
	        //创建行  
	        Row row = sheet.createRow(0); 
	        //设置行高  
	        row.setHeightInPoints(30); 
	        //创建样式  
	        CellStyle cs = wb.createCellStyle(); 
	        cs.setAlignment(CellStyle.ALIGN_CENTER); 
	        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER); 
	        cs.setBorderBottom(CellStyle.BORDER_DOTTED);  
	        cs.setBorderLeft(CellStyle.BORDER_THIN);  
	        cs.setBorderRight(CellStyle.BORDER_THIN); 
	        cs.setBorderTop(CellStyle.BORDER_THIN); 
	        //创建单元格  
	        Cell cell = null;
	        cell = row.createCell(0); 
	        //设置单元格样式  
	        cell.setCellStyle(cs); 
	        //设置单元格的值  
	        cell.setCellValue("交易号"); 
	        cell = row.createCell(1); 
	        cell.setCellStyle(cs); 
	        cell.setCellValue("收货地址");
	        cell = row.createCell(2); 
	        cell.setCellStyle(cs); 
	        cell.setCellValue("相似交易号");
	        cell = row.createCell(3); 
	        cell.setCellStyle(cs); 
	        cell.setCellValue("相似收货地址");
	        cell = row.createCell(4); 
	        cell.setCellStyle(cs); 
	        cell.setCellValue("相似度");
			
			fos = new FileOutputStream(output);
			
			int i = 1;
			for (String newTid : newTradeIds) {
				String newValue = tradeInfoMap.get(newTid);
				tradeInfoMap.remove(newTid);
				for (String tid : tradeInfoMap.keySet()) {
					String address = tradeInfoMap.get(tid);
					Float res = calculateSimilarity(newValue, address);
					if (res >= threshold) {
						similar.add(newTid);
						row = sheet.createRow(i++);
						row.createCell(0).setCellValue(newTid);
						row.createCell(1).setCellValue(newValue);
						row.createCell(2).setCellValue(tid);
						row.createCell(3).setCellValue(address);
						row.createCell(4).setCellValue(res.toString());
						break;
					}
				}
				tradeInfoMap.put(newTid, newValue);
			}
			wb.write(fos);
			
			removeRow(input);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(fos);
		}
	}
	
	private static void removeRow(File excel) throws FileNotFoundException, IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(excel));
		// 打开HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);

		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			HSSFSheet st = wb.getSheetAt(sheetIndex);
			// 第一行为标题，不取
			int ignoreRows = 1;
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null || getColumn(row.getCell(0)) == null) {
					continue;
				}
				// get tid
				String tid = getColumn(row.getCell(3));
				if(!similar.contains(tid)) {
					st.removeRow(row);
				}
			}
		}
		FileOutputStream os = new FileOutputStream(excel);
		wb.write(os);
		IoUtils.closeQuietly(in, os);
	}

	private static void loadOldTrade(String oldFilePath) throws FileNotFoundException {
		File oldFile = null;
		/*Calendar now = Calendar.getInstance();
		int year = 0;
		int month = 0;
		int day = 0;*/
		BufferedReader br = null;
		//StringBuilder sb = null;
		try {
			oldFile = new File(oldFilePath);
			if (!oldFile.exists()) {
				System.out.println(oldFile + " don't exist!");
			}
			br = new BufferedReader(new FileReader(oldFile));
			String line = null;
			// 第一行，表头："tid,receiver_address"，忽略
			line = br.readLine();
			while (null != (line = br.readLine())) {
				String[] arr = line.split(",", 2);
				if (null == arr || arr.length != 2) {
					continue;
				}
				tradeInfoMap.put(arr[0], arr[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(br);
		}
	}

	private static float calculateSimilarity(String addr1, String addr2) {
		// 计算两个字符串的长度。
		int len1 = addr1.length();
		int len2 = addr2.length();
		// 建立上面说的数组，比字符长度大一个空间
		int[][] dif = new int[len1 + 1][len2 + 1];
		// 赋初值，步骤B。
		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}
		// 计算两个字符是否一样，计算左上的值
		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (addr1.charAt(i - 1) == addr2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 取三个值中最小的
				dif[i][j] = Math.min(Math.min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1), dif[i - 1][j] + 1);
			}
		}
		return (1 - (float) dif[len1][len2] / Math.max(addr1.length(), addr2.length()));
	}

	public static void loadNewTrade(File file, int ignoreRows) throws FileNotFoundException, IOException {

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		// 打开HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);

		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			HSSFSheet st = wb.getSheetAt(sheetIndex);
			// 第一行为标题，不取
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null || getColumn(row.getCell(0)) == null) {
					continue;
				}
				// get tid
				String tid = getColumn(row.getCell(1));
				// get address
				String address = getColumn(row.getCell(2));
				tradeInfoMap.put(tid, address);
				newTradeIds.add(tid);
			}
		}
		IoUtils.closeQuietly(in);
	}

	private static String getColumn(HSSFCell cell) {
		if(cell == null) {
			return null;
		}
		String value = null;

		// 注意：一定要设成这个，否则可能会出现乱码

		// cell.setEncoding(HSSFCell.ENCODING_UTF_16);

		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				if (date != null) {
					value = new SimpleDateFormat("yyyy-MM-dd").format(date);
				} else {
					value = "";
				}
			} else {
				value = new DecimalFormat("0").format(cell.getNumericCellValue());
			}
			break;
		case HSSFCell.CELL_TYPE_FORMULA:
			// 导入时如果为公式生成的数据则无值
			if (!cell.getStringCellValue().equals("")) {
				value = cell.getStringCellValue();
			} else {
				value = cell.getNumericCellValue() + "";
			}
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			break;
		case HSSFCell.CELL_TYPE_ERROR:
			value = "";
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			value = (cell.getBooleanCellValue() == true ? "Y" : "N");
			break;
		default:
			value = "";
		}
		return value;
	}

	public static void main(String[] args) {
		Date start = new Date();
		System.out.println("task start time is : " + start.getTime());
		CalculateDistance("f:/calculate/old/2015921.csv", "f:/calculate/in/9.21支付宝.xls", "f:/calculate/out/", 0.7f);
		Date end = new Date();
		System.out.println("task end time is : " + end.getTime());
		System.out.println("task waste time is : " + (end.getTime() - start.getTime()));
	}

}
