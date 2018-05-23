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
package com.alibaba.tuna.client.httpcb.impl.codec;

import java.util.Map;

import com.alibaba.tuna.fastjson.JSON;
import com.alibaba.tuna.client.httpcb.impl.AlibabaHttpRequest;

/**
 *
 *
 */
public class MessageDecoder {

	public <T> void decodeTo(Map<String, String> requestParameter, Class<T> resultType,
			AlibabaHttpRequest<T> alibabaHttpRequest) {
		String signature = requestParameter.get("_aop_signature");
		alibabaHttpRequest.setSignatureFromClient(signature);
		String message = requestParameter.get("message");
		T result = JSON.parseObject(message, resultType);
		alibabaHttpRequest.setRequestContent(result);
	}
}
