/*
 * Copyright (C) 2012 The Netty Project
 * Copyright (C) 1999-2018 Alibaba Group Holding Limited
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.tuna.client.websocket.impl;

import com.alibaba.tuna.client.websocket.WebSocketMessageType;
import com.alibaba.tuna.client.websocket.WebSocketMessage;
import com.alibaba.tuna.netty.bootstrap.Bootstrap;
import com.alibaba.tuna.netty.channel.Channel;
import com.alibaba.tuna.netty.channel.ChannelOption;
import com.alibaba.tuna.netty.channel.EventLoopGroup;
import com.alibaba.tuna.netty.channel.nio.NioEventLoopGroup;
import com.alibaba.tuna.netty.channel.socket.nio.NioSocketChannel;
import com.alibaba.tuna.netty.handler.codec.http.DefaultHttpHeaders;
import com.alibaba.tuna.netty.handler.codec.http.HttpHeaders;
import com.alibaba.tuna.netty.handler.codec.http.websocketx.*;
import com.alibaba.tuna.util.ClientUtils;

import java.net.URI;

import com.alibaba.tuna.fastjson.JSON;

/**
 * websocket client封装
 * @author qiheng
 *
 */
public final class WebSocketClient {
	public static String CLIENT_VERSION="client_version";

	private EventLoopGroup group;
	private Bootstrap boot;
	private ClientLog.InnerHandler innerHandler;
	private String oceanUrl;

	private Channel channel = null;

	private static final int DEFAULT_PORT = 80;

	//内部使用
	private String host;
	private int port;

	public WebSocketClient(String oceanUrl,ClientLog.InnerHandler innerHandler){
		this.oceanUrl = oceanUrl;
		this.innerHandler = innerHandler;
	}

	public void connect() throws Exception {
		try {
			//关闭上次的资源
			if(group!=null){
				group.shutdownGracefully();
			}

			if(channel!=null){
				channel.close();
			}

			boot = new Bootstrap();
			group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

			URI uri = new URI(oceanUrl);
			String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
			final boolean ssl = "wss".equalsIgnoreCase(scheme);

			port = uri.getPort() == -1 ? DEFAULT_PORT : uri.getPort();
			host = uri.getHost();

			//初始化handshake
			HttpHeaders httpHeaders = new DefaultHttpHeaders();
			httpHeaders.add(CLIENT_VERSION,1);
			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
			// or
			// V00.
			// If you change it to V00, ping is not supported and remember to
			// change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
			// pipeline.
			WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, httpHeaders);
			TextWebSocketFrameHandler textWebSocketFrameHandler= new TextWebSocketFrameHandler();
			textWebSocketFrameHandler.setInnerHandler(innerHandler);

			boot.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new WebSocketClientInitializer(webSocketClientHandshaker,textWebSocketFrameHandler));


			doConnect();
		} catch (Exception e) {
			ClientLog.error("webclient connect error" ,e);
			throw e;
		}

	}

	private void doConnect() throws Exception{
		channel = boot.connect(host, port).sync().channel();
	}

	public void send(Object msg) {
		if (channel != null && channel.isActive()) {
			TextWebSocketFrame frame = new TextWebSocketFrame(
					JSON.toJSONString(msg));
			channel.writeAndFlush(frame);
		}
	}

	public boolean isValid() {
		if (channel != null && channel.isActive()) {
			return true;
		} else {
			return false;
		}
	}

	public void shutDown() {
		ClientLog.warn("webclient shutdown");

		if (group != null) {
			ClientLog.warn("shudown group");
			group.shutdownGracefully();
		}
		if (channel != null) {
			ClientLog.warn("channel close");
			channel.close();
		}
	}

	public void sendClose() {
		if (channel != null && channel.isActive()) {
			channel.writeAndFlush(new CloseWebSocketFrame());
		}
	}

	public void sendConnect(String appkey,String secret) {

		WebSocketMessage tm = new WebSocketMessage();
		tm.setAppKey(appkey);
		tm.setType(WebSocketMessageType.CONNECT.name());
		tm.setPubTime(System.currentTimeMillis());
		try {
			tm.setSign(ClientUtils.sign(tm, secret));
		} catch (Exception e) {
			e.printStackTrace();
			ClientLog.warn("TunaWebsocketClient sign error" + e.getMessage());
		}
		ClientLog.warn("TunaWebsocketClient doconnect" + JSON.toJSONString(tm));

		send(tm);
	}

}