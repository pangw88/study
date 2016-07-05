package com.wp.study.algorithm.digester;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigesterCoder {

	private static final Logger LOG = LoggerFactory.getLogger(DigesterCoder.class);

	/**
	 * 以指定摘要算法计算字符串摘要
	 * 
	 * @param string
	 * @param algorithm
	 *            如：MD5、SHA-1等算法
	 * @return
	 */
	public static String getStringDigest(String string, String algorithm) {
		String stringDigest = null;
		if (string != null) {
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance(algorithm);
				digest.update(string.getBytes());
				stringDigest = bytesToHexString(digest.digest());
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			LOG.warn("string is null!");
		}
		return stringDigest;
	}

	public static void main(String[] args) {
		System.out.println(getFileDigest(new File("D:/123.txt"), "MD5"));

	}

	/**
	 * 以指定摘要算法计算文件摘要
	 * 
	 * @param file
	 * @param algorithm
	 *            如：MD5、SHA-1等算法
	 * @return
	 */
	public static String getFileDigest(File file, String algorithm) {
		String fileDigest = null;
		if (file != null && file.exists()) {
			if (file.isFile()) {
				MessageDigest digest = null;
				FileInputStream in = null;
				byte[] buffer = null;
				int len = 0;
				try {
					digest = MessageDigest.getInstance(algorithm);
					in = new FileInputStream(file);
					buffer = new byte[8192];
					while ((len = in.read(buffer)) != -1) {
						digest.update(buffer, 0, len);
					}
					fileDigest = bytesToHexString(digest.digest());
					in.close();
				} catch (Exception e) {
					LOG.error(e.getMessage());
				} finally {
					try {
						if (in != null) {
							in.close();
						}
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
				}
			} else {
				LOG.warn("file is directory!");
			}
		} else {
			LOG.warn("file is null!");
		}
		return fileDigest;
	}

	/**
	 * 将byte数组转换为十六进制字符串 byte（字节,8bit二进制数）类型值范围：-128（binary：10000000）~ 127
	 * java以补码存储整形数据 byte b = -1（jdk1.7以下二进制不可显示表示，jdk1.7以上可用b=0b11111111表示-1）
	 * 原码：10000001 反码：11111110 （正数同原码，负数原码除符号位取反） 补码：11111111 （正数同原码，负数反码加1）
	 * b在计算机中存储为：11111111
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		String str = null;
		if (src != null && src.length > 0) {
			StringBuilder sb = new StringBuilder("");
			for (int i = 0; i < src.length; i++) {
				/*
				 * Integer.toHexString方法的入参是int类型（4字节），对于负的byte类型值，
				 * 0xFF默认是整形，byte类型和0xFF进行"&"运算，会将byte转为整形（32位），
				 * 这样高24位为0，低8位为byte实际值。而负值若byte b=-1直接赋值给int类型， 其补码表示为：11111111
				 * 11111111 11111111 11111111， 而期望正确值为：00000000 00000000
				 * 00000000 11111111
				 */
				int v = src[i] & 0xFF;
				String hv = Integer.toHexString(v);
				if (hv.length() < 2) {
					sb.append(0);
				}
				sb.append(hv);
			}
			str = sb.toString();
		} else {
			LOG.warn("byte array is empty!");
		}
		return str;
	}
	
	/**
	 * 将十六进制字符串转换为byte数组
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		byte[] bytes = null;
		if (StringUtils.isNotEmpty(hexString)) {
			if ((hexString.length() % 2) == 0) {
				int length = hexString.length() / 2;
				bytes = new byte[length];
				for (int i = 0; i < length; i++) {
					int pos = i * 2;
					String vString = hexString.substring(pos, pos + 2);
					try {
						bytes[i] = (byte) Integer.parseInt(vString, 16);
					} catch (Exception e) {
						LOG.error("{} convert to byte failed, error is:{}", vString, e.getMessage());
					}
				}
			} else {
				LOG.warn("hex string length={} is odd!", hexString.length());
			}
		} else {
			LOG.warn("hex string is empty!");
		}
		return bytes;
	}
}
