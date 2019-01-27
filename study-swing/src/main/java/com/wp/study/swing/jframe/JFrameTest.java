package com.wp.study.swing.jframe;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.digester.DigesterCoder;
import com.wp.study.algorithm.encryption.ClassicalCoder;
import com.wp.study.base.pojo.Entity;
import com.wp.study.base.util.CacheUtils;
import com.wp.study.swing.gbcs.GBCsUtil;
import com.wp.study.swing.jdialog.JDialogTest;
import com.wp.study.swing.jtable.JTableTest;
import com.wp.study.swing.jtable.MyCellEditor;
import com.wp.study.swing.jtable.MyCellRenderer;
import com.wp.study.swing.jtable.MyTableModel;
import com.wp.study.swing.service.DBConfigService;
import com.wp.study.swing.service.EntityService;
import com.wp.study.swing.util.CommonUtil;

public class JFrameTest extends JFrame {

	private static final Logger LOG = LoggerFactory.getLogger(JFrameTest.class);

	private static final long serialVersionUID = -2513141144769610707L;

	public JFrameTest() {
		// init
		cp = getContentPane();
		entity = new Entity();
		dataColumns = Entity.getDataColumns();
		key = DBConfigService.getDBConfig("access", String.class);
		temp1 = DBConfigService.getDBConfig("access1", String.class);
		temp2 = DBConfigService.getDBConfig("access2", String.class);
		es = new EntityService();
		layout = new GridBagLayout();
		gbcs = new GridBagConstraints();
		mouseListener = false;
		setTitle("JFrameTest");
		setLayout(layout);

		// get screen dimensions
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		int height = 800;
		int width = 650;
		setBounds((screenWidth - width) / 2, (screenHeight - height) / 5, width, height);
		setResizable(false);

		// create menus
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu = new JMenu("menu");
		menuBar.add(menu);
		// add menu
		JMenuItem addMenu = new JMenuItem("add");
		addMenu.addActionListener(new AddListener());
		menu.add(addMenu);
		menu.addSeparator();
		// edit menu
		JMenuItem editMenu = new JMenuItem("edit");
		editMenu.addActionListener(new EditListener());
		menu.add(editMenu);
		menu.addSeparator();
		// query menu
		JMenuItem queryMenu = new JMenuItem("query");
		queryMenu.addActionListener(new QueryListener());
		menu.add(queryMenu);
		menu.addSeparator();
		// delete menu
		JMenuItem deleteMenu = new JMenuItem("delete");
		deleteMenu.addActionListener(new DeleteListener());
		menu.add(deleteMenu);
	}

