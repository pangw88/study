package com.wp.study.praxis.image.similar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.util.IoUtils;

public class SimilarImageInspect {

	private static final Logger LOG = LoggerFactory.getLogger(SimilarImageInspect.class);

	private static Map<String, Set<String>> similarMap = new ConcurrentHashMap<String, Set<String>>();
	private static Map<String, Boolean> hasCheckMap = new ConcurrentHashMap<String, Boolean>();
	private static Map<String, String> originTargetMap = new ConcurrentHashMap<String, String>();
	private static Set<String> picSet = new HashSet<String>();

	private static ExecutorService computeSimilarPool = new ThreadPoolExecutor(16, 16, 5, TimeUnit.MINUTES,
			new ArrayBlockingQueue<Runnable>(10000));

	public static void main(String[] args) {
		File originDir = new File("D:\\origin_pic");
		File targetDir = new File("D:\\希捷数据救护\\done\\Asami Kondou");
		if (!originDir.exists() || !originDir.isDirectory() || !targetDir.exists() || !targetDir.isDirectory()) {
			return;
		}
		FileWriter fw = null;
		try {
			allImageInspect(originDir, targetDir);
			// 重命名详情输出
			fw = new FileWriter(new File("E:\\image\\similarity_" + System.currentTimeMillis())) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			for (String str : picSet) {
				if (!Boolean.TRUE.equals(hasCheckMap.get(str))) {
					LOG.error("similarity false {}", str);
					// 判断是否补全
					File dir = new File(str).getParentFile();
					for (File image : dir.listFiles()) {
						if (!Boolean.TRUE.equals(hasCheckMap.get(image.getAbsolutePath()))) {
							continue;
						}
						String strPre = str.substring(0, str.length() - 7);
						String imagePre = image.getAbsolutePath().substring(0, image.getAbsolutePath().length() - 7);
						if (!strPre.equals(imagePre)) {
							continue;
						}
						String existK = image.getAbsolutePath();
						String existV = originTargetMap.get(existK);
						int ki = Integer.valueOf(existK.substring(existK.length() - 6, existK.length() - 4));
						int vi = Integer.valueOf(existV.substring(existV.length() - 6, existV.length() - 4));
						int tki = Integer.valueOf(str.substring(str.length() - 6, str.length() - 4));
						int tvi = tki - ki + vi;
						String target = existV.substring(0, existV.length() - 6) + (tvi < 10 ? ("0" + tvi) : tvi)
								+ ".jpg";
						fw.write(str + "=" + target);
						fw.flush();
						break;
					}
				} else {
					fw.write(str + "=" + originTargetMap.get(str));
					fw.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(fw);
			computeSimilarPool.shutdown();
		}
	}

	/**
	 * 第1张图进行检验
	 * 
	 * @param originFilePath
	 * @param targetDirPath
	 */
	public static void allImageInspect(File originDir, File targetDir) {
		File[] subOriginDirs = originDir.listFiles();
		if (null == subOriginDirs || subOriginDirs.length == 0) {
			return;
		}
		File[] subTargetDirs = targetDir.listFiles();
		if (null == subTargetDirs || subTargetDirs.length == 0) {
			return;
		}
		int size = subOriginDirs.length;
		if (originDir.getAbsolutePath().equals(targetDir.getAbsolutePath())) {
			size = (size / 2) + 1;
		}
		for (int i = 0; i < size; i++) {
			File subOriginDir = subOriginDirs[i];
			if (subOriginDir.isFile() || subOriginDir.getName().endsWith("_repaint")) {
				continue;
			}
			File[] files = subOriginDir.listFiles();
			if (null == files || files.length == 0) {
				continue;
			}
			String dirName = subOriginDir.getAbsolutePath();
			Set<String> similarSet = similarMap.get(dirName);
			if (null == similarSet) {
				similarMap.put(dirName, new HashSet<String>());
			}
			for (File file : files) {
				LOG.error("process rate={}%, current={}", (int) (i * 100 / size), subOriginDir);
				if (!file.isFile()) {
					LOG.error("{} is not file", file);
					continue;
				}
				picSet.add(file.getAbsolutePath());
				Set<String> set = similarMap.get(subOriginDir.getAbsolutePath());
				if (set.size() == 0) {
					// 取第1张检验
					for (File subTargetDir : subTargetDirs) {
						if (subTargetDir.isDirectory() && !subTargetDir.getName().endsWith("_repaint")
								&& !file.getParentFile().getName().equals(subTargetDir.getName())) {
							File[] targetFiles = subTargetDir.listFiles();
							if (null == targetFiles || targetFiles.length == 0) {
								continue;
							}
							File[] temps = { targetFiles[0] };
							computeSimilarity(file, temps);
							if (Boolean.TRUE.equals(hasCheckMap.get(file.getAbsolutePath()))) {
								break;
							}
						} else {
//							LOG.error("{} skip", subTargetDir);
						}
					}
				} else {
					for (String dirPath : set) {
						File[] temps = new File(dirPath).listFiles();
						computeSimilarity(file, temps);
						if (Boolean.TRUE.equals(hasCheckMap.get(file.getAbsolutePath()))) {
							break;
						}
					}
				}
				if (Boolean.TRUE.equals(hasCheckMap.get(file.getAbsolutePath()))) {
					continue;
				}
				for (File subTargetDir : subTargetDirs) {
					if (subTargetDir.isDirectory() && !subTargetDir.getName().endsWith("_repaint")
							&& !file.getParentFile().getName().equals(subTargetDir.getName())) {
						File[] targetFiles = subTargetDir.listFiles();
						if (null == targetFiles || targetFiles.length == 0) {
							continue;
						}
						computeSimilarity(file, targetFiles);
						if (Boolean.TRUE.equals(hasCheckMap.get(file.getAbsolutePath()))) {
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * 计算相似度
	 * 
	 * @param originFile
	 * @param targetFiles
	 */
	public static void computeSimilarity(File originFile, File[] targetFiles) {
		if (null == targetFiles || targetFiles.length == 0) {
			return;
		}
		try {
			final CountDownLatch latch = new CountDownLatch(targetFiles.length);
			for (File targetFile : targetFiles) {
				computeSimilarPool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							if (!targetFile.exists() || !targetFile.isFile()) {
								return;
							}
							if (Boolean.TRUE.equals(hasCheckMap.get(originFile.getAbsolutePath()))) {
								return;
							}
							float similarity = FingerPrint.getSimilarity(originFile, targetFile);
							if (similarity > 0.97f) {
								hasCheckMap.put(originFile.getAbsolutePath(), Boolean.TRUE);
								similarMap.get(originFile.getParent()).add(targetFile.getParent());
								originTargetMap.put(originFile.getAbsolutePath(), targetFile.getAbsolutePath());
								return;
							}
						} catch (Exception e) {
							LOG.error("fail {}={}, error:", originFile, targetFile, e);
						} finally {
							latch.countDown();
						}
					}
				});
			}
			try {
				latch.await();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载文件信息
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, String> loadFingers(File file) {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
				return map;
			}
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while (null != (line = br.readLine())) {
				if (StringUtils.isNotBlank(line)) {
					line = line.trim();
					String[] arr = line.split("=");
					if (arr.length == 2) {
						map.put(arr[0], arr[1]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(br);
		}
		return map;
	}

}
