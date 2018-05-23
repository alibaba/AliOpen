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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.alibaba.tuna.fastjson.JSON;
import com.alibaba.tuna.util.SignatureUtil;

/**
 * 模拟消息中心向客户端消息。
 */
public class TunaHttpCbServerMock {
    public static String buildQuery(Map<String, Object> params) throws IOException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder query = new StringBuilder();
        Set<Entry<String, Object>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, Object> entry : entries) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }
                query.append(name).append("=").append(URLEncoder.encode(String.valueOf(value), "utf-8"));
            }
        }

        return query.toString();
    }

    public static void main(String[] args) {
        try {
            URL url = new URL("http", "localhost", 8018, "/pushMessage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            HttpCbMessage pm = new HttpCbMessage();
            pm.setGmtBorn(System.currentTimeMillis());
            Map parameters = new HashMap();
            parameters.put("name", "Vincent");
            parameters.put("age", "18");
            parameters.put("city", "Hangzhou");
            parameters.put("title", "ISV Developer");
            pm.setData(parameters);
            pm.setExtraInfo(parameters);
            pm.setMsgId(UUID.randomUUID().toString());
            pm.setType("SYSTEM_TEST_PLATFORM");
            pm.setUserInfo("Vincent");

            String json = JSON.toJSONString(pm);

            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("message", json);

            byte[] g = SignatureUtil.hmacSha1(requestBody, "test");

            requestBody.put("_aop_signature", SignatureUtil.encodeHexStr(g) + 1);

            String query = buildQuery(requestBody);
            byte[] content = {};
            if (query != null) {
                content = query.getBytes("utf-8");
            }
            OutputStream out = conn.getOutputStream();
            out.write(content);
            InputStream is = conn.getInputStream();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
