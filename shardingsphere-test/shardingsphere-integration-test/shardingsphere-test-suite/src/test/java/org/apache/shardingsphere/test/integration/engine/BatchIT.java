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

package org.apache.shardingsphere.test.integration.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrateTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BatchIT extends BaseIT {
    
    private static DataSetEnvironmentManager dataSetEnvironmentManager;
    
    private final IntegrateTestCaseContext testCaseContext;
    
    private final String sql;
    
    private final Collection<DataSet> dataSets;
    
    protected BatchIT(final IntegrateTestCaseContext testCaseContext, 
                      final String ruleType, final DatabaseType databaseType, final String sql) throws IOException, JAXBException, SQLException {
        super(ruleType, databaseType);
        this.testCaseContext = testCaseContext;
        this.sql = sql;
        dataSets = new LinkedList<>();
        for (IntegrateTestCaseAssertion each : testCaseContext.getTestCase().getIntegrateTestCaseAssertions()) {
            dataSets.add(DataSetLoader.load(testCaseContext.getParentPath(), ruleType, databaseType, each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(ruleType), getActualDataSources());
    }
    
    @BeforeClass
    public static void initDatabasesAndTables() {
        setUpDatabasesAndTables();
    }
    
    @AfterClass
    public static void destroyDatabasesAndTables() {
        dropDatabases();
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        dataSetEnvironmentManager.initialize();
    }
    
    @After
    public void clearData() {
        dataSetEnvironmentManager.clear();
    }
    
    protected final void assertDataSet(final int[] actualUpdateCounts) throws SQLException {
        Collection<DataSet> expectedList = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(dataSets.size()));
        int count = 0;
        for (DataSet each : dataSets) {
            try {
                assertThat(actualUpdateCounts[count], is(each.getUpdateCount()));
                expectedList.add(each);
            } catch (final AssertionError ex) {
                log.error("[ERROR] SQL::{}, Expect::{}", sql, each);
                throw ex;
            }
            count++;
        }
        DataSet expected = merge(expectedList);
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata expectedDataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(expectedDataSetMetadata.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = getActualDataSources().get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY 1", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findRows(dataNode), expectedDataSetMetadata);
            }
        }
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
    }
    
    private DataSet merge(final Collection<DataSet> expectedList) {
        DataSet result = new DataSet();
        Set<List<String>> existedRowValues = new HashSet<>();
        for (DataSet each : expectedList) {
            mergeMetadata(each, result);
            mergeRow(each, result, existedRowValues);
        }
        sortRow(result);
        return result;
    }
    
    private void mergeMetadata(final DataSet original, final DataSet dist) {
        if (dist.getMetadataList().isEmpty()) {
            dist.getMetadataList().addAll(original.getMetadataList());
        }
    }
    
    private void mergeRow(final DataSet original, final DataSet dist, final Set<List<String>> existedRowValues) {
        for (DataSetRow each : original.getRows()) {
            if (existedRowValues.add(each.getValues())) {
                dist.getRows().add(each);
            }
        }
    }
    
    private void sortRow(final DataSet dataSet) {
        dataSet.getRows().sort(Comparator.comparingInt(o -> Integer.parseInt(o.getValues().get(0))));
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<DataSetColumn> columnMetadataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetadataList.size()));
        int index = 1;
        for (DataSetColumn each : columnMetadataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    if (!NOT_VERIFY_FLAG.equals(each)) {
                        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(index)), is(each));
                    }
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
