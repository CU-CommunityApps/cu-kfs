package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.Types;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.sys.util.TestTransactionManagementExtension;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.dataaccess.impl.TransactionDetailProcessorDaoJdbcImpl;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(TestTransactionManagementExtension.class)
public class TaxFileGenerationServiceSprintaxImplIntegTest {

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-sprintax-file-test.xml");

    private TestDataHelperDao testDataHelperDao;
    private TransactionDetailProcessorDaoJdbcImpl transactionDetailProcessorDao;

    @BeforeEach
    void setUp() throws Exception {
        transactionDetailProcessorDao = springContextExtension.getBean(
                TaxSpringBeans.TRANSACTION_DETAIL_PROCESSOR_DAO, TransactionDetailProcessorDaoJdbcImpl.class);
        testDataHelperDao = springContextExtension.getBean(TestDataHelperDao.BEAN_NAME,
                TestDataHelperDao.class);
    }

    private void deleteConflictingTransactionDetails() throws Exception {
        final CuSqlQuery query = new CuSqlChunk()
                .append("DELETE FROM KFS.TX_TRANSACTION_DETAIL_T")
                .append(" WHERE REPORT_YEAR = ").appendAsParameter(Types.INTEGER, 2024)
                .append(" AND FORM_1042S_BOX IS NOT NULL")
                .toQuery();
        testDataHelperDao.runQuery(query);
    }

    @AfterEach
    void tearDown() throws Exception {
        testDataHelperDao = null;
        transactionDetailProcessorDao = null;
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void testSomething(final int value) throws Exception {
        deleteConflictingTransactionDetails();
        testDataHelperDao.loadCsvDataIntoDatabase(
                "classpath:edu/cornell/kfs/tax/batch/sprintax-file-test/test-bootstrap.csv");
        //final DataSource dataSource = springContextExtension.getBean("dataSource", DataSource.class);
        //System.out.println(dataSource.toString());
    }

}
