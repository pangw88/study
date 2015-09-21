package com.wp.study.netty4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {

	private int port;
	
	public EchoServer(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// create ServerBootstrap instance
			ServerBootstrap b = new ServerBootstrap();
			// Specifies NIO transport, local socket address
			// Adds handler to channel pipeline
			b.group(group).channel(NioServerSocketChannel.class)
					/*.localAddress(port)*/
					.childHandler(new ChannelInitializer<Channel>() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							ch.pipeline().addLast(new EchoServerHandler());
						}
					});
			// Binds server, waits for server to close, and releases resources
			ChannelFuture f0 = b.bind(63332).sync();
			ChannelFuture f1 = b.bind(63333).sync();
			
			System.out.println(EchoServer.class.getName()
					+ "started and listen on " + f0.channel().localAddress());
			System.out.println(EchoServer.class.getName()
					+ "started and listen on " + f1.channel().localAddress());
			f0.channel().closeFuture().sync();
			f1.channel().closeFuture().sync();
			
		}
		// 先执行finally子句，再执行异常。
		finally {
			group.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
		new EchoServer(65535).start();
	}

}
