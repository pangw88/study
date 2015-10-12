package com.wp.study.praxis.address;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.wp.study.similar.StringSimilarty;

public class AddressSimilar {
	
	// 历史订单地址和新订单地址合集，主键为tid，值为订单地址
	private static Map<String, String> addresses = new ConcurrentHashMap<String, String>();
	// 每个要匹配新订单文件下每个Sheet表格中抽取的新订单地址信息，Map参数从左到右分别为：新订单文件，订单文件中Sheet表格序号，订单要匹配信息
	private static Map<File, Map<Integer, List<OrderInfo>>> needCalculateAddresses = new HashMap<File, Map<Integer, List<OrderInfo>>>();
	
	
	/**
	 * 计算新订单地址与历史订单地址中满足指定相似度的地址
	 * 
	 * 只是当前文件进行相似度计算
	 * 
	 * @param newFilePath
	 * @param threshold
	 */
	public static void calculateDistance(String newFilePath, final float threshold) {
		calculateDistance(null, newFilePath, threshold);
	}
	
	/**
	 * 计算新订单地址与历史订单地址中满足指定相似度的地址
	 * 
	 * 对比当前文件的同时，对比历史文件
	 * 
	 * @param oldFilePath
	 * @param newFilePath
	 * @param threshold
	 */
	public static void calculateDistance(String oldFilePath, String newFilePath, final float threshold) {
		try {
			// 加载历史订单信息
			if(oldFilePath != null) {
				loadOldAddress(oldFilePath);
			}
			
			// 加载新订单信息
			loadNewAddress(newFilePath);
			
			// 计算每个Sheet表格中地址相似度
			if(needCalculateAddresses.size() > 0) {
				for(File file : needCalculateAddresses.keySet()) {
					// 当前新订单文件下要匹配地址信息
					Map<Integer, List<OrderInfo>> fileNeedCalculateAddresses = needCalculateAddresses.get(file);
					if(fileNeedCalculateAddresses.size() > 0) {
						for(Integer sheetIndex : fileNeedCalculateAddresses.keySet()) {
							// 每个表格下要匹配的订单地址
							List<OrderInfo> sheetNeedCalculateAddresses = fileNeedCalculateAddresses.get(sheetIndex);
							if(sheetNeedCalculateAddresses.size() > 0) {
								calculateDistanceOfSheet(file, sheetIndex, sheetNeedCalculateAddresses, threshold);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算单个Sheet表格中地址相似度，并输出结果
	 * 
	 * @param file
	 * @param sheetIndex
	 * @param orderInfoList
	 * @param threshold
	 */
	private static void calculateDistanceOfSheet(File file, Integer sheetIndex, 
			List<OrderInfo> orderInfoList, final float threshold) {
		try {
			// 执行相似度计算线程池
			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
			// 线程相似度计算结果集
			List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
			final Map<Integer, Float> sheetCalculateResults = new ConcurrentHashMap<Integer, Float>();
			for(OrderInfo oi : orderInfoList) {
				final Integer rowNum = oi.getRowNum();
				final String tid = oi.getTid();
				final String address = oi.getAddress();
				futures.add(pool.submit(new Callable<Boolean>(){
					public Boolean call() {
						float res = 0;
						float temp = 0;
	        			for(String key : addresses.keySet()) {
	        				// 匹配前先剔除当前地址
	        				if(key != null && !key.equals(tid)) {
	        					temp = StringSimilarty.levenshteinDistance(address, addresses.get(key));
		        				if(temp == 1.0f) {
		        					res = 1.0f;
		        					break;
		        				} else if(temp > res) {
		        					res = temp;
		        				}
	        				}
		        		}
	        			// 添加相似度大于阀值的订单信息
	        			if(res >= threshold) {
	        				sheetCalculateResults.put(rowNum, res);
	        			}
	        			return true;
					}
				}));
			}
			pool.shutdown();
	        for(Future<Boolean> future : futures) {
	        	// 等待线程执行结果
	        	future.get();
	        }
	        // 输出结果集
	        outputSimilarResult(file, sheetIndex, sheetCalculateResults);
	        // 文件重命名
	        fileRename(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载历史订单中的地址
	 * 
	 * 若oldFilePath是一个文件，则加载文件中地址信息；
	 * 若oldFilePath是一个文件夹，则加载文件夹下有效文件中地址信息
	 * 
	 * @param oldFilePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void loadOldAddress(String oldFilePath) throws FileNotFoundException, IOException {
		// 获取要加载的历史文件列表
		File oldFile = new File(oldFilePath);
		List<File> fileList = new ArrayList<File>();
		if(!oldFile.exists()) {
			throw new FileNotFoundException(oldFilePath + "not found!");
		} else if(oldFile.isDirectory()) {
			File[] files = oldFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}
			});
			if(files != null && files.length > 0) {
				for(File f : files) {
					if(f.isFile()) {
						fileList.add(f);
					}
				}
			}
			
		} else {
			fileList.add(oldFile);
		}
		
		// 读取每个要加载的历史文件，获取需要加载数据
		BufferedReader br;
		String line;
		if(fileList.size() > 0) {
			for(File f : fileList) {
				br = new BufferedReader(new FileReader(f));
				// 第一行，表头："tid,receiver_address"，忽略
				line = br.readLine();
				while (null != (line = br.readLine())) {
					String[] arr = line.split(",", 2);
					if (null == arr || arr.length != 2) {
						continue;
					}
					addresses.put(arr[0], arr[1]);
				}
				br.close();
			}
		}
	}
	
	/**
	 * 加载新订单地址
	 * 
	 * 若newFilePath是一个文件，则加载文件中地址信息；
	 * 若newFilePath是一个文件夹，则加载文件夹下有效文件中地址信息
	 * 
	 * @param newFilePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadNewAddress(String newFilePath) throws FileNotFoundException, IOException {
		// 获取要加载的历史文件列表
		File newFile = new File(newFilePath);
		List<File> fileList = new ArrayList<File>();
		if(!newFile.exists()) {
			throw new FileNotFoundException(newFilePath + "not found!");
		} else if(newFile.isDirectory()) {
			File[] files = newFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return !(name.indexOf("_匹配结果") > -1);
				}
			});
			if(files != null && files.length > 0) {
				for(File f : files) {
					if(f.isFile()) {
						fileList.add(f);
					}
				}
			}
			
		} else {
			fileList.add(newFile);
		}
		
		// 读取每个要加载的新文件，获取需要加载数据
		BufferedInputStream bis;
		POIFSFileSystem fs;
		HSSFWorkbook wb;
		HSSFSheet st;
		HSSFRow row;
		if(fileList.size() > 0) {
			for(File f : fileList) {
				// 当前新订单文件下要匹配地址信息
				Map<Integer, List<OrderInfo>> fileNeedCalculateAddresses = new HashMap<Integer, List<OrderInfo>>();
				bis = new BufferedInputStream(new FileInputStream(f));
				fs = new POIFSFileSystem(bis);
				// 打开HSSFWorkbook
				wb = new HSSFWorkbook(fs);
				// 遍历excel中所有表格
				for (int i = 0; i < wb.getNumberOfSheets(); i ++) {
					// 每个表格下要匹配的订单地址
					List<OrderInfo> sheetNeedCalculateAddresses = new ArrayList<OrderInfo>();
					st = wb.getSheetAt(i);
					// 遍历表格下每一行。第一行为标题，不取
					for (int rowNum = 1; rowNum <= st.getLastRowNum(); rowNum ++) {
						row = st.getRow(rowNum);
						if (row == null || getColumn(row.getCell(0)) == null) {
							continue;
						}
						// 获取该行序号为3单元格中的tid信息
						String tid = getColumn(row.getCell(3));
						// 获取该行序号为11单元格中的address信息
						String address = getColumn(row.getCell(11));
						// 添加地址到地址匹配合集中
						addresses.put(tid, address);
						// 添加Sheet表格中每行要匹配地址
						sheetNeedCalculateAddresses.add(new OrderInfo(rowNum, tid, address));
					}
					// 添加文件中每个Sheet表格中要匹配地址
					fileNeedCalculateAddresses.put(i, sheetNeedCalculateAddresses);
				}
				// 添加每个文件要匹配地址
				needCalculateAddresses.put(f, fileNeedCalculateAddresses);
				bis.close();
				wb.close();
			}
		}
	}

	/**
	 * 删除不符合条件的数据，并添加相似度的值
	 * 
	 * @param excel
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void outputSimilarResult(File file, Integer sheetIndex, 
			Map<Integer, Float> sheetCalculateResults) throws FileNotFoundException, IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		// 打开HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(bis);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet st = wb.getSheetAt(sheetIndex);
		Set<Integer> rowNums = new HashSet<Integer>();
		rowNums.addAll(sheetCalculateResults.keySet());
		int i = st.getLastRowNum();
		HSSFRow row;
		while(i > 0) {
			row = st.getRow(i);
			if(rowNums.contains(i)) {
				row.createCell(12).setCellValue(sheetCalculateResults.get(i));
				rowNums.remove(i);
			} else if(i == st.getLastRowNum()) {
				if(row != null) {
					st.removeRow(row);
				}
			} else {
				st.shiftRows(i + 1, st.getLastRowNum(), -1);
			}
			i --;
		}
		// 输出不可以定义在前面，会出现异常
		FileOutputStream fos = new FileOutputStream(file);
		wb.write(fos);
		bis.close();
		fos.close();
		wb.close();
	}
	
	private static void fileRename(File file) {
		if(!file.exists() || file.isDirectory()) {
			return;
		}
		File parentDir = file.getParentFile();
		String filename = file.getName();
		int separator = filename.lastIndexOf(".");
		if(separator > -1) {
			filename = filename.substring(0, separator) + "_匹配结果" + filename.substring(separator, filename.length());
		} else {
			filename += "_匹配结果"; 
		}
		file.renameTo(new File(parentDir, filename));
	}

	/**
	 * 获取指定单元格的值
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
					}
				} else {
					value = new DecimalFormat("0").format(cell.getNumericCellValue());
				}
				break;
			case HSSFCell.CELL_TYPE_FORMULA:
				// 导入时如果为公式生成的数据则无值
				if (!"".equals(cell.getStringCellValue())) {
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
				break;
		}
		return value;
	}
	
	private static class OrderInfo {
		private Integer rowNum;
		private String tid;
		private String address;
		
		OrderInfo(Integer rowNum, String tid, String address) {
			this.rowNum = rowNum;
			this.tid = tid;
			this.address = address;
		}
		
		public Integer getRowNum() {
			return rowNum;
		}
		public String getTid() {
			return tid;
		}
		public String getAddress() {
			return address;
		}
	}

	public static void main(String[] args) {
		Date start = new Date();
		System.out.println("task start time is : " + start.getTime());
		calculateDistance("F:/new", 0.7f);
		Date end = new Date();
		System.out.println("task end time is : " + end.getTime());
		System.out.println("task waste time is : " + (end.getTime() - start.getTime()));
	}
}
