package edu.cornell.kfs.sys.service.impl;

import java.sql.Types;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.service.MyMiddleTestService;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class TransactionManagementTest {

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/sys/cu-spring-test-transactions.xml");

    private MyMiddleTestService myMiddleTestService;
    private TestDataHelperDao testDataHelperDao;

    @BeforeEach
    void setUp() throws Exception {
        myMiddleTestService = springContextExtension.getBean("myMiddleTestService", MyMiddleTestService.class);
        testDataHelperDao = springContextExtension.getBean("testDataHelperDao", TestDataHelperDao.class);

        final CuSqlQuery query = CuSqlQuery.of(
                "CREATE TABLE KFS.MY_TEST_TABLE (MY_KEY VARCHAR2(100), MY_LABEL VARCHAR2(100))");
        testDataHelperDao.execute(query);

        final Function<String, String> labelDeriver = str -> str + "LBL";
        final CuSqlQuery batchQuery = new CuSqlChunk()
                .append("INSERT INTO KFS.MY_TEST_TABLE (MY_KEY, MY_LABEL) VALUES (")
                .appendAsParameter(Types.VARCHAR, String::toString)
                .append(", ")
                .appendAsParameter(Types.VARCHAR, labelDeriver)
                .append(")")
                .toQuery();
        final List<String> values = IntStream.range(0, 100)
                .mapToObj(String::valueOf)
                .collect(Collectors.toUnmodifiableList());
        testDataHelperDao.executeBatchUpdate(batchQuery, values);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (testDataHelperDao != null) {
            final CuSqlQuery query = CuSqlQuery.of("DROP TABLE KFS.MY_TEST_TABLE");
            testDataHelperDao.execute(query);
        }
        myMiddleTestService = null;
        testDataHelperDao = null;
    }

    @Test
    void testMultiThread() throws Exception {
        myMiddleTestService.triggerUpdates();
    }

}
