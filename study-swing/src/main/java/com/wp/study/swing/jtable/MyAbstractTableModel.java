package com.wp.study.swing.jtable;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class MyAbstractTableModel<T> extends AbstractTableModel {

	private static final Logger LOG = LoggerFactory.getLogger(MyAbstractTableModel.class);
	
	private static final long serialVersionUID = -2012677929636798458L;
	
	protected boolean[][] editable;
	
	protected int rowCount;
	
	protected int columnCount;
	
	protected Vector dataVector;
	
	protected int dataColumnCount;
	
	protected List<String> allColumns;
	
	public MyAbstractTableModel(List<T> datas) {
		try {
			// 获取展示数据实例
			Class<T> clazz = (Class<T>)((ParameterizedType)getClass()   
	                .getGenericSuperclass()).getActualTypeArguments()[0];
			T t = clazz.newInstance();
			List<String> dataColumns = (List<String>)t.getClass().getMethod("getDataColumns").invoke(t);
			this.dataColumnCount = dataColumns.size();
			// 获取展示数据
			this.dataVector = new Vector();
			if(datas != null && datas.size() > 0) {
				for(T data : datas) {
					Vector v = new Vector();
					for(int i=0; i<dataColumns.size(); i++) {
						String methodName = "get" + Character.toUpperCase(dataColumns.get(i).charAt(0))
								+ dataColumns.get(i).substring(1);
						v.add(data.getClass().getMethod(methodName).invoke(data));
					}
					this.dataVector.add(v);
				}
			}
			this.rowCount = dataVector.size();
			// 获取表格展示列名
			this.allColumns = (List<String>)t.getClass().getMethod("getAllColumns").invoke(t);
			this.columnCount = allColumns.size();
			// 设置表格可编辑
			this.editable = new boolean[this.rowCount][this.columnCount];
			for(int i=0; i<this.rowCount; i++) {
				for(int j=0; j<this.columnCount; j++) {
					if(j < this.dataColumnCount) {
						this.editable[i][j] = false;
					} else { // 保证JButton列有效
						this.editable[i][j] = true;
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	@Override
	public int getRowCount() {
		return this.rowCount;
	}

	@Override
	public int getColumnCount() {
		return this.columnCount;
	}

	@Override
	public String getColumnName(int column) {
		return allColumns.get(column);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object value = null;
		try {
			if(columnIndex < dataColumnCount) {
				Vector data = (Vector)dataVector.get(rowIndex);
				if(data != null) {
					value = data.get(columnIndex);
				}
			}
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
		return value;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if(column < dataColumnCount) {
			Vector rowVector = (Vector)dataVector.elementAt(row);
	        rowVector.setElementAt(aValue, column);
	        fireTableCellUpdated(row, column);
		}
    }
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return this.editable[rowIndex][columnIndex];
	}
	
	/**
	 * 设置数据表单元格的可编辑性
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param editable
	 */
	public void setEditable(int rowIndex, int columnIndex, boolean editable) {
		try {
			// 序号列和JButton列不可设置可编辑性
			if(columnIndex > 0 && columnIndex < this.dataColumnCount) {
				this.editable[rowIndex][columnIndex] = editable;
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			LOG.error("row={},column={},error:", 
					new Object[]{rowIndex, columnIndex, e.getMessage()});
		}
	}
	
	/**
	 * 设置数据表行的可编辑性
	 * 
	 * @param rowIndex
	 * @param editable
	 */
	public void setRowEditable(int rowIndex, boolean editable) {
		for(int i=0; i<this.columnCount; i++) {
			setEditable(rowIndex, i, editable);
		}
	}
	
	/**
	 * 设置数据表列的可编辑性
	 * 
	 * @param columnIndex
	 * @param editable
	 */
	public void setColumnEditable(int columnIndex, boolean editable) {
		for(int i=0; i<this.rowCount; i++) {
			setEditable(i, columnIndex, editable);
		}
	}
}