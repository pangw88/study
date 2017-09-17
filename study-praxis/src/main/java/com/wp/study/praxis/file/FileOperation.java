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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.algorithm.digester.DigesterCoder;
import com.wp.study.base.util.CacheUtil;
import com.wp.study.base.util.IoUtil;

public class FileOperation {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileOperation.class);

	/**
	 * 计算文件（夹）大小
	 * 
	 * @param file
	 * @return
	 */
	public static long getSize(File file) {
		long bytes = 0L;
		if(file == null || !file.exists()) {
			LOG.error("<{}> not exist", file);
			return bytes;
		}
		if (file.isFile()) { // 若为文件
			bytes += file.length();
		} else { // 若为文件夹
			// 获取所有子文件
			List<File> subFiles = loadFiles(file);
			if(null != subFiles) {
				for(File subFile : subFiles) {
					bytes += subFile.length();
				}
			}
		}
		return bytes;
	}
	
	/**
	 * 加载当前文件（夹）下所有文件
	 * 
	 * @param files
	 * @return
	 */
	public static List<File> loadFiles(File... files) {
		List<File> fileList = new ArrayList<File>();
		if(null == files || files.length == 0) {
			return fileList;
		}
		Stack<File> dirs = new Stack<File>();
		try {
			// 先处理提交的文件or文件夹
			for(File file : files) {
				if(null == file || !file.exists()) {
					continue;
				}
				if(file.isFile()) {
					fileList.add(file);
				} else {
					dirs.push(file);
				}
			}
			
			// 处理文件夹内容
			File dir = null;
			while(!dirs.isEmpty()) {
				dir = dirs.pop();
				File[] subFiles = dir.listFiles();
				if(null == subFiles || subFiles.length == 0) {
					continue;
				}
				for(File subFile : subFiles) {
					if(null == subFile || !subFile.exists()) {
						continue;
					}
					if(subFile.isFile()) {
						fileList.add(subFile);
					} else {
						dirs.push(subFile);
					}
				}
			}
		} catch(Exception e) {
			LOG.error("loadFiles fail, files={}, error:", files, e);
		}
		return fileList;
	}
	
	/**
	 * 以父文件夹名称为基准重命名文件
	 * 文件夹名：xxx
	 * 重命名文件：xxx_001.jpg、xxx_002.jpg 
	 * 
	 * @param parent
	 */
	public static void rename(File parent) {
		if(parent == null || !parent.exists() || !parent.isDirectory()) {
			LOG.error("can not find directory <{}>", parent);
			return;
		}
		FileWriter fw = null;
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(parent);
			if(null == subFiles || subFiles.isEmpty()) {
				return;
			}
			
			// 重命名详情输出
			fw = new FileWriter(new File("F:/photo/rename.txt")) {
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
				} else if(path.toLowerCase().
						matches("^[\\s\\S]*\\.(mp4|mkv|avi|wmv|mov|m4v|zip|rar)$")) {
					String name = subFile.getName();
					// .*匹配除换行符外任意长度字符串
					if(name.matches("^.*(_mov|_mk).*$")) {
						String rename = name.replaceAll("_mov", "").
								replaceAll("_mk", "");
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
							if (name1.matches(pattern)
									&& name2.matches(pattern)) {
								int i1 = Integer.valueOf(name1
										.split("\\(")[1].split("\\)")[0]);
								int i2 = Integer.valueOf(name2
										.split("\\(")[1].split("\\)")[0]);
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
				if(null == ai) {
					ai = new AtomicInteger(1);
					indexMap.put(file.getParentFile().getName(), ai);
				}
				String name = file.getName();
				// 文件后缀，形如：'.jpg'
				String suffix = name.lastIndexOf('.') >= 0 ? name
						.substring(name.lastIndexOf('.')).toLowerCase().replace("jpeg", "jpg") : "";
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
		} catch(Exception e) {
			LOG.error(e.getMessage());
		} finally {
			IoUtil.closeQuietly(fw);
		}
	}

	/**
	 * 获取文件（夹）及其子文件的md5值
	 * 
	 * @param files
	 * @param regex
	 *      匹配正则表达式的文件不计算md5值
	 * @param isCut
	 * 		是否剪切MD5值重复的文件
	 * @return
	 */
	public static void getMD5(String regex, boolean isCut, File... files) {
		if(files == null || files.length == 0) {
			LOG.error("files is null");
			return;
		}
		// 定义md5Map存储文件md5和文件path
		Map<String, String> md5Map = new HashMap<String, String>();
		FileWriter fw = null;
		try {
			// 获取所有子文件
			List<File> subFiles = loadFiles(files);
			if(null == subFiles || subFiles.isEmpty()) {
				return;
			}
			
			// md5相同文件列表输出
			fw = new FileWriter(new File("F:/photo/md5.txt")) {
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			
			for(File subFile : subFiles) {
				String filePath = subFile.getPath();
				// 过滤匹配这则表达式的文件
				if(filePath.matches(regex)) {
					LOG.info("<{}> is filtered", filePath);
					continue;
				}
				String md5 = DigesterCoder.getFileDigest(subFile, "MD5");
				if (md5Map.containsKey(md5)) {
					// 记录MD5值相同的文件
					fw.write(md5Map.get(md5) + " === " + filePath
							+ "，md5 = " + md5);
					if(isCut) {
						// 剪切重复文件到磁盘根目录的temp临时文件夹
						int index = filePath.indexOf(File.separator);
						File root = new File(index == -1 ? filePath : 
								filePath.substring(0, index));
						File temp = new File(root, "temp");
						cut(subFile, temp);
					}
				} else {
					md5Map.put(md5, subFile.getPath());
				}
			
			}
		} catch(Exception e) {
			LOG.error(e.getMessage());
		} finally {
			IoUtil.closeQuietly(fw);
		}
	}
	
	/**
	 * 检查文件是否存在
	 * 
	 * @param dir
	 * 		要检查的文件夹
	 * @param info
	 * 		记录着需要检查文件列表的文件
	 */
	public static void checkExist(File dir, File info) {
		if(dir == null || !dir.exists() || !dir.isDirectory()) {
			LOG.error("can not find directory <{}>", dir);
			return;
		}
		if(info == null || !info.exists() || !info.isFile()) {
			LOG.error("can not find info file <{}>", info);
			return;
		}
		
		// 获取所有子文件
		List<File> subFiles = loadFiles(dir);
		if(null == subFiles || subFiles.isEmpty()) {
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
			while((line = br.readLine()) != null) {
				// 获取文件的类型，以及文件名开始和结尾索引
				int start = -1;
				int end = -1;
				String type = "";
				if((end = line.indexOf(".zip")) > 0) {
					type = ".zip";
				} else if((end = line.indexOf(".mp4")) > 0) {
					type = ".mp4";
				} else if((end = line.indexOf(".wmv")) > 0) {
					type = ".wmv";
				} else {
					continue;
				}
				for(int i = end - 1; 0 <= i; i --) {
					char ch = line.charAt(i);
					if(!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') 
							|| (ch >= '0' && ch <= '9') || ch == '_' || ch == '-')) {
						start = i;
						break;
					}
				}
				// 获取本地文件格式文件名
				String name = line.substring(start + 1, end);
				String filename = name + type;
				if(!type.equals(".zip")) {
					int index = -1;
					char ch;
					if((index = filename.indexOf("_mov")) > 0) {
						ch = filename.charAt(index - 1);
						if(ch >= '0' && ch <= '9') {
							name = filename.replaceFirst("mov", "");
						} else {
							name = filename.replaceFirst("_mov", "");
						}
					} else if((index = filename.indexOf("_mk")) > 0) {
						ch = filename.charAt(index - 1);
						if(ch >= '0' && ch <= '9') {
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
				if(uriEnd > 0 && (uriStart = line.substring(0, uriEnd).lastIndexOf("http")) >= 0) {
					uriEnd += uriTemp.length();
					// 区别处理下载地址是以.html等形式
					if(uriEnd < line.length() && line.charAt(uriEnd) == '.') {
						for(int i = uriEnd + 1; i < line.length(); i ++) {
							char ch = line.charAt(i);
							if(!(ch >= 'a' && ch <= 'z')) {
								uriEnd = i;
								break;
							} 
							if(i == line.length() - 1) {
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
			for(File subFile : subFiles) {
				if(sortMap.containsKey(subFile.getName())) {
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
			for(Map.Entry<String, String> entry : sortMap.entrySet()) {
				fw.write(entry.getKey() + " --> " + entry.getValue());
			}
			fw.flush();
		} catch(Exception e) {
			LOG.error(e.getMessage());
		} finally {
			IoUtil.closeQuietly(br, fw);
		}
	}
	
	/**
	 * 将源文件复制到指定文件夹
	 * 使用缓冲输入输出流实现
	 * 
	 * @param origin
	 * 		源文件
	 * @param path
	 * 		指定文件夹
	 */
	public static boolean copy0(File origin, File path) {
		boolean result = false;
		if(origin == null || !origin.exists() || !origin.isFile()) {
			LOG.error("can not find copy file <{}>", origin);
			return result;
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			// 获取输入流
			bis = new BufferedInputStream(new FileInputStream(origin));
			if(!path.exists()) {
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
			while((len = bis.read(buf)) != -1) {
				bos.write(buf, 0, len);
			}
			
			// 关闭输出流前先刷新
			bos.flush();
			result = true;
		} catch(Exception e) {
			LOG.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtil.closeQuietly(bis, bos);
		}
		if(!result) {
			LOG.error("<{}> copy to directory <{}> failed", origin, path);
		}
		return result;
	}
	
	/**
	 * 将源文件复制到指定文件夹
	 * 使用文件通道（FileChannel）实现
	 * 
	 * @param origin
	 * 		源文件
	 * @param path
	 * 		指定文件夹
	 */
	public static boolean copy(File origin, File path) {
		boolean result = false;
		if(origin == null || !origin.exists() || !origin.isFile()) {
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
			if(!path.exists()) {
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
		} catch(Exception e) {
			LOG.error(e.getMessage());
		} finally {
			// 关闭输入输出流
			IoUtil.closeQuietly(fis, in, fos, out);
		}
		if(!result) {
			LOG.error("<{}> copy to directory <{}> failed", origin, path);
		}
		return result;
	}
	
	/**
	 * 将源文件剪切到指定文件夹
	 * 
	 * @param origin
	 * 		源文件
	 * @param path
	 * 		指定文件夹
	 */
	public static boolean cut(File origin, File path) {
		boolean result = false;
		// 文件复制
		result = copy(origin, path);
		if(result) {
			// 删除origin文件
			if(origin.delete()) {
				result = true;
			} else {
				LOG.error("<{}> delete failed", origin);
			}
		}
		return result;
	}
	
	/**
	 * 获取系统中匹配表达式的所有文件
	 * 
	 * @param regex
	 * 		正则表达式
	 * @param ignoreDir
	 * 		是否忽略文件夹，true：忽略，false：保留
	 * @return
	 */
	public static List<File> search(String regex, boolean ignoreDir) {
		List<File> matches = new ArrayList<File>();
        // 获取系统盘符
		File[] roots = File.listRoots();
		if(roots != null && roots.length > 0) {
			FileSystemView fsv = FileSystemView.getFileSystemView();
			for(File root : roots) {
				if(!fsv.isFileSystemRoot(root) || root.isFile()) {
					continue;
				}

				List<File> tempDirs = new ArrayList<File>();
				tempDirs.add(root);
				while(tempDirs.size() > 0) {
					// 检验当前文件夹是否匹配
					File curDir = tempDirs.get(0);
					tempDirs.remove(0);
					if(curDir.getName().matches(regex) && !ignoreDir) {
						matches.add(curDir);
					}
					// 检验
					File[] files = curDir.listFiles();
					if(null == files || files.length == 0) {
						continue;
					}
					for(File file : files) {
						if(file.isDirectory()) {
							tempDirs.add(file);
							continue;
						}
						if(file.getName().matches(regex)) {
							matches.add(file);
						}
					}
				}
			}
        }
		return matches;
	}
	
	/**
	 * 执行子目录内容的压缩
	 * 
	 * @param winrar
	 * @param dir
	 * @param password
	 * @param layer
	 *     压缩文件夹在当前目录层级数
	 */
	public static void compress(File winrar, File dir, String password, int layer) {
		if(null == dir || !dir.exists() || !dir.isDirectory()) {
			LOG.error("can not find directory <{}>", dir);
		}

		List<File> parents = new ArrayList<File>();
		List<File> childs = null;
		parents.add(dir);
		for(int i = 0; i < layer; i ++) {
			childs = new ArrayList<File>();
			if(null == parents || parents.isEmpty()) {
				continue;
			}
			for(File parent : parents) {
				if(!parent.isDirectory()) {
					continue;
				}
				FileSystemView fsv = FileSystemView.getFileSystemView();
				// 系统顶级目录C:\，D:\...是隐藏属性
				if(fsv.isFileSystemRoot(parent) || !parent.isHidden()) {
					File[] files = parent.listFiles();
					if(files != null && files.length != 0) {
						for(File file : files) {
							childs.add(file);
						}
					}
				}
			}
			parents = childs;
		}
		
		if(childs != null && !childs.isEmpty()) {
			for(File comp : childs) {
				compress(winrar, comp, password);
			}
		}
	}
	
	/**
	 * 压缩文件（夹）到同级目录，文件以rar标准格式压缩，文件夹以zip标准格式压缩
	 * rar <命令> -<参数 1> -<参数 N> <压缩文件> <文件...> <@列表文件...> <解压路径\>
	 * <命令>
  	 * 		a：添加文件到压缩文件
  	 * <参数>
  	 * 		hp[password]：加密文件数据和文件头
  	 * 		m<0..5>：设置压缩级别(0-存储...3-默认...5-最大)
  	 * 
	 * @param origin
	 * 		需压缩文件
	 * @param password
	 * 		压缩密码，为空时无密码压缩
	 * @return
	 */
	public static boolean compress(File winrar, File origin, String password) {
		boolean result = false;
		if(origin.getName().matches("^[\\s\\S]*\\.(rar|zip|7z)$")) {
			LOG.error("<{}> has been compress file", origin);
			return result;
		}
		
		// 获取压缩文件路径（与源文件同级目录）
		String oriFileName = origin.getName();
		String compFileName = null;
		if(origin.isDirectory()) {
			compFileName = oriFileName + ".zip";
		} else {
			int separator = oriFileName.lastIndexOf(".");
			if(separator != -1) {
				compFileName = oriFileName.substring(0, separator) + ".rar";
			} else {
				compFileName = oriFileName + ".rar";
			}
		}
		File compFile = new File(origin.getParentFile(), compFileName);
		
		// 压缩文件已存在，退出程序
		if(compFile.exists()) {
			LOG.error("<{}> has existed", compFile);
			return result;
		}
		
		// 获取WinRAR压缩程序
		if(null == winrar || !winrar.exists()) {
			if(CacheUtil.exists("winrar")) {
				winrar = CacheUtil.getCache("winrar", File.class);
			} else {
				String winrarName = "WinRAR\\.exe";
				List<File> programs = search(winrarName, true);
				if(programs == null || programs.size() != 1) {
					LOG.error("get compress program <{}> failed", winrarName);
					return result;
				}
				winrar = programs.get(0);
				CacheUtil.setCache("winrar", programs.get(0));
			}
		} else {
			// 缓存WinRAR
			CacheUtil.setCache("winrar", winrar);
		}
		
		// -m3采用标准方式压缩文件
		StringBuffer cmd = new StringBuffer(winrar.getPath());
		cmd.append(" a -m3");
		
		/* 压缩时不保留父级目录结构
		 * ep：完全排除目录路径
		 * ep1：排除父级及以上目录路径
		 * ep2：保留完整目录路径
		 * ep3：保留包含盘符完整目录路径
		 */
		cmd.append(" -ep1 ");
		
		// hp开关加密文件数据、文件名、大小、属性、注释等所有可感知压缩文件区域
		if(StringUtils.isNotBlank(password)) {
			cmd.append(" -hp").append(password);
		}
		
		try {
			// 添加压缩文件和源文件路径
			cmd.append(" ").append(compFile.getPath()).
					append(" ").append(origin.getPath());
			// 调用winrar程序进行文件压缩
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (proc.waitFor() == 0) { // 进程正常结束标志
            	if (proc.exitValue() == 0) { // 子进程正常结束标志
            		LOG.info("succeed in executing command <{}>", cmd);
            		result = true;
            	}
            }
		} catch (Exception e) {
			LOG.error("<{}> compress failed, error <{}>", origin, e.getMessage());
		}
		return result;
	}
	
	/**
	 * 解压文件（夹）到同级目录
	 * rar <命令> -<参数 1> -<参数 N> <压缩文件> <文件...> <@列表文件...> <解压路径\>
	 * <命令>
	 * 		e：提取文件无需压缩文件的路径
	 * 		l[t[a],b]：列出压缩文件内容 [technical[all], bare]
  	 * 		x：以完整路径提取文件
  	 * <参数>
  	 * 		hp[password]：加密文件数据和文件头
  	 * 		m<0..5>：设置压缩级别(0-存储...3-默认...5-最大)
	 * 
	 * @param origin
	 * 		需解压文件
	 * @param password
	 * 		解压密码，为空时无密码解压
	 * @return
	 */
	public static boolean uncompress(File origin, String password) {
		boolean result = false;
		if(!origin.getName().matches("^[\\s\\S]*\\.(rar|zip|7z)$")) {
			LOG.error("<{}> is not compress file", origin);
			return result;
		}
		
		// 获取解压文件程序
		File winrar = null;
		if(CacheUtil.exists("winrar")) {
			winrar = CacheUtil.getCache("winrar", File.class);
		} else {
			String winrarName = "WinRAR\\.exe";
			List<File> programs = search(winrarName, true);
			if(programs == null || programs.size() != 1) {
				LOG.error("get compress program <{}> failed", winrarName);
				return result;
			}
			winrar = programs.get(0);
			CacheUtil.setCache("winrar", programs.get(0));
		}
		
		// x提取压缩文件中完整路径，-m3标准方式解压
		StringBuffer cmd = new StringBuffer(winrar.getPath());
		cmd.append(" x -m3");
		if(password != null) {
			// hp开关加密文件数据、文件名、大小、属性、注释等所有可感知压缩文件区域
			cmd.append(" -hp").append(password);
		}
		
		// 获取解压目录（与源文件同级目录）
		File uncompDir = origin.getParentFile();
		try {
			// 添加源文件路径和压缩目录
			cmd.append(" ").append(origin.getPath()).
					append(" ").append(uncompDir.getPath());
			// 调用winrar程序进行文件解压
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (proc.waitFor() == 0) { // 进程正常结束标志
            	if (proc.exitValue() == 0) { // 子进程正常结束标志
            		LOG.info("succeed in executing command <{}>", cmd);
            		result = true;
            	}
            }
		} catch (Exception e) {
			LOG.error("<{}> uncompress failed, error <{}>", origin, e.getMessage());
		}
		return result;
	}
	
	/**
	 * 将一组文件合并为指定文件
	 * 使用文件通道（FileChannel）实现
	 * 
	 * @param mergedFile
	 * 		合并后文件
	 * @param files
	 * 		源文件列表
	 */
	public static boolean merge(File mergedFile, File... files) {
		boolean result = false;
		if (mergedFile == null || mergedFile.isDirectory()) {
			LOG.warn("merged file is null or is directory");
			return result;
		}
		if(files == null || files.length == 0) {
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
				if(file != null && file.exists() && file.isFile()) {
					try {
						// 获取输入文件通道
						fis = new FileInputStream(file);
						in = fis.getChannel();
						// 连接两个通道，从in通道读取，然后写入out通道
						in.transferTo(0, in.size(), out);
					} catch(Exception e) {
					} finally {
						// 关闭输入输出流
						IoUtil.closeQuietly(fis, in);
					}
				}
			}
			// 刷新输出流
			fos.flush();
			result = true;
		} catch(Exception e) {
			LOG.error("merge file fail, e:", e);
		} finally {
			// 关闭输入输出流
			IoUtil.closeQuietly(fos, out);
		}
		return result;
	}
	
}

