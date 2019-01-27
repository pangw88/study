package com.wp.study.praxis.image.reptile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
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

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtils;
import com.wp.study.base.util.IoUtils;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;

public class ImageReptile2 {
	
	private static String downloadFileName = "download_info";
	private static String downloadFilePath = "F:/photo/download_info.txt";
	private static Lock lock = new ReentrantLock();
	private static Map<String, FileWriter> fwMap = new ConcurrentHashMap<String, FileWriter>() {
		private static final long serialVersionUID = -3442731360328676574L;
		{
			try {
				put(downloadFileName, new FileWriter(new File(downloadFilePath)));
			} catch(Exception e) {
			}
		}
		
	};

	/**
	 * 加载要爬取的页面url
	 * 
	 * @param urlFilePath
	 * @return
	 */
	public static Set<String> loadPageUrl(String urlFilePath) {
		Set<String> pageUrls = new HashSet<String>();
		if (StringUtils.isBlank(urlFilePath)) {
			throw new RuntimeException("invalid file path");
		}
		File pageFile = new File(urlFilePath);
		if (!pageFile.exists() || !pageFile.isFile()) {
			throw new RuntimeException("file not exists");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pageFile));
			String line = null;
			while (null != (line = br.readLine())) {
				if (StringUtils.isNotBlank(line)) {
					pageUrls.add(line.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(br);
		}
		return pageUrls;
	}
	
	/**
	 * 生成图集名称
	 * 
	 * @param input
	 * @return
	 */
	public static String getAlbumName(String input) {
		String albumName = null;
		try {
			if (StringUtils.isBlank(input)) {
				throw new RuntimeException("input is blank");
			}
			int offset = 0;
			int begin = 0;
			int end = 0;
			for (int i = 0; i < input.length(); i++) {
				if (input.startsWith("Reload this Page", i)) {
					offset = i;
					break;
				}
			}
			for(int i = offset; i >= 0; i--) {
				if (input.startsWith("<a", i)) {
					begin = i;
					break;
				}
			}
			for(int i = offset; ; i++) {
				if (input.startsWith(">", i)) {
					end = i;
					break;
				}
			}
			String title = input.substring(begin, end + 1);
			for(int i = 0; i < title.length(); i++) {
				if (title.startsWith("href", i)) {
					int nameBegin = 0;
					int nameEnd = 0;
					loop: for(int j = i + 1; j < title.length(); j++) {
						if(title.startsWith("threads/", j)) {
							j = j + 8;
							nameBegin = j;
							for(int k = j + 1; k < title.length(); k++) {
								if(title.startsWith("?s=", k)) {
									nameEnd = k;
									break loop;
								}
							}
						}
					}
					albumName = title.substring(nameBegin, nameEnd);
					break;
				}
			}
			System.out.println(albumName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return albumName;
	}
	
	/**
	 * 生成下载链接
	 * 
	 * @param aUrl
	 * @param imgUrl
	 * @return
	 */
	public static String getDownloadLink(String aUrl, String imgUrl) {
		String downloadLink = null;
		try {
			if (StringUtils.isBlank(imgUrl)) {
				throw new RuntimeException("input parameter is blank, imgUrl=" + imgUrl);
			}
			if (!imgUrl.startsWith("http")) {
				throw new RuntimeException("not support protocol, imgUrl=" + imgUrl);
			}
			if (imgUrl.length() <= 10) {
				throw new RuntimeException("invalid url length, imgUrl=" + imgUrl);
			}
			if (imgUrl.endsWith(".gif") || imgUrl.endsWith(".png")) {
				throw new RuntimeException("not support image format, imgUrl=" + imgUrl);
			}
			if(imgUrl.indexOf(aUrl) >= 0) {
				downloadLink = "https://i.imgbox.com"+ imgUrl.substring(imgUrl.lastIndexOf("/"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadLink;
	}
	
	private static ExecutorService threadPool = new ThreadPoolExecutor(3, 3, 200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5000));
	
	/**
	 * 下载图片
	 * 
	 * @param albumName
	 * @param downloadUrls
	 */
	public static void downloadPic(String albumName, final List<String> downloadUrls) {
		FileWriter fw = null;
		try {
			if(StringUtils.isBlank(albumName) || null == downloadUrls
					|| downloadUrls.size() == 0) {
				throw new RuntimeException("input parameter is blank");
			}
			final File dic = new File("F:/photo/" + albumName);
			if(!dic.exists()) {
				dic.mkdirs();
			}
			// 下载
			final CountDownLatch latch = new CountDownLatch(downloadUrls.size());
			List<Future<String>> futures = new ArrayList<Future<String>>(downloadUrls.size());
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < downloadUrls.size(); i++) {
				final int index = i;
				futures.add(threadPool.submit(new Callable<String>() {
					@Override
					public String call() {
						String log = null;
						try {
							long startTime = System.currentTimeMillis();
							File output = new File(dic, index + 101 + ".jpg");
							int status = HttpUtils.doGetDownload(downloadUrls.get(index), output);
							System.out.println(output.getPath());
							log = downloadUrls.get(index) + " status=" + status + " waste=" + (System.currentTimeMillis() - startTime);
							System.out.println(log);
						} catch(Throwable t) {
							t.printStackTrace();
						} finally {
							latch.countDown();
						}
						return log;
					}
				}));
			}
			try {
				latch.await();
			} catch(Exception e) {
				e.printStackTrace();
			}
			for(Future<String> f : futures) {
				sb.append(f.get()).append("\n");
			}
			sb.setLength(sb.length() - 2);
			// 输出
			fw = new FileWriter(new File(dic.getParentFile(), albumName + ".output"));
			fw.write(sb.toString());
			fw.flush();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			IoUtils.closeQuietly(fw);
		}
	}
	
	/**
	 * 
	 * @param urlFilePath
	 */
	public static void batchReptile(String urlFilePath) {
		// 加载要下载的页面地址
		Set<String> pageUrls = loadPageUrl(urlFilePath);
		if (null == pageUrls || pageUrls.size() == 0) {
			return;
		}
		for (String pageUrl : pageUrls) {
			try {
				String pageContent = HttpUtils.doGet(pageUrl, String.class);
				if (StringUtils.isBlank(pageContent)) {
					throw new RuntimeException("page content is blank");
				}
				// 获取文件名
				String albumName =  getAlbumName(pageContent);
				List<String> downloadUrls = new ArrayList<String>();
				
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
					String downloadLink = getDownloadLink(aUrl, imgUrl);
					if (StringUtils.isNotBlank(downloadLink)) {
						downloadUrls.add(downloadLink);
					}
				}
				
				// 下载
				downloadPic(albumName, downloadUrls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void write(String fwName, String log) {
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

		System.out.println(log);
		if(null != fw) {
			try {
				fw.write(log);
				fw.write("\n");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println("start time:" + new Date());
		String urlFilePath = "F:/page_url.txt";
		batchReptile(urlFilePath);
		threadPool.shutdown();
		System.out.println("end time:" + new Date());
	}

}

