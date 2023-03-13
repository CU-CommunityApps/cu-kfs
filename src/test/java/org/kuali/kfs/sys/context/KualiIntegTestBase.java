/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.context;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.parallel.Execution;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.ksb.util.KSBConstants;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.batch.service.CacheService;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.ConfigurableDateService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * CU Customization: Backported the FINP-8690 integration test fixes from the 2022-07-20 financials patch.
 * This overlay can be removed when we upgrade to the 2022-07-20 patch.
 * 
 * This class should be extended by all Kuali unit tests.
 *
 * @see ConfigureContext
 */
@Execution(SAME_THREAD)
public abstract class KualiIntegTestBase extends TestCase {

    private static RuntimeException configurationFailure;
    private static boolean springContextInitialized;
    private static boolean batchScheduleInitialized;
    private static TransactionStatus transactionStatus;

    /**
     * Determines whether to actually run the test using the RelatesTo annotation, configures the appropriate context
     * using the ConfigureContext annotation, and logs extra details if the test invocation's OJB operations happen to
     * encounter an OptimisticLockException or if this test has related Jiras.
     */
    @Override
    public final void runBare() throws Throwable {
        GlobalVariables.clear();
        final ConfigureContext contextConfiguration =
                getMethod(getName()).getAnnotation(ConfigureContext.class) != null ?
                        getMethod(getName()).getAnnotation(ConfigureContext.class) :
                        getMethod("setUp").getAnnotation(ConfigureContext.class) != null ?
                                getMethod("setUp").getAnnotation(ConfigureContext.class) :
                                getClass().getAnnotation(ConfigureContext.class);
        if (contextConfiguration != null) {
            configure(contextConfiguration);
            SpringContext.getBean(ConfigurableDateService.class, "dateTimeService")
                    .setCurrentDate(new java.util.Date());
            ConfigContext.getCurrentContextConfig()
                    .putProperty(KSBConstants.Config.MESSAGE_DELIVERY, KSBConstants.MESSAGING_SYNCHRONOUS);
        }

        try {
            setUp();
            try {
                runTest();
            } finally {
                tearDown();
                if (springContextInitialized) {
                    clearAllCaches();
                }
            }
        } catch (final ValidationException ex) {
            fail("Test threw an unexpected ValidationException: " + IntegTestUtils.dumpMessageMapErrors());
        } catch (final Throwable ex) {
            if (ex instanceof CannotGetJdbcConnectionException
                    || StringUtils.contains(ex.getMessage(), "GenericPool:checkOut")
                    || StringUtils.contains(ex.getMessage(), "no connection available")) {
                fail("CONFIGURATION ERROR: UNABLE TO OBTAIN DATABASE CONNECTION!");
            }
            throw ex;
        } finally {
            if (contextConfiguration != null) {
                endTestTransaction();
            }
            GlobalVariables.setUserSession(null);
            GlobalVariables.clear();
        }
    }

    @CacheEvict(allEntries = true, value = "")
    public void clearAllCaches() {
        SpringContext.getBean(CacheService.class).clearSystemCaches();
    }

    protected void changeCurrentUser(final UserNameFixture sessionUser) {
        GlobalVariables.setUserSession(new UserSession(sessionUser.toString()));
    }

    private static void configure(final ConfigureContext contextConfiguration) {
        if (configurationFailure != null) {
            throw configurationFailure;
        }
        if (!springContextInitialized) {
            try {
                KFSTestStartup.initializeKfsTestContext();
                springContextInitialized = true;
            } catch (final RuntimeException e) {
                configurationFailure = e;
                throw e;
            }
        }
        if (!batchScheduleInitialized && contextConfiguration.initializeBatchSchedule()) {
            SpringContext.getBean(SchedulerService.class).initialize();
            batchScheduleInitialized = true;
        }
        if (!contextConfiguration.shouldCommitTransactions()) {
            final DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
            defaultTransactionDefinition.setTimeout(3600);
            transactionStatus = getTransactionManager().getTransaction(defaultTransactionDefinition);
            transactionStatus.setRollbackOnly();
        } else {
            final DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
            defaultTransactionDefinition.setTimeout(3600);
            transactionStatus = getTransactionManager().getTransaction(defaultTransactionDefinition);
        }
        final UserNameFixture sessionUser = contextConfiguration.session();
        if (sessionUser != UserNameFixture.NO_SESSION) {
            GlobalVariables.setUserSession(new UserSession(sessionUser.toString()));
        }
    }

    private static void endTestTransaction() {
        if (transactionStatus != null) {
            if (transactionStatus.isRollbackOnly()) {
                getTransactionManager().rollback(transactionStatus);
            } else {
                getTransactionManager().commit(transactionStatus);
            }
        }
    }

    private static PlatformTransactionManager getTransactionManager() {
        return (PlatformTransactionManager) SpringContext.getService("transactionManager");
    }

    private Method getMethod(final String methodName) {
        Class<? extends Object> clazz = getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredMethod(methodName);
            } catch (final NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("KualiTestBase was unable to getMethod: " + methodName);
    }

}
