package com.wp.study.swing.jtable;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.wp.study.jdbc.derby.pojo.Entity;

@SuppressWarnings("rawtypes")
public class JTableTest extends JTable {

	private static final long serialVersionUID = 2145138650600128959L;

	private MyTableModel myTableModel;

	public JTableTest(List<Entity> list) {
		this.myTableModel = new MyTableModel(list);
		this.setModel(myTableModel);
		TableColumnModel tcm = this.getColumnModel();
		// id列设置隐藏
		TableColumn id = tcm.getColumn(0);
		id.setWidth(0);
		id.setMaxWidth(0);
		id.setMinWidth(0);
		id.setPreferredWidth(0);
		// 操作按钮列设定
		TableColumn operate = tcm.getColumn(Entity.OPERATE_INDEX);
		operate.setCellRenderer(new MyCellRenderer());
		operate.setCellEditor(new MyCellEditor());
		fitTableColumns(this);
	}

	@Override
	public void setModel(TableModel dataModel) {
		if(dataModel instanceof MyTableModel) {
			this.myTableModel = (MyTableModel)dataModel;
		}
		super.setModel(dataModel);
	}

	/**
	 * 设置数据表单元格的可编辑性
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param editable
	 */
	public void setCellEditable(int rowIndex, int columnIndex, boolean editable) {
		if (rowIndex == -1) {
			myTableModel.setColumnEditable(columnIndex, editable);
		} else if (columnIndex == -1) {
			myTableModel.setRowEditable(rowIndex, editable);
		} else {
			myTableModel.setEditable(rowIndex, columnIndex, editable);
		}
	}

	/**
	 * 调整数据表单元格宽度
	 * 
	 * @param myTable
	 */
	public static void fitTableColumns(JTable myTable) {
		myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader header = myTable.getTableHeader();
		int rowCount = myTable.getRowCount();
		TableColumnModel tcm = myTable.getColumnModel();
		Enumeration columns = tcm.getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			if(tcm.getColumn(0) == column) { // id列设置隐藏
				column.setWidth(0);
				column.setMaxWidth(0);
				column.setMinWidth(0);
				column.setPreferredWidth(0);
			} else {
				int col = header.getColumnModel().getColumnIndex(
						column.getIdentifier());
				int width = (int) header
						.getDefaultRenderer()
						.getTableCellRendererComponent(myTable,
								column.getIdentifier(), false, false, -1, col)
						.getPreferredSize().getWidth();
				for (int row = 0; row < rowCount; row++) {
					int preferedWidth = (int) myTable
							.getCellRenderer(row, col)
							.getTableCellRendererComponent(myTable,
									myTable.getValueAt(row, col), false, false,
									row, col).getPreferredSize().getWidth();
					width = Math.max(width, preferedWidth);
				}
				header.setResizingColumn(column); // 此行很重要
				// +10用于预留一定空间，若不加为紧凑型
				width = width + myTable.getIntercellSpacing().width + 10;
				column.setWidth(width < 100 ? 100 : (width > 200 ? 200 : width));
			}
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		List<Entity> list = new ArrayList<Entity>();
		Entity e = new Entity();
		e.setId(1);
		e.setSite("site");
		list.add(e);
		Entity e2 = new Entity();
		e2.setId(2);
		e2.setSite("site2");
		list.add(e2);
		JTableTest table = new JTableTest(list);
		// table.setFillsViewportHeight(true);
		f.setTitle("testTabel");
		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(1, 0));
		JScrollPane scrollPane = new JScrollPane(table);
		jp.add(scrollPane);
		f.add(jp);
		f.setBounds(300, 10, 1000, 688);
		f.setVisible(true);
	}
}
