package com.wp.study.algorithm.encryption;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.wp.study.base.constant.CommonConstants;

/**
 * DES安全编码组件 <br>
 * Java 6 只支持56bit密钥<br>
 * Bouncy Castle 支持64bit密钥
 * 
 * @version 1.0
 */
public class DESCoder implements IEncryptionCoder {

	/**
	 * 转换密钥
	 * 
	 * @param key 二进制密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	public Key toKey(byte[] key) throws Exception {
		// 实例化DES密钥材料
		DESKeySpec dks = new DESKeySpec(key);
		// 实例化秘密密钥工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CommonConstants.ENCRYPTION_ALGO_DES);
		// 生成秘密密钥
		return keyFactory.generateSecret(dks);
	}

	/**
	 * 生成密钥 <br>
	 * Java 6 只支持56bit密钥 <br>
	 * Bouncy Castle 支持64bit密钥 <br>
	 * 
	 * @return byte[] 二进制密钥
	 * @throws Exception
	 */
	public byte[] initKey() throws Exception {
		/*
		 * 实例化密钥生成器
		 * 
		 * 若要使用64bit密钥注意替换 将下述代码中的KeyGenerator.getInstance(CIPHER_ALGORITHM);
		 * 替换为KeyGenerator.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		KeyGenerator kg = KeyGenerator.getInstance(CommonConstants.ENCRYPTION_ALGO_DES);
		/*
		 * 初始化密钥生成器 若要使用64bit密钥注意替换 将下述代码kg.init(56); 替换为kg.init(64);
		 */
		kg.init(56, new SecureRandom());
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
		// 实例化
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_DES_CIPHER);
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
		// 实例化
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_DES_CIPHER);
		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

}
