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
package com.alibaba.tuna.client.httpcb.impl;

import com.alibaba.tuna.client.api.TunaClient;

/**
 *
 *
 */
public interface OpenSDKHttpServer extends TunaClient {

	public int getPort();

	public void setPort(int port);

	public boolean isUseSSL();

	public void setUseSSL(boolean useSSL);

}
