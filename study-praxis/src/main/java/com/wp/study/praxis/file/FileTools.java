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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

//import com.idrsolutions.image.JDeli;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.digester.DigesterCoder;
import com.wp.study.base.util.ImageUtils;
import com.wp.study.base.util.IoUtils;

public class FileTools {

	private static final Logger LOG = LoggerFactory.getLogger(FileTools.class);

	public static File invalidPath = new File("E:/photo/invalid");
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
			LOG.error("loadFiles fail, files={}, error:", files, e);
		}
		return fileList;
	}

	/**
	 * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
	 *
	 * @param parent
	 */
	public static void rename(File parent) {
		if (parent == null || !parent.exists() || !parent.isDirectory()) {
			LOG.error("can not find directory <{}>", parent);
			return;
		}
		FileWriter fw = null;
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(parent);
			if (null == subFiles || subFiles.isEmpty()) {
				return;
			}

			// 重命名详情输出
			fw = new FileWriter(new File("E:/photo/rename.txt")) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};

			// 过滤有效文件
			List<File> files = new ArrayList<File>();
			for (File subFile : subFiles) {
				String path = subFile.getPath();
				if (path.matches("^[\\s\\S]*\\.ini$")) {
					LOG.info("exist .ini file <{}>", path);
					subFile.delete();
				} else if (path.matches("^[\\s\\S]*\\.db$")) { // \s匹配任意的空白符，\S则是匹配任意非空白符的字符
					// 过滤数据库缓存文件
					LOG.info("exist .db file <{}>", path);
				} else if (path.toLowerCase().matches("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov|m4v|zip|rar)$")) {
					String name = subFile.getName();
					// .*匹配除换行符外任意长度字符串
					if (name.matches("^.*(_mov|_mk).*$")) {
						String rename = name.replaceAll("_mov", "").replaceAll("_mk", "");
						subFile.renameTo(new File(subFile.getParentFile(), rename));
						fw.write(name + " rename to " + rename);
					} else {
						// 过滤视频文件
						LOG.info("ignore video file <{}>", path);
					}
				} else { // 非数据库、视频文件处理
					files.add(new File(path) {
						// 重写File类的compareTo方法
						private static final long serialVersionUID = 122810055536327561L;

						@Override
						public int compareTo(File pathname) {
							String name1 = this.getName();
							String name2 = pathname.getName();
							String pattern = "^.*\\(\\d{1,}\\).*$";
							if (name1.matches(pattern) && name2.matches(pattern)) {
								int i1 = Integer.valueOf(name1.split("\\(")[1].split("\\)")[0]);
								int i2 = Integer.valueOf(name2.split("\\(")[1].split("\\)")[0]);
								return i1 - i2;
							}
							return super.compareTo(pathname);
						}
					});
				}
			}

			// 文件按名称进行排序
			Collections.sort(files);
			Map<String, AtomicInteger> indexMap = new HashMap<String, AtomicInteger>();
			// 文件以文件夹为基础进行重命名
			for (File file : files) {
				AtomicInteger ai = indexMap.get(file.getParentFile().getName());
				if (null == ai) {
					ai = new AtomicInteger(1);
					indexMap.put(file.getParentFile().getName(), ai);
				}
				String name = file.getName();
				// 文件后缀，形如：'.jpg'
				String suffix = name.lastIndexOf('.') >= 0
						? name.substring(name.lastIndexOf('.')).toLowerCase().replace("jpeg", "jpg")
						: "";
				// 当前文件的重命名序号
				String num = String.valueOf(ai.getAndIncrement());
				StringBuffer sb = new StringBuffer(file.getParentFile().getName() + "_");
				// 图片数量不超过1000，故最多补2个"0"
				for (int j = 0; j < 3 - num.length(); j++) {
					sb.append("0");
				}
				String rename = sb.append(num).append(suffix).toString();
				if (!rename.equals(name)) {
					file.renameTo(new File(file.getParentFile(), rename));
					fw.write(name + " rename to " + rename);
				}
			}
			fw.flush();
		} catch (Exception e) {
			LOG.error("rename fail, parent={}, error:", parent, e);
		} finally {
			IoUtils.closeQuietly(fw);
		}
	}

	/**
	 * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
	 *
	 * @param baseDir
	 * @param targetDir
	 *
	 */
	public static void replaceName(File baseDir, File targetDir) {
		if (null == baseDir || null == targetDir || !baseDir.exists() || !targetDir.exists()) {
			LOG.error("can not find baseDir <{}>, targetDir <{}>", baseDir, targetDir);
			return;
		}
		if (!baseDir.isDirectory() || !targetDir.isDirectory() || baseDir.list().length != targetDir.list().length) {
			LOG.error("can not find baseDir length <{}>, targetDir length <{}>", baseDir.list().length,
					targetDir.list().length);
			return;
		}
		try {
			// 过滤有效文件
			List<File> originFiles = new ArrayList<File>();
			// 获取所有子文件
			for (File file : baseDir.listFiles()) {
				originFiles.add(new File(file.getAbsolutePath()) {
					// 重写File类的compareTo方法
					private static final long serialVersionUID = 122810055536327561L;

					@Override
					public int compareTo(File pathname) {
						String name1 = this.getName().substring(0, this.getName().indexOf("."));
						String name2 = pathname.getName().substring(0, pathname.getName().indexOf("."));
						int len = name1.length() > name2.length() ? name2.length() : name1.length();
						int compare = name1.substring(0, len).compareTo(name2.substring(0, len));
						if (compare == 0 && name1.length() != name2.length()) {
							compare = name1.length() > name2.length() ? 1 : -1;
						}
						return compare;
					}
				});
			}
			// 文件按名称进行排序
			Collections.sort(originFiles);
			// 过滤有效文件
			List<File> targetFiles = new ArrayList<File>();
			// 获取所有子文件
			for (File file : targetDir.listFiles()) {
				targetFiles.add(new File(file.getAbsolutePath()) {
					private static final long serialVersionUID = 1L;

					// 重写File类的compareTo方法
					@Override
					public int compareTo(File pathname) {
						String name1 = this.getName();
						String name2 = pathname.getName();
						String pattern = "^.*\\(\\d{1,}\\).*$";
						if (name1.matches(pattern) && name2.matches(pattern)) {
							int i1 = Integer.valueOf(name1.split("\\(")[1].split("\\)")[0]);
							int i2 = Integer.valueOf(name2.split("\\(")[1].split("\\)")[0]);
							return i1 - i2;
						}
						return super.compareTo(pathname);
					}
				});
			}
			// 文件按名称进行排序
			Collections.sort(targetFiles);

			// 文件以文件夹为基础进行重命名
			for (int i = 0; i < targetFiles.size(); i++) {
				File ori = originFiles.get(i);
				File tar = targetFiles.get(i);
				System.out.println(ori.getName() + "==" + tar.getName());
				tar.renameTo(new File(tar.getParentFile(), ori.getName()));
			}
		} catch (Exception e) {
			LOG.error("rename fail, originDir={}, targetDir={}, error:", baseDir, targetDir, e);
		}
	}

	/**
	 * 获取文件（夹）及其子文件的md5值
	 *
	 * @param files
	 * @param regex 匹配正则表达式的文件不计算md5值
	 * @param isCut 是否剪切MD5值重复的文件
	 * @return
	 */
	public static void getMD5(String regex, boolean isCut, File... files) {
		if (files == null || files.length == 0) {
			LOG.error("files is null");
			return;
		}
		// 定义md5Map存储文件md5和文件path
		Map<String, String> md5Map = new HashMap<String, String>();
		FileWriter fw = null;
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(files);
			if (null == subFiles || subFiles.isEmpty()) {
				return;
			}

			// md5相同文件列表输出
			fw = new FileWriter(new File("E:/photo/md5.txt")) {
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};

			for (File subFile : subFiles) {
				String filePath = subFile.getPath();
				// 过滤匹配这则表达式的文件
				if (filePath.matches(regex)) {
					LOG.info("<{}> is filtered", filePath);
					continue;
				}
				String md5 = DigesterCoder.getFileDigest(subFile, "MD5");
				if (md5Map.containsKey(md5)) {
					// 记录MD5值相同的文件
					fw.write(md5Map.get(md5) + " === " + filePath + "，md5 = " + md5);
					if (isCut) {
						// 剪切重复文件到磁盘根目录的temp临时文件夹
						int index = filePath.indexOf(File.separator);
						File root = new File(index == -1 ? filePath : filePath.substring(0, index));
						File temp = new File(root, "temp");
						cut(subFile, temp);
					}
				} else {
					md5Map.put(md5, subFile.getPath());
				}

			}
		} catch (Exception e) {
			LOG.error("getMD5 fail, regex={}, isCut={}, error:", regex, isCut, e);
		} finally {
			IoUtils.closeQuietly(fw);
		}
	}

	/**
	 * 检查文件是否存在
	 *
	 * @param dir  要检查的文件夹
	 * @param info 记录着需要检查文件列表的文件
	 */
	public static void checkExist(File dir, File info) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			LOG.error("can not find directory <{}>", dir);
			return;
		}
		if (info == null || !info.exists() || !info.isFile()) {
			LOG.error("can not find info file <{}>", info);
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
			LOG.error("checkExist fail, dir={}, info={}, error:", dir, info, e);
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
			LOG.error("can not find copy file <{}>", origin);
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
			LOG.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(bis, bos);
		}
		if (!result) {
			LOG.error("copy0 fail, dir={}, info={}", origin, path);
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
			LOG.error("can not find copy file <{}>", origin);
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
			LOG.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(fis, in, fos, out);
		}
		if (!result) {
			LOG.error("copy fail, origin={}, path={}", origin, path);
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
				LOG.error("delete fail, origin={}, path={}", origin, path);
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
			LOG.error("checkValidAndCut fail, origin={}, error:", origin, e);
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
					LOG.error("submitTask fail, subFile={}, error:", subFile, e);
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
			LOG.error("checkSubValidAndCut fail, error:", e);
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
			LOG.warn("merged file is null or is directory");
			return result;
		}
		if (files == null || files.length == 0) {
			LOG.warn("orgin files is empty");
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
			LOG.error("merge fail, mergedFile={}, files={}, error:", mergedFile, files, e);
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
			LOG.error("write fail, filePath={}, error:", filePath, e);
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
			LOG.error("read fail, file={}, error:", file, e);
		} finally {
			IoUtils.closeQuietly(fr, bufr);
		}
		return sb.toString();
	}

	/**
	 * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
	 *
	 * @param parent
	 */
	public static void renameLivp2ZipAndUncompress(File parent) {
		if (parent == null || !parent.exists() || !parent.isDirectory()) {
			LOG.error("can not find directory <{}>", parent);
			return;
		}
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(parent);
			if (null == subFiles || subFiles.isEmpty()) {
				return;
			}
			// 过滤有效文件
			for (File subFile : subFiles) {
				String path = subFile.getPath();
				if (path.toLowerCase().endsWith(".livp")) {
					String name = subFile.getName();
					String rename = name.replaceAll(".livp", ".zip");
					rename = rename.replaceAll(" ", "-");
					subFile.renameTo(new File(subFile.getParentFile(), rename));
					WinRarTools.uncompress(new File(subFile.getParentFile(), rename), null);
				} else if (path.toLowerCase().endsWith(".zip")) {
					WinRarTools.uncompress(subFile, null);
				}
			}
		} catch (Exception e) {
			LOG.error("renameLivp2Zip fail, parent={}, error:", parent, e);
		}
	}

	/**
	 * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
	 *
	 * @param dir
	 */
	public static void replaceRename(File dir, String keyStr, String replaceStr, String fileType) {
		replaceRename(dir, keyStr, replaceStr, fileType, true);
	}

	/**
	 * 以父文件夹名称为基准重命名文件 文件夹名：xxx 重命名文件：xxx_001.jpg、xxx_002.jpg
	 * readSystemFileTime 读取系统文件时间
	 *
	 * @param dir
	 */
	public static void replaceRename(File dir, String keyStr, String replaceStr, String fileType, boolean readSystemFileTime) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			LOG.error("can not find directory <{}>", dir);
			return;
		}
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(dir);
			if (null == subFiles || subFiles.isEmpty()) {
				return;
			}
			// 过滤有效文件
			for (File subFile : subFiles) {
				String path = subFile.getPath();
				Map<String,Object> attributes = Files.readAttributes(Paths.get(path), "*", LinkOption.NOFOLLOW_LINKS);
				FileTime lastModifiedTime = (FileTime) attributes.get("lastModifiedTime");

				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lastModifiedTime.toMillis()));

				int month = cal.get(Calendar.MONTH) + 1;
				int day = cal.get(Calendar.DAY_OF_MONTH);
				int hours = cal.get(Calendar.HOUR_OF_DAY);
				int minutes = cal.get(Calendar.MINUTE);
				int seconds = cal.get(Calendar.SECOND);
				String monthDay = (month<10?"0":"") + (month*100 + day);
				String time = (hours<10?"0":"") + (hours*10000 + minutes*100 + seconds);
				if (path.indexOf(keyStr) >= 0) {
					String name = subFile.getName();
					String rename = null;
					if (readSystemFileTime) {
						rename = replaceStr + monthDay + "_" + time + (StringUtils.isBlank(fileType) ? "" : fileType);
					} else {
						rename = name.replaceAll(keyStr, replaceStr);
					}
					subFile.renameTo(new File(subFile.getParentFile(), rename));
				}
			}
		} catch (Exception e) {
			LOG.error("replaceRename fail, dir={}, error:", dir, e);
		}
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
