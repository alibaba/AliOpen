package com.alibaba.tuna.client.api;

/**
 * 客户端
 */
public interface TunaClient {

    /**
     * 启动客户端。一般在应用启动时调用
     *
     * @throws ClientStartException
     */
    void start() throws ClientStartException;

    /**
     * 关闭客户端。一般在应用结束时调用
     */
    void shutdown();
}
