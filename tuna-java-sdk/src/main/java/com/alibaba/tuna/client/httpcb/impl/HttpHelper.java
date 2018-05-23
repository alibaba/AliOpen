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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class HttpHelper {
	public final static Map<String, String> parseParameters(String query) {
		if (query == null) {
			return new HashMap<String, String>();
		}
		String[] keyValues = query.split("&");
		if (keyValues == null) {
			return new HashMap<String, String>();
		} else {
			Map<String, String> result = new HashMap<String, String>();
			for (String keyValue : keyValues) {
				int pos = keyValue.indexOf("=");
				if (pos == -1) {
					result.put(keyValue, "");
				} else {
					String key = keyValue.substring(0, pos);
					String value = keyValue.substring(pos + 1);
					result.put(key, URLDecoder.decode(value));
				}
			}
			return result;
		}
	}
}
