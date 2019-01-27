package com.wp.study.algorithm.encryption;

import java.security.Key;

/**
 * 加密算法接口
 * 
 * @author wp
 *
 */
public interface IEncryptionCoder {
	
	/**
	 * 转换密钥
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
  	Key toKey(byte[] key) throws Exception;
  	
  	/**
  	 * 生成密钥
  	 * 
  	 * @return
  	 * @throws Exception
  	 */
  	byte[] initKey() throws Exception;
  	
  	/**
  	 * 解密
  	 * 
  	 * @param data
  	 * @param key
  	 * @return
  	 * @throws Exception
  	 */
  	byte[] decrypt(byte[] data, byte[] key) throws Exception;
  	
  	/**
  	 * 加密
  	 * 
  	 * @param data
  	 * @param key
  	 * @return
  	 * @throws Exception
  	 */
  	byte[] encrypt(byte[] data, byte[] key) throws Exception;
  	
}
