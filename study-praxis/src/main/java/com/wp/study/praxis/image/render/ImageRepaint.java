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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wp.study.base.util.IoUtils;

public class ImageRepaint {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImageRepaint.class);

	private static ExecutorService repaintPool = new ThreadPoolExecutor(8, 8, 5, TimeUnit.MINUTES,
			new ArrayBlockingQueue<Runnable>(10000));

	public static void main(String[] args) {
		try {
			// 重命名详情输出
			FileWriter fw = new FileWriter(new File("E:\\image\\repaint_1552149325923")) {
				@Override
				public void write(String str) throws IOException {
					super.write(str + "\r\n"); // 换行
				}
			};
			File file = new File("E:\\image\\similarity_1552149325923");
			Map<String, String> map = loadFingers(file);
			LOG.error("map.size={}", map.size());

			boolean skipImage = false;
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
							result = repaintAssignArea(new File(entry.getKey()), new File(entry.getValue()), 150, 1, 1, 0.25f,
									skipImage);
						} catch (Throwable e) {
							LOG.error("error:", e);
						} finally {
							try {
								fw.write("" + result + "  " + entry.getKey() + "=" + entry.getValue());
							} catch (Throwable e) {
								LOG.error("error:", e);
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

	public static boolean repaintAssignArea(File image0, File image1, int h, int x, int y, float logoWidthRatio,
											boolean skipImage) {
		BufferedImage scaleImage = null;
		BufferedImage targetImage = null;
		boolean result = false;
		try {
			File target = new File(new File(image1.getParent() + "_repaint"), image1.getName());
			if (skipImage && target.exists()) {
				return true;
			}
			// 0：左下，1：左上，2：右下，3：右上
			String location = "";
			if (new File(new File("E:\\temp\\右上"), image0.getName()).exists()) {
				image0 = new File(new File("E:\\temp\\右上"), image0.getName());
				location = "右上";
			} else if (new File(new File("E:\\temp\\右下"), image0.getName()).exists()) {
				image0 = new File(new File("E:\\temp\\右下"), image0.getName());
				location = "右下";
			} else if (new File(new File("E:\\temp\\左上"), image0.getName()).exists()) {
				image0 = new File(new File("E:\\temp\\左上"), image0.getName());
				location = "左上";
			} else if (new File(new File("E:\\temp\\左下"), image0.getName()).exists()) {
				image0 = new File(new File("E:\\temp\\左下"), image0.getName());
				location = "左下";
			}

			targetImage = ImageIO.read(image1);

			if (x == y) {
				scaleImage = ImageIO.read(image0);
			} else {
				scaleImage = getFasterScaledInstance(image0, targetImage.getWidth(), true);
			}

			int w = (int) (targetImage.getWidth() * logoWidthRatio);
			replacePixel(targetImage, scaleImage, w, h, location);

			ImageWriter writer = null;
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(targetImage);
			Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
			if (iter.hasNext()) {
				writer = iter.next();
			}
			if (writer != null) {
				IIOImage iioImage = new IIOImage(targetImage, null, null);
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(1.0f);
				ImageOutputStream outputStream = ImageIO.createImageOutputStream(target);
				writer.setOutput(outputStream);
				writer.write(null, iioImage, param);
				LOG.error("{}", image1);
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
			LOG.error("{}  {}={}", result, image0, image1);
		}
		return result;
	}

	/**
	 * 重新绘制图片
	 *
	 * @param mainImagePath 主图片
	 * @param assitImagePath 辅助图片
	 * @param location 待替换位置，”右上“，”右下“，”左上“，”左下“
	 * @param replaceAreaW 待替换区域宽
	 * @param replaceAreaH 待替换区域高
	 * @return
	 */
	public static boolean repaintAssignArea(String mainImagePath, String assitImagePath, String location, int replaceAreaW, int replaceAreaH) {
		BufferedImage scaleAssitImage = null;
		BufferedImage targetImage = null;
		boolean result = false;
		File mainImage = new File(mainImagePath);
		File assitImage = new File(assitImagePath);
		try {
			File repaintDir = new File(mainImage.getParent() + "_repaint");
			if (!repaintDir.exists()) {
				repaintDir.mkdirs();
			}
			File target = new File(repaintDir, mainImage.getName());
			if (target.exists()) {
				return true;
			}

			// 生成的目标图像以主图像为准
			targetImage = ImageIO.read(mainImage);

			// 缩放后的辅助图片
			scaleAssitImage = getFasterScaledInstance(assitImage, targetImage.getWidth(), true);

			replacePixel(targetImage, scaleAssitImage, replaceAreaW, replaceAreaH, location);
			
			ImageWriter writer = null;
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(targetImage);
			Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
			if (iter.hasNext()) {
				writer = iter.next();
			}
			if (writer != null) {
				IIOImage iioImage = new IIOImage(targetImage, null, null);
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(1.0f);
				ImageOutputStream outputStream = ImageIO.createImageOutputStream(target);
				writer.setOutput(outputStream);
				writer.write(null, iioImage, param);
				LOG.error("{}", assitImage);
			}
			result = target.exists();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != scaleAssitImage) {
				scaleAssitImage.flush();
			}
			if (null != targetImage) {
				targetImage.flush();
			}
		}
		if (!result) {
			LOG.error("{}  {}={}", result, mainImage, assitImage);
		}
		return result;
	}
	
	public static void replacePixel(BufferedImage targetImage, BufferedImage scaleImage, int replaceAreaW, int replaceAreaH, String location) {
		// 此方式为沿Height方向扫描
		if ("左上".equals(location)) { // 左上
			for (int i = 0; i < replaceAreaW; i++) {
				for (int j = 0; j < replaceAreaH; j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
		} else if ("左下".equals(location)) { // 左下
			for (int i = 0; i < replaceAreaW; i++) {
				for (int j = targetImage.getHeight() - replaceAreaH; j < targetImage.getHeight(); j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
		} else if ("右上".equals(location)) { // 右上
			for (int i = (targetImage.getWidth() - replaceAreaW); i < targetImage.getWidth(); i++) {
				for (int j = 0; j < replaceAreaH; j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
		} else if ("右下".equals(location)) { // 右下
			for (int i = (targetImage.getWidth() - replaceAreaW); i < targetImage.getWidth(); i++) {
				for (int j = targetImage.getHeight() - replaceAreaH; j < targetImage.getHeight(); j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
		}
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

	public static BufferedImage getFasterScaledInstance(File imageFile, int scale2With, boolean progressiveBilinear) {
		BufferedImage originImg = null;
		try {
			originImg = ImageIO.read(imageFile);
			if (originImg.getWidth() == scale2With) {
				// 原始图片与要缩放的宽相同，直接返回
				return originImg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferedImage ret = null;
		BufferedImage scratchImage = null;
		Graphics2D g2 = null;
		try {
			int type = (originImg.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
					: BufferedImage.TYPE_INT_ARGB;
			int scale2Height = scale2With * originImg.getHeight() / originImg.getWidth();
			ret = originImg;
			int w, h;
			int prevW = ret.getWidth();
			int prevH = ret.getHeight();
			if (progressiveBilinear) {
				w = originImg.getWidth();
				h = originImg.getHeight();
			} else {
				w = scale2With;
				h = scale2Height;
			}
			do {
				if (progressiveBilinear && w > scale2With) {
					w /= 2;
					if (w < scale2With) {
						w = scale2With;
					}
				}
				if (progressiveBilinear && h > scale2Height) {
					h /= 2;
					if (h < scale2Height) {
						h = scale2Height;
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
			} while (w != scale2With || h != scale2Height);
			if (g2 != null) {
				g2.dispose();
			}
			if (scale2With != ret.getWidth() || scale2Height != ret.getHeight()) {
				scratchImage = new BufferedImage(scale2With, scale2Height, type);
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
			if (null != originImg) {
				originImg.flush();
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
				LOG.error("map.size()={}", map.size());
				LOG.error("{}", i++);
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
