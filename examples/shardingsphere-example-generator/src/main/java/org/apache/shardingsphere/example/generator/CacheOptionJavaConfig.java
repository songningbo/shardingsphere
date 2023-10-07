package org.apache.shardingsphere.example.generator;

import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

import java.util.Properties;

public class CacheOptionJavaConfig {
    public static void main(String[] args) {
        CacheOption cacheOption = new CacheOption(128, 1024L);
        SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
        ParseASTNode parseASTNode = parserEngine.parse("SELECT t.id, t.name, t.age FROM table1 AS t ORDER BY t.id DESC;", false);
        SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "DML",false,new Properties());
        MySQLStatement sqlStatement = visitorEngine.visit(parseASTNode);
        System.out.println(sqlStatement.toString());
    }
}
