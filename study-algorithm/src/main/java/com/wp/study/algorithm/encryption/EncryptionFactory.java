package com.wp.study.algorithm.encryption;

import com.wp.study.base.constant.CommonConstants;

/**
 * 加密算法工厂
 * 
 * @author wp
 *
 */
public class EncryptionFactory {

	/**
	 * 获取加密算法coder
	 * 
	 * @param encryption
	 * @return
	 */
	public static IEncryptionCoder getEncryptionCoder(String encryption) {
		IEncryptionCoder encryptionCoder = null;
		switch (encryption) {
		case CommonConstants.ENCRYPTION_ALGO_AES:
			encryptionCoder = new AESCoder();
			break;
		case CommonConstants.ENCRYPTION_ALGO_DES:
			encryptionCoder = new DESCoder();
			break;
		case CommonConstants.ENCRYPTION_ALGO_DESEDE:
			encryptionCoder = new DESedeCoder();
			break;
		case CommonConstants.ENCRYPTION_ALGO_IDEA:
			encryptionCoder = new IDEACoder();
			break;
		default:
			break;
		}
		return encryptionCoder;
	}
	
	/**
	 * 加密
	 * 
	 * @param encryption
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(String encryption, byte[] data, byte[] key) throws Exception {
		IEncryptionCoder encryptionCoder = getEncryptionCoder(encryption);
		if (null == encryptionCoder) {
			throw new Exception("EncryptionCoder not exist, encryption=" + encryption);
		}
		return encryptionCoder.encrypt(data, key);
	}

	/**
	 * 解密
	 * 
	 * @param encryption
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(String encryption, byte[] data, byte[] key) throws Exception {
		IEncryptionCoder encryptionCoder = getEncryptionCoder(encryption);
		if (null == encryptionCoder) {
			throw new Exception("EncryptionCoder not exist, encryption=" + encryption);
		}
		return encryptionCoder.decrypt(data, key);
	}

}
