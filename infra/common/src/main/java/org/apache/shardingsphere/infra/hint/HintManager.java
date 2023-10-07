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

package org.apache.shardingsphere.infra.hint;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * The manager that use hint to inject sharding key directly through {@code ThreadLocal}.
 */
// 使用hint直接通过ThreadLocal注入分片key的管理器。

// AutoCloseable接口用于自动释放资源，使用AutoCloseable接口，在try语句结束时，
// 不需要实现finally语句就会自动将这些资源关闭，JDK会通过回调的方式，调用close方法来做到这一点。
// 这种机制被称为 try with resource。
// HintManager通过实现AutoCloseable接口支持资源的自动释放。
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {

    // 基于ThreadLocal存储HintManager实例
    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();
    // 数据库分片值
    private final Multimap<String, Comparable<?>> databaseShardingValues = ArrayListMultimap.create();
    // 数据表分片值
    private final Multimap<String, Comparable<?>> tableShardingValues = ArrayListMultimap.create();
    // 是否只有数据库分片
    private boolean databaseShardingOnly;
    // 是否只路由主库。
    private boolean writeRouteOnly;

    // 数据源名称
    @Setter
    private String dataSourceName;
    
    /**
     * Get a new instance for {@code HintManager}.
     *
     * @return {@code HintManager} instance
     */
    // 从ThreadLocal中获取或设置针对当前线程的HintManager实例。
    public static HintManager getInstance() {
        Preconditions.checkState(null == HINT_MANAGER_HOLDER.get(), "Hint has previous value, please clear first.");
        HintManager result = new HintManager();
        HINT_MANAGER_HOLDER.set(result);
        return result;
    }
    
    /**
     * Set sharding value for database sharding only.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param value sharding value
     */
    public void setDatabaseShardingValue(final Comparable<?> value) {
        databaseShardingValues.clear();
        tableShardingValues.clear();
        databaseShardingValues.put("", value);
        databaseShardingOnly = true;
    }
    
    /**
     * Add sharding value for database.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public void addDatabaseShardingValue(final String logicTable, final Comparable<?> value) {
        if (databaseShardingOnly) {
            databaseShardingValues.removeAll("");
        }
        databaseShardingValues.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    /**
     * Add sharding value for table.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public void addTableShardingValue(final String logicTable, final Comparable<?> value) {
        if (databaseShardingOnly) {
            databaseShardingValues.removeAll("");
        }
        tableShardingValues.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    /**
     * Get database sharding values.
     *
     * @return database sharding values
     */
    public static Collection<Comparable<?>> getDatabaseShardingValues() {
        return getDatabaseShardingValues("");
    }
    
    /**
     * Get database sharding values.
     *
     * @param logicTable logic table
     * @return database sharding values
     */
    public static Collection<Comparable<?>> getDatabaseShardingValues(final String logicTable) {
        return null == HINT_MANAGER_HOLDER.get() ? Collections.emptyList() : HINT_MANAGER_HOLDER.get().databaseShardingValues.get(logicTable);
    }
    
    /**
     * Get table sharding values.
     *
     * @param logicTable logic table name
     * @return table sharding values
     */
    public static Collection<Comparable<?>> getTableShardingValues(final String logicTable) {
        return null == HINT_MANAGER_HOLDER.get() ? Collections.emptyList() : HINT_MANAGER_HOLDER.get().tableShardingValues.get(logicTable);
    }
    
    /**
     * Judge whether database sharding only.
     *
     * @return database sharding or not
     */
    public static boolean isDatabaseShardingOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().databaseShardingOnly;
    }
    
    /**
     * Set database operation force route to write database only.
     */
    public void setWriteRouteOnly() {
        writeRouteOnly = true;
    }
    
    /**
     * Set database routing to be automatic.
     */
    public void setReadwriteSplittingAuto() {
        writeRouteOnly = false;
    }
    
    /**
     * Judge whether route to write database only or not.
     *
     * @return route to write database only or not
     */
    public static boolean isWriteRouteOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().writeRouteOnly;
    }
    
    /**
     * Clear thread local for hint manager.
     */
    public static void clear() {
        HINT_MANAGER_HOLDER.remove();
    }
    
    /**
     * Clear sharding values.
     */
    public void clearShardingValues() {
        databaseShardingValues.clear();
        tableShardingValues.clear();
        databaseShardingOnly = false;
    }
    
    /**
     * Judge whether hint manager instantiated or not.
     *
     * @return whether hint manager instantiated or not
     */
    public static boolean isInstantiated() {
        return null != HINT_MANAGER_HOLDER.get();
    }
    
    /**
     * Get data source name.
     *
     * @return dataSource name
     */
    public static Optional<String> getDataSourceName() {
        return Optional.ofNullable(HINT_MANAGER_HOLDER.get()).map(optional -> optional.dataSourceName);
    }
    
    @Override
    public void close() {
        clear();
    }
}
