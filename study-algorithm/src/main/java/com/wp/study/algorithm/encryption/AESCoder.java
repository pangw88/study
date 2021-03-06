package com.wp.study.algorithm.encryption;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.wp.study.base.constant.CommonConstants;

/**
 * AES安全编码组件</br>
 * 加密/解密算法 / 工作模式 / 填充方式 </br>
 * Java 6支持PKCS5Padding填充方式 </br>
 * 需使用扩展包Bouncy Castle支持PKCS7Padding填充方式
 * 
 * @version 1.0
 */
public class AESCoder implements IEncryptionCoder {

	/**
	 * 转换密钥
	 * 
	 * @param key 二进制密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	public Key toKey(byte[] key) throws Exception {
		// 实例化AES密钥材料
		return new SecretKeySpec(key, CommonConstants.ENCRYPTION_ALGO_AES);
	}

	/**
	 * 生成密钥 <br>
	 * 
	 * @return byte[] 二进制密钥
	 * @throws Exception
	 */
	public byte[] initKey() throws Exception {
		// 实例化
		KeyGenerator kg = KeyGenerator.getInstance(CommonConstants.ENCRYPTION_ALGO_AES);
		/*
		 * AES 要求密钥长度为 128位、192位或 256位
		 */
		kg.init(256);
		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}

	/**
	 * 解密
	 * 
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BCF");
		 */
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_AES_CIPHER);
		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 加密
	 * 
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_AES_CIPHER);
		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

}
