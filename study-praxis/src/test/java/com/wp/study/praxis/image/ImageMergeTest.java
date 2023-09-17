package com.wp.study.praxis.image;

import com.wp.study.praxis.image.render.ImageRepaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ImageMergeTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImageMergeTest.class);
	
	public static void main(String[] args) {
		try {
			// 任务开始
			long startTime = System.currentTimeMillis();
			LOG.info("task begin");
			File mainDir = new File("D:\\迅雷下载\\p_dvd2_fuuka03");
			String assitDir = "E:\\Fuuka Nishihama\\minisuka\\p_dvd2_fuuka03\\";
			for (File mainFile : mainDir.listFiles()) {
				String name = mainFile.getName().toLowerCase();
				if (name.endsWith("jpg") || name.endsWith("jpeg")) {
					ImageRepaint.repaintAssignArea(
							mainFile.getPath(), assitDir + mainFile.getName(),
							"左下", 400, 150);
//					240, 60);
				}
			}
			// 任务结束
			LOG.info("wasteTime=" + (System.currentTimeMillis() - startTime));
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}

}
