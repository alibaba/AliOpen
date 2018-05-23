package com.alibaba.tuna.fastjson.parser.deserializer;

import java.lang.reflect.Type;

import com.alibaba.tuna.fastjson.parser.DefaultJSONParser;
import com.alibaba.tuna.fastjson.parser.JSONToken;

public class JSONObjectDeserializer implements ObjectDeserializer {
    public final static JSONObjectDeserializer instance = new JSONObjectDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) parser.parseObject();
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
