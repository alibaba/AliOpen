package com.alibaba.tuna.fastjson.parser;

@Deprecated
public class JavaBeanMapping extends ParserConfig {
	private final static JavaBeanMapping instance = new JavaBeanMapping();

	public static JavaBeanMapping getGlobalInstance() {
		return instance;
	}
}
