package com.alibaba.tuna.client.api;

import java.io.Serializable;
import java.util.Map;

import com.alibaba.tuna.fastjson.JSON;

/**
 * 接收到的消息内容
 */
public class MessageContent implements Serializable {
    private static final long serialVersionUID = -7202203058089086315L;

    /**
     * 消息ID，消息唯一性标识。如 210239
     */
    protected String msgId;
    /**
     * 消息产生时间。1970.1.1 到现在的毫秒数
     */
    protected long gmtBorn;
    /**
     * 具体推送的业务消息数据，json格式，字段说明，参考各个业务消息说明
     * 如{"key1":"value1"}
     */
    protected Map<String, Object> data;
    /**
     * memberId
     */
    protected String userInfo;
    /**
     * 消息类型，每个业务消息都唯一对应一个类型，参考业务消息的类型定义
     */
    protected String type;

    /** 业务主键,可选*/
    protected String              bizKey;

    /**
     * 扩展字段，暂未启用
     */
    protected Map<String, Object> extraInfo;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getGmtBorn() {
        return gmtBorn;
    }

    public void setGmtBorn(long gmtBorn) {
        this.gmtBorn = gmtBorn;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
