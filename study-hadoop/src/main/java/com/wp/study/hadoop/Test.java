package com.wp.study.hadoop;

import java.io.File;
import java.net.URI;

public class Test {

	public static void main(String[] args) {
		File f = new File("4广东 广州       越秀区中山六路95号 距离市中心约6455米");
		URI uri = f.toURI();
		new File(uri);
		System.out.println(uri.getPath());
		System.out.println("4广东 广州       越秀区中山六路95号 距离市中心约6455米".replaceAll("\\s*", ""));
	}
}
