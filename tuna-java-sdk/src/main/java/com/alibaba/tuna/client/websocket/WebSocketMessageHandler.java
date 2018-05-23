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

import com.alibaba.tuna.client.api.MessageProcessException;

/**
 * WebSocket 通道模式下，消息处理 Handler。需要用户实现。
 */
public interface WebSocketMessageHandler {

	/**
	 * 消息通道客户端收到消息后，会回调该方法处理具体的业务，处理结果可以通过以下两种方式来表述：
	
	 * @return false:消息处理失败，如未达重试次数上限，开放平台；true:消息处理成功。不会再重发。
	 * @throws MessageProcessException 消息处理失败，消息通道将会择机重发消息
	 */
	public boolean onMessage(WebSocketMessage message) throws MessageProcessException;

}
