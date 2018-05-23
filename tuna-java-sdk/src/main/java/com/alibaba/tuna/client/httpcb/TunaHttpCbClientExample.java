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
 * {@link TunaHttpCbClient} 使用示例代码
 */
public class TunaHttpCbClientExample {

    public static void main(String[] args) {
    /*
     * 1. 创建 Client。参数说明：
     *  > port
     *  > 是否启用 SSL
     */
        TunaHttpCbClient client = new TunaHttpCbClient(8018, false);

        // 2. 创建 消息处理 Handler
        HttpCbMessageHandler messageHandler = new HttpCbMessageHandler<HttpCbMessage, Void>() {

            /**
             * 应用密钥
             */
            public String getSignKey() {
                return "your appSecret";
            }

            /**
             * 为了防止消息篡改，开放平台推送的数据包含签名信息。
             * 字段名为 _aop_signature，假设值为 serverSign。
             *
             * 接收到消息后，SDK 首先会使用秘钥对接收到的内容进行签名，
             * 假设值为 clientSign。
             *
             * 1. 若 serverSign 与 clientSign 相同，则直接调用 {@link #onMessage(Object)} 方法。
             * 2. 若 serverSign 与 clientSign 不同，则调用该方法。若该方法返回 true，则继续
             * 	调用 {@link #onMessage(Object)} 方法；否则直接返回状态码 401。
             */
            public boolean continueOnSignatureValidationFailed(String clientSign, String serverSign) {
                return false;
            }

            /**
             * 消费消息。
             * @throws MessageProcessException 消息消费不成功，如未达重试次数上限，开放平台将会择机重发消息
             */
            public Void onMessage(HttpCbMessage message) throws MessageProcessException {
                System.out.println("message: " + message);
                // do something really meaningful
                return null;
            }
        };
        // 注册 Handler
        client.registerMessageHandler("/pushMessage", messageHandler);

        // 3. 启动 Client
        client.start();

        // 4. 在应用关闭时，关闭客户端
        // client.shutdown();
    }
}
