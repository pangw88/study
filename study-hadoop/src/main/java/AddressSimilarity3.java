
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class AddressSimilarity3 {
	
	public static class AddressSimilarityMap extends Mapper<LongWritable, Text, Text, FloatWritable> {
	
		// 对历史地址的文件进行缓存
		private static List<String> oldAddrs = new ArrayList<String>();
		
		// 相似度阀值
		private float threshold;
		
		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			BufferedReader br = null;
			try {
				Configuration conf = context.getConfiguration();
				// 从配置中读取阀值
				threshold = Float.parseFloat(conf.get("threshold"));
				
				// 从当前作业中获取要缓存的文件
				Path[] localCacheFiles = DistributedCache.getLocalCacheFiles(conf);
				if(null != localCacheFiles && localCacheFiles.length > 0) {
					oldAddrs.add(localCacheFiles[0].toString());
					/*for(Path path : localCacheFiles) {
						br = new BufferedReader(new FileReader(path.toString()));
						String line = null;
						while (null != (line = br.readLine()))  
						{  
							//缓存文件中的地址数据
							oldAddrs.add(line);
						}
					}*/
				}
				// br.close();
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(nfe);
			} finally {
				if(br != null) {
					try {
						br.close();
					} catch(IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			}
		}
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			context.write(new Text(oldAddrs.get(0)), new FloatWritable(0.8f));
			/*for(String oldAddr : oldAddrs) {
				String newAddr = value.toString();
				float res = calculateSimilarity(oldAddr, newAddr);
				if(res >= threshold) {
					context.write(new Text(newAddr), new FloatWritable(res));
					break;
				}
			}*/
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("threshold", "0.8");
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length < 2)
	    {
	      System.err.println("Usage: wordcount <in> [<in>...] <out>");
	      System.exit(2);
	    }
	    Job job = new Job(conf, "address similarity");
	    job.setJarByClass(AddressSimilarity3.class);
	    job.setMapperClass(AddressSimilarityMap.class);
	    job.setNumReduceTasks(0);
	    
	    job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FloatWritable.class);
	    
		for (int i = 0; i < otherArgs.length - 1; i++) {
	      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
	      DistributedCache.addCacheFile(new Path(otherArgs[i]).toUri(), conf);
	      //job.addCacheFile(new Path(otherArgs[i]).toUri());
	    }
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[(otherArgs.length - 1)]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
	private static float calculateSimilarity(String addr1, String addr2) {
		//计算两个字符串的长度。
		int len1 = addr1.length();
		int len2 = addr2.length();
		//建立上面说的数组，比字符长度大一个空间
		int[][] dif = new int[len1 + 1][len2 + 1];
		//赋初值，步骤B。
		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}
		//计算两个字符是否一样，计算左上的值
		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (addr1.charAt(i - 1) == addr2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				//取三个值中最小的
				dif[i][j] = Math.min(Math.min(
					dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1), dif[i - 1][j] + 1);
			}
		}
		return (1 - (float) dif[len1][len2] / Math.max(addr1.length(), addr2.length()));
	}
}
