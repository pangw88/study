package com.wp.study.praxis.address;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class AddressSimilar1 {
	
	private static HashMap<String, String> addresses = new HashMap<String, String>();
	// 匹配结果集
	private static Map<Integer, HashMap<Integer, Float>> matchResults = new HashMap<Integer, HashMap<Integer, Float>>();
	// 需要匹配的新地址集
	private static Map<Integer, HashMap<String, Integer>> matchAddresses = new HashMap<Integer, HashMap<String, Integer>>();

	/**
	 * 计算新订单地址与历史订单地址中满足指定相似度的地址
	 * 
	 * @param oldFilePath
	 * @param newFilePath
	 * @param threshold
	 */
	public static void CalculateDistance(String oldFilePath, String newFilePath, final float threshold) {
		try {
			// 加载历史订单信息
			loadHistoryAddress(oldFilePath);
			// 加载新订单信息
			loadNewAddress(newFilePath);
			
			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
			
			// 标记每张表格中匹配度大于阀值的行
	        if(matchAddresses.size() > 0) {
	        	for(Integer key : matchAddresses.keySet()) {
	        		final HashMap<String, Integer> matchAddr = matchAddresses.get(key);
	        		final HashMap<Integer, Float> matchRes;
	        		if(matchResults.get(key) == null) {
	        			matchRes = new HashMap<Integer, Float>();
	        			matchResults.put(key, matchRes);
	        		} else {
	        			matchRes = matchResults.get(key);
	        		}
	        		
	        		for(final String newAddr : matchAddr.keySet()) {
	        			futures.add(pool.submit(new Callable<Boolean>(){

							public Boolean call() {
			        			for(String oldAddr : addresses.keySet()) {
				        			Float res = calculateSimilarity(newAddr, oldAddr);
				        			// 去除与自身地址的匹配
				        			if(res >= threshold && res < 1.0f) {
				        				matchRes.put(matchAddr.get(newAddr), res);
				        				return true;
				        			}
				        		}
			        			return true;
							}
	        				
	        			}));
	        		}
		        }
	        }
	        pool.shutdown();
	        for(Future<Boolean> f : futures) {
	        	// 等待线程执行结果
	        	f.get();
	        }
	        // 输出结果集
	        outputSimilarResult(newFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载历史订单中的地址
	 * 
	 * @param oldFilePath
	 * @throws FileNotFoundException
	 */
	private static void loadHistoryAddress(String oldFilePath) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(oldFilePath));
		String line = null;
		// 第一行，表头："tid,receiver_address"，忽略
		line = br.readLine();
		while (null != (line = br.readLine())) {
			String[] arr = line.split(",", 2);
			if (null == arr || arr.length != 2) {
				continue;
			}
			if(addresses.containsKey(arr[1])) {
				String value = addresses.get(arr[1]);
				if(value.compareTo(arr[0]) < 0) {
					continue;
				}
			} 
			addresses.put(arr[1], arr[0]);
		}
		br.close();
	}
	
	/**
	 * 加载新订单地址
	 * 
	 * @param file
	 * @param ignoreRows
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadNewAddress(String newFilePath) throws FileNotFoundException, IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(newFilePath));
		// 打开HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(bis);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		// 遍历excel中所有表格
		for (int i = 0; i < wb.getNumberOfSheets(); i ++) {
			HSSFSheet st = wb.getSheetAt(i);
			// 遍历表格下每一行。第一行为标题，不取
			HashMap<Integer, Float> matchRes = new HashMap<Integer, Float>();
			HashMap<String, Integer> matchAddr = new HashMap<String, Integer>();
			for (int rowIndex = 1; rowIndex <= st.getLastRowNum(); rowIndex ++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null || getColumn(row.getCell(0)) == null) {
					continue;
				}
				// get tid
				String tid = getColumn(row.getCell(1));
				// get address
				String address = getColumn(row.getCell(2));
				if(addresses.containsKey(address) && !addresses.get(address).equals(tid)) {
					matchRes.put(rowIndex, 1.0f);
				} else {
					addresses.put(address, tid);
					matchAddr.put(address, rowIndex);
				}
			}
			matchResults.put(i, matchRes);
			matchAddresses.put(i, matchAddr);
		}
		bis.close();
		wb.close();
	}

	/**
	 * 删除不符合条件的数据，并添加相似度的值
	 * 
	 * @param excel
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void outputSimilarResult(String filePath) throws FileNotFoundException, IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		// 打开HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(bis);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		for(int sheetIndex : matchResults.keySet()) {
			HSSFSheet st = wb.getSheetAt(sheetIndex);
			HashMap<Integer, Float> matchRes = matchResults.get(sheetIndex);
			Set<Integer> rows = matchRes.keySet();
			// 添加相似度
			for(Integer rowIndex : rows) {
				HSSFRow row = st.getRow(rowIndex);
				row.createCell(3).setCellValue(matchRes.get(rowIndex));
			}
			// 移除不符合条件的行
			for(int i = 1; i <= st.getLastRowNum(); i ++) {
				if(!rows.contains(i)) {
					st.createRow(i);
				}
			}
		}
		// 输出不可以定义在前面，会出现异常
		FileOutputStream fos = new FileOutputStream(filePath);
		wb.write(fos);
		bis.close();
		fos.close();
		wb.close();
	}

	/**
	 * 编辑距离算法，计算两个字符串相似度
	 * 
	 * @param addr1
	 * @param addr2
	 * @return
	 */
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

	/**
	 * 获取指定一列的值
	 * 
	 * @param cell
	 * @return
	 */
	private static String getColumn(HSSFCell cell) {
		if(cell == null) {
			return null;
		}
		String value = null;
		// 注意：一定要设成这个，否则可能会出现乱码
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
		CalculateDistance("F:/calculate/old/2015921.csv", "F:/calculate/in/9.21支付宝.xls", 0.7f);
		Date end = new Date();
		System.out.println("task end time is : " + end.getTime());
		System.out.println("task waste time is : " + (end.getTime() - start.getTime()));
	}
}
