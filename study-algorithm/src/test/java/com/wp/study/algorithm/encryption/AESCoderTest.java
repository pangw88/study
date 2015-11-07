package com.wp.study.algorithm.encryption;

import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AES安全编码组件校验
 * 
 * @version 1.0
 */
public class AESCoderTest {

	private static final Logger LOG = LoggerFactory.getLogger(AESCoderTest.class);
	
	/**
	 * 测试
	 * 
	 * @throws Exception
	 */
	@Test
	public final void test() {
		try {
			String inputStr = "AES";
			byte[] inputData = inputStr.getBytes();
			LOG.info("原文:{}", inputStr);
			// 初始化密钥
			byte[] key = AESCoder.initKey();
			LOG.info("密钥:{}", Base64.encodeBase64String(key));
			// 加密
			inputData = AESCoder.encrypt(inputData, key);
			LOG.info("加密后:{}", Base64.encodeBase64String(inputData));
			// 解密
			byte[] outputData = AESCoder.decrypt(inputData, key);
			String outputStr = new String(outputData);
			LOG.info("解密后:{}", outputStr);
			// 校验
			assertEquals(inputStr, outputStr);
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
