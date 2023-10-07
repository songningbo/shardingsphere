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

package org.apache.shardingsphere.mode.repository.cluster;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;

import java.util.Properties;

/**
 * Cluster persist repository configuration.
 */
// 集群持久化仓库配置
@RequiredArgsConstructor
@Getter
public final class ClusterPersistRepositoryConfiguration implements PersistRepositoryConfiguration {
    
    private final String type;
    
    private final String namespace;
    
    private final String serverLists;
    
    private final Properties props;
}
