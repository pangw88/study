package com.wp.study.praxis.image.reptile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtil;
import com.wp.study.base.util.JsonUtil;
import com.wp.study.praxis.image.reptile.adapter.WebsiteAdapterUtils;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;
import com.wp.study.praxis.image.reptile.model.DownloadDO;

public class ImageReptile {
	
	private static String urlFilePath = "F:/photo/page_url.txt";
	private static String downloadedFileName = "downloaded_info";
	private static String downloadedFilePath = "F:/photo/downloaded_info.txt";
	
	private static ExecutorService downloadPool = new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000));
	private static Map<String, FileWriter> fwMap = new ConcurrentHashMap<String, FileWriter>() {
		private static final long serialVersionUID = -3442731360328676574L;
		{
			try {
				put(downloadedFileName, new FileWriter(new File(downloadedFilePath)));
			} catch(Exception e) {
			}
		}
		
	};
	private static Map<String, DownloadDO> downloadedMap = new ConcurrentHashMap<String, DownloadDO>();
	private static Lock lock = new ReentrantLock();
	

	/**
	 * 加载文件信息
	 * 
	 * @param filePath
	 * @return
	 */
	public static Set<String> loadFileInfo(String filePath) {
		Set<String> fileInfoSet = new HashSet<String>();
		if (StringUtils.isBlank(filePath)) {
			throw new RuntimeException("invalid file path");
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new RuntimeException("file not exists, filePath=" + filePath);
		}
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
			if (null != null) {
				try {
					br.close();
				} catch (Exception e2) {
				}
			}
		}
		return fileInfoSet;
	}

	/**
	 * 下载图片
	 * 
	 * @param albumName
	 * @param downloadUrls
	 */
	public static void downloadPic(List<DownloadDO> downloads) {
		FileWriter fw = null;
		if(null == downloads || downloads.isEmpty()) {
			return;
		}
		try {
			// 下载
			final CountDownLatch latch = new CountDownLatch(downloads.size());
			List<Future<?>> futures = new ArrayList<Future<?>>(downloads.size());
			String format = "picName=%s downUrl=%s aUrl=%s status=%s waste=%s";
			for(DownloadDO download : downloads) {
				futures.add(downloadPool.submit(new Runnable() {
					@Override
					public void run() {
						long startTime = System.currentTimeMillis();
						int status = 1;
						File output = null;
						try {
							File dic = new File("F:/photo/" + download.getAlbumName());
							if(!dic.exists()) {
								dic.mkdirs();
							}
							output = new File(dic, download.getPicName());
							if(!output.exists()) {
								status = HttpUtil.doGetDownload(download.getDownUrl(), output);
								if(output.exists()) {
									BufferedImage bi = ImageIO.read(output);
									if(bi.getHeight() < 500 || bi.getWidth() < 500) {
										output.delete();
									} else {
										download.setHasDown(true);
									}
									bi.flush();
								}
							} else {
								download.setHasDown(true);
								System.out.println("has download, aUrl=" + download.getaUrl());
							}
							downloadedMap.put(download.getaUrl(), download);
						} catch(Throwable t) {
							t.printStackTrace();
						} finally {
							latch.countDown();
							if(null == output || !output.exists()) {
								try {
									// 通过浏览器打开无法识别的页面
									Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + download.getDownUrl());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							System.out.println(String.format(format, download.getPicName(), download.getDownUrl(), 
									download.getaUrl(), status, System.currentTimeMillis() - startTime));
						}
					}
				}));
			}
			try {
				latch.await();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(null != fw) {
				try {
					fw.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 生成下载链接
	 * 
	 * @param aUrl
	 * @param imgUrl
	 * @return
	 */
	public static List<DownloadDO> getDownloadInfo(Map<String, DownloadDO> urlMap) {
		long startTime = System.currentTimeMillis();
		List<DownloadDO> downloads = new ArrayList<DownloadDO>();
		if(null == urlMap || urlMap.isEmpty()) {
			return downloads;
		}
		try {
			final CountDownLatch latch = new CountDownLatch(urlMap.size());
			List<Future<DownloadDO>> futures = new ArrayList<Future<DownloadDO>>(urlMap.size());
			for(Map.Entry<String, DownloadDO> entry : urlMap.entrySet()) {
				futures.add(downloadPool.submit(new Callable<DownloadDO>() {
					@Override
					public DownloadDO call() {
						DownloadDO download = null;
						try {
							String aUrl = entry.getKey();
							if(null != entry.getValue()) {
								download = entry.getValue();
							} else {
								download = WebsiteAdapterUtils.getPicUrl(aUrl);
							}
							
							/*if (imgUrl.startsWith("https://img.yt") || imgUrl.startsWith("http://img.yt")
									|| imgUrl.startsWith("http://imgcandy.net")) {
								downLink = imgUrl.replace("small", "big");
								if(imgUrl.contains("img.yt/upload")) {
									downLink = downLink.replace("img.yt/upload", "x001.img.yt");
								}
							} else if (imgUrl.startsWith("http://chronos.to")) {
								downLink = imgUrl.replace("/t/", "/i/");
							} else if (imgUrl.startsWith("http://pic-maniac.com")) {
								downLink = imgUrl.substring(0, imgUrl.lastIndexOf("/") + 1);
								downLink += aUrl.substring(aUrl.lastIndexOf("/") + 1);
								downLink += imgUrl.substring(imgUrl.lastIndexOf("."));
							} else if (aUrl.contains("imagetwist.com")) {
								downLink = imgUrl.replace("/th/", "/i/") + "/photo_001.jpg";
							}*/
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							latch.countDown();
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
			for(Future<DownloadDO> f : futures) {
				if (null != f.get()) {
					downloads.add(f.get());
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			System.out.println("getDownloadInfo wasteTime=" + (System.currentTimeMillis() - startTime));
		}
		return downloads;
	}
	
	/**
	 * 
	 * @param urlFilePath
	 */
	public static void batchReptile(String urlFilePath, String downloadedPath, boolean cleanHistory) {
		// 加载要下载的主页面地址
		Set<String> pageUrls = loadFileInfo(urlFilePath);
		if (null == pageUrls || pageUrls.size() == 0) {
			return;
		}
		
		// 加载已经下载的资源信息
		if(!cleanHistory) {
			Set<String> records = loadFileInfo(downloadedPath);
			for(String record : records) {
				if(StringUtils.isBlank(record)) {
					continue;
				}
				DownloadDO download = JsonUtil.convertJsonToBean(record, DownloadDO.class);
				if(null != download) {
					downloadedMap.put(download.getaUrl(), download);
				}
			}
		}
		
		// 加载实际图片存储的页面地址
		Map<String, DownloadDO> urlMap = new HashMap<String, DownloadDO>();
		for (String pageUrl : pageUrls) {
			try {
				String pageContent = HttpUtil.doGet(pageUrl, String.class);
				if (StringUtils.isBlank(pageContent)) {
					System.out.println("page content is blank, pageUrl=" + pageUrl);
					continue;
				}
				
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
						System.out.println("input parameter is blank, imgUrl=" + imgUrl);
						continue;
					}
					if (!imgUrl.startsWith("http")) {
						System.out.println("not support protocol, imgUrl=" + imgUrl);
						continue;
					}
					if (imgUrl.endsWith(".gif") || imgUrl.endsWith(".png")) {
						System.out.println("not support image format, imgUrl=" + imgUrl);
						continue;
					}
					urlMap.put(aUrl, downloadedMap.get(aUrl));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 获取实际图片下载对象
		List<DownloadDO> downloads = getDownloadInfo(urlMap);
		if(null == downloads || downloads.isEmpty()) {
			return;
		}
		// 下载图片
		downloadPic(downloads);
		
		// 打印已经下载完成的资源
		for(Map.Entry<String, DownloadDO> entry : downloadedMap.entrySet()) {
			write(downloadedFileName, JsonUtil.convertBeanToJson(entry.getValue()));
		}
		
		// 关闭各种资源池
		closeSource();
	}
	
	private static void write(String fwName, String log) {
		// 加载writer
		FileWriter fw = null;
		if(null == fwName) {
			return;
		}
		fw = fwMap.get(fwName);
		if(null == fw) {
			try {
				lock.lock();
				fw = new FileWriter("F:/photo/" + fwName + "/" + fwName);
				fwMap.put(fwName, fw);
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		if(null != fw) {
			try {
				fw.write(log);
				fw.write("\n");
				fw.flush();
			} catch (Throwable e) {
				e.printStackTrace();
			}
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
			try {
				entry.getValue().close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		batchReptile(urlFilePath, downloadedFilePath, false);
		System.out.println("wasteTime=" + (System.currentTimeMillis() - startTime));
	}

}

