package com.wp.study.swing.jtable;

import java.util.List;

import com.wp.study.jdbc.derby.pojo.Entity;

public class MyTableModel extends MyAbstractTableModel<Entity> {

	private static final long serialVersionUID = -5945588390631866958L;

	public MyTableModel(List<Entity> datas) {
		super(datas);
	}
}