	private void setQueryOptions(boolean complex) {
		if (complex) {
			JPanel complexJP = new JPanel();
			levl = new JLabel("level:");
			levl.setForeground(Color.red);
			levl.setFont(new Font("Dialog", 1, 16));
			complexJP.add(levl);
			levt = new JTextField(17);
			complexJP.add(levt);
			keyl = new JLabel("key1:");
			keyl.setForeground(Color.red);
			keyl.setFont(new Font("Dialog", 1, 16));
			complexJP.add(keyl);
			keyt = new JPasswordField(17);
			complexJP.add(keyt);
			cp.add(complexJP);
			layout.setConstraints(complexJP, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
		}
		JPanel query = new JPanel();
		qls = new JLabel("qrys:");
		qls.setForeground(Color.red);
		qls.setFont(new Font("Dialog", 1, 16));
		query.add(qls);
		qts = new JTextField(17);
		query.add(qts);
		qln = new JLabel("qryn:");
		qln.setForeground(Color.red);
		qln.setFont(new Font("Dialog", 1, 16));
		query.add(qln);
		qtn = new JTextField(17);
		query.add(qtn);
		cp.add(query);
		layout.setConstraints(query, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
	}

	private void setResultOptions() {
		for (int i = 0; i < dataColumns.size(); i++) {
			// label
			JLabel jl = new JLabel(dataColumns.get(i) + ": ", JLabel.TRAILING);
			jl.setFont(new Font("Dialog", 1, 16));
			cp.add(jl);
			layout.setConstraints(jl, GBCsUtil.gbcsSet(gbcs, 1, 0, 0));
			// textField
			JTextField jt = new JTextField();
			if (i == 0) {
				jt.setEditable(false);
			}
			cp.add(jt);
			layout.setConstraints(jt, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			// space panel
			JPanel sp = new JPanel();
			cp.add(sp);
			layout.setConstraints(sp, GBCsUtil.gbcsSet(gbcs, 0, 0, 1));
		}
	}

	private static JFrame frame;
	private static Container cp;
	private Entity entity;
	private List<Entity> list;
	private List<String> dataColumns;
	private JTableTest table;
	private JScrollPane scrollPane;
	private static String key;
	private static String key1;
	private static String key2;
	private static String temp1;
	private static String temp2;
	private static EntityService es;
	private GridBagLayout layout;
	private GridBagConstraints gbcs;
	private JLabel qls;
	private JTextField qts;
	private JLabel qln;
	private JTextField qtn;
	private JLabel levl;
	private JTextField levt;
	private JLabel keyl;
	private JPasswordField keyt;
	private boolean mouseListener;

	class AddListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			cp.removeAll();
			setResultOptions();
			// add button
			final JButton jb = new JButton("Add");
			jb.setFont(new Font("Dialog", 1, 20));
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					entity = new Entity();
					try {
						// add entity
						for (int i = 0; i < dataColumns.size(); i++) {
							String value = ((JTextField) cp.getComponent(i * 3 + 1)).getText();
							if (StringUtils.isNotEmpty(value)) {
								CommonUtil.setField(entity, dataColumns.get(i), value);
							}
						}
						if (StringUtils.isEmpty(entity.getSite())) {
							return;
						}
						int result = es.addEntity(entity, key, key1);
						if (result == 1) {
							JOptionPane.showMessageDialog(cp, "Add success!");
						} else if (result == 0) {
							JOptionPane.showMessageDialog(cp, "Add failed!");
						} else if (result == -1) {
							JOptionPane.showMessageDialog(cp, "Add existed!");
						}
					} catch (Exception ex) {
						LOG.error(ex.getMessage());
					}
				}
			});
			cp.add(jb);
			layout.setConstraints(jb, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			cp.validate();
			cp.repaint();
		}
	}

	class EditListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			cp.removeAll();
			setQueryOptions(false);
			setResultOptions();
			// edit button
			JPanel edit = new JPanel();
			final JButton queryB = new JButton("Query");
			queryB.setFont(new Font("Dialog", 1, 20));
			queryB.setPreferredSize(new Dimension(238, 30));
			queryB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String qs = qts.getText();
						String qn = qtn.getText();
						if (StringUtils.isEmpty(qs) || StringUtils.isEmpty(qn)) {
							JOptionPane.showMessageDialog(cp, "Parameters error!");
							return;
						}
						list = es.queryEntity(null, qs, qn, key, key1);
						entity = list == null || list.size() == 0 ? null : list.get(0);
						if (entity != null) {
							for (int i = 0; i < dataColumns.size(); i++) {
								JTextField value = (JTextField) cp.getComponent(1 + i * 3 + 1);
								if (i < 2) {
									value.setEditable(false);
								}
								value.setText((String) CommonUtil.getField(entity, dataColumns.get(i)));
							}
						}
					} catch (Exception ex) {
						LOG.error(ex.getMessage());
					}
				}
			});
			edit.add(queryB);
			final JButton editB = new JButton("Edit");
			editB.setFont(new Font("Dialog", 1, 20));
			// 因为JButen是属于小器件类型的，所以setSize不能对其惊醒大小的设置，一般用以下方法设置大小
			editB.setPreferredSize(new Dimension(238, 30));
			editB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

				}
			});
			edit.add(editB);
			cp.add(edit);
			layout.setConstraints(edit, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			cp.validate();
			cp.repaint();
		}
	}

	class QueryListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			list = new ArrayList<Entity>();
			cp.removeAll();
			mouseListener = false;
			setQueryOptions(true);
			// query button
			final JButton jb = new JButton("Query");
			jb.setFont(new Font("Dialog", 1, 20));
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String qlev = levt.getText();
						String qkey = keyt.getPassword() == null ? null : new String(keyt.getPassword());
						String qs = qts.getText();
						String qn = qtn.getText();
						if (key1.equals(qkey)) {
							// show query datas, can flush
							table.setModel(new MyTableModel(es.queryEntity(qlev, qs, qn, key, key1)));
							TableColumnModel tcm = table.getColumnModel();
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
							JTableTest.fitTableColumns(table);
							if (!mouseListener) {
								table.addMouseListener(new TableMouseAdapter());
								mouseListener = true;
							}
						}
					} catch (Exception ex) {
						LOG.error(ex.getMessage());
					}
				}
			});
			cp.add(jb);
			layout.setConstraints(jb, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			// table list
			table = new JTableTest(list);
			scrollPane = new JScrollPane(table);
			cp.add(scrollPane);
			layout.setConstraints(scrollPane, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			cp.validate();
			cp.repaint();
		}
	}

	class DeleteListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			cp.removeAll();
			setQueryOptions(false);
			setResultOptions();
			// query button
			JPanel edit = new JPanel();
			final JButton queryB = new JButton("Query");
			queryB.setFont(new Font("Dialog", 1, 20));
			queryB.setPreferredSize(new Dimension(238, 30));
			queryB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});
			edit.add(queryB);
			final JButton deleteB = new JButton("Delete");
			deleteB.setFont(new Font("Dialog", 1, 20));
			// 因为JButen是属于小器件类型的，所以setSize不能对其惊醒大小的设置，一般用以下方法设置大小
			deleteB.setPreferredSize(new Dimension(238, 30));
			deleteB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});
			edit.add(deleteB);
			cp.add(edit);
			layout.setConstraints(edit, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
			cp.validate();
			cp.repaint();
		}
	}

	/**
	 * 鼠标双击时间响应
	 * 
	 * @author wp
	 *
	 */
	class TableMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) { // 实现双击
				// 获得行位置
				int row = ((JTable) e.getSource()).rowAtPoint(e.getPoint());
				// 获得列位置
				int col = ((JTable) e.getSource()).columnAtPoint(e.getPoint());
				// 获得点击单元格数据，复制到剪切板
				String value = String.valueOf(table.getValueAt(row, col));
				StringSelection stringSelection = new StringSelection(value);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
				// JOptionPane.showMessageDialog(cp, "复制成功！");
			}
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrameTest();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				if (key == null) {
					JOptionPane.showMessageDialog(cp, "获取key失败！");
					System.exit(0);
				}
				JDialogTest jd = new JDialogTest(frame, "check");
				while (StringUtils.isEmpty(key1) || StringUtils.isEmpty(key2)) {
					try {
						jd.setVisible(true);
						List<String> inputs = jd.getInputs();
						if (inputs != null) {
							String t1 = ClassicalCoder.substitutionDecrypt(temp1, inputs.get(0));
							String t2 = ClassicalCoder.substitutionDecrypt(temp2, inputs.get(1));
							if (DigesterCoder.getStringDigest(inputs.get(0), "MD5").equals(t1)
									&& DigesterCoder.getStringDigest(inputs.get(1), "MD5").equals(t2)) {
								key1 = inputs.get(0);
								key2 = inputs.get(1);
								key = ClassicalCoder.substitutionDecrypt(key, key2);
								CacheUtils.setCache("key", key);
								CacheUtils.setCache("key1", key1);
								CacheUtils.setCache("key2", key2);
							}
						}
					} catch (Exception ex) {
						LOG.error(ex.getMessage());
						System.exit(0);
					}
				}
			}
		});
	}
}
