package com.wp.study.praxis.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelOperation {
	
	public static void main(String[] args) {
		//readExcel(new File("F:/test.xls"));
	}
	
	/**
	 * 读excel表格
	 * 
	 * @param excel
	 */
	public static void readExcel(File excel) {
		FileInputStream fis = null;
		Workbook wb = null;
		try {
			fis = new FileInputStream(excel);
			// 打开HSSFWorkbook
			wb = new HSSFWorkbook(fis);
			// 获取子表格sheet数量
			int sheetNum = wb.getNumberOfSheets();
			// 遍历所有sheet
			for (int i = 0; i < sheetNum; i ++) {
				Sheet sh = wb.getSheetAt(i);
				// 获取终止行值
				int rowLast = sh.getLastRowNum();
				// 遍历取sheet的行
				for(int j = 0; j <= rowLast; j ++) {
					// 取表中行
					Row row = sh.getRow(j);
					if (row == null) {
						continue;
					}
					
					// getLastCellNum获取的是该行cell的个数，即lastCellIndex+1
					int cellLast = row.getLastCellNum();
					Cell cell = null;
					// 遍历行中的列
					for(int k = 0; k <= cellLast; k ++) {
						cell = row.getCell(k);
						// 将列值转换为String类型
						String value = convertCellToString(cell);
						// TODO 取出每行后可以根据需求进行操作
						System.out.println(value);
					}
				}
			}
			fis.close();
			wb.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if(wb != null) {
				try {
					wb.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}

	/**
	 * 写excel表格
	 * 需要注意，xls格式的excel文档中一个sheet最多可以有60000行，
	 * 使用Apache poi进行写操作，有每次Short.MAX_VALUE = 32767行的上限限制，
	 * 超过会报IllegalArgumentException异常。
	 * 
	 * @param excel
	 */
	public static void writeExcel(File excel) {
		FileOutputStream fos = null;
		Workbook wb = null;
		Row row = null;
		Cell cell = null;
		try {
			fos = new FileOutputStream(excel);
			// 打开HSSFWorkbook
			wb = new HSSFWorkbook();
			//创建名为sheet_01的表格  
	        Sheet sheet = wb.createSheet("sheet_01");
	        // 定义一个列样式
	        CellStyle cs = wb.createCellStyle();
	        cs.setAlignment(CellStyle.ALIGN_CENTER);
	        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        cs.setBorderBottom(CellStyle.BORDER_DOTTED); 
	        cs.setBorderLeft(CellStyle.BORDER_THIN);
	        cs.setBorderRight(CellStyle.BORDER_THIN);
	        cs.setBorderTop(CellStyle.BORDER_THIN);
	        
	        // 创建标题行（第一行）
	        row = sheet.createRow(0);
	        //设置行高  
	        row.setHeightInPoints(30);
	        cell = row.createCell(0);
	        // 添加列样式
	        cell.setCellStyle(cs); 
	        // 设置单元格的值 
	        cell.setCellValue("column1");
	        cell = row.createCell(1);
	        cell.setCellStyle(cs); 
	        cell.setCellValue("column2");
	        
	        // 创建一行示例数据
	        for(int i = 1; i < 2; i ++) {
	        	row = sheet.createRow(i);
	        	row.createCell(0).setCellValue("value0");
		        row.createCell(1).setCellValue("value1");
	        }
	        wb.write(fos);
			fos.close();
			wb.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if(wb != null) {
				try {
					wb.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 移除excel表格指定行
	 * 
	 * 若删除表格中多行，则要从Sheet表格的最后一行开始遍历，确保删除操作不会打乱表格上方尚未删除行的位置。
	 * 需要注意删除行会导致表格有效行数，所以每次都要以getLastRowNum取得最后一行的行标。
	 * 表格最后一行如果是空行，不做任何操作；如果不是空行，则使用removeRow方法删除。
	 * 对于删除非最后一行，使用shiftRows将该行下方区域上移，覆盖要删除的行。
	 * 
	 * @param excel
	 */
	private static void removeRow(Sheet sh, Row row, boolean retainRow) {
		if(row == null) {
			return;
		}
		if(retainRow) {
			// 移除表格中指定行，但是会留下空行，操作在Workbook调用write方法时才会执行
			sh.removeRow(row);
		} else {
			/* 移除指定行，不留空行，利用shiftRows(int startRow, int endRow, int n)方法。
			 * shiftRows方法提供指定表格区域的移动，startRow表示需移动区域起始行，endRow表示需移动区域终止行，
			 * n表示向上（负数值）或向下（正数值）移动行数。
			 * 需要注意的是，Workbook调用write方法时才实际执行，每一次进行shiftRows操作都会动态改变Sheet中
			 * 的有效行数，以及被移动区域中Row的实际行标Num值，所以进行Sheet遍历移动操作时，一定要时刻注意有效行数，
			 * 以及需要操作Row的当前行标值。*/
			int rowNum = row.getRowNum();
			if(rowNum + 1 <= sh.getLastRowNum()) {
				// 通过移动被删除行下方表格区域，将要删除的行覆盖，实现删除不留空行
				sh.shiftRows(rowNum + 1, sh.getLastRowNum(), -1);
			} else if(rowNum == sh.getLastRowNum()) {
				// 最后一行的移除，无法通过移动区域覆盖，使用removeRow将该行删除为空行
				sh.removeRow(row);
			}
		}
	}
	
	/**
	 * 将excel表格列值转换为字符串
	 * 
	 * @param excel
	 */
	private static String convertCellToString(Cell cell) {
		if(cell == null) {
			return null;
		}
		// 将列的值转换为String类型
		String value = null;
		// 获取该列的类型
		int cellType = cell.getCellType();
		switch (cellType) {
			case HSSFCell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					value = date == null ? ""
							: new SimpleDateFormat("yyyy-MM-dd").format(date);
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
}
