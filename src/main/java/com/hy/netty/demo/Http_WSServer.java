package com.hy.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @ Author     ：hyy.
 * @ Date       ：Created in 11:38 2018/11/14
 * @ Description：${description}
 * @ Modified By：hyy.
 * @Version: $version$
 */
public class HttpServer {
    public void openServer(){
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(NioServerSocketChannel.class);
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(8);
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>(){

            @Override
            protected void initChannel(NioSocketChannel channel) throws Exception {
                channel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                //添加了HttpObjectAggregator解码器，
                //它的作用是将多个消息转换为单一的FullHttpRequest或者FullHttpResponse，
                //原因是HTTP解码器在每个HTTP消息中会生成多个消息对象。
                //（1）HttpRequest / HttpResponse；
                //（2）HttpContent(1...n)；
                //（3）LastHttpContent。
                channel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                channel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                //新增Chunked handler，它的主要作用是支持异步发送大的码流（例如大的文件传输），
                //但不占用过多的内存，防止发生Java内存溢出错误。
                channel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                channel.pipeline().addLast("WebSocket-protocol",
                        new WebSocketServerProtocolHandler("/ws"));//include encode and decode
                channel.pipeline().addLast("WebSocket-handler",
                        new WebSocketServerHandler());
            }
        });
        bootstrap.group(boss, worker);
        try {
            ChannelFuture future = bootstrap.bind(8090).sync();
            System.out.println("server start at:8090");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    private  static class HttpSereverHandler extends SimpleChannelInboundHandler<FullHttpRequest>{

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
            if("/ws".equalsIgnoreCase(fullHttpRequest.uri())){
                channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
                return;
            }
            File f =new File("E:\\hyy\\Barrage\\src\\main\\resources\\HelloWorld.html");
            RandomAccessFile file = new RandomAccessFile(f, "r");
            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html; charset-UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,file.length());
            channelHandlerContext.write(response);
            channelHandlerContext.write(new ChunkedNioFile(file.getChannel()));
            ChannelFuture future = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            future.addListener(ChannelFutureListener.CLOSE);
            file.close();
        }
    }
    private class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
            System.out.println("accept:" + textWebSocketFrame.text());
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame("hello world"));
        }
    }

    public static void main(String[] args) {
        HttpSimpleServer simpleServer = new HttpSimpleServer();
        simpleServer.openServer();
    }
}
