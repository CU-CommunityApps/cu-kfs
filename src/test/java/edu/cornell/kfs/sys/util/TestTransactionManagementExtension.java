package edu.cornell.kfs.sys.util;

import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * JUnit extension for tests that need each test case/method to run in its own dedicated transaction.
 * The transaction will automatically be rolled back after each test case.
 * 
 * NOTE: this extension depends on the TestSpringContextExtension, and the Spring context is expected
 *       to contain a "transactionManager" bean.
 * 
 * NOTE: The transaction will start at the before-test-execution phase and will be rolled back
 *       at the after-test-execution phase. Therefore, do *NOT* put any database-updating logic
 *       in the test class's "BeforeEach" or "AfterEach" annotated methods, because such updates
 *       will occur outside of the transaction!
 */
public class TestTransactionManagementExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;

    @Override
    public void beforeTestExecution(final ExtensionContext junitContext) throws Exception {
        final ClassPathXmlApplicationContext springContext = TestSpringContextExtension.getCurrentTestSpringContext(
                junitContext);
        Validate.validState(springContext != null, "The test Spring context has not been initialized");
        transactionManager = springContext.getBean(
                KRADServiceLocatorInternal.TRANSACTION_MANAGER, PlatformTransactionManager.class);
        Validate.validState(transactionManager != null,
                "The test Spring context does not contain a 'transactionManager' bean");

        final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setTimeout(3600);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    @Override
    public void afterTestExecution(final ExtensionContext junitContext) throws Exception {
        try {
            if (transactionManager != null && transactionStatus != null) {
                transactionManager.rollback(transactionStatus);
            }
        } finally {
            transactionStatus = null;
            transactionManager = null;
        }
    }

}
