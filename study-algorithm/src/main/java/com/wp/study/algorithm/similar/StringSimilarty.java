package com.wp.study.algorithm.similar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSimilarty {

	private static final Logger LOG = LoggerFactory.getLogger(StringSimilarty.class);

	/**
	 * 编辑距离算法
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double levenshteinDistance(String str1, String str2) {
		double res = 0.0;
		if (StringUtils.isNotEmpty(str1) && StringUtils.isNotEmpty(str2)) {
			// 计算两个字符串的长度。
			int len1 = str1.length();
			int len2 = str2.length();
			// 建立上面说的数组，比字符长度大一个空间
			int[][] dif = new int[len1 + 1][len2 + 1];
			// 赋初值，步骤B。
			for (int a = 0; a <= len1; a++) {
				dif[a][0] = a;
			}
			for (int a = 0; a <= len2; a++) {
				dif[0][a] = a;
			}
			// 计算两个字符是否一样，计算左上的值
			int temp;
			for (int i = 1; i <= len1; i++) {
				for (int j = 1; j <= len2; j++) {
					if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
						temp = 0;
					} else {
						temp = 1;
					}
					// 取三个值中最小的
					dif[i][j] = Math.min(Math.min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1), dif[i - 1][j] + 1);
				}
			}
			res = (1 - dif[len1][len2] / Math.max(len1, len2));
		} else {
			LOG.warn("input string is empty!");
		}
		return res;
	}

	/**
	 * 余弦定理算法
	 * 
	 * 本方法采用计算向量间的夹角（余弦公式）来判断相似度。
	 * 简易起见本法采用字作为向量（词作为向量准确度更高）。
	 * 每个字在字符串中出现的次数，便是此字向量的值。
	 * 现在假设：
	 * 字符串1中出现的字为：Z1c1,Z1c2,……Z1cn，在字符串中的个数为：Z1n1,Z1n2,……Z1nm；
	 * 字符串2中出现的字为：Z2c1,Z2c2,……Z2cn，在字符串中的个数为：Z2n1,Z2n2,……Z2nm；
	 * 其中Z1c1和Z2c1表示两个文本中同一个字，Z1n1和Z2n1是它们分别对应的个数。
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double cosineSimilar(String str1, String str2) {
		double res = 0.0;
		if (StringUtils.isNotEmpty(str1) && StringUtils.isNotEmpty(str2)) {
			Map<Integer, int[]> vectorDictionary = new HashMap<Integer, int[]>();
			// 将两个字符串中的中文字符以及出现的总数封装到，AlgorithmMap中
			for (int i = 0; i < str1.length(); i++) {
				int charIndex = str1.charAt(i);
				int[] fq = vectorDictionary.get(charIndex);
				if (fq != null && fq.length == 2) {
					fq[0]++;
				} else {
					fq = new int[2];
					fq[0] = 1;
					fq[1] = 0;
					vectorDictionary.put(charIndex, fq);
				}
			}
			for (int i = 0; i < str2.length(); i++) {
				int charIndex = str2.charAt(i);
				int[] fq = vectorDictionary.get(charIndex);
				if (fq != null && fq.length == 2) {
					fq[1]++;
				} else {
					fq = new int[2];
					fq[0] = 0;
					fq[1] = 1;
					vectorDictionary.put(charIndex, fq);
				}
			}
			Iterator<Integer> iterator = vectorDictionary.keySet().iterator();
			double sqdoc1 = 0;
			double sqdoc2 = 0;
			double denominator = 0;
			while (iterator.hasNext()) {
				int[] c = vectorDictionary.get(iterator.next());
				denominator += c[0] * c[1];
				sqdoc1 += c[0] * c[0];
				sqdoc2 += c[1] * c[1];
			}
			res = denominator / Math.sqrt(sqdoc1 * sqdoc2);
		} else {
			LOG.warn("input string is empty!");
		}
		return res;
	}

}
