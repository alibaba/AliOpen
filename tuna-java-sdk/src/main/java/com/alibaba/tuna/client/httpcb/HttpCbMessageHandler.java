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
package com.alibaba.tuna.client.httpcb;

import com.alibaba.tuna.client.api.MessageProcessException;

/**
 * Http Callback 通道模式下，消息处理 Handler。需要用户实现。
 */
public interface HttpCbMessageHandler<Message, Result> {

	/**
	 * appSecret
	 *
	 * @return
	 * @see #continueOnSignatureValidationFailed
	 */
	String getSignKey();

	/**
	 * 为了防止消息篡改，消息中心推送的数据包含签名信息。字段名为 _aop_signature，值为 {@code signInServerSide}。
	 * 接收到消息后，SDK 首先会使用 appSecret 作为签名 key 对接收到的内容进行签名，值为 {@code signFromClient}。
	 *
	 * 1. 若 {@code signInServerSide} 与 {@code signFromClient} 相同，则直接调用 {@link #onMessage(Object)} 方法。
	 * 2. 若 {@code signInServerSide} 与 {@code signFromClient} 不同，则调用该方法。若该方法返回 true，则继续
	 * 	调用 {@link #onMessage(Object)} 方法；否则直接返回状态码 401。
	 * 
	 * @param signFromClient
	 * @param signInServerSide
	 * @return
	 */
	boolean continueOnSignatureValidationFailed(String signFromClient, String signInServerSide);

	/**
	 * 消费消息。
	 * 
	 * @param message
	 * @return
	 * @throws MessageProcessException 消息消费不成功，如未达重试次数上限，开放平台将会择机重发消息
	 */
	Result onMessage(Message message) throws MessageProcessException;

}
