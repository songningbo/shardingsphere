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

package org.apache.shardingsphere.encrypt.api.config.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Encrypt column rule configuration.
 */
// 加密列规则配置
@RequiredArgsConstructor
@Getter
public final class EncryptColumnRuleConfiguration {

    // 逻辑列
    private final String logicColumn;
    // 密文列
    private final String cipherColumn;

    // 查询辅助列配置
    private final String assistedQueryColumn;

    // 模糊查询列配置
    private final String likeQueryColumn;

    // 明文列
    private final String plainColumn;

    // 加密列算法名称
    private final String encryptorName;

    // 查询复制加密列配置
    private final String assistedQueryEncryptorName;

    // 模糊查询加密列配置
    private final String likeQueryEncryptorName;

    // 是否从加密列中查询
    // spring.shardingsphere.props.query.with.cipher.comlum对应该列
    private final Boolean queryWithCipherColumn;
    
    public EncryptColumnRuleConfiguration(final String logicColumn, final String cipherColumn, final String assistedQueryColumn, final String likeQueryColumn,
                                          final String plainColumn, final String encryptorName, final Boolean queryWithCipherColumn) {
        this(logicColumn, cipherColumn, assistedQueryColumn, likeQueryColumn, plainColumn, encryptorName, null, null, queryWithCipherColumn);
    }
}
