package com.wp.study.algorithm.encryption;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 古典安全编码组件校验
 * 
 * @version 1.0
 */
public class ClassicalCoderTest {

	private static final Logger LOG = LoggerFactory.getLogger(ClassicalCoderTest.class);
	
	/**
	 * 测试
	 * 
	 * @throws Exception
	 */
	@Test
	public final void test() {
		try {
			String inputStr = "Classical";
			LOG.info("原文:{}", inputStr);
			// 初始化密钥
			String key = "999";
			LOG.info("密钥:{}", key);
			// 加密
			String inputData = ClassicalCoder.substitutionEncrypt(inputStr, key);
			LOG.info("加密后:{}", inputData);
			// 解密
			String outputData = ClassicalCoder.substitutionDecrypt(inputData, key);
			LOG.info("解密后:{}", outputData);
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
