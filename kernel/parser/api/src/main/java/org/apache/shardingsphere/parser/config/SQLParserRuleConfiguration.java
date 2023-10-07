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

package org.apache.shardingsphere.parser.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

/**
 * SQL parser rule configuration.
 */
// SQL解析规则配置
@RequiredArgsConstructor
@Getter
public final class SQLParserRuleConfiguration implements GlobalRuleConfiguration {

    // 是否解析 SQL 注释
    private final boolean sqlCommentParseEnabled;
    // 解析语法树本地缓存配置
    private final CacheOption parseTreeCache;
    // 	SQL 语句本地缓存配置
    private final CacheOption sqlStatementCache;
}
