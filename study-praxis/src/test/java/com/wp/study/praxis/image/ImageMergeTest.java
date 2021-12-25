package com.wp.study.praxis.image;

import com.wp.study.praxis.file.FileOperation;
import com.wp.study.praxis.image.render.ImageRepaint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageMergeTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImageMergeTest.class);
	
	public static void main(String[] args) {
		try {
			// 任务开始
			long startTime = System.currentTimeMillis();
			LOG.info("task begin");
			String mainDir = "D:\\迅雷下载\\fuuka-n_n202\\";
			String assitDir = "E:\\Fuuka Nishihama\\minisuka\\p_dvd2_fuuka04\\";
			Map<String, String> map = new HashMap<String, String>(){
				{
					put("gh_fuuka-n_n2056.jpg", "p_fuuka2_02_011.jpg");
				}
			};
			for (Map.Entry<String, String> entry : map.entrySet()) {
				ImageRepaint.repaintAssignArea(
						mainDir + entry.getKey(), assitDir + entry.getValue(),
						"左下", 240, 60);
			}
			// 任务结束
			LOG.info("wasteTime=" + (System.currentTimeMillis() - startTime));
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}

}
