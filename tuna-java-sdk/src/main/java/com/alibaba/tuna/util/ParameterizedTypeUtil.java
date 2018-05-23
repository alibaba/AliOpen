/*
 * Copyright (C) 1999-2018 Alibaba Group Holding Limited
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.tuna.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 *
 *
 */
public class ParameterizedTypeUtil {
	private static HashMap<Class, Type[]> parameterizedTypesCache = new HashMap<Class, Type[]>();

	public static Type[] getGenericTypes(Class beanClass) {

		if (beanClass == null) {
			throw new IllegalArgumentException("No bean class specified");
		}

		Type[] types = parameterizedTypesCache.get(beanClass);
		if (types != null) {
			return types;
		}

		Type[] myGenericClass = beanClass.getGenericInterfaces();
		if (myGenericClass == null || myGenericClass.length == 0) {
			types = null;
		} else {
			ParameterizedType pt = ((ParameterizedType) myGenericClass[0]);
			types = pt.getActualTypeArguments();
		}

		parameterizedTypesCache.put(beanClass, types);
		return (types);

	}

	public static Type[] getGenericTypes(Object bean) {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		return getGenericTypes(bean.getClass());
	}

}
