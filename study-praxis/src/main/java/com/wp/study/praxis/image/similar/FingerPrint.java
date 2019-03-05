package com.wp.study.praxis.image.similar;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.wp.study.base.util.ByteUtils;
import com.wp.study.base.util.IoUtils;

/**
 * 均值哈希实现图像指纹比较
 * 
 */
public final class FingerPrint {

	private static Map<String, Map<String, String>> fingerMap = new ConcurrentHashMap<String, Map<String, String>>();
	private static Map<String, FileWriter> fwMap = new ConcurrentHashMap<String, FileWriter>();

	private static ExecutorService computeSimilarPool = new ThreadPoolExecutor(8, 8, 5, TimeUnit.MINUTES,
			new ArrayBlockingQueue<Runnable>(10000));

	public static void main(String[] args) {
		try {
			computeImageFinger(new File("D:\\QMDownload\\tt"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			computeSimilarPool.shutdown();
			destroy();
		}
	}

	public static void destroy() {
		for (Map.Entry<String, FileWriter> entry : fwMap.entrySet()) {
			try {
				FileWriter fw = entry.getValue();
				Map<String, String> fingerDirMap = fingerMap.get(entry.getKey());
				if (null != fingerDirMap) {
					for (Map.Entry<String, String> fingerEntry : fingerDirMap.entrySet()) {
						fw.write(fingerEntry.getKey() + "=" + fingerEntry.getValue());
					}
				}
				fw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtils.closeQuietly(entry.getValue());
			}
		}
	}

	/**
	 * 计算图片指纹
	 * 
	 * @param dir
	 */
	public static void computeImageFinger(File dir) {
		File[] subDirs = dir.listFiles();
		if (null == subDirs || subDirs.length == 0) {
			return;
		}
		for (File subDir : subDirs) {
			if (subDir.isFile()) {
				continue;
			}
			File[] imageFiles = subDir.listFiles();
			if (null == imageFiles || imageFiles.length == 0) {
				continue;
			}
			FileWriter fw = null;
			try {
				String dirPath = subDir.getAbsolutePath();
				String fingerFileName = dirPath.replaceAll("\\\\", "~");
				fingerFileName = fingerFileName.replaceAll(":", "@");
				File fingerFile = new File("E:\\image\\fingers", fingerFileName);
				if (!fingerFile.exists()) {
					fingerFile.createNewFile();
				}
				fingerMap.put(fingerFileName, loadFingers(fingerFile));
				fw = new FileWriter(fingerFile) {
					@Override
					public void write(String str) throws IOException {
						super.write(str + "\r\n"); // 换行
					}
				};
				final CountDownLatch latch = new CountDownLatch(imageFiles.length);
				for (File imageFile : imageFiles) {
					computeSimilarPool.submit(new Runnable() {
						@Override
						public void run() {
							try {
								String finger = getFinger(imageFile);
								System.out.println(imageFile + "=" + finger);
							} catch (Exception e) {
								System.out.println();
								System.out.println("fail imageFile=" + imageFile);
								e.printStackTrace();
							} finally {
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
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtils.closeQuietly(fw);
			}
		}
	}

	public static String getFinger(File imageFile) {
		String finger = null;
		try {
			String dirPath = imageFile.getParentFile().getAbsolutePath();
			String fingerFileName = dirPath.replaceAll("\\\\", "~");
			fingerFileName = fingerFileName.replaceAll(":", "@");
			Map<String, String> fingerDirMap = fingerMap.get(fingerFileName);
			finger = fingerDirMap.get(imageFile.getName());
			if (StringUtils.isBlank(finger)) {
				byte[] bytes = FingerPrint.hashValue(ImageIO.read(imageFile));
				finger = fingerDirMap.get(imageFile.getName());
				if (StringUtils.isBlank(finger)) {
					synchronized (FingerPrint.class) {
						finger = ByteUtils.bytes2String(bytes);
						fingerDirMap.put(imageFile.getName(), finger);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finger;
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
			while (null != (line = br.readLine())) {
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

	public static float getSimilarity(String imagePath0, String imagePath1) {
		return getSimilarity(new File(imagePath0), new File(imagePath1));
	}

	public static float getSimilarity(File image0, File image1) {
		float compare = 0.0f;
		try {
			FingerPrint fp1 = new FingerPrint(ImageIO.read(image0));
			FingerPrint fp2 = new FingerPrint(ImageIO.read(image1));
			compare = fp1.compare(fp2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compare;
	}

	public static float getSimilarity(byte[] images0, byte[] images1) {
		float compare = 0.0f;
		try {
			FingerPrint fp1 = new FingerPrint(images0);
			FingerPrint fp2 = new FingerPrint(images1);
			compare = fp1.compare(fp2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compare;
	}

	/**
	 * 图像指纹的尺寸,将图像resize到指定的尺寸，来计算哈希数组
	 */
	private static final int HASH_SIZE = 16;
	/**
	 * 保存图像指纹的二值化矩阵
	 */
	private final byte[] binaryzationMatrix;

	public FingerPrint(byte[] hashValue) {
		if (hashValue.length != HASH_SIZE * HASH_SIZE)
			throw new IllegalArgumentException(String.format("length of hashValue must be %d", HASH_SIZE * HASH_SIZE));
		this.binaryzationMatrix = hashValue;
	}

	public FingerPrint(String hashValue) {
		this(toBytes(hashValue));
	}

	public FingerPrint(BufferedImage src) {
		this(hashValue(src));
	}

	public static byte[] hashValue(BufferedImage src) {
		BufferedImage hashImage = resize(src, HASH_SIZE, HASH_SIZE);
		byte[] matrixGray = (byte[]) toGray(hashImage).getData().getDataElements(0, 0, HASH_SIZE, HASH_SIZE, null);
		return binaryzation(matrixGray);
	}

	/**
	 * 从压缩格式指纹创建{@link FingerPrint}对象
	 * 
	 * @param compactValue
	 * @return
	 */
	public static FingerPrint createFromCompact(byte[] compactValue) {
		return new FingerPrint(uncompact(compactValue));
	}

	public static boolean validHashValue(byte[] hashValue) {
		if (hashValue.length != HASH_SIZE)
			return false;
		for (byte b : hashValue) {
			if (0 != b && 1 != b)
				return false;
		}
		return true;
	}

	public static boolean validHashValue(String hashValue) {
		if (hashValue.length() != HASH_SIZE)
			return false;
		for (int i = 0; i < hashValue.length(); ++i) {
			if ('0' != hashValue.charAt(i) && '1' != hashValue.charAt(i))
				return false;
		}
		return true;
	}

	public byte[] compact() {
		return compact(binaryzationMatrix);
	}

	/**
	 * 指纹数据按位压缩
	 * 
	 * @param hashValue
	 * @return
	 */
	private static byte[] compact(byte[] hashValue) {
		byte[] result = new byte[(hashValue.length + 7) >> 3];
		byte b = 0;
		for (int i = 0; i < hashValue.length; ++i) {
			if (0 == (i & 7)) {
				b = 0;
			}
			if (1 == hashValue[i]) {
				b |= 1 << (i & 7);
			} else if (hashValue[i] != 0)
				throw new IllegalArgumentException("invalid hashValue,every element must be 0 or 1");
			if (7 == (i & 7) || i == hashValue.length - 1) {
				result[i >> 3] = b;
			}
		}
		return result;
	}

	/**
	 * 压缩格式的指纹解压缩
	 * 
	 * @param compactValue
	 * @return
	 */
	private static byte[] uncompact(byte[] compactValue) {
		byte[] result = new byte[compactValue.length << 3];
		for (int i = 0; i < result.length; ++i) {
			if ((compactValue[i >> 3] & (1 << (i & 7))) == 0)
				result[i] = 0;
			else
				result[i] = 1;
		}
		return result;
	}

	/**
	 * 字符串类型的指纹数据转为字节数组
	 * 
	 * @param hashValue
	 * @return
	 */
	private static byte[] toBytes(String hashValue) {
		hashValue = hashValue.replaceAll("\\s", "");
		byte[] result = new byte[hashValue.length()];
		for (int i = 0; i < result.length; ++i) {
			char c = hashValue.charAt(i);
			if ('0' == c)
				result[i] = 0;
			else if ('1' == c)
				result[i] = 1;
			else
				throw new IllegalArgumentException("invalid hashValue String");
		}
		return result;
	}

	/**
	 * 缩放图像到指定尺寸
	 * 
	 * @param src
	 * @param width
	 * @param height
	 * @return
	 */
	private static BufferedImage resize(Image src, int width, int height) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = result.getGraphics();
		try {
			g.drawImage(src.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		} finally {
			g.dispose();
		}
		return result;
	}

	/**
	 * 计算均值
	 * 
	 * @param src
	 * @return
	 */
	private static int mean(byte[] src) {
		long sum = 0;
		// 将数组元素转为无符号整数
		for (byte b : src)
			sum += (long) b & 0xff;
		return (int) (Math.round((float) sum / src.length));
	}

	/**
	 * 二值化处理
	 * 
	 * @param src
	 * @return
	 */
	private static byte[] binaryzation(byte[] src) {
		byte[] dst = src.clone();
		int mean = mean(src);
		for (int i = 0; i < dst.length; ++i) {
			// 将数组元素转为无符号整数再比较
			dst[i] = (byte) (((int) dst[i] & 0xff) >= mean ? 1 : 0);
		}
		return dst;

	}

	/**
	 * 转灰度图像
	 * 
	 * @param src
	 * @return
	 */
	private static BufferedImage toGray(BufferedImage src) {
		if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			return src;
		} else {
			// 图像转灰
			BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, grayImage);
			return grayImage;
		}
	}

	@Override
	public String toString() {
		return toString(true);
	}

	/**
	 * @param multiLine 是否分行
	 * @return
	 */
	public String toString(boolean multiLine) {
		StringBuffer buffer = new StringBuffer();
		int count = 0;
		for (byte b : this.binaryzationMatrix) {
			buffer.append(0 == b ? '0' : '1');
			if (multiLine && ++count % HASH_SIZE == 0)
				buffer.append('\n');
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FingerPrint) {
			return Arrays.equals(this.binaryzationMatrix, ((FingerPrint) obj).binaryzationMatrix);
		} else
			return super.equals(obj);
	}

	/**
	 * 与指定的压缩格式指纹比较相似度
	 * 
	 * @param compactValue
	 * @return
	 * @see #compare(FingerPrint)
	 */
	public float compareCompact(byte[] compactValue) {
		return compare(createFromCompact(compactValue));
	}

	/**
	 * @param hashValue
	 * @return
	 * @see #compare(FingerPrint)
	 */
	public float compare(String hashValue) {
		return compare(new FingerPrint(hashValue));
	}

	/**
	 * 与指定的指纹比较相似度
	 * 
	 * @param hashValue
	 * @return
	 * @see #compare(FingerPrint)
	 */
	public float compare(byte[] hashValue) {
		return compare(new FingerPrint(hashValue));
	}

	/**
	 * 与指定图像比较相似度
	 * 
	 * @param image2
	 * @return
	 * @see #compare(FingerPrint)
	 */
	public float compare(BufferedImage image2) {
		return compare(new FingerPrint(image2));
	}

	/**
	 * 比较指纹相似度
	 * 
	 * @param src
	 * @return
	 * @see #compare(byte[], byte[])
	 */
	public float compare(FingerPrint src) {
		if (src.binaryzationMatrix.length != this.binaryzationMatrix.length)
			throw new IllegalArgumentException("length of hashValue is mismatch");
		return compare(binaryzationMatrix, src.binaryzationMatrix);
	}

	/**
	 * 判断两个数组相似度，数组长度必须一致否则抛出异常
	 * 
	 * @param f1
	 * @param f2
	 * @return 返回相似度(0.0~1.0)
	 */
	private static float compare(byte[] f1, byte[] f2) {
		if (f1.length != f2.length)
			throw new IllegalArgumentException("mismatch FingerPrint length");
		int sameCount = 0;
		for (int i = 0; i < f1.length; ++i) {
			if (f1[i] == f2[i])
				++sameCount;
		}
		return (float) sameCount / f1.length;
	}

	public static float compareCompact(byte[] f1, byte[] f2) {
		return compare(uncompact(f1), uncompact(f2));
	}

	public static float compare(BufferedImage image1, BufferedImage image2) {
		return new FingerPrint(image1).compare(new FingerPrint(image2));
	}
}
