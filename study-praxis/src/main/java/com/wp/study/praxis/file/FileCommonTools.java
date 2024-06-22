package com.wp.study.praxis.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

//import com.idrsolutions.image.JDeli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.util.ImageUtils;
import com.wp.study.base.util.IoUtils;

public class FileCommonTools {

	private static Logger logger = LoggerFactory.getLogger(FileCommonTools.class);

	private static File invalidPath = new File("E:/photo/invalid");
	private static AtomicInteger checkSize = new AtomicInteger(0);

	/**
	 * 加载当前文件（夹）下所有文件
	 *
	 * @param files
	 * @return
	 */
	public static List<File> loadFiles(File... files) {
		List<File> fileList = new ArrayList<File>();
		if (null == files || files.length == 0) {
			return fileList;
		}
		Stack<File> dirs = new Stack<File>();
		try {
			// 先处理提交的文件or文件夹
			for (File file : files) {
				if (null == file || !file.exists()) {
					continue;
				}
				if (file.isFile()) {
					fileList.add(file);
				} else {
					dirs.push(file);
				}
			}

			// 处理文件夹内容
			File dir = null;
			while (!dirs.isEmpty()) {
				dir = dirs.pop();
				File[] subFiles = dir.listFiles();
				if (null == subFiles || subFiles.length == 0) {
					continue;
				}
				for (File subFile : subFiles) {
					if (null == subFile || !subFile.exists()) {
						continue;
					}
					if (subFile.isFile()) {
						fileList.add(subFile);
					} else {
						dirs.push(subFile);
					}
				}
			}
		} catch (Exception e) {
			logger.error("loadFiles fail, files={}, error:", files, e);
		}
		return fileList;
	}

