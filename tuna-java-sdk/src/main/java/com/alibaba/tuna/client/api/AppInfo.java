package com.alibaba.tuna.client.api;

import java.io.Serializable;

/**
 * 应用信息
 */
public class AppInfo implements Serializable {

    public AppInfo(String appKey, String secret) {
        this.appKey = appKey;
        this.secret = secret;
    }

    private String appKey;

    private String secret;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
