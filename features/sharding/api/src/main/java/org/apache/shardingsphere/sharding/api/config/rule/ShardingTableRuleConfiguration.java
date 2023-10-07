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
 * Sharding table rule configuration.
 */
// 分片表规则配置
@RequiredArgsConstructor
@Getter
@Setter
public final class ShardingTableRuleConfiguration {

    // 分片逻辑表名称
    private final String logicTable;

    // 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持行表达式
    // 默认：已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况
    private final String actualDataNodes;

    // 分库策略，使用默认分库策略
    private ShardingStrategyConfiguration databaseShardingStrategy;

    // 分表策略，使用默认分表策略
    private ShardingStrategyConfiguration tableShardingStrategy;

    // 自增列生成器，使用自增主键生成器
    private KeyGenerateStrategyConfiguration keyGenerateStrategy;

    // 分片审计策略，使用默认分片审计策略
    private ShardingAuditStrategyConfiguration auditStrategy;
}
