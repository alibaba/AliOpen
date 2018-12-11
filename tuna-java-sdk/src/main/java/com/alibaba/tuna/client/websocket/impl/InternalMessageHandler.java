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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.alibaba.tuna.client.websocket.TunaWebSocketClient;
import com.alibaba.tuna.client.websocket.WebSocketMessageType;
import com.alibaba.tuna.client.websocket.WebSocketMessage;

public class InternalMessageHandler implements ClientLog.InnerHandler {

	private ThreadPoolExecutor threadPool;
	private TunaWebSocketClient tunaClient;
	protected volatile boolean stopped;


	public InternalMessageHandler(ThreadPoolExecutor threadPool) {
		this.threadPool=threadPool;
	}

	@Override
	public void onMessage(final WebSocketMessage message) {
		while (!stopped) {
			try {
				threadPool.submit(new Runnable() {
					public void run() {
						boolean status = true;
						WebSocketMessageType type = null;
						try {
							type = WebSocketMessageType.valueOf(message.getType());
						} catch(Exception e) {
							ClientLog.error("tuna message type:" + message.getType(), e);
							return;
						}

						switch (type) {
							//处理服务端连接成功确认
							case CONNECT_ACK: {
								tunaClient.setConnect();
								break;
							}
							//处理服务端推送的消息
							case SERVER_PUSH: {
								long start = System.currentTimeMillis();
								try {
									status = tunaClient.getTunaMessageHandler().onMessage(message);
								} catch (Exception e) {
									ClientLog.error("InternalMessageHandler onMessage error", e);
								}
								if (status) {
									long end = System.currentTimeMillis();
									//记录耗时，并上传至服务端
									message.setCostInIsv(end - start);
									//如果是业务消息，则确认。
									tunaClient.confirm(message);
								}
								break;
							}

							case CLOSE: {
								break;
							}

							case SYSTEM: {
								ClientLog.warn("system error:" + message.getContent());
								break;
							}

							default: {
								ClientLog.warn("unknown error:" + message);
								break;
							}
						}
					}
				});

				break;
			} catch (RejectedExecutionException ree) {
				ClientLog.warn(String.format("all tuna worker threads are currently busy"));
				try {
					Thread.sleep(50L);
				} catch (InterruptedException ie) {
					//do nothing
				}
			}
		}

	}

	public void setTunaClient(TunaWebSocketClient tunaClient) {
		this.tunaClient = tunaClient;
	}
	public void stop (){
		stopped = true;

		if (threadPool != null) {
			threadPool.shutdown();
			while (!threadPool.isTerminated()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// ignore,do nothing
				}
			}
		}
	}
}
