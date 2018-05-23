/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.tuna.netty.handler.codec.http.websocketx;

import com.alibaba.tuna.netty.channel.ChannelFuture;
import com.alibaba.tuna.netty.channel.ChannelFutureListener;
import com.alibaba.tuna.netty.channel.ChannelHandlerContext;
import com.alibaba.tuna.netty.channel.ChannelInboundHandlerAdapter;
import com.alibaba.tuna.netty.channel.ChannelPipeline;
import com.alibaba.tuna.netty.handler.codec.http.DefaultFullHttpResponse;
import com.alibaba.tuna.netty.handler.codec.http.FullHttpRequest;
import com.alibaba.tuna.netty.handler.codec.http.HttpHeaders;
import com.alibaba.tuna.netty.handler.codec.http.HttpRequest;
import com.alibaba.tuna.netty.handler.codec.http.HttpResponse;
import com.alibaba.tuna.netty.handler.ssl.SslHandler;

import static com.alibaba.tuna.netty.handler.codec.http.HttpHeaders.*;
import static com.alibaba.tuna.netty.handler.codec.http.HttpMethod.*;
import static com.alibaba.tuna.netty.handler.codec.http.HttpResponseStatus.*;
import static com.alibaba.tuna.netty.handler.codec.http.HttpVersion.*;

/**
 * Handles the HTTP handshake (the HTTP Upgrade request) for {@link WebSocketServerProtocolHandler}.
 */
class WebSocketServerProtocolHandshakeHandler
        extends ChannelInboundHandlerAdapter {

    private final String websocketPath;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadSize;

    WebSocketServerProtocolHandshakeHandler(String websocketPath, String subprotocols,
            boolean allowExtensions, int maxFrameSize) {
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.allowExtensions = allowExtensions;
        this.maxFramePayloadSize = maxFrameSize;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest req = (FullHttpRequest) msg;
        try {
            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }

            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(ctx.pipeline(), req, websocketPath), subprotocols,
                            allowExtensions, maxFramePayloadSize);
            final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
                handshakeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            ctx.fireExceptionCaught(future.cause());
                        } else {
                            ctx.fireUserEventTriggered(
                                    WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
                        }
                    }
                });
                WebSocketServerProtocolHandler.setHandshaker(ctx, handshaker);
                ctx.pipeline().replace(this, "WS403Responder",
                        WebSocketServerProtocolHandler.forbiddenHttpRequestResponder());
            }
        } finally {
            req.release();
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
        String protocol = "ws";
        if (cp.get(SslHandler.class) != null) {
            // SSL in use so use Secure WebSockets
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaders.Names.HOST) + path;
    }

}
