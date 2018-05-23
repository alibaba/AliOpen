package com.alibaba.tuna.client.api;

/**
 *
 */
public class MessageProcessException extends RuntimeException {
    private static final long serialVersionUID = 4661149938679072586L;

    public MessageProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageProcessException(String message) {
        super(message);
    }

    public MessageProcessException(Throwable cause) {
        super(cause);
    }
}