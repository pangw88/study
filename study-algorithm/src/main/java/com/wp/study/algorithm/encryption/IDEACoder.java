package com.wp.study.algorithm.encryption;

import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.wp.study.base.constant.CommonConstants;

/**
 * IDEA安全编码组件
 * 
 * @version 1.0
 * @since 1.0
 */
public abstract class IDEACoder {

	/**
	 * 转换密钥
	 * 
	 * @param key 二进制密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	private static Key toKey(byte[] key) throws Exception {
		// 生成秘密密钥
		return new SecretKeySpec(key, CommonConstants.ENCRYPTION_ALGO_IDEA);
	}

	/**
	 * 解密
	 * 
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 加入BouncyCastleProvider支持
		Security.addProvider(new BouncyCastleProvider());
		// 还原密钥
		Key k = toKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_IDEA_CIPHER);
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
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 加入BouncyCastleProvider支持
		Security.addProvider(new BouncyCastleProvider());
		// 还原密钥
		Key k = toKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(CommonConstants.ENCRYPTION_ALGO_IDEA_CIPHER);
		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 生成密钥 <br>
	 * 
	 * @return byte[] 二进制密钥
	 * @throws Exception
	 */
	public static byte[] initKey() throws Exception {
		// 加入BouncyCastleProvider支持
		Security.addProvider(new BouncyCastleProvider());
		// 实例化
		KeyGenerator kg = KeyGenerator.getInstance(CommonConstants.ENCRYPTION_ALGO_IDEA);
		// 初始化
		kg.init(128);
		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}
}
