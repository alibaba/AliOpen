package com.alibaba.tuna.client.api;

/**
 * 客户端启动异常
 */
public class ClientStartException extends RuntimeException {
    private static final long serialVersionUID = -8767338692591458408L;

    public ClientStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientStartException(String message) {
        super(message);
    }

    public ClientStartException(Throwable cause) {
        super(cause);
    }
}
