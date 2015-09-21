package com.wp.study.common.encryption;

/**
 * 古典安全编码组件
 * 
 * @version 1.0
 */
public abstract class ClassicalCoder {

	/**
	 * 移位加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String transpositionEncrypt(String data) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int length = data.length();
		int group = length/5 + (length%5==0 ? 0 : 1);
		char[] dataArr = data.toCharArray();
		for(int i=0; i<group; i++) {
			for(int j=0; j<3; j++) {
				int index = i*5+j;
				if(index+2 < length) {
					char temp = dataArr[index+2];
					dataArr[index+2] = dataArr[index];
					dataArr[index] = temp;
				}
			}
		}
		return new String(dataArr);
	}
	
	/**
	 * 移位解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String transpositionDecrypt(String data) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int length = data.length();
		int group = length/5 + (length%5==0 ? 0 : 1);
		char[] dataArr = data.toCharArray();
		for(int i=0; i<group; i++) {
			for(int j=4; j>1; j--) {
				int index = i*5+j;
				if(index+1 > length) {
					continue;
				}
				char temp = dataArr[index];
				dataArr[index] = dataArr[index-2];
				dataArr[index-2] = temp;
			}
		}
		return new String(dataArr);
	}
	
	/**
	 * 替代加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String substitutionEncrypt(String data) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int length = data.length();
		char[] dataArr = data.toCharArray();
		for(int i=0; i< length; i++) {
			if(dataArr[i] >= 32 && dataArr[i] <= 126) {
				dataArr[i] = (char)((dataArr[i]+3-32)%95+32);
			}
		}
		return new String(dataArr);
	}
	
	/**
	 * 替代解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String substitutionDecrypt(String data) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int length = data.length();
		char[] dataArr = data.toCharArray();
		for(int i=0; i< length; i++) {
			if(dataArr[i] >= 32 && dataArr[i] <= 126) {
				dataArr[i] = (char)((dataArr[i]-32+95-3)%95+32);
			}
		}
		return new String(dataArr);
	}
	
	/**
	 * 替代加密
	 * 
	 * @param data 待加密数据
	 * 
	 */
	public static String substitutionEncrypt(String data, String key) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int sec = 0;
		Integer value = Integer.valueOf(key);
		while(value != 0) {
			sec += value%10;
			value /= 10;
		}
		if(sec != 0) {
			int length = data.length();
			char[] dataArr = data.toCharArray();
			for(int i=0; i< length; i++) {
				if(dataArr[i] >= 32 && dataArr[i] <= 126) {
					dataArr[i] = (char)(dataArr[i]*sec%95+32);
				}
			}
			return new String(dataArr);
		} else {
			return null;
		}
	}
	
	/**
	 * 替代解密
	 * 
	 * @param data 待解密数据
	 * 
	 */
	public static String substitutionDecrypt(String data, String key) {
		if(data == null || data.length() == 0) {
			return null;
		}
		int sec = 0;
		Integer value = Integer.valueOf(key);
		while(value != 0) {
			sec += value%10;
			value /= 10;
		}
		if(sec != 0) {
			int length = data.length();
			char[] dataArr = data.toCharArray();
			for(int i=0; i< length; i++) {
				if(dataArr[i] >= 32 && dataArr[i] <= 126) {
					int temp = dataArr[i]-32;
					for(int j=32; j<=126; j++) {
						if(temp == j*sec%95) {
							dataArr[i] = (char)j;
						}
						
					}
				}
			}
			return new String(dataArr);
		} else {
			return null;
		}
	}
}
