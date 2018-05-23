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
package com.alibaba.tuna.client.httpcb.impl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class AlibabaHttpRequest<T> {
	private String path;
	private Map<String, String> header = new HashMap<String, String>();
	private String signatureFromClient;
	private T requestContent;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public String getSignatureFromClient() {
		return signatureFromClient;
	}

	public void setSignatureFromClient(String signatureFromClient) {
		this.signatureFromClient = signatureFromClient;
	}

	public T getRequestContent() {
		return requestContent;
	}

	public void setRequestContent(T requestContent) {
		this.requestContent = requestContent;
	}

}
