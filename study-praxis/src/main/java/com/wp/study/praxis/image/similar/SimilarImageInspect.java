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

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.ByteUtils;
import com.wp.study.base.util.IoUtils;

public class SimilarImageInspect {

	private static Map<String, Map<String, String>> fingerMap = new ConcurrentHashMap<String, Map<String, String>>();
	private static Map<String, FileWriter> fwMap = new ConcurrentHashMap<String, FileWriter>();
	private static Map<String, Set<String>> similarMap = new ConcurrentHashMap<String, Set<String>>();
	private static Map<String, Boolean> hasCheckMap = new ConcurrentHashMap<String, Boolean>();

	private static ExecutorService computeSimilarPool = new ThreadPoolExecutor(8, 8, 5, TimeUnit.MINUTES,
			new ArrayBlockingQueue<Runnable>(10000));

	public static void main(String[] args) {
		File originDir = new File("D:\\QMDownload\\tt");
		File targetDir = new File("D:\\希捷数据救护\\done\\Fuuka Nishihama--done");
		if (!originDir.exists() || !originDir.isDirectory() || !targetDir.exists() || !targetDir.isDirectory()) {
			return;
		}
		FileWriter fw = null;
		try {
			// 重命名详情输出
			fw = new FileWriter(new File("E:\\image\\similarity_" + System.currentTimeMillis())) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			allImageInspect(fw, originDir, targetDir);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(fw);
			computeSimilarPool.shutdown();
			destroy();
		}
	}

	public static void destroy() {
		for (Map.Entry<String, FileWriter> entry : fwMap.entrySet()) {
			try {
				FileWriter fw = entry.getValue();
				Map<String, String> fingerDirMap = fingerMap.get(entry.getKey());
				if (null != fingerDirMap) {
					for (Map.Entry<String, String> fingerEntry : fingerDirMap.entrySet()) {
						fw.write(fingerEntry.getKey() + "=" + fingerEntry.getValue());
					}
				}
				fw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtils.closeQuietly(entry.getValue());
			}
		}
	}

	/**
	 * 第1张图进行检验
	 * 
	 * @param originFilePath
	 * @param targetDirPath
	 */
	public static void allImageInspect(FileWriter fw, File originDir, File targetDir) {
		File[] subOriginDirs = originDir.listFiles();
		if (null == subOriginDirs || subOriginDirs.length == 0) {
			return;
		}
		File[] subTargetDirs = targetDir.listFiles();
		if (null == subTargetDirs || subTargetDirs.length == 0) {
			return;
		}
		for (File subOriginDir : subOriginDirs) {
			if (subOriginDir.isFile()) {
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
				if (!file.isFile()) {
					System.out.println(file + " is not file");
					continue;
				}
				Set<String> set = similarMap.get(subOriginDir.getAbsolutePath());
				if (set.size() == 0) {
					// 取第1张检验
					for (File subTargetDir : subTargetDirs) {
						if (subTargetDir.isDirectory()) {
							File[] targetFiles = subTargetDir.listFiles();
							if (null == targetFiles || targetFiles.length == 0) {
								continue;
							}
							File[] temps = { targetFiles[0] };
							computeSimilarity(fw, file, temps);
						}
					}
				} else {
					for (String dirPath : set) {
						File[] temps = new File(dirPath).listFiles();
						computeSimilarity(fw, file, temps);
					}
				}
				if (Boolean.TRUE.equals(hasCheckMap.get(file.getAbsolutePath()))) {
					continue;
				}
				for (File subTargetDir : subTargetDirs) {
					if (subTargetDir.isDirectory()) {
						File[] targetFiles = subTargetDir.listFiles();
						if (null == targetFiles || targetFiles.length == 0) {
							continue;
						}
						computeSimilarity(fw, file, targetFiles);
					}
				}
			}
		}
	}

	/**
	 * 计算相似度
	 * 
	 * @param fw
	 * @param originFile
	 * @param targetFiles
	 */
	public static void computeSimilarity(FileWriter fw, File originFile, File[] targetFiles) {
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
							float similarity = FingerPrint.getSimilarity(getFinger(originFile), getFinger(targetFile));
							System.out.println("similarity=" + similarity + "  " + originFile + "=" + targetFile);
							if (similarity > 0.93f) {
								hasCheckMap.put(originFile.getAbsolutePath(), Boolean.TRUE);
								similarMap.get(originFile.getParent()).add(targetFile.getParent());
								fw.write(originFile + "=" + targetFile);
								fw.flush();
								return;
							}
						} catch (Exception e) {
							System.out.println();
							System.out.println("fail  " + originFile + "=" + targetFile);
							e.printStackTrace();
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

	public static byte[] getFinger(File imageFile) {
		byte[] bytes = null;
		try {
			String dirPath = imageFile.getParentFile().getAbsolutePath();
			String fingerFileName = dirPath.replaceAll("\\\\", "~");
			fingerFileName = fingerFileName.replaceAll(":", "@");
			Map<String, String> fingerDirMap = fingerMap.get(fingerFileName);
			String finger = null;
			if (null == fingerDirMap) {
				synchronized (FingerPrint.class) {
					File file = new File("E:\\image\\fingers", fingerFileName);
					fingerDirMap = loadFingers(file);
					FileWriter fw = fwMap.get(fingerFileName);
					if (null == fw) {
						fw = new FileWriter(file) {
							@Override
							public void write(String str) throws IOException {
								super.write(str + "\r\n"); // 换行
							}
						};
						fwMap.put(fingerFileName, fw);
					}
					fingerMap.put(fingerFileName, fingerDirMap);
				}
			}
			finger = fingerDirMap.get(imageFile.getName());
			if (StringUtils.isNotBlank(finger)) {
				bytes = ByteUtils.string2Bytes(finger);
			}
			if (null == bytes || bytes.length == 0) {
				bytes = FingerPrint.hashValue(ImageIO.read(imageFile));
				finger = fingerDirMap.get(imageFile.getName());
				if (StringUtils.isBlank(finger)) {
					synchronized (FingerPrint.class) {
						finger = ByteUtils.bytes2String(bytes);
						fingerDirMap.put(imageFile.getName(), finger);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
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
