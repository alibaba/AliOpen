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

import com.alibaba.tuna.client.api.ClientStartException;
import com.alibaba.tuna.client.api.TunaClient;
import com.alibaba.tuna.client.httpcb.impl.netty.NettyOpenSDKhttpServer;

/**
 * Http Callback 通道模式下，客户端实现
 */
public class TunaHttpCbClient implements TunaClient {
    private NettyOpenSDKhttpServer openSDKHttpServer = new NettyOpenSDKhttpServer();

    public TunaHttpCbClient(int port) {
        this(port, false);
    }

    public TunaHttpCbClient(int port, boolean useSSL) {
        openSDKHttpServer.setPort(port);
        openSDKHttpServer.setUseSSL(useSSL);
    }

    public void registerMessageHandler(String key, HttpCbMessageHandler messageHandler) {
        openSDKHttpServer.registerMessageHandler(key, messageHandler);
    }

    @Override
    public void start() throws ClientStartException {
        openSDKHttpServer.start();
    }

    @Override
    public void shutdown() {
        openSDKHttpServer.shutdown();
    }
}
