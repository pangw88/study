package com.wp.study.swing.jtable;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.pojo.Entity;
import com.wp.study.base.util.CacheUtil;
import com.wp.study.swing.service.EntityService;
import com.wp.study.swing.util.CommonUtil;

public class MyCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	private static final Logger LOG = LoggerFactory.getLogger(MyCellEditor.class);
	
	private static final long serialVersionUID = -3099575006353015825L;

	int row;
	int editRow;
	JTableTest jt;
	JPanel jp;
	JButton edit;
	JButton submit;
	JButton delete;
	EntityService es;
	Container cp;
	
	public MyCellEditor() {
		jp = new JPanel();
		jp.setLayout(new GridLayout(1, 2));
		edit = new JButton("编辑");
		submit = new JButton("编辑");
		delete = new JButton("删除");
		edit.addActionListener(this);
		submit.addActionListener(this);
		delete.addActionListener(this);
		jp.add(edit);
		jp.add(delete);
		es = new EntityService();
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			int choose = 1;
			int result = 0;
			if(e.getSource() == edit) {
				this.editRow = this.row;
				jp.removeAll();
				jp.add(submit);
				jp.add(delete);
				jp.repaint();
				jt.setCellEditable(row, -1, true);
				jt.repaint();
			} else if(e.getSource() == delete) {
				Integer id = (Integer)jt.getValueAt(row, 0);
				choose = JOptionPane.showConfirmDialog(cp, "确认删除？", "删除", 
						JOptionPane.YES_NO_OPTION);
				if(choose == 0) {
					result = es.deleteEntity(id);
					if(result == 1) {
						JOptionPane.showMessageDialog(cp, "删除成功!");
					} else {
						JOptionPane.showMessageDialog(cp, "删除失败!");
					}
				}
			} else if(e.getSource() == submit) {
				if(row != editRow) {
					jp.removeAll();
					jp.add(edit);
					jp.add(delete);
					jp.repaint();
					jt.setCellEditable(editRow, -1, false);
					jt.repaint();
					return;
				}
				Entity entity = new Entity();
				for(int i=0; i<Entity.getDataColumns().size(); i++) {
					Object value = jt.getValueAt(row, i);
					if(value != null) {
						CommonUtil.setField(entity, Entity.getDataColumns().get(i), 
							value);
					}
				}
				choose = JOptionPane.showConfirmDialog(cp, "提交修改？", "编辑", 
						JOptionPane.YES_NO_OPTION);
				if(choose == 0) {
					result = es.editEntity(entity, CacheUtil.getCache("key", String.class), 
							CacheUtil.getCache("key1", String.class));
					if(result == 1) {
						JOptionPane.showMessageDialog(cp, "编辑成功!");
					} else {
						JOptionPane.showMessageDialog(cp, "编辑失败!");
					}
				}
				jp.removeAll();
				jp.add(edit);
				jp.add(delete);
				jp.repaint();
				jt.setCellEditable(row, -1, false);
				jt.repaint();
			}
		} catch(Exception ex) {
			LOG.error(ex.getMessage());
		}
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.jt = (JTableTest)table;
		Container c = null;
		c = jt.getParent();
		while(!(c instanceof JRootPane)) {
			c = c.getParent();
		}
		this.cp = ((JRootPane)c).getContentPane();
		this.row = row;
		return jp;
	}

}
