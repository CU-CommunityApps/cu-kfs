package edu.cornell.kfs.coa.rest.resource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentControllerTestData;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentListingFixture;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class AccountAttachmentControllerTest {

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/coa/cu-spring-coa-account-attach-test.xml");

    @SpringXmlTestBeanFactoryMethod
    public static AccountService buildMockAccountService() {
        final Map<String, Account> accountsMap = null; Arrays.stream(AccountAttachmentControllerTestData.values())
                .map(fixture -> FixtureUtils.getAnnotationBasedFixture(fixture, AccountAttachmentListingFixture.class))
                .collect(Collectors.toUnmodifiableMap(
                        fixture -> createAccountKey(fixture.chartOfAccountsCode(), fixture.accountNumber()),
                        AccountAttachmentListingFixture.Utils::toAccount));

        return new CuMockBuilder<>(AccountService.class)
                .withAnswer(
                        service -> service.getByPrimaryId(Mockito.anyString(), Mockito.anyString()),
                        invocation -> getAccount(invocation, accountsMap))
                .build();
    }

    private static Account getAccount(final InvocationOnMock invocation, final Map<String, Account> accountsMap) {
        final String accountKey = createAccountKey(invocation.getArgument(0), invocation.getArgument(1));
        return accountsMap.get(accountKey);
    }

    private static String createAccountKey(final String chartOfAccountsCode, final String accountNumber) {
        return StringUtils.join(chartOfAccountsCode, KFSConstants.DASH, accountNumber);
    }

    @SpringXmlTestBeanFactoryMethod
    public static BusinessObjectService buildMockBusinessObjectService() {
        return null;
    }

    @SpringXmlTestBeanFactoryMethod
    public static DataDictionaryService buildMockDataDictionaryService() {
        return null;
    }

}
