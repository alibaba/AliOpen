/*
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
package com.alibaba.tuna.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import com.alibaba.tuna.client.websocket.WebSocketMessage;

public   class ClientUtils {


	/**
	 *  对发送的消息进行签名
	 * @param tm 要签名的消息体
	 * @param secret 密钥
	*/
	public static String sign(WebSocketMessage tm , String secret) throws UnsupportedEncodingException, GeneralSecurityException  {

	
		StringBuilder query = new StringBuilder(secret);

		if(tm.getAppKey()!=null&&!"".equals(tm.getAppKey())){
			query.append(tm.getAppKey());
		}

		if(tm.getContent()!=null&&!"".equals(tm.getContent())){
			query.append(tm.getContent());
		}

		if(tm.getPubTime()!=null&&!"".equals(tm.getPubTime())){
			query.append(tm.getPubTime());
		}
		byte[] bytes = encryptMD5(query.toString());

		return byte2hex(bytes);
	}
 
 

	private static byte[] encryptMD5(String data) throws UnsupportedEncodingException, GeneralSecurityException   {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
 			throw gse;
		}
		return bytes;
	}

	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

}
