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
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * SignatureUtil
 * 
 *
 */
public final class SignatureUtil {
	public static final String HMAC_SHA1 = "HmacSHA1";
	public static final String CHARSET_NAME_UTF8 = "UTF-8";
	public static final Charset CHARSET_UTF8 = Charset
			.forName(CHARSET_NAME_UTF8);
	public static final char[] digital = "0123456789ABCDEF".toCharArray();

	private static SecretKeySpec buildKey(byte[] key) {
		return new SecretKeySpec(key, HMAC_SHA1);
	}

	public static String encodeHexStr(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		StringBuilder str = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			str.append(digital[(bytes[i] & 0xf0) >> 4]);
			str.append(digital[bytes[i] & 0x0f]);
		}
		return str.toString();
	}

	public static byte[] hmacSha1(String path, Map<String, Object> parameters,
			String signingKey) {
		Mac mac;
		try {
			mac = Mac.getInstance(HMAC_SHA1);
			mac.init(buildKey(toBytes(signingKey)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		mac.update(path.getBytes(CHARSET_UTF8));
		List<String> parameterToSort = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			parameterToSort.add(entry.getKey() + entry.getValue());
		}
		Collections.sort(parameterToSort);
		for (String paramToSign : parameterToSort) {
			mac.update(paramToSign.getBytes(CHARSET_UTF8));
		}
		return mac.doFinal();
	}

	public static byte[] hmacSha1(Map<String, Object> parameters,
			String signingKey) {
		Mac mac;
		try {
			mac = Mac.getInstance(HMAC_SHA1);
			mac.init(buildKey(toBytes(signingKey)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		List<String> parameterToSort = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			parameterToSort.add(entry.getKey() + entry.getValue());
		}
		Collections.sort(parameterToSort);
		for (String paramToSign : parameterToSort) {
			mac.update(paramToSign.getBytes(CHARSET_UTF8));
		}
		return mac.doFinal();
	}

	private static byte[] toBytes(final String str) {
		if (str == null) {
			return null;
		}
		try {
			return str.getBytes(CHARSET_NAME_UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
