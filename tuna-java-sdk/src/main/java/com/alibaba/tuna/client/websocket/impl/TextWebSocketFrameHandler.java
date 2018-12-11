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

import com.alibaba.tuna.client.websocket.WebSocketMessage;
import com.alibaba.tuna.client.websocket.impl.ClientLog;
import com.alibaba.tuna.fastjson.JSON;
import com.alibaba.tuna.netty.channel.ChannelHandlerContext;
import com.alibaba.tuna.netty.channel.SimpleChannelInboundHandler;
import com.alibaba.tuna.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.alibaba.tuna.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * 处理TextWebSocketFrame的handler
 * Created by yichun.wangyc on 2018/2/23.
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ClientLog.InnerHandler innerHandler;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            //do nothing
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
        ClientLog.warn("WebSocket Client received message: " + textFrame.text());
        WebSocketMessage message = JSON.parseObject(textFrame.text(), WebSocketMessage.class);
        innerHandler.onMessage(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ClientLog.error(cause.getMessage(), cause);
        ctx.close();
    }

    public void setInnerHandler(ClientLog.InnerHandler innerHandler) {
        this.innerHandler = innerHandler;
    }
}
