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

import com.alibaba.tuna.client.websocket.TunaWebSocketClient;
import com.alibaba.tuna.client.websocket.WebSocketMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientLog {

	private static final Log log = LogFactory.getLog(TunaWebSocketClient.class);
	public static void warn(Object msg){
		log.warn(msg);
	}
	public static void error(Object msg,Throwable e){
		log.error(msg,e);
	}
	public static void info(Object msg){
		log.info(msg);
	}

	/**
     * 内部消息处理
     */
    public interface InnerHandler {
		/**
		 * SDK处理收到的WebSocketMessage消息逻辑
		 * @param message
		 */
		void onMessage(WebSocketMessage message);

		/**
		 *
		 * @param tunaClient
		 */
        void setTunaClient(TunaWebSocketClient tunaClient);

        void stop();

    }
}
