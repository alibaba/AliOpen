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

import com.alibaba.tuna.fastjson.JSON;

/**
 * WebSocket 通道模式下，接收到的消息内容
 */
public class WebSocketMessage {
	/**
	 * 系统内部字段
	 */
	private Long relatedId;

	private Long relatedMsgTime;

	/**
	 * 应用 AppKey
	 */
	String appKey;

	/**
	 * 应用 AppSecret
	 */
	String secret;

	/**
	 * webocket消息类型
	 */
	String type;

	/**
	 * 消息id
	 */
	private String id;

	/**
	 * 消息推送时间
	 */
	private Long pubTime;

	/**
	 * 消息内容，json串格式
	 */
	private String content;

	/**
	 * 签名
	 */
	private String sign;

	/**
	 * isv端消息处理耗时
	 */
	private long costInIsv;

	/**
	 * 数据来源。包括
	 * MOCK: 测试数据。使用消息测试工具产生。
	 * REAL: 真实数据。此字段为空时默认为真实数据。
	 */
	private String msgSource;

	public long getCostInIsv() {
		return costInIsv;
	}

	public void setCostInIsv(long costInIsv) {
		this.costInIsv = costInIsv;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getPubTime() {
		return pubTime;
	}

	public void setPubTime(Long pubTime) {
		this.pubTime = pubTime;
	}

	public Long getRelatedMsgTime() {
		return relatedMsgTime;
	}

	public void setRelatedMsgTime(Long relatedMsgTime) {
		this.relatedMsgTime = relatedMsgTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long outgoingId) {
		this.relatedId = outgoingId;
	}

	public String getMsgSource() {
		return msgSource;
	}

	public void setMsgSource(String msgSource) {
		this.msgSource = msgSource;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
