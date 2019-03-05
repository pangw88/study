package com.wp.study.praxis.image.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.IoUtils;

public class ImageRepaint {

	private static ExecutorService repaintPool = new ThreadPoolExecutor(8, 8, 5, TimeUnit.MINUTES,
			new ArrayBlockingQueue<Runnable>(10000));

	public static void main(String[] args) {
		try {
			// 重命名详情输出
			FileWriter fw = new FileWriter(new File("E:\\image\\repaint_" + System.currentTimeMillis())) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			File file = new File("E:\\image\\similarity_1551642277122");
			Map<String, String> map = loadFingers(file);
			System.out.println(map.size());
			final CountDownLatch latch = new CountDownLatch(map.size());
			for (Map.Entry<String, String> entry : map.entrySet()) {
				// 将bufferedImage对象输出到磁盘上
				File targetDir = new File(new File(entry.getValue()).getParent() + "_repaint");
				if (!targetDir.exists()) {
					targetDir.mkdirs();
				}
				repaintPool.submit(new Runnable() {
					@Override
					public void run() {
						boolean result = false;
						try {
							result = repaint(new File(entry.getKey()), new File(entry.getValue()), 120, 0.33f);
						} catch (Throwable e) {
							System.out.println(e);
						} finally {
							try {
								fw.write("" + result + "  " + entry.getKey() + "=" + entry.getValue());
							} catch (Throwable e) {
								System.out.println(e);
							}
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
			IoUtils.closeQuietly(fw);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			repaintPool.shutdown();
		}
	}

	public static boolean repaint(File image0, File image1, int h, float logoWidthRatio) {
		BufferedImage scaleImage = null;
		BufferedImage targetImage = null;
		boolean result = false;
		try {
			scaleImage = getFasterScaledInstance(image0, 1280, 1200, true);
			targetImage = ImageIO.read(image1);
			int w = (int) (targetImage.getWidth() * logoWidthRatio);
			// 此方式为沿Height方向扫描
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
			for (int i = (targetImage.getWidth() - w); i < targetImage.getWidth(); i++) {
				for (int j = 0; j < h; j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
			ImageWriter writer = null;
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(targetImage);
			Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
			if (iter.hasNext()) {
				writer = iter.next();
			}
			File target = new File(new File(image1.getParent() + "_repaint"), image1.getName());
			if (writer != null) {
				IIOImage iioImage = new IIOImage(targetImage, null, null);
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(1.0f);
				ImageOutputStream outputStream = ImageIO.createImageOutputStream(target);
				writer.setOutput(outputStream);
				writer.write(null, iioImage, param);
			}
			result = target.exists();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != scaleImage) {
				scaleImage.flush();
			}
			if (null != targetImage) {
				targetImage.flush();
			}
		}
		if (!result) {
			System.out.println("" + result + "  " + image0 + "=" + image1);
		}
		return result;
	}

	public static BufferedImage getCommonScaledInstance(File imageFile, int x, int y) {
		BufferedImage scaleImage = null;
		Graphics graphics = null;
		Image srcImage = null;
		try {
			// 读取原始位图
			srcImage = ImageIO.read(imageFile);
			int width = (int) (srcImage.getWidth(null) * y / x);
			int height = (int) (srcImage.getHeight(null) * y / x);
			// 将原始位图缩小后绘制到bufferedImage对象中
			scaleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			graphics = scaleImage.getGraphics();
			graphics.drawImage(srcImage, 0, 0, width, height, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != graphics) {
				graphics.dispose();
			}
			if (null != srcImage) {
				srcImage.flush();
			}
		}
		return scaleImage;
	}

	public static BufferedImage getFasterScaledInstance(File imageFile, int x, int y, boolean progressiveBilinear) {
		BufferedImage ret = null;
		BufferedImage scratchImage = null;
		Graphics2D g2 = null;
		try {
			BufferedImage img = ImageIO.read(imageFile);
			int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
					: BufferedImage.TYPE_INT_ARGB;
			int targetWidth = (int) (img.getWidth(null) * y / x);
			int targetHeight = (int) (img.getHeight(null) * y / x);
			ret = img;
			int w, h;
			int prevW = ret.getWidth();
			int prevH = ret.getHeight();
			if (progressiveBilinear) {
				w = img.getWidth();
				h = img.getHeight();
			} else {
				w = targetWidth;
				h = targetHeight;
			}
			do {
				if (progressiveBilinear && w > targetWidth) {
					w /= 2;
					if (w < targetWidth) {
						w = targetWidth;
					}
				}
				if (progressiveBilinear && h > targetHeight) {
					h /= 2;
					if (h < targetHeight) {
						h = targetHeight;
					}
				}
				if (scratchImage == null) {
					scratchImage = new BufferedImage(w, h, type);
					g2 = scratchImage.createGraphics();
				}
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
				prevW = w;
				prevH = h;
				ret = scratchImage;
			} while (w != targetWidth || h != targetHeight);
			if (g2 != null) {
				g2.dispose();
			}
			if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
				scratchImage = new BufferedImage(targetWidth, targetHeight, type);
				g2 = scratchImage.createGraphics();
				g2.drawImage(ret, 0, 0, null);
				g2.dispose();
				ret = scratchImage;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != g2) {
				g2.dispose();
			}
		}
		return ret;
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
			int i = 0;
			while (null != (line = br.readLine())) {
				System.out.print("map.size()=" + map.size() + " ");
				System.out.println(i++);
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
