/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.tuna.client.httpcb.impl.netty;

import java.util.Map;

import com.alibaba.tuna.netty.channel.ChannelInitializer;
import com.alibaba.tuna.netty.channel.ChannelPipeline;
import com.alibaba.tuna.netty.channel.socket.SocketChannel;
import com.alibaba.tuna.netty.handler.codec.http.HttpObjectAggregator;
import com.alibaba.tuna.netty.handler.codec.http.HttpRequestDecoder;
import com.alibaba.tuna.netty.handler.codec.http.HttpResponseEncoder;
import com.alibaba.tuna.netty.handler.codec.http.cors.CorsConfig;
import com.alibaba.tuna.netty.handler.codec.http.cors.CorsHandler;
import com.alibaba.tuna.netty.handler.ssl.SslContext;
import com.alibaba.tuna.netty.handler.stream.ChunkedWriteHandler;
import com.alibaba.tuna.client.httpcb.HttpCbMessageHandler;

/**
 * 
 *
 *
 */
public class HttpCorsServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;

	private final Map<String, HttpCbMessageHandler> messageHandlers;

	public HttpCorsServerInitializer(SslContext sslCtx, Map<String, HttpCbMessageHandler> messageHandlers) {
		this.sslCtx = sslCtx;
		this.messageHandlers = messageHandlers;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		CorsConfig corsConfig = CorsConfig.withAnyOrigin().build();
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpResponseEncoder());
		pipeline.addLast(new HttpRequestDecoder());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(new CorsHandler(corsConfig));
		pipeline.addLast(new SimpleHttpProcessorHandler(messageHandlers));
	}

}
