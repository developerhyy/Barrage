package com.hy.netty.Barrage;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @ Author     ：hyy.
 * @ Date       ：Created in 10:15 2018/11/22
 * @ Description：${description}
 * @ Modified By：hyy.
 * @Version: $version$
 */
public class WebsocketBarrageServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http-decoder", new HttpRequestDecoder());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("http-encoder", new HttpResponseEncoder());
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        pipeline.addLast("http-request", new HttpRequestHandler("/ws"));
        pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast("WebSocket-request", new TextWebSocketFrameHandler());
    }
}
