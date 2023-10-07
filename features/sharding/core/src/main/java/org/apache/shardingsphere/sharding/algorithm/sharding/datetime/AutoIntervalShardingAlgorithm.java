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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime;

import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.algorithm.sharding.ShardingAutoTableAlgorithmUtil;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Auto interval sharding algorithm.
 */
// 自动时间段分片算法
public final class AutoIntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {

    // 分片的起始时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss
    private static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    // 分片的结束时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss
    private static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    // 单一分片所能承载的最大时间，单位：秒，允许分片键的时间戳格式的秒带有时间精度，但秒后的时间精度会被自动抹去
    private static final String SHARDING_SECONDS_KEY = "sharding-seconds";
    // 时间格式
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private LocalDateTime dateTimeLower;
    
    private long shardingSeconds;
    
    @Getter
    private int autoTablesAmount;
    
    @Override
    public void init(final Properties props) {
        dateTimeLower = getDateTime(props);
        shardingSeconds = getShardingSeconds(props);
        autoTablesAmount = (int) (Math.ceil((double) (parseDate(props.getProperty(DATE_TIME_UPPER_KEY)) / shardingSeconds)) + 2);
    }
    
    private LocalDateTime getDateTime(final Properties props) {
        String value = props.getProperty(DATE_TIME_LOWER_KEY);
        ShardingSpherePreconditions.checkNotNull(value, () -> new ShardingAlgorithmInitializationException(getType(), String.format("%s cannot be null.", DATE_TIME_LOWER_KEY)));
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (final DateTimeParseException ex) {
            throw new InvalidDatetimeFormatException(DATE_TIME_LOWER_KEY, value, "yyyy-MM-dd HH:mm:ss");
        }
    }
    
    private long getShardingSeconds(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_SECONDS_KEY),
                () -> new ShardingAlgorithmInitializationException(getType(), String.format("%s cannot be null.", SHARDING_SECONDS_KEY)));
        return Long.parseLong(props.getProperty(SHARDING_SECONDS_KEY));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        String tableNameSuffix = String.valueOf(doSharding(parseDate(shardingValue.getValue())));
        return ShardingAutoTableAlgorithmUtil.findMatchedTargetName(availableTargetNames, tableNameSuffix, shardingValue.getDataNodeInfo()).orElse(null);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int firstPartition = getFirstPartition(shardingValue.getValueRange());
        int lastPartition = getLastPartition(shardingValue.getValueRange());
        for (int i = firstPartition; i <= lastPartition; i++) {
            String suffix = String.valueOf(i);
            ShardingAutoTableAlgorithmUtil.findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).ifPresent(result::add);
        }
        return result;
    }
    
    private int doSharding(final long shardingValue) {
        String position = new DecimalFormat("0.00").format((double) shardingValue / shardingSeconds);
        return Math.min(Math.max(0, (int) Math.ceil(Double.parseDouble(position))), autoTablesAmount - 1);
    }
    
    private int getFirstPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasLowerBound() ? doSharding(parseDate(valueRange.lowerEndpoint())) : 0;
    }
    
    private int getLastPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasUpperBound() ? doSharding(parseDate(valueRange.upperEndpoint())) : autoTablesAmount - 1;
    }
    
    private long parseDate(final Comparable<?> shardingValue) {
        LocalDateTime dateValue = LocalDateTime.from(DATE_TIME_FORMAT.parse(shardingValue.toString(), new ParsePosition(0)));
        return Duration.between(dateTimeLower, dateValue).toMillis() / 1000;
    }
    
    @Override
    public String getType() {
        return "AUTO_INTERVAL";
    }
}
