package com.wp.study.praxis.image.reptile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.HttpUtil;
import com.wp.study.praxis.image.reptile.filter.XmlFilter;

public class ImageReptile {

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
			if (null != null) {
				try {
					br.close();
				} catch (Exception e2) {
				}
			}
		}
		return pageUrls;
	}

	/**
	 * 生成图集名称
	 * 
	 * @param input
	 * @param albumBorder
	 * @return
	 */
	public static String getAlbumName(String input, String albumBorder) {
		String albumName = null;
		try {
			if (StringUtils.isBlank(input)) {
				throw new RuntimeException("input is blank");
			}
			String pendingStr = null;
			loop: for (int i = 0; i < input.length(); i++) {
				if (input.startsWith(albumBorder, i)) {
					int offset = i + albumBorder.length();
					while (offset < input.length()) {
						char nextCh = input.charAt(offset);
						if (nextCh == '>') {
							int startP = offset + 1;
							while (offset < input.length()) {
								nextCh = input.charAt(offset);
								if (nextCh == '<') {
									pendingStr = input.substring(startP, offset);
									break loop;
								}
								offset++;
							}
						}
						offset++;
					}
				}
			}
			if (StringUtils.isNotBlank(pendingStr)) {
				loop: for(int i = input.length() - 1; i >= 0; i--) {
					char ch = input.charAt(i);
					if(ch >= '1' && ch <= '9') {
						int offset = i - 1;
						ch = input.charAt(offset--);
						if(ch >= '0' && ch <= '9') {
							ch = input.charAt(offset--);
							if(ch >= 'a' && ch <= 'z') {
								ch = input.charAt(offset--);
								if(ch == '_') {
									while(offset >= 0) {
										ch = input.charAt(offset--);
										if(!(ch >= 'a' && ch <= 'z') && 
												!(ch >= '0' && ch <= '9') && ch != '_') {
											albumName = input.substring(offset + 2, i + 1);
											break loop;
										}
									}
								}
							} else if(ch == '_') {
								while(offset >= 0) {
									ch = input.charAt(offset--);
									if(!(ch >= 'a' && ch <= 'z') && 
											!(ch >= '0' && ch <= '9') && ch != '_') {
										albumName = input.substring(offset + 2, i + 1);
										break loop;
									}
								}
							}
						}
					}
				}
			}
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
				throw new RuntimeException("input parameter is blank");
			}
			if (!imgUrl.startsWith("http")) {
				throw new RuntimeException("not support protocol");
			}
			if (imgUrl.length() <= 10) {
				throw new RuntimeException("invalid url length");
			}
			if (imgUrl.endsWith(".gif") || imgUrl.endsWith(".png")) {
				throw new RuntimeException("not support image format");
			}
			if (imgUrl.startsWith("https://img.yt") || imgUrl.startsWith("http://img.yt")
					|| imgUrl.startsWith("http://imgcandy.net")) {
				downloadLink = imgUrl.replace("small", "big");
			} else if (imgUrl.startsWith("http://chronos.to")) {
				downloadLink = imgUrl.replace("/t/", "/i/");
			} else if (imgUrl.startsWith("http://pic-maniac.com")) {
				downloadLink = imgUrl.substring(0, imgUrl.lastIndexOf("/") + 1);
				downloadLink += aUrl.substring(aUrl.lastIndexOf("/") + 1);
				downloadLink += imgUrl.substring(imgUrl.lastIndexOf("."));
			} else if (aUrl.startsWith("http://imgcandy.net")) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadLink;
	}
	
	private static ExecutorService threadPool = new ThreadPoolExecutor(3, 3, 200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
	
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
							File output = new File(dic, index + 100 + ".jpg");
							int status = HttpUtil.doDownload(downloadUrls.get(index), 30000, 300000, output);
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
			fw = new FileWriter(new File(dic, albumName + ".output"));
			fw.write(sb.toString());
			fw.flush();
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
	 * 
	 * @param urlFilePath
	 * @param albumBorder
	 */
	public static void batchReptile(String urlFilePath, String albumBorder) {
		// 加载要下载的页面地址
		Set<String> pageUrls = loadPageUrl(urlFilePath);
		if (null == pageUrls || pageUrls.size() == 0) {
			return;
		}
		for (String pageUrl : pageUrls) {
			try {
				String pageContent = HttpUtil.doGet(pageUrl, 30000, 200000, String.class);
				if (StringUtils.isBlank(pageContent)) {
					throw new RuntimeException("page content is blank");
				}
				// 获取文件名
				String albumName =  getAlbumName(pageContent, albumBorder);
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
	
	public static void main(String[] args) {
		System.out.println("start time:" + new Date());
		String urlFilePath = "F:/photo/page_url.txt";
		String albumBorder = "Reload this Page";
		batchReptile(urlFilePath, albumBorder);
		threadPool.shutdown();
		System.out.println("end time:" + new Date());
	}

}

