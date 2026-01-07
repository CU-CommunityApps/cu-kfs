package edu.cornell.kfs.coa.rest.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentControllerTestData;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentControllerTestMetadata;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentListingFixture;
import edu.cornell.kfs.coa.service.CuAccountService;
import edu.cornell.kfs.sys.CUKFSConstants.EndpointCodes;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticationMapping;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticator;
import edu.cornell.kfs.sys.businessobject.ApiEndpointDescriptor;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class AccountAttachmentControllerTest {

    private static final String TEST_CREDENTIALS = "testuser1:testpass1";

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/coa/cu-spring-coa-account-attach-test.xml");

    @SpringXmlTestBeanFactoryMethod
    public static CuAccountService buildMockAccountService() {
        final Map<String, Account> accountsMap = Arrays.stream(AccountAttachmentControllerTestData.values())
                .map(fixture -> FixtureUtils.getAnnotationBasedFixture(fixture, AccountAttachmentListingFixture.class))
                .collect(Collectors.toUnmodifiableMap(
                        fixture -> createAccountKey(fixture.chartOfAccountsCode(), fixture.accountNumber()),
                        AccountAttachmentListingFixture.Utils::toAccountWithNotesList));

        return new CuMockBuilder<>(CuAccountService.class)
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
        final ApiEndpointDescriptor descriptor = buildEndpointDescriptor();
        return new CuMockBuilder<>(BusinessObjectService.class)
                .withReturn(
                        service -> service.findBySinglePrimaryKey(
                                ApiEndpointDescriptor.class, EndpointCodes.ACCOUNT_ATTACHMENTS),
                        descriptor)
                .build();
    }

    private static ApiEndpointDescriptor buildEndpointDescriptor() {
        final ApiEndpointDescriptor descriptor = new ApiEndpointDescriptor();
        descriptor.setEndpointCode(EndpointCodes.ACCOUNT_ATTACHMENTS);
        descriptor.setEndpointDescription("Test descriptor for Account Attachments endpoint");
        descriptor.setActive(true);

        final ApiAuthenticator authenticator = buildApiAuthenticator();
        final ApiAuthenticationMapping mapping = new ApiAuthenticationMapping();
        mapping.setApiAuthenticator(authenticator);
        mapping.setApiEndpointDescriptor(descriptor);
        mapping.setActive(true);

        descriptor.getAuthenticationMappings().add(mapping);
        authenticator.getAuthenticationMappings().add(mapping);

        return descriptor;
    }

    private static ApiAuthenticator buildApiAuthenticator() {
        final ApiAuthenticator authenticator = new ApiAuthenticator();
        authenticator.setAuthenticatorId(1001);
        authenticator.setAuthenticatorDescription("Test authenticator for Account Attachments endpoint");
        authenticator.setCredentials(TEST_CREDENTIALS);
        authenticator.setActive(true);
        return authenticator;
    }

    @SpringXmlTestBeanFactoryMethod
    public static DataDictionaryService buildMockDataDictionaryService() {
        final List<Pair<Class<?>, AccountAttachmentControllerTestMetadata>> metadataEntries = List.of(
                Pair.of(Account.class, AccountAttachmentControllerTestMetadata.CHART_OF_ACCOUNTS_CODE_ATTRIBUTE),
                Pair.of(Account.class, AccountAttachmentControllerTestMetadata.ACCOUNT_NUMBER_ATTRIBUTE),
                Pair.of(Attachment.class, AccountAttachmentControllerTestMetadata.ATTACHMENT_IDENTIFIER_ATTRIBUTE)
        );

        final CuMockBuilder<DataDictionaryService> mockBuilder = new CuMockBuilder<>(DataDictionaryService.class);
        // TODO: FINISH!
        return mockBuilder.build();
    }

}
