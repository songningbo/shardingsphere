package org.apache.shardingsphere.example.generator;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

public class AlgorithmJavaConfig {
    public static void main(String[] args) throws SQLException {
        DataSource dataSource = getDataSource();
    }

    public static DataSource getDataSource() throws SQLException {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("username", "name_encryptor", "", "", "", "AES", true);

        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd",
                "pwd_encryptor", "assisted_query_pwd", "like_pwd", "pwd", "AES", "pwd_encryptor", "like_pwd", true);
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest), false);
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new HashMap<>();
        encryptAlgorithmConfigs.put("name_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("pwd_encryptor", new AlgorithmConfiguration("assistedTest", props));
        encryptAlgorithmConfigs.put("like_encryptor", new AlgorithmConfiguration("CHAR_DIGEST_LIKE", new Properties()));
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
        return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(encryptRuleConfig), props);
    }
}
