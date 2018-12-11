/**
 * 
 */
package com.alibaba.tuna.client.httpcb.impl.netty;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.tuna.client.api.MessageProcessException;
import com.alibaba.tuna.netty.buffer.ByteBuf;
import com.alibaba.tuna.netty.channel.ChannelFutureListener;
import com.alibaba.tuna.netty.channel.ChannelHandlerContext;
import com.alibaba.tuna.netty.channel.SimpleChannelInboundHandler;
import com.alibaba.tuna.netty.handler.codec.http.DefaultFullHttpRequest;
import com.alibaba.tuna.netty.handler.codec.http.DefaultFullHttpResponse;
import com.alibaba.tuna.netty.handler.codec.http.DefaultHttpRequest;
import com.alibaba.tuna.netty.handler.codec.http.FullHttpResponse;
import com.alibaba.tuna.netty.handler.codec.http.HttpHeaders;
import com.alibaba.tuna.netty.handler.codec.http.HttpResponseStatus;
import com.alibaba.tuna.netty.handler.codec.http.HttpVersion;
import com.alibaba.tuna.client.httpcb.impl.AlibabaHttpRequest;
import com.alibaba.tuna.client.httpcb.impl.HttpHelper;
import com.alibaba.tuna.client.httpcb.HttpCbMessageHandler;
import com.alibaba.tuna.client.httpcb.impl.Parts;
import com.alibaba.tuna.client.httpcb.impl.codec.MessageDecoder;
import com.alibaba.tuna.util.GenericsUtil;
import com.alibaba.tuna.util.ParameterizedTypeUtil;
import com.alibaba.tuna.util.SignatureUtil;
import com.alibaba.tuna.util.logging.InternalLogger;
import com.alibaba.tuna.util.logging.InternalLoggerFactory;

/**
 *
 *
 */
public class SimpleHttpProcessorHandler extends SimpleChannelInboundHandler<DefaultHttpRequest> {

	protected final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

	private Map<String, HttpCbMessageHandler> messageHandlers;

	private MessageDecoder messageDecoder;

	public SimpleHttpProcessorHandler(Map<String, HttpCbMessageHandler> messageHandlers) {
		super();
		this.messageHandlers = messageHandlers;
		this.messageDecoder = new MessageDecoder();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {
		AlibabaHttpRequest alibabaHttpRequest = new AlibabaHttpRequest();
		try {
			String uri = msg.getUri();
			Parts parts = new Parts(uri);
			alibabaHttpRequest.setPath(parts.getPath());
			HttpHeaders httpHeaders = msg.headers();
			List<Map.Entry<String, String>> entries = httpHeaders.entries();
			for (Map.Entry<String, String> entry : entries) {
				alibabaHttpRequest.getHeader().put(entry.getKey(), entry.getValue());
			}

			String query = parts.getQuery();
			Map<String, String> parameters = HttpHelper.parseParameters(query);

			if (msg instanceof DefaultFullHttpRequest) {
				DefaultFullHttpRequest fullMsg = (DefaultFullHttpRequest) msg;
				ByteBuf content = fullMsg.content();
				HttpParamOutputStream baos = new HttpParamOutputStream("utf-8");
				if (content.readableBytes() > 0) {
					content.readBytes(baos, content.readableBytes());
					Map<String, String> parameterInContent = baos.toParameters();
					parameters.putAll(parameterInContent);
				}
			}
			String path = parts.getPath();
			HttpCbMessageHandler messageHandler = messageHandlers.get(path);
			if (messageHandler == null) {
				logger.warn("Due to no message-handler found for path[" + path + "], server stop processing this message. The supported path is "
						+ messageHandlers.keySet());
				final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.NOT_FOUND);
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				return;
			}

			Type[] genericTypes = ParameterizedTypeUtil.getGenericTypes(messageHandler);
			if (genericTypes == null || genericTypes.length != 2) {
				// TODO error message
			} else {
				messageDecoder.decodeTo(parameters, (Class) genericTypes[0], alibabaHttpRequest);
			}

			String signKey = messageHandler.getSignKey();
			if (!GenericsUtil.isBlank(signKey)) {
				Map<String, Object> parametersToSign = new HashMap<String, Object>();
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					if (!"_aop_signature".equals(entry.getKey())) {
						parametersToSign.put(entry.getKey(), entry.getValue());
					}
				}
				byte[] bytes = SignatureUtil.hmacSha1(parametersToSign, signKey);
				String signInServerSide = SignatureUtil.encodeHexStr(bytes);
				String signFromClient = alibabaHttpRequest.getSignatureFromClient();
				if (!signInServerSide.equalsIgnoreCase(signFromClient)) {
					logger.warn("The signature from the client is not same with the signature in server side.");
					boolean continueOnSignatureValidationFailed = messageHandler.continueOnSignatureValidationFailed(
							signFromClient, signInServerSide);
					if (!continueOnSignatureValidationFailed) {
						final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
								HttpResponseStatus.UNAUTHORIZED);
						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						return;
					}
					logger.info("Continue on processing this request even signature is incorrect...");
				}
			}

			messageHandler.onMessage(alibabaHttpRequest.getRequestContent());
			final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.NO_CONTENT);

			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			return;
		} catch (MessageProcessException mEx) {
			logger.error("MessageProcessException occurs.", mEx);
			final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.BAD_REQUEST);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			return;
		} catch (Exception mEx) {
			logger.error("Exception occurs.", mEx);
			final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.INTERNAL_SERVER_ERROR);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			return;
		}

	}
}
