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

package org.apache.shardingsphere.sharding.algorithm.keygen;

import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.exception.algorithm.keygen.KeyGenerateAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.exception.algorithm.keygen.SnowflakeClockMoveBackException;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Calendar;
import java.util.Properties;

/**
 * Snowflake key generate algorithm.
 * 
 * <pre>
 *     Length of key is 64 bit.
 *     1 bit sign bit.
 *     41 bits timestamp offset from 2016.11.01(ShardingSphere distributed primary key published data) to now.
 *     10 bits worker process id.
 *     12 bits auto increment offset in one mills
 * </pre>
 */
// 基于雪花算法的分布式主键生成算法。
public final class SnowflakeKeyGenerateAlgorithm implements KeyGenerateAlgorithm, InstanceContextAware {
    
    public static final long EPOCH;

    // 最大抖动上限值，范围[0, 4096)。注：若使用此算法生成值作分片值，建议配置此属性。
    // 此算法在不同毫秒内所生成的 key 取模 2^n (2^n一般为分库或分表数) 之后结果总为 0 或 1。
    // 为防止上述分片问题，建议将此属性值配置为 (2^n)-1
    private static final String MAX_VIBRATION_OFFSET_KEY = "max-vibration-offset";
    
    private static final String MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS_KEY = "max-tolerate-time-difference-milliseconds";
    
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long WORKER_ID_BITS = 10L;
    
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;
    
    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;
    
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;
    
    private static final int DEFAULT_VIBRATION_VALUE = 1;

    // 最大容忍时钟回退时间，单位：毫秒
    private static final int MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 10;

    // 工作机器唯一标识：默认值是0。
    private static final int DEFAULT_WORKER_ID = 0;
    
    @Setter
    private static TimeService timeService = new TimeService();
    
    private Properties props;
    
    private int maxVibrationOffset;
    
    private int maxTolerateTimeDifferenceMilliseconds;
    
    private volatile int sequenceOffset = -1;
    
    private volatile long sequence;
    
    private volatile long lastMilliseconds;
    
    private volatile InstanceContext instanceContext;
    
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.NOVEMBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        maxVibrationOffset = getMaxVibrationOffset(props);
        maxTolerateTimeDifferenceMilliseconds = getMaxTolerateTimeDifferenceMilliseconds(props);
    }
    
    @Override
    public void setInstanceContext(final InstanceContext instanceContext) {
        this.instanceContext = instanceContext;
        if (null != instanceContext) {
            instanceContext.generateWorkerId(props);
        }
    }
    
    private int getMaxVibrationOffset(final Properties props) {
        int result = Integer.parseInt(props.getOrDefault(MAX_VIBRATION_OFFSET_KEY, DEFAULT_VIBRATION_VALUE).toString());
        ShardingSpherePreconditions.checkState(result >= 0 && result <= SEQUENCE_MASK, () -> new KeyGenerateAlgorithmInitializationException(getType(), "Illegal max vibration offset."));
        return result;
    }
    
    private int getMaxTolerateTimeDifferenceMilliseconds(final Properties props) {
        return Integer.parseInt(props.getOrDefault(MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS_KEY, MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS).toString());
    }
    
    @Override
    public synchronized Long generateKey() {
        // 获取当前时间戳
        long currentMilliseconds = timeService.getCurrentMillis();
        // 如果出现了时钟回拨，则抛出异常或进行时钟等待
        if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
            currentMilliseconds = timeService.getCurrentMillis();
        }
        // 如果上次的生成时间与本次的是同一毫秒
        if (lastMilliseconds == currentMilliseconds) {
            // 这个位运算保证始终就是在4096这个范围内，避免你自己传递的sequence超过了4096这个范围
            if (0L == (sequence = (sequence + 1) & SEQUENCE_MASK)) {
                // 如果位运算结果为0，则需要等待下一个毫秒继续生成
                currentMilliseconds = waitUntilNextTime(currentMilliseconds);
            }
        } else {// 如果不是，则生成新的sequence
            vibrateSequenceOffset();
            sequence = sequenceOffset;
        }
        lastMilliseconds = currentMilliseconds;
        // 先将当前时间戳左移放到完成41个bit，然后将工作进程为左移到10个bit，再将序号为放到最后的12个bit
        // 最后拼接起来成一个64 bit的二进制数字
        return ((currentMilliseconds - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | ((long) getWorkerId() << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
    }
    
    @SneakyThrows(InterruptedException.class)
    private boolean waitTolerateTimeDifferenceIfNeed(final long currentMilliseconds) {
        if (lastMilliseconds <= currentMilliseconds) {
            return false;
        }
        long timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds;
        ShardingSpherePreconditions.checkState(timeDifferenceMilliseconds < maxTolerateTimeDifferenceMilliseconds, () -> new SnowflakeClockMoveBackException(lastMilliseconds, currentMilliseconds));
        Thread.sleep(timeDifferenceMilliseconds);
        return true;
    }
    
    private long waitUntilNextTime(final long lastTime) {
        long result = timeService.getCurrentMillis();
        while (result <= lastTime) {
            result = timeService.getCurrentMillis();
        }
        return result;
    }
    
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void vibrateSequenceOffset() {
        sequenceOffset = sequenceOffset >= maxVibrationOffset ? 0 : sequenceOffset + 1;
    }
    
    private int getWorkerId() {
        return null == instanceContext ? DEFAULT_WORKER_ID : instanceContext.getWorkerId();
    }
    
    @Override
    public String getType() {
        return "SNOWFLAKE";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
