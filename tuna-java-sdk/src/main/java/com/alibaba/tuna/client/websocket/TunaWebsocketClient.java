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
package com.alibaba.tuna.client.websocket;

import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.tuna.client.api.ClientStartException;
import com.alibaba.tuna.client.api.TunaClient;
import com.alibaba.tuna.client.websocket.impl.ClientLog;
import com.alibaba.tuna.client.websocket.impl.InternalMessageHandler;
import com.alibaba.tuna.client.websocket.impl.NamedThreadFactory;
import com.alibaba.tuna.client.websocket.impl.WebSocketClient;
import com.alibaba.tuna.fastjson.JSON;
import com.alibaba.tuna.util.ClientUtils;

/**
 * WebSocket 通道模式下，客户端实现
 */
public class TunaWebSocketClient implements TunaClient {
	/**
	 * 消息缓冲队列大小
	 */
	public static  int QUEUE_SIZE = 2000;
	/**
	 * SDK TunaWebSocketClient默认并发处理的线程数量
	 */
	public static  int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 40;
	/**
	 * 长链心跳时间，单位s
	 */
	public static long FETCH_PERIOD = 30;
	/**
	 * 长链重连间隔时间，单位s
	 */
	public static long RECONNECT_INTERVAL = 10;
	/**
	 * appKey,开放平台唯一标识
	 */
	private String appKey;
	/**
	 * appKey对应的密钥
	 */
	private String secret;
	/**
	 * 开放平台推送服务端地址
	 */
	private String oceanUrl;
	/**
	 * 客户端接收消息线程数
	 */
	private int threadNum;

	private WebSocketClient webSocketClient;
	private WebSocketMessageHandler tunaMessageHandler;
	private InternalMessageHandler internalMessageHandler;

	private ScheduledExecutorService heartBeatTimer;
	private ScheduledExecutorService reconnectTimer;

	/**
	 * 当前是否和服务端保持长链接（双方都感知）
	 */
	final AtomicBoolean isConnect = new AtomicBoolean(false);

	public TunaWebSocketClient(String appKey, String secret, String url) {
		this(appKey, secret, url, DEFAULT_THREAD_COUNT);
	}

	public TunaWebSocketClient(String appKey, String secret, String url, int threadNum) {
		super();
		ClientLog.warn("TunaWebSocketClient init,appKey," + appKey + ",secret," + secret);
		this.appKey = appKey;
		this.secret = secret;
		this.oceanUrl = url;
		this.threadNum = (threadNum <= 0) ? DEFAULT_THREAD_COUNT : threadNum;

		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threadNum/4, threadNum,
				FETCH_PERIOD*2, TimeUnit.MICROSECONDS,
				new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
				new NamedThreadFactory("tuna-worker"));

		internalMessageHandler = new InternalMessageHandler(threadPool);
		internalMessageHandler.setTunaClient(this);

