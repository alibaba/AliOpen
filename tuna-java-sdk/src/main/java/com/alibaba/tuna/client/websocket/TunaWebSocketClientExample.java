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
 * {@link TunaWebsocketClient} 使用示例代码
 */
public class TunaWebSocketClientExample {

    public static void main(String[] args) throws InterruptedException {
        // 开放平台 1688 环境
        String url = "ws://message.1688.com/websocket";
        // 您的 AppKey
        String appkey = "yourAppKey";
        // 您的应用秘钥
        String secret = "yourSecret";

        // 1. 创建 Client
        TunaWebsocketClient client = new TunaWebsocketClient(appkey, secret, url);
        // 2. 创建 消息处理 Handler
        WebSocketMessageHandler tunaMessageHandler = new WebSocketMessageHandler() {
            /**
             * 消费消息。
             * 如果抛异常或返回 false，表明消费失败，如未达重试次数上限，开放平台将会择机重发消息
             */
            public boolean onMessage(WebSocketMessage message) throws MessageProcessException {
                boolean success = true;
                /* 说明，服务端推送的消息分为2种，
                业务数据：SERVER_PUSH
                系统消息：SYSTEM，如 appKey 与 secret 不匹配等，一般可忽略。*/
                if(WebSocketMessageType.SERVER_PUSH.name().equals(message.getType())){
                    try {
                        System.out.println("message"+message);// json串
                    } catch (Exception e) {
                        success = false;
                    }
                }
                return success;
            }
        };
        client.setTunaMessageHandler(tunaMessageHandler);

        // 3. 启动 Client
        client.connect();

        // 4. 在应用关闭时，关闭客户端
        // client.shutdown();
    }
}
