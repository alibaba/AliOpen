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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
public class TunaWebsocketClient implements TunaClient {
	public static  int QUEUE_SIZE = 2000; // 消息缓冲队列大小
	public static  int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 10; // 并发处理的线程数量
	public static long FETCH_PEROID = 30; //长链心跳时间，单位s
	public static long RECONNECT_INTERVAL = 10;//长链重连间隔时间，单位s

	/**
	 * appkey,开放平台唯一标识
	 */
	private String appKey;
	/**
	 * appkey对应的密钥
	 */
	private String secret;
	/**
	 * 开放王平台推送服务端地址
	 */
	private String oceanUrl;

	private WebSocketClient webSocketClient;
	private WebSocketMessageHandler tunaMessageHandler;
	private InternalMessageHandler internalMessageHandler;

	private Timer heartBeatTimer;
	private Timer reconnectTimer;

	/**
	 * 当前是否和服务端保持长链接（双方都感知）
	 */
	final AtomicBoolean isConnect = new AtomicBoolean(false);

	public TunaWebsocketClient(String appKey, String secret, String url) {
		super();
		ClientLog.warn("TunaWebsocketClient init,appkey," + appKey + ",secret," + secret);
		this.appKey = appKey;
		this.secret = secret;
		this.oceanUrl = url;

		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
				FETCH_PEROID*2, TimeUnit.MICROSECONDS,
				new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
				new NamedThreadFactory("tuna-worker"));

		internalMessageHandler = new InternalMessageHandler(threadPool);
		internalMessageHandler.setTunaClient(this);

		webSocketClient = new WebSocketClient(url,internalMessageHandler);
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
		this.webSocketClient.sendConnect(appKey,secret);
		this.doHearBeat();
		this.startReconnect();
	}

	private void doHearBeat() {
		TimerTask heartBeatTimerTask = new TimerTask() {
			public void run() {
				if(isConnect()){
					ClientLog.warn("heartBeatTimerTask start");
					WebSocketMessage tm = new WebSocketMessage();
					tm.setAppKey(appKey);
					tm.setType(WebSocketMessageType.HEARTBEAT.name());
					tm.setPubTime(System.currentTimeMillis());
					try {
						tm.setSign(ClientUtils.sign(tm, secret));
					} catch (Exception e) {
						ClientLog.warn("TunaWebsocketClient sign error:" + e.getMessage());
					}

					webSocketClient.send(tm);
					ClientLog.warn("heartBeatTimerTask end" + JSON.toJSONString(tm));
				}else{
					//do nothing
				}
			}
		};
		Date begin = new Date();
		begin.setTime(begin.getTime() + FETCH_PEROID * 1000L);
		this.heartBeatTimer = new Timer("tuna-heartbeat", true);
		this.heartBeatTimer.schedule(heartBeatTimerTask, begin, FETCH_PEROID * 1000L);
	}

	private void stopHearBeat() {
		ClientLog.warn("stopHearBeat start");

		if (this.heartBeatTimer != null) {
			this.heartBeatTimer.cancel();
			this.heartBeatTimer = null;
			ClientLog.warn("stopHearBeat canceled");
		}
	}

	private void stopReconnect() {
		ClientLog.warn("stopReconnect start");

		if (this.reconnectTimer != null) {
			this.reconnectTimer.cancel();
			this.reconnectTimer = null;
			ClientLog.warn("reconnectTimer stopped");

		}
	}

	void send(WebSocketMessage message) {
		ClientLog.warn("websocket client send:" + JSON.toJSONString(message));
		this.webSocketClient.send(message);
	}

	private void startReconnect() {
		this.reconnectTimer = new Timer("tuna-reconnect", true);
		this.reconnectTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					ClientLog.warn("reconnect start");
					if (!webSocketClient.isValid()) {//链接已经断开，重新链接
						isConnect.set(false);
						webSocketClient.connect();
						webSocketClient.sendConnect(appKey,secret);
					}else if (!isConnect.get() && webSocketClient.isValid()){ //链接还在，但是未保持websocket长链
						isConnect.set(false);
						webSocketClient.sendConnect(appKey,secret);
					} else{
						ClientLog.info("reconnect end,no need reconnect");
					}
				} catch (Exception e) {
					ClientLog.error("reconnect error", e);

				}
			}
		}, RECONNECT_INTERVAL * 1000L, RECONNECT_INTERVAL * 1000L);
	}

	@Override
	public void start() throws ClientStartException {
		connect();
	}

	public void shutdown() {
		ClientLog.warn("shutdownGracefully start");

		this.stopHearBeat();
		this.stopReconnect();
		internalMessageHandler.stop();
		webSocketClient.sendClose();
		try {
			ClientLog.warn("shutdownGracefully sleep start");

			Thread.sleep(2000);
			ClientLog.warn("shutdownGracefully sleep end");
		} catch (InterruptedException e) {
			ClientLog.error("shutdownGracefully Interrupted", e);
		}
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

	public WebSocketMessageHandler getTunaMessageHandler() {
		return tunaMessageHandler;
	}

	public void setTunaMessageHandler(WebSocketMessageHandler tunaMessageHandler) {
		this.tunaMessageHandler = tunaMessageHandler;
	}

	public void setConnect(){
		this.isConnect.set(true);
	}

	/**
	 * 是否正常连接
	 * @return
     */
	public boolean isConnect(){
		if(isConnect.get() && webSocketClient.isValid()){
			return true;
		}
		return false;
	}
}
