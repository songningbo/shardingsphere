/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cache option.
 */
// 本地缓存配置
@RequiredArgsConstructor
@Getter
public final class CacheOption {
    // 	本地缓存初始容量。语法树本地缓存默认值 128，SQL 语句缓存默认值 2000
    private final int initialCapacity;
    // 本地缓存最大容量。语法树本地缓存默认值 1024，SQL 语句缓存默认值 65535
    private final long maximumSize;
    
    @Override
    public String toString() {
        return String.format("initialCapacity: %d, maximumSize: %d", initialCapacity, maximumSize);
    }
}
