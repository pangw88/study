package com.wp.study.swing.gbcs;

import java.awt.GridBagConstraints;

public class GBCsUtil {

	public static GridBagConstraints gbcsSet(GridBagConstraints gbcs, 
			int gridwidth, double weightx, double weighty) {
		gbcs.fill = GridBagConstraints.BOTH;
		//该方法是设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
		gbcs.gridwidth = gridwidth; 
		//该方法设置组件水平的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
    	gbcs.weightx = weightx; 
    	//该方法设置组件垂直的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
    	gbcs.weighty = weighty; 
    	return gbcs;
	}
	
}
