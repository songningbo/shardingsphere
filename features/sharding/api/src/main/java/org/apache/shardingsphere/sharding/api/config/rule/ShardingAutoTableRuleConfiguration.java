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

package org.apache.shardingsphere.sharding.api.config.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

/**
 * Sharding auto table rule configuration.
 */
// 自动分片规则配置
@RequiredArgsConstructor
@Getter
@Setter
public final class ShardingAutoTableRuleConfiguration {

    // 分片逻辑表名称
    private final String logicTable;
    // 数据源名称，多个数据源以逗号分隔，默认值是全部配置的数据源。
    private final String actualDataSources;
    // 分片策略，默认分片策略
    private ShardingStrategyConfiguration shardingStrategy;
    // 自增列生成器，默认自增主键生成器
    private KeyGenerateStrategyConfiguration keyGenerateStrategy;
    // 分片审计策略，模式分片审计策略
    private ShardingAuditStrategyConfiguration auditStrategy;
}
