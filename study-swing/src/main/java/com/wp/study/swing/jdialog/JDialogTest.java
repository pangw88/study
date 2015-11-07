package com.wp.study.swing.jdialog;

import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.wp.study.swing.gbcs.GBCsUtil;

public class JDialogTest extends JDialog {
	
	private static final long serialVersionUID = 119572102115798540L;
	
	public JDialogTest(Frame owner, String title) {
		super(owner, title, true);
		Point p = owner.getLocationOnScreen();
		int parentW = owner.getWidth();
		int parentH = owner.getHeight();
		int parentX = (int)p.getX();
		int parentY = (int)p.getY();
		int jdW = 250;
		int jdH = 150;
		int jdX = parentX+(parentW-jdW)/2;
		int jdY = parentY+(parentH-jdH)/2-50;
		setBounds(jdX, jdY, jdW, jdH);
		cp = getContentPane();
		layout = new GridBagLayout();
		setLayout(layout);
		gbcs = new GridBagConstraints();
		JPanel sp0 = new JPanel();
        cp.add(sp0);
        layout.setConstraints(sp0, GBCsUtil.gbcsSet(gbcs, 0, 0, 1));
		JLabel jl1 = new JLabel("input1: ");
		jl1.setFont(new Font("Dialog", 1, 16));
    	cp.add(jl1);
        layout.setConstraints(jl1, GBCsUtil.gbcsSet(gbcs, 1, 0, 0));
        jp1 = new JPasswordField();
        cp.add(jp1);
        gbcs.gridwidth = 0;
    	gbcs.weightx = 1;
    	gbcs.weighty = 0;
        layout.setConstraints(jp1, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
        JPanel sp1 = new JPanel();
        cp.add(sp1);
        layout.setConstraints(sp1, GBCsUtil.gbcsSet(gbcs, 0, 0, 1));
        JLabel jl2 = new JLabel("input2: ");
        jl2.setFont(new Font("Dialog", 1, 16));
    	cp.add(jl2);
        layout.setConstraints(jl2, GBCsUtil.gbcsSet(gbcs, 1, 0, 0));
        jp2 = new JPasswordField();
        cp.add(jp2);
        layout.setConstraints(jp2, GBCsUtil.gbcsSet(gbcs, 0, 1, 0));
        JPanel sp2 = new JPanel();
        cp.add(sp2);
        layout.setConstraints(sp2, GBCsUtil.gbcsSet(gbcs, 0, 0, 1));
        JButton jb = new JButton("check");
        jb.setFont(new Font("Dialog", 1, 16));
        jb.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			char[] in1 = jp1.getPassword();
    			char[] in2 = jp2.getPassword();
    			if(in1 != null && in2 != null) {
    				inputs = new ArrayList<String>();
        			inputs.add(new String(in1));
        			inputs.add(new String(in2));
        			dispose();
    			}
    		}
    	});
        cp.add(jb);
        layout.setConstraints(jb, GBCsUtil.gbcsSet(gbcs, 0, 0, 1));
		//setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	@Override
	protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }
	
	public List<String> getInputs() {
		return inputs;
	}
	
	private Container cp;
	private GridBagLayout layout;
	private GridBagConstraints gbcs;
	private List<String> inputs;
	private JPasswordField jp1;
	private JPasswordField jp2;
}
