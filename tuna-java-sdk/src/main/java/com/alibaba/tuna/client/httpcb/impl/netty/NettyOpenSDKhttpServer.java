package com.alibaba.tuna.client.httpcb.impl.netty;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;

import com.alibaba.tuna.client.api.ClientStartException;
import com.alibaba.tuna.netty.bootstrap.ServerBootstrap;
import com.alibaba.tuna.netty.channel.EventLoopGroup;
import com.alibaba.tuna.netty.channel.nio.NioEventLoopGroup;
import com.alibaba.tuna.netty.channel.socket.nio.NioServerSocketChannel;
import com.alibaba.tuna.netty.handler.logging.LogLevel;
import com.alibaba.tuna.netty.handler.logging.LoggingHandler;
import com.alibaba.tuna.netty.handler.ssl.SslContext;
import com.alibaba.tuna.netty.handler.ssl.util.SelfSignedCertificate;
import com.alibaba.tuna.client.httpcb.HttpCbMessageHandler;
import com.alibaba.tuna.client.httpcb.impl.OpenSDKHttpServer;

/**
 *
 *
 */
public class NettyOpenSDKhttpServer implements OpenSDKHttpServer {
	private int port;
	private boolean useSSL;
	private Map<String, HttpCbMessageHandler> messageHandlers = new HashMap<String, HttpCbMessageHandler>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public void setMessageHandlers(Map<String, HttpCbMessageHandler> messageHandlers) {
		this.messageHandlers = messageHandlers;
	}

	public void registerMessageHandler(String key, HttpCbMessageHandler messageHandler) {
		this.messageHandlers.put(key, messageHandler);
	}

	@Override
	public void start() throws ClientStartException {
		final SslContext sslCtx;
		if (useSSL) {
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
			} catch (CertificateException cettifactionException) {
				throw new ClientStartException("Exception occured when initial with SSL.", cettifactionException);
			} catch (SSLException sslException) {
				throw new ClientStartException("Exception occured when initial with SSL.", sslException);
			}

		} else {
			sslCtx = null;
		}

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new HttpCorsServerInitializer(sslCtx, messageHandlers));

			bootstrap.bind(port).sync().channel().closeFuture().sync();
		} catch (Exception exception) {
			throw new ClientStartException("Exception occured when initial.", exception);
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	public void shutdown() {

	}

}
