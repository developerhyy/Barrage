package com.hy.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
/**
 * @ Author     ：hyy.
 * @ Date       ：Created in 11:36 2018/11/14
 * @ Description：${description}
 * @ Modified By：hyy.
 * @Version: $version$
 */
public class HttpSimpleServer {
    public void openServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(NioServerSocketChannel.class);
        EventLoopGroup boot = new NioEventLoopGroup(1);
        EventLoopGroup work = new NioEventLoopGroup(8);
        bootstrap.group(boot, work);
        bootstrap.childHandler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast("http-decode", new HttpRequestDecoder());//解码request 1
                ch.pipeline().addLast("http-encode", new HttpResponseEncoder());// 编码response 3
                ch.pipeline().addLast("http-server", new HttpServerHandler()); // 业务处理2 //-handler
            }
        });
        try {
            ChannelFuture f = bootstrap.bind(8081).sync();
            System.out.println("服务启动成功：8081");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boot.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    private static class HttpServerHandler extends SimpleChannelInboundHandler {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

            DefaultFullHttpResponse response =
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
            String src = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>hello hy</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "hello hy\n" +
                    "</body>\n" +
                    "</html>";
            response.content().writeBytes(src.getBytes("UTF-8"));
            //ChannelFuture f = ctx.writeAndFlush(response);
            ChannelFuture f=ctx.channel().writeAndFlush(response);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
    public static void main(String[] args) {
        HttpSimpleServer server = new HttpSimpleServer();
        server.openServer();
    }
}
