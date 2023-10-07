package org.apache.shardingsphere.example.generator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

// 数据分库分表配置
public class DataShardingJavaConfig {
    public static void main(String[] args) throws SQLException {
        // 运行模式配置，单机模式
        ModeConfiguration modeConfig = new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("JDBC", new Properties()));
        // Cluster模式
        ModeConfiguration modeConfigCluster = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "governance-sharding-db", "localhost:2181", new Properties()));
        // 配置策略
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        // 配置第1个数据源
        HikariDataSource dataSource1 = new HikariDataSource();
        dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/ds_1");
        dataSource1.setUsername("root");
        dataSource1.setPassword("");
        dataSourceMap.put("ds_1", dataSource1);
        // 配置第2个数据源
        HikariDataSource dataSource2 = new HikariDataSource();
        dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource2.setJdbcUrl("jdbc:mysql://localhost:3306/ds_2");
        dataSource2.setUsername("root");
        dataSource2.setPassword("");
        dataSourceMap.put("ds_2", dataSource2);
        // 数据分片策略配置
        ShardingRuleConfiguration resultRule = new ShardingRuleConfiguration();
        // 分表规则策略
        resultRule.getTables().add(getOrderTableRuleConfiguration());
        resultRule.getTables().add(getOrderItemTableRuleConfiguration());
        // 配置绑表
        resultRule.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_order, t_order_item"));
        // 配置分库策略
        resultRule.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
        // 配置分表策略
        resultRule.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "standard_test_tbl"));
        // 分库表达式配置
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "demo_ds_${user_id % 2}");
        resultRule.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", props));
        resultRule.getShardingAlgorithms().put("standard_test_tbl", new AlgorithmConfiguration("STANDARD_TEST_TBL", new Properties()));
        resultRule.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        resultRule.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        // 配置传播表配置
        resultRule.setBroadcastTables(Collections.singleton("t_address"));

        // 构建分库分表数据源
        DataSource dataSource = ShardingSphereDataSourceFactory
                .createDataSource(modeConfig,dataSourceMap, Arrays.asList(resultRule), new Properties());
    }
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "demo_ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("sharding_key_required_auditor"), true));
        return result;
    }
    private static ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order_item", "demo_ds_${0..1}.t_order_item_${[0, 1]}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
        return result;
    }
}
