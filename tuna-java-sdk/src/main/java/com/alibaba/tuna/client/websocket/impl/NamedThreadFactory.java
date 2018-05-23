/*
 * Copyright (C) 2012 The Netty Project
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
package com.alibaba.tuna.client.websocket.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);

	final AtomicInteger threadNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final String prefix;
	final boolean isDaemon;
	final int priority;

	public NamedThreadFactory() {
		this("pool");
	}

	public NamedThreadFactory(String prefix) {
		this(prefix, false, Thread.NORM_PRIORITY);
	}

	public NamedThreadFactory(String prefix, boolean isDaemon, int priority) {
		SecurityManager s = System.getSecurityManager();
		this.group = (s != null) ?
				s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
		this.prefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
		this.isDaemon = isDaemon;
		this.priority = priority;
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement(), 0);
		t.setDaemon(isDaemon);
		t.setPriority(priority);
		return t;
	}

}
