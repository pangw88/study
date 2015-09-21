package com.wp.study.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.common.encryption.ClassicalCoder;

public class CommonUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);

	/**
	 * 判断字符串为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null)
			return true;
		return "".equals(str.trim());
	}

	/**
	 * 判读字符串不为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 判断字符数组为空
	 * 
	 * @param chs
	 * @return
	 */
	public static boolean isEmpty(char[] chs) {
		if (chs == null)
			return true;
		return chs.length == 0;
	}

	/**
	 * 判断字符数组不为空
	 * 
	 * @param chs
	 * @return
	 */
	public static boolean isNotEmpty(char[] chs) {
		return !isEmpty(chs);
	}

	/**
	 * 将byte数组转换为十六进制字符串
	 * byte（字节,8bit二进制数）类型值范围：-128（binary：10000000）~ 127
	 * java以补码存储整形数据
	 * byte b = -1（jdk1.7以下二进制不可显示表示，jdk1.7以上可用b=0b11111111表示-1）
	 * 原码：10000001
	 * 反码：11111110 （正数同原码，负数原码除符号位取反）
	 * 补码：11111111 （正数同原码，负数反码加1）
	 * b在计算机中存储为：11111111
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder sb = new StringBuilder("");
		if (src == null || src.length < 1) {
			LOG.error("byte array is null");
			return null;
		} else {
			for (int i = 0; i < src.length; i++) {
				/* Integer.toHexString方法的入参是int类型（4字节），对于负的byte类型值，
				 * 0xFF默认是整形，byte类型和0xFF进行"&"运算，会将byte转为整形（32位），
				 * 这样高24位为0，低8位为byte实际值。而负值若byte b=-1直接赋值给int类型，
				 * 其补码表示为：11111111 11111111 11111111 11111111，
				 * 而期望正确值为：00000000 00000000 00000000 11111111*/
				int v = src[i] & 0xFF;
				String hv = Integer.toHexString(v);
				if (hv.length() < 2) {
					sb.append(0);
				}
				sb.append(hv);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 将十六进制字符串转换为byte数组
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		byte[] bytes = null;
		if (isEmpty(hexString)) {
			LOG.error("hex string is null");
	    } else if((hexString.length() % 2) != 0) {
	    	LOG.error("hex string length={} is odd", hexString.length());
	    } else {
		    int length = hexString.length() / 2;
		    bytes = new byte[length];
		    for (int i = 0; i < length; i++) {  
		        int pos = i * 2;
		        String vString = hexString.substring(pos, pos + 2);
		        try {
			        bytes[i] = (byte)Integer.parseInt(vString, 16);
		        } catch(Exception e) {
		        	LOG.error("{} convert to byte failed, error is:{}", 
		        			vString, e.getMessage());
		        }
		    }  
	    }
		return bytes;
	}
	
	/**
	 * 获取level等级
	 * 
	 * @param level
	 * @return
	 * @throws Exception
	 */
	public static int calLevel(String level) throws Exception {
		if (isEmpty(level) || level.length() != 1)
			throw new Exception();
		return level.charAt(0) - 'a';
	}

	/**
	 * 获取实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param returnType
	 * @return
	 * @throws Exception
	 */
	public static <E, T> T getField(E e, String name, Class<T> returnType)
			throws Exception {
		String methodName = "get" + Character.toUpperCase(name.charAt(0))
				+ name.substring(1);
		Object value = e.getClass().getMethod(methodName).invoke(e);
		return returnType.cast(value);
	}

	/**
	 * 获取实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static <E> Object getField(E e, String name) throws Exception {
		String methodName = "get" + Character.toUpperCase(name.charAt(0))
				+ name.substring(1);
		Method method = e.getClass().getMethod(methodName);
		if (method != null) {
			Class<?> returnType = method.getReturnType();
			return returnType.cast(method.invoke(e));
		} else {
			throw new NoSuchMethodException();
		}
	}

	/**
	 * 设置实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param value
	 * @param parameterType
	 * @throws Exception
	 */
	public static <E, T> void setField(E e, String name, T value,
			Class<T> parameterType) throws Exception {
		if(value != null) {
			String methodName = "set" + Character.toUpperCase(name.charAt(0))
					+ name.substring(1);
			e.getClass().getMethod(methodName, parameterType).invoke(e, value);
		}
	}

	/**
	 * 设置实例e中指定field的值
	 * 
	 * @param e
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static <E> void setField(E e, String name, Object value)
			throws Exception {
		if(value != null) {
			String methodName = "set" + Character.toUpperCase(name.charAt(0))
					+ name.substring(1);
			Method[] methods = e.getClass().getMethods();
			if (methods != null && methods.length > 0) {
				for (Method method : methods) {
					if (method.getName().equals(methodName)) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes != null && parameterTypes.length == 1) {
							method.invoke(e, parameterTypes[0].cast(value));
							break;
						}
					}
				}
			} else {
				throw new NoSuchMethodException();
			}
		}
	}

	/**
	 * 对str进行替代加密
	 * 
	 * @param str
	 * @param key1
	 * @return
	 */
	public static String strEncrypt(String str, String key) {
		try {
			if (isNotEmpty(str)) {
				str = ClassicalCoder.substitutionEncrypt(
						Base64.encodeBase64String(str.getBytes()), key);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return str;
	}

	/**
	 * 以key值对map进行排序
	 * 
	 * @param oriMap
	 * @return
	 */
	public Map<String, String> sortMapByKey(Map<String, String> oriMap) {
		if (oriMap == null || oriMap.isEmpty()) {
			return null;
		}
		Map<String, String> sortedMap = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String key1, String key2) {
				return key1.compareTo(key2);
			}
		});
		sortedMap.putAll(oriMap);
		return sortedMap;
	}

	/**
	 * 以key值对map进行排序
	 * 
	 * @param oriMap
	 * @return
	 */
	public Map<String, String> sortMapByValue(Map<String, String> oriMap) {
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(
					oriMap.entrySet());
			Collections.sort(entryList,
					new Comparator<Map.Entry<String, String>>() {
						public int compare(Entry<String, String> entry1,
								Entry<String, String> entry2) {
							String value1 = entry1.getValue();
							String value2 = entry2.getValue();
							if (value1 == null) {
								return -1;
							} else if (value2 == null) {
								return 1;
							}
							return value1.compareTo(value2);
						}
					});
			Iterator<Map.Entry<String, String>> iter = entryList.iterator();
			Map.Entry<String, String> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return sortedMap;
	}

	/**
	 * 以指定摘要算法计算字符串摘要
	 * 
	 * @param string
	 * @param algorithm
	 * 		如：MD5、SHA-1等算法
	 * @return
	 */
	public static String getStringDigest(String string, String algorithm) {
		String stringDigest = null;
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(string.getBytes());
			stringDigest = bytesToHexString(digest.digest());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return stringDigest;
	}

	/**
	 * 以指定摘要算法计算文件摘要
	 * 
	 * @param file
	 * @param algorithm
	 * 		如：MD5、SHA-1等算法
	 * @return
	 */
	public static String getFileDigest(File file, String algorithm) {
		String fileDigest = null;
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte[] buffer = new byte[8192];
		int len;
		try {
			digest = MessageDigest.getInstance(algorithm);
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			fileDigest = bytesToHexString(digest.digest());
			in.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			try {
				if(in != null) {
					in.close();
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
		return fileDigest;
	}
}
