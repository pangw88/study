package com.wp.study.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class AddressSimilarity {
	
	public static class AddressSimilarityMap extends Mapper<LongWritable, Text, Text, Text> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			context.getInputSplit();
			String pathName = ((FileSplit) context.getInputSplit()).getPath().toString();
			if(pathName.contains("new.txt")) {
				// 去除空白字符
				context.write(new Text("addr"), new Text("n:" + value.toString().replaceAll("\\s*", "")));
			} else {
				context.write(new Text("addr"), new Text(value.toString().replaceAll("\\s*", "")));
			}
		}
	}
	
	public static class AddressSimilarityReduce extends Reducer<Text, Text, Text, Text> {
		
		// 相似度阀值
		private float threshold;
		
		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			try {
				Configuration conf = context.getConfiguration();
				// 从配置中读取阀值
				threshold = Float.parseFloat(conf.get("threshold"));
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(nfe);
			}
		}
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			List<String> news = new ArrayList<String>();
			List<String> olds = new ArrayList<String>();
			for(Text value : values) {
				String addr = value.toString();
				if(addr.startsWith("n:")) {
					news.add(addr.substring(2));
				} else {
					olds.add(addr);
				}
			}
			Float res = 0f;
			for(String n : news) {
				for(String o : olds) {
					res = calculateSimilarity(n, o);
					if(res >= threshold) {
						context.write(new Text(n), new Text(Float.toString(res)));
						break;
					}
				}
			}
				
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
	    job.setJarByClass(AddressSimilarity.class);
	    job.setMapperClass(AddressSimilarityMap.class);
	    job.setReducerClass(AddressSimilarityReduce.class);
	    job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
	    
		for (int i = 0; i < otherArgs.length - 1; i++) {
	      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
	      job.addCacheFile(new Path(otherArgs[i]).toUri());
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
