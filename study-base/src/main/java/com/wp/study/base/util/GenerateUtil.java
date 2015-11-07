package com.wp.study.base.util;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateUtil {

	private static final Logger LOG = LoggerFactory.getLogger(GenerateUtil.class);
	private static int[][] CHARS;
	
	static {
		CHARS = new int[4][];
		CHARS[0] = new int[10];
		CHARS[1] = new int[26];
		CHARS[2] = new int[26];
		CHARS[3] = new int[8];
		for(int i = 0; i < 10; i++) {
			// numbers
			CHARS[0][i] = '0' + i;
		}
		for(int j = 0; j < 26; j++) {
			// lowercases
			CHARS[1][j] = 'a' + j;
			// capitals
			CHARS[2][j] = 'A' + j;
		}
		// symbols
		CHARS[3] = new int[]{'-','+','_','$','%','&','*','.'};
	}
	
	public static String getStr(int length, boolean complex) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		int group = -1;
		int seek = -1;
		int temp = -1;
		for(int i = 0; i < length; i++) {
			int random = r.nextInt(length * length);
			// 取二位数组某一组
			temp = random % (complex ? 4 : 3);
			if(group == temp) {
				group = (group + 1) % (complex ? 4 : 3);
			} else {
				group = temp;
			}
			// 取组内具体元素
			temp = random % CHARS[group].length;
			if(seek == temp) {
				seek = (seek + 1) % CHARS[group].length;
			} else {
				seek = temp;
			}
			sb.append((char) CHARS[group][seek]);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		// 任务开始
		LOG.info("Task start!");
		for (int i = 0; i < 10; i++) {
			String str = getStr(10, true);
			LOG.info(i + "  " + str);
		}
		// 任务结束
		LOG.info("Task end!");
	}
}
