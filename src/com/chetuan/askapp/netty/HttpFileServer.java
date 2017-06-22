package com.chetuan.askapp.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {

	private static final String http_IP = "127.0.0.1";
	public static final String http_URL = "/src/com/chetuan/html/";

	public HttpFileServer(final int port) {
		try {
			run(port, http_URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void run(final int port, final String url) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// 服务端，对请求解码
					ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
					// 聚合器，把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
					ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
					// 服务端，对响应编码
					ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
					// 块写入处理器
					ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
					// 自定义服务端处理器
					ch.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url));
				}
			});
			ChannelFuture future = b.bind(http_IP, port).sync();
			System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://" + http_IP + ":" + port + url);
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
