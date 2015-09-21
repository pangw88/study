package com.wp.study.swing.jtable;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MyCellRenderer extends JComponent implements TableCellRenderer {

	private static final long serialVersionUID = 4305758775683846183L;
	
	JPanel jp;
	JButton edit;
	JButton delete;

	public MyCellRenderer() {
		jp = new JPanel();
		jp.setLayout(new GridLayout(1, 2));
		edit = new JButton("编辑");
		delete = new JButton("删除");
		jp.add(edit);
		jp.add(delete);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return jp;
	}

}
