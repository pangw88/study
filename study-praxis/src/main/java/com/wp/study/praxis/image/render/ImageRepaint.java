package com.wp.study.praxis.image.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImageRepaint {

	public static void main(String[] args) {
		File image0 = new File("D:\\QMDownload\\tt\\fuuka-n01\\gh_fuuka-n001.jpg");
		File image1 = new File("D:\\希捷数据救护\\done\\Fuuka Nishihama--done\\p_fuuka_st1_01\\p_fuuka_st1_01_001.jpg");
		long begin0 = System.currentTimeMillis();
		try {
			repaint(image0, image1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("waste=" + (System.currentTimeMillis() - begin0));
		}
	}

	public static void repaint(File image0, File image1) {
		BufferedImage scaleImage = null;
		BufferedImage targetImage = null;
		try {
			scaleImage = getFasterScaledInstance(image0, 1280, 1200, true);
			targetImage = ImageIO.read(image1);
			// 此方式为沿Height方向扫描
			for (int i = 0; i < targetImage.getWidth(); i++) {
				for (int j = 0; j < 50; j++) {
					targetImage.setRGB(i, j, scaleImage.getRGB(i, j));
				}
			}
			// 将bufferedImage对象输出到磁盘上
			File targetDir = new File(image1.getParentFile().getAbsolutePath() + "_repaint");
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			ImageIO.write(targetImage, "jpg", new File(targetDir, image1.getName()));
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

}
