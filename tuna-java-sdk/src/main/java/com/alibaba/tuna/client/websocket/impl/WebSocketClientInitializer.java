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


import com.alibaba.tuna.netty.channel.ChannelInitializer;
import com.alibaba.tuna.netty.channel.ChannelPipeline;
import com.alibaba.tuna.netty.channel.socket.SocketChannel;
import com.alibaba.tuna.netty.handler.codec.http.HttpClientCodec;
import com.alibaba.tuna.netty.handler.codec.http.HttpObjectAggregator;
import com.alibaba.tuna.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import com.alibaba.tuna.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;

/**
 * 添加websocket协议支持，通道创建时，pipeline默认添加webscoket handler
 */
public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    public static int MAX_LENGTH=65536;

    private TextWebSocketFrameHandler textWebSocketFrameHandler;
    private WebSocketClientHandshaker webSocketClientHandshaker;

     WebSocketClientInitializer(WebSocketClientHandshaker webSocketClientHandshaker, TextWebSocketFrameHandler textWebSocketFrameHandler){
         this.textWebSocketFrameHandler = textWebSocketFrameHandler;
         this.webSocketClientHandshaker=webSocketClientHandshaker;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception{
        ChannelPipeline p = ch.pipeline();
        //if (sslCtx != null) {
        //p.addLast(sslCtx.newHandler(ch.alloc(), host,
        //	port));
        //}
        p.addLast(new HttpClientCodec());//Http协议编码解码器
        p.addLast(new HttpObjectAggregator(MAX_LENGTH));//聚合 HttpRequest
        p.addLast(new WebSocketClientProtocolHandler(webSocketClientHandshaker,true));
        p.addLast(textWebSocketFrameHandler);

    }
}