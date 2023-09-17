package com.wp.study.praxis.image.reptile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtils;
import com.wp.study.base.util.IoUtils;
import com.wp.study.base.util.JsonUtils;
import com.wp.study.praxis.file.FileCommonTools;
import com.wp.study.praxis.image.reptile.adapter.WebsiteAdapterUtils;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;
import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class ImageReptile {
	
	private static String urlFilePath = "E:/photo/page_url.txt";
	private static String fileDirPath = "E:/photo/page_dir";
	private static String downloadedFilePath = "E:/photo/downloaded_info.txt";
	
	private static ExecutorService downloadPool = new ThreadPoolExecutor(15, 15, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(5000));
	private static Map<String, FileWriter> fwMap = new ConcurrentHashMap<String, FileWriter>() {
		private static final long serialVersionUID = -3442731360328676574L;
		{
			try {
				put(downloadedFilePath, new FileWriter(downloadedFilePath, true));
			} catch(Exception e) {
			}
		}
		
	};
	private static Map<String, DownloadDO> downloadedMap = new ConcurrentHashMap<String, DownloadDO>();
	private static Lock lock = new ReentrantLock();
	private static AtomicInteger generateImageUrlSize = new AtomicInteger(0);
	private static AtomicInteger downloadImageUrlSize = new AtomicInteger(0);
	
	/**
	 * 加载文件信息
	 * 
	 * @param dirPath
	 * @return
	 */
	public static File[] loadFileInfo1(String dirPath) {
		if (StringUtils.isBlank(dirPath)) {
			throw new RuntimeException("invalid dir path");
		}
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new RuntimeException("dir not exists, filePath=" + dirPath);
		}
		return dir.listFiles();
	}

	/**
	 * 加载文件信息
	 * 
	 * @param filePath
	 * @return
	 */
	public static Set<String> loadFileInfo0(String filePath) {
		if (StringUtils.isBlank(filePath)) {
			throw new RuntimeException("invalid file path");
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new RuntimeException("file not exists, filePath=" + filePath);
		}
		Set<String> fileInfoSet = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while (null != (line = br.readLine())) {
				if (StringUtils.isNotBlank(line)) {
					fileInfoSet.add(line.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(br);
		}
		return fileInfoSet;
	}

	/**
	 * 下载图片
	 * 
	 * @param albumName
	 * @param downloadUrls
	 */
	public static void downloadImage(List<DownloadDO> downloads) {
		if(null == downloads || downloads.isEmpty()) {
			return;
		}
		FileWriter fw = null;
		List<DownloadDO> nextDownloads = new ArrayList<DownloadDO>();
		try {
			// 下载
			final CountDownLatch latch = new CountDownLatch(downloads.size());
			for(DownloadDO download : downloads) {
				downloadPool.submit(new Runnable() {
					@Override
					public void run() {
						long startTime = System.currentTimeMillis();
						int status = 1;
						try {
							if(download.addTryTimes() > 5) {
								System.out.println("download fail over 5 times, download=" + JsonUtils.convertBeanToJson(download));
								download.setHasDown(true);
								return;
							}
							File dic = new File("E:/photo/" + download.getAlbumName());
							if(!dic.exists()) {
								dic.mkdirs();
							}
							File output = new File(dic, download.getImageName());
							if(!output.exists()) {
								status = HttpUtils.doGetDownload(download.getDownUrl(), output);
								download.setStatus(status);
							}
							// 校验图片有效性
							if(output.exists()) {
								download.setHasDown(FileCommonTools.checkValidAndCut(output));
							}
						} catch(Throwable t) {
							// t.printStackTrace();
						} finally {
							latch.countDown();
							if(!download.isHasDown()) {
								nextDownloads.add(download);
								// 通过浏览器打开无法识别的页面
								/*try {
									Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + download.getDownUrl());
								} catch (Exception e) {
									e.printStackTrace();
								}*/
							}
							int size = downloadImageUrlSize.incrementAndGet();
							download.setWasteTime(System.currentTimeMillis() - startTime);
							presentRateProgress("downloadImage has process: ", downloads.size(), size);
							// 打印已经下载完成的资源
							writeDownloaded(download);
						}
					}
				});
			}
			try {
				latch.await();
			} catch(Throwable e) {
				e.printStackTrace();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		} finally {
			// 关闭输入输出流
			IoUtils.closeQuietly(fw);
		}
		// 遍历下载图片
		downloadImage(nextDownloads);
	}
	
	/**
	 * 生成下载链接
	 * 
	 * @param aUrl
	 * @param imgUrl
	 * @return
	 */
	public static List<DownloadDO> getDownloads(final Map<String, DownloadDO> urlMap) {
		if(null == urlMap || urlMap.isEmpty()) {
			return new ArrayList<DownloadDO>();
		}
		List<DownloadDO> downloads = new ArrayList<DownloadDO>();
		try {
			final CountDownLatch latch = new CountDownLatch(urlMap.size());
			List<Future<DownloadDO>> futures = new ArrayList<Future<DownloadDO>>();
			for(Map.Entry<String, DownloadDO> entry : urlMap.entrySet()) {
				futures.add(downloadPool.submit(new Callable<DownloadDO>() {
					@Override
					public DownloadDO call() {
						DownloadDO download = null;
						try {
							String aUrl = entry.getKey();
							download = entry.getValue();
							if(null != download && StringUtils.isBlank(download.getDownUrl())) {
								download = WebsiteAdapterUtils.getImageUrl(download.getAlbumName(), aUrl, download.getImgUrl());
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							latch.countDown();
							int size = generateImageUrlSize.incrementAndGet();
							presentRateProgress("getDownloads has process: ", urlMap.size(), size);
						}
						return download;
					}
				}));
			}
			try {
				latch.await();
			} catch(Exception e) {
				e.printStackTrace();
			}
			for(Future<DownloadDO> future : futures) {
				DownloadDO download = future.get();
				if (null != download) {
					downloads.add(download);
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return downloads;
	}
	
	public static String getAlbum(String pageUrl) {
		if(null == pageUrl) {
			return null;
		}
		String albumName = null;
		int index = pageUrl.indexOf("&album=");
		if(index > -1) { // 强制设置，直接返回
			return pageUrl.substring(pageUrl.indexOf("&album=") + 7, pageUrl.length());
		}
		if(pageUrl.contains("vipergirls.to")) {
			albumName = pageUrl.substring(pageUrl.indexOf("-") + 1, pageUrl.indexOf("?"));
		}
		return albumName;
	}
	
	/**
	 * 
	 * @param fileDirPath
	 * @param downloadedPath
	 * @param cleanHistory
	 */
	public static void batchReptile1(String fileDirPath, String downloadedPath, boolean cleanHistory) {
		// 加载要下载的主页面地址
		File[] files = loadFileInfo1(fileDirPath);
		if (null == files || files.length == 0) {
			return;
		}
		
		// 加载已经下载的资源信息
		if(!cleanHistory) {
			Set<String> records = loadFileInfo0(downloadedPath);
			for(String record : records) {
				if(StringUtils.isBlank(record)) {
					continue;
				}
				DownloadDO download = JsonUtils.convertJsonToBean(record, DownloadDO.class);
				if(null != download) {
					downloadedMap.put(download.getaUrl(), download);
				}
			}
		}
		
		// 加载实际图片存储的页面地址
		Map<String, DownloadDO> urlMap = new HashMap<String, DownloadDO>();
		for (File file : files) {
			try {
				String pageContent = FileCommonTools.read(file);
				if (StringUtils.isBlank(pageContent)) {
					System.out.println("page content is blank, file=" + file);
					continue;
				}
				
				String albumName = file.getName();
				
				// 生成下载链接
				List<String> eles = XmlFilter.getElements(pageContent, "a");
				for (String ele : eles) {
					// 获取a标签href
					List<String> hrefValues = XmlFilter.getAttributeValues(ele, "href");
					if (null == hrefValues || hrefValues.size() == 0) {
						continue;
					}
					// 获取a->img标签src
					List<String> subImgEles = XmlFilter.getElements(ele, "img");
					if (null == subImgEles || subImgEles.size() == 0) {
						continue;
					}
					List<String> srcValues = XmlFilter.getAttributeValues(subImgEles.get(0), "src");
					if (null == srcValues || srcValues.size() == 0) {
						continue;
					}
					
					String aUrl = hrefValues.get(0);
					String imgUrl = srcValues.get(0);
					if (StringUtils.isBlank(imgUrl)) {
						// System.out.println("input parameter is blank, imgUrl=" + imgUrl);
						continue;
					}
					if (!imgUrl.startsWith("http")) {
						// System.out.println("not support protocol, imgUrl=" + imgUrl);
						continue;
					}
					if (imgUrl.endsWith(".gif") || imgUrl.endsWith(".png")) {
						// System.out.println("not support image format, imgUrl=" + imgUrl);
						continue;
					}
					DownloadDO download = downloadedMap.get(aUrl);
					if(null == download) {
						download = new DownloadDO();
						download.setAlbumName(albumName);
						download.setImgUrl(imgUrl);
					}
					urlMap.put(aUrl, download);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 获取实际图片下载对象
		List<DownloadDO> downloads = getDownloads(urlMap);
		if(null == downloads || downloads.isEmpty()) {
			return;
		}
		// 下载图片
		downloadImage(downloads);
		
		// 关闭各种资源池
		closeSource();
	}
	
	/**
	 * 
	 * @param urlFilePath
	 * @param downloadedPath
	 * @param cleanHistory
	 */
	public static void batchReptile0(String urlFilePath, String downloadedPath, boolean cleanHistory) {
		// 加载要下载的主页面地址
		Set<String> pageUrls = loadFileInfo0(urlFilePath);
		if (null == pageUrls || pageUrls.size() == 0) {
			return;
		}
		
		// 加载已经下载的资源信息
		if(!cleanHistory) {
			Set<String> records = loadFileInfo0(downloadedPath);
			for(String record : records) {
				if(StringUtils.isBlank(record)) {
					continue;
				}
				DownloadDO download = JsonUtils.convertJsonToBean(record, DownloadDO.class);
				if(null != download) {
					downloadedMap.put(download.getaUrl(), download);
				}
			}
		}
		
		// 加载实际图片存储的页面地址
		Map<String, DownloadDO> urlMap = new HashMap<String, DownloadDO>();
		for (String pageUrl : pageUrls) {
			try {
				String pageContent = HttpUtils.doGet(pageUrl, String.class);
				if (StringUtils.isBlank(pageContent)) {
					System.out.println("page content is blank, pageUrl=" + pageUrl);
					continue;
				}
				
				String albumName = getAlbum(pageUrl);
				
				// 生成下载链接
				List<String> eles = XmlFilter.getElements(pageContent, "a");
				for (String ele : eles) {
					// 获取a标签href
					List<String> hrefValues = XmlFilter.getAttributeValues(ele, "href");
					if (null == hrefValues || hrefValues.size() == 0) {
						continue;
					}
					// 获取a->img标签src
					List<String> subImgEles = XmlFilter.getElements(ele, "img");
					if (null == subImgEles || subImgEles.size() == 0) {
						continue;
					}
					List<String> srcValues = XmlFilter.getAttributeValues(subImgEles.get(0), "src");
					if (null == srcValues || srcValues.size() == 0) {
						continue;
					}
					
					String aUrl = hrefValues.get(0);
					String imgUrl = srcValues.get(0);
					if (StringUtils.isBlank(imgUrl)) {
						// System.out.println("input parameter is blank, imgUrl=" + imgUrl);
						continue;
					}
					if (!imgUrl.startsWith("http")) {
						// System.out.println("not support protocol, imgUrl=" + imgUrl);
						continue;
					}
					if (imgUrl.endsWith(".gif") || imgUrl.endsWith(".png")) {
						// System.out.println("not support image format, imgUrl=" + imgUrl);
						continue;
					}
					DownloadDO download = downloadedMap.get(aUrl);
					if(null == download) {
						download = new DownloadDO();
						download.setAlbumName(albumName);
						download.setImgUrl(imgUrl);
					}
					urlMap.put(aUrl, download);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 获取实际图片下载对象
		List<DownloadDO> downloads = getDownloads(urlMap);
		if(null == downloads || downloads.isEmpty()) {
			return;
		}
		// 下载图片
		downloadImage(downloads);
		
		// 关闭各种资源池
		closeSource();
	}
	
	public static void writeDownloaded(DownloadDO download) {
		if(null != download && !download.isHasDown()) {
			write(downloadedFilePath, JsonUtils.convertBeanToJson(download));
		}
	}
	
	private static void write(String fwName, String log) {
		if(null == fwName) {
			return;
		}
		// 加载writer
		FileWriter fw = fwMap.get(fwName);
		if(null == fw) {
			try {
				lock.lock();
				fw = new FileWriter(fwName);
				fwMap.put(fwName, fw);
			} catch (Throwable e) {
				// e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		if(null != fw) {
			try {
				fw.write(log + "\n");
				fw.flush();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void presentRateProgress(String prefix, int totalSize, int currentIndex) {
		double percentD = currentIndex * 100D / totalSize;
		int percentI = (int)percentD;
		if(percentD - percentI < 0.2) {
			System.out.println(prefix + percentI + "%s");
		}
	}
	
	
	private static void closeSource() {
		// 关闭线程池
		try {
			downloadPool.shutdown();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// 关闭filewriter
		for(Map.Entry<String, FileWriter> entry : fwMap.entrySet()) {
			// 关闭输入输出流
			IoUtils.closeQuietly(entry.getValue());
		}
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		batchReptile1(fileDirPath, downloadedFilePath, false);
		System.out.println("wasteTime=" + (System.currentTimeMillis() - startTime));
	}

}