	/**
	 * 检查文件是否存在
	 *
	 * @param dir  要检查的文件夹
	 * @param info 记录着需要检查文件列表的文件
	 */
	public static void checkExist(File dir, File info) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			logger.error("can not find directory <{}>", dir);
			return;
		}
		if (info == null || !info.exists() || !info.isFile()) {
			logger.error("can not find info file <{}>", info);
			return;
		}

		// 获取所有子文件
		List<File> subFiles = loadFiles(dir);
		if (null == subFiles || subFiles.isEmpty()) {
			return;
		}

		// 从info文件中读取文件列表详情
		BufferedReader br = null;
		// 预读的文件详情，在sortMap中以文件名排序
		Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		// 保存检查结果
		File check = new File(info.getParentFile(), "check.txt");
		FileWriter fw = null;
		try {
			br = new BufferedReader(new FileReader(info));
			String line = null;
			// 从记录文件列表的info中按行加载文件详情
			while ((line = br.readLine()) != null) {
				// 获取文件的类型，以及文件名开始和结尾索引
				int start = -1;
				int end = -1;
				String type = "";
				if ((end = line.indexOf(".zip")) > 0) {
					type = ".zip";
				} else if ((end = line.indexOf(".mp4")) > 0) {
					type = ".mp4";
				} else if ((end = line.indexOf(".wmv")) > 0) {
					type = ".wmv";
				} else {
					continue;
				}
				for (int i = end - 1; 0 <= i; i--) {
					char ch = line.charAt(i);
					if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_'
							|| ch == '-')) {
						start = i;
						break;
					}
				}
				// 获取本地文件格式文件名
				String name = line.substring(start + 1, end);
				String filename = name + type;
				if (!type.equals(".zip")) {
					int index = -1;
					char ch;
					if ((index = filename.indexOf("_mov")) > 0) {
						ch = filename.charAt(index - 1);
						if (ch >= '0' && ch <= '9') {
							name = filename.replaceFirst("mov", "");
						} else {
							name = filename.replaceFirst("_mov", "");
						}
					} else if ((index = filename.indexOf("_mk")) > 0) {
						ch = filename.charAt(index - 1);
						if (ch >= '0' && ch <= '9') {
							name = filename.replaceFirst("mk", "");
						} else {
							name = filename.replaceFirst("_mk", "");
						}
					} else {
						name = filename;
					}
				}
				// 获取文件实际下载地址
				int uriStart = -1;
				int uriEnd = -1;
				String uri = "";
				String uriTemp = "/" + filename;
				uriEnd = line.indexOf(uriTemp);
				if (uriEnd > 0 && (uriStart = line.substring(0, uriEnd).lastIndexOf("http")) >= 0) {
					uriEnd += uriTemp.length();
					// 区别处理下载地址是以.html等形式
					if (uriEnd < line.length() && line.charAt(uriEnd) == '.') {
						for (int i = uriEnd + 1; i < line.length(); i++) {
							char ch = line.charAt(i);
							if (!(ch >= 'a' && ch <= 'z')) {
								uriEnd = i;
								break;
							}
							if (i == line.length() - 1) {
								uriEnd = i + 1;
							}
						}
					}
					uri = line.substring(uriStart, uriEnd);
				} else {
					uri = filename;
				}
				sortMap.put(name, uri);
			}

			// 移除本地已经存在的文件
			for (File subFile : subFiles) {
				if (sortMap.containsKey(subFile.getName())) {
					sortMap.remove(subFile.getName());
				}
			}

			// 打印本地不存在的文件信息
			fw = new FileWriter(check) {
				@Override
				public void write(String str) throws IOException {
					// 换行
					super.write(str + "\r\n");
				}
			};
			for (Map.Entry<String, String> entry : sortMap.entrySet()) {
				fw.write(entry.getKey() + " --> " + entry.getValue());
			}
			fw.flush();
		} catch (Exception e) {
			logger.error("checkExist fail, dir={}, info={}, error:", dir, info, e);
		} finally {
			IoUtils.closeQuietly(br, fw);
		}
	}

	/**
	 * 将源文件复制到指定文件夹 使用缓冲输入输出流实现
	 *
	 * @param origin 源文件
	 * @param path   指定文件夹
	 */
	public static boolean copy0(File origin, File path) {
		boolean result = false;
		if (origin == null || !origin.exists() || !origin.isFile()) {
			logger.error("can not find copy file <{}>", origin);
			return result;
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			// 获取输入流
			bis = new BufferedInputStream(new FileInputStream(origin));
			if (!path.exists()) {
				// 创建目标文件夹，及必要的父文件夹
				path.mkdirs();
			}
			File destination = new File(path, origin.getName());
			destination.createNewFile();
			// 获取输出流
			bos = new BufferedOutputStream(new FileOutputStream(destination));
			byte[] buf = new byte[2048];
			int len = 0;
			// 最后一个byte数组可能不足2048，为了防止写入脏数据，每次写入要指定数据长度
			while ((len = bis.read(buf)) != -1) {
				bos.write(buf, 0, len);
			}

			// 关闭输出流前先刷新
			bos.flush();
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(bis, bos);
		}
		if (!result) {
			logger.error("copy0 fail, dir={}, info={}", origin, path);
		}
		return result;
	}

	/**
	 * 将源文件复制到指定文件夹 使用文件通道（FileChannel）实现
	 *
	 * @param origin 源文件
	 * @param path   指定文件夹
	 */
	public static boolean copy(File origin, File path) {
		boolean result = false;
		if (origin == null || !origin.exists() || !origin.isFile()) {
			logger.error("can not find copy file <{}>", origin);
			return result;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			// 获取输入文件通道
			fis = new FileInputStream(origin);
			in = fis.getChannel();
			if (!path.exists()) {
				// 创建目标文件夹，及必要的父文件夹
				path.mkdirs();
			}
			File destination = new File(path, origin.getName());
			destination.createNewFile();
			// 获取输出文件通道
			fos = new FileOutputStream(destination);
			out = fos.getChannel();
			// 连接两个通道，从in通道读取，然后写入out通道
			in.transferTo(0, in.size(), out);
			// 刷新输入输出流
			fos.flush();
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(fis, in, fos, out);
		}
		if (!result) {
			logger.error("copy fail, origin={}, path={}", origin, path);
		}
		return result;
	}

	/**
	 * 将源文件剪切到指定文件夹
	 *
	 * @param origin 源文件
	 * @param path   指定文件夹
	 */
	public static boolean cut(File origin, File path) {
		boolean result = false;
		// 文件复制
		result = copy(origin, path);
		if (result) {
			// 删除origin文件
			if (origin.delete()) {
				result = true;
			} else {
				logger.error("delete fail, origin={}, path={}", origin, path);
			}
		}
		return result;
	}

	/**
	 * 将校验源文件并剪切无效文件到指定文件夹
	 *
	 * @param origin 源文件
	 */
	public static boolean checkValidAndCut(File origin) {
		boolean valid = false;
		if (null == origin || !origin.exists()) {
			return valid;
		}
		try {
			if (!invalidPath.exists()) {
				invalidPath.mkdirs();
			}
			if (!origin.getPath().matches("^[\\s\\S]*\\.jpg$")) {
				return valid;
			}

			valid = ImageUtils.isValidImage(origin.toURI().toURL(), 500);
			if (!valid) {
				// 文件复制
				valid = cut(origin, invalidPath);
			}
		} catch (Exception e) {
			logger.error("checkValidAndCut fail, origin={}, error:", origin, e);
		}
		return valid;
	}

	/**
	 * 将校验源文件并剪切无效文件到指定文件夹
	 *
	 * @param files 源文件夹
	 */
	public static void checkSubValidAndCut(File... files) {
		if (null == files || files.length == 0) {
			return;
		}
		ExecutorService checkPool = null;
		try {
			// 获取所有子文件
			final List<File> subFiles = loadFiles(files);
			if (null == subFiles || subFiles.isEmpty()) {
				return;
			}

			checkPool = Executors.newFixedThreadPool(15);
			ExecutorCompletionService<Boolean> ecs = new ExecutorCompletionService<Boolean>(checkPool);
			for (final File subFile : subFiles) {
				try {
					ecs.submit(new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return checkValidAndCut(subFile);
						}
					});
				} catch (Throwable e) {
					logger.error("submitTask fail, subFile={}, error:", subFile, e);
				}
				try {
					// 控制任务提交速度
					Thread.sleep(10);
				} catch (Throwable e) {
				}
			}

			// 获取结果
			for (int i = 0; i < subFiles.size(); i++) {
				ecs.take().get();
				int size = checkSize.incrementAndGet();
				System.out.println("checkSubValidAndCut has process: " + (int) (size * 100 / subFiles.size()) + "%s");
			}
		} catch (Exception e) {
			logger.error("checkSubValidAndCut fail, error:", e);
		} finally {
			if (null != checkPool) {
				try {
					checkPool.shutdownNow();
				} catch (Throwable e) {
				}
			}
		}
	}

	/**
	 * 将一组文件合并为指定文件 使用文件通道（FileChannel）实现
	 *
	 * @param mergedFile 合并后文件
	 * @param files      源文件列表
	 */
	public static boolean merge(File mergedFile, File... files) {
		boolean result = false;
		if (mergedFile == null || mergedFile.isDirectory()) {
			logger.warn("merged file is null or is directory");
			return result;
		}
		if (files == null || files.length == 0) {
			logger.warn("orgin files is empty");
			return result;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			if (!mergedFile.exists()) {
				mergedFile.getParentFile().mkdirs();
				mergedFile.createNewFile();
			}
			// 获取输出文件通道
			fos = new FileOutputStream(mergedFile);
			out = fos.getChannel();
			for (File file : files) {
				if (file != null && file.exists() && file.isFile()) {
					try {
						// 获取输入文件通道
						fis = new FileInputStream(file);
						in = fis.getChannel();
						// 连接两个通道，从in通道读取，然后写入out通道
						in.transferTo(0, in.size(), out);
					} catch (Exception e) {
					} finally {
						// 关闭输入输出流
						IoUtils.closeQuietly(fis, in);
					}
				}
			}
			// 刷新输出流
			fos.flush();
			result = true;
		} catch (Exception e) {
			logger.error("merge fail, mergedFile={}, files={}, error:", mergedFile, files, e);
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(fos, out);
		}
		return result;
	}

	public static void write(String filePath, String content) {
		FileWriter fw = null;
		try {
			// 重命名详情输出
			fw = new FileWriter(new File(filePath));
			fw.write(content);
			fw.flush();
		} catch (Exception e) {
			logger.error("write fail, filePath={}, error:", filePath, e);
		} finally {
			IoUtils.closeQuietly(fw);
		}
	}

	public static String read(File file) {
		FileReader fr = null;
		BufferedReader bufr = null;
		StringBuilder sb = new StringBuilder();
		try {
			// 重命名详情输出
			fr = new FileReader(file);
			bufr = new BufferedReader(fr);
			String line = null;
			while ((line = bufr.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (Exception e) {
			logger.error("read fail, file={}, error:", file, e);
		} finally {
			IoUtils.closeQuietly(fr, bufr);
		}
		return sb.toString();
	}

//	public static void toJpg(String heic, String targetJpg) {
//		BufferedImage heicImage = null;
//		try {
//			heicImage = JDeli.read(new File(heic));
//			Thumbnails.of(heicImage).scale(1.0d).outputQuality(1.0d).toFile(targetJpg);
//		} catch (Throwable e) {
//			LOG.error("toJpg fail, error:", e);
//		} finally {
//			try {
//				if (null != heicImage) {
//					heicImage.flush();
//					heicImage = null;
//				}
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//		}
//	}

}
