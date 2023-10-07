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

package org.apache.shardingsphere.mode.persist;

import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.List;

/**
 * Persist repository.
 */
// 持久化仓库
public interface PersistRepository extends TypedSPI {
    
    /**
     * Path separator.
     */
    // 路径分隔
    String PATH_SEPARATOR = "/";
    
    /**
     * Get value from registry center directly.
     *
     * @param key key
     * @return value
     */
    // 直接从注册中心获取值。
    String getDirectly(String key);
    
    /**
     * Get names of sub-node.
     *
     * @param key key of data
     * @return sub-node names
     */
    // 获取子节点名字列表
    List<String> getChildrenKeys(String key);
    
    /**
     * Judge node is exist or not.
     *
     * @param key key
     * @return node is exist or not
     */
    // 判断节点是否存在
    boolean isExisted(String key);
    
    /**
     * Persist data.
     *
     * @param key key of data
     * @param value value of data
     */
    // 持久化数据
    void persist(String key, String value);
    
    /**
     * Update data.
     *
     * @param key key
     * @param value value
     */
    // 更新数据
    void update(String key, String value);
    
    /**
     * Delete node.
     *
     * @param key key of data
     */
    // 删除节点
    void delete(String key);
    
    /**
     * Close.
     */
    // 关闭
    void close();
}