		webSocketClient = new WebSocketClient(url, internalMessageHandler);
	}

	/**
	 * 启动链接
	 * @throws ClientStartException
	 */
	public void connect() throws ClientStartException {
		ClientLog.warn("connect");
		if (isConnect()) {
			ClientLog.warn("already connected");
			return;
		}

		try {
			webSocketClient.connect();
		} catch (Exception e) {
			ClientLog.error("connect error", e);
			throw new ClientStartException(e);
		}
		this.webSocketClient.sendConnect(appKey, secret);
		this.doHeartBeat();
		this.startReconnect();
	}

	private void doHeartBeat() {
		this.heartBeatTimer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("heartbeat-schedule-pool", Boolean.TRUE, Thread.NORM_PRIORITY));
		TimerTask heartBeatTimerTask = new TimerTask() {
			public void run() {
				if (isConnect()) {
					ClientLog.warn("heartBeatTimerTask start");
					WebSocketMessage wsm = new WebSocketMessage();
					wsm.setAppKey(appKey);
					wsm.setType(WebSocketMessageType.HEARTBEAT.name());
					wsm.setPubTime(System.currentTimeMillis());
					try {
						wsm.setSign(ClientUtils.sign(wsm, secret));
					} catch (Exception e) {
						ClientLog.warn("TunaWebSocketClient sign error: " + e.getMessage());
					}

					webSocketClient.send(wsm);
					ClientLog.warn("heartBeatTimerTask end " + JSON.toJSONString(wsm));
				} else {
					//do nothing
				}
			}
		};
		this.heartBeatTimer.scheduleAtFixedRate(heartBeatTimerTask, FETCH_PERIOD * 1000L, FETCH_PERIOD * 1000L, TimeUnit.MILLISECONDS);
	}

	private void stopHeartBeat() {
		ClientLog.warn("stopHeartBeat start");

		if (this.heartBeatTimer != null) {
			this.heartBeatTimer.shutdown();
			this.heartBeatTimer = null;
			ClientLog.warn("stopHeartBeat end");
		}
	}

	private void stopReconnect() {
		ClientLog.warn("stopReconnect start");

		if (this.reconnectTimer != null) {
			this.reconnectTimer.shutdown();
			this.reconnectTimer = null;
			ClientLog.warn("stopReconnect end");

		}
	}

	void send(WebSocketMessage message) {
		ClientLog.warn("WebSocket client send:" + JSON.toJSONString(message));
		this.webSocketClient.send(message);
	}

	private void startReconnect() {
		this.reconnectTimer = Executors.newSingleThreadScheduledExecutor();
		this.reconnectTimer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("reconnect-schedule-pool", Boolean.TRUE, Thread.NORM_PRIORITY));
		this.reconnectTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					ClientLog.warn("reconnect start");
					if (!webSocketClient.isValid()) {
						//链接已经断开，重新链接
						isConnect.set(false);
						webSocketClient.connect();
						webSocketClient.sendConnect(appKey, secret);
					} else if (!isConnect.get() && webSocketClient.isValid()) {
						//链接还在，但是未保持WebSocket长链
						isConnect.set(false);
						webSocketClient.sendConnect(appKey, secret);
					} else {
						ClientLog.info("reconnect end,no need reconnect");
					}
				} catch (Exception e) {
					ClientLog.error("reconnect error", e);

				}
			}
		}, RECONNECT_INTERVAL * 1000L, RECONNECT_INTERVAL * 1000L, TimeUnit.MILLISECONDS);
	}

	@Override
	public void start() throws ClientStartException {
		connect();
	}

	public void shutdown() {
		ClientLog.warn("shutdownGracefully start");

		this.stopHeartBeat();
		this.stopReconnect();
		webSocketClient.sendClose();
		internalMessageHandler.stop();
		try {
			ClientLog.warn("shutdownGracefully sleep start");

			Thread.sleep(2000);
			ClientLog.warn("shutdownGracefully sleep end");
		} catch (InterruptedException e) {
			ClientLog.error("shutdownGracefully Interrupted", e);
		}
		isConnect.set(false);
		this.shutdownFinally();

	}

	private void shutdownFinally() {
		ClientLog.warn("shutdownFinally start");

		webSocketClient.shutDown();
		ClientLog.warn("shutdownFinally end");

	}

	public void confirm(WebSocketMessage message) {
		WebSocketMessage confirmMessage = new WebSocketMessage();
		confirmMessage.setAppKey(this.appKey);
		confirmMessage.setId(this.getUUID());
		confirmMessage.setPubTime(System.currentTimeMillis());
		confirmMessage.setRelatedMsgTime(message.getPubTime());
		confirmMessage.setRelatedId(Long.parseLong(message.getId()));
		confirmMessage.setType(WebSocketMessageType.CONFIRM.name());
		confirmMessage.setCostInIsv(message.getCostInIsv());
		confirmMessage.setMsgSource(message.getMsgSource());
		this.send(confirmMessage);
	}

	private String getUUID() {
		String s = UUID.randomUUID().toString();
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
				+ s.substring(19, 23) + s.substring(24);
	}


	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getOceanUrl() {
		return oceanUrl;
	}

	public void setOceanUrl(String oceanUrl) {
		this.oceanUrl = oceanUrl;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public WebSocketMessageHandler getTunaMessageHandler() {
		return tunaMessageHandler;
	}

	public void setTunaMessageHandler(WebSocketMessageHandler tunaMessageHandler) {
		this.tunaMessageHandler = tunaMessageHandler;
	}

	public void setConnect() {
		this.isConnect.set(true);
	}

	/**
	 * 是否正常连接
	 * @return
     */
	public boolean isConnect() {
		if (isConnect.get() && webSocketClient.isValid()) {
			return true;
		}
		return false;
	}
}
