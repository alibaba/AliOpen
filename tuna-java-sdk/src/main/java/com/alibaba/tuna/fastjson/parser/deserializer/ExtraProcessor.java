package com.alibaba.tuna.fastjson.parser.deserializer;

/**
 * 
 *
 * @since 1.1.34
 */
public interface ExtraProcessor extends ParseProcess {

    void processExtra(Object object, String key, Object value);
}
