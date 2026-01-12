package edu.cornell.kfs.coa.rest.resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Filter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.util.ClasspathOrFileResourceLoader;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import edu.cornell.kfs.coa.CuCoaTestConstants;
import edu.cornell.kfs.coa.CuCoaTestConstants.CoaTestBeanNames;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentErrorResponseDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentControllerTestBoEntry;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentControllerTestData;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentListingFixture;
import edu.cornell.kfs.coa.rest.resource.fixture.AccountAttachmentNoteFixture;
import edu.cornell.kfs.coa.service.AccountAttachmentService;
import edu.cornell.kfs.coa.service.CuAccountService;
import edu.cornell.kfs.coa.service.impl.AccountAttachmentServiceImpl;
import edu.cornell.kfs.krad.fixture.AttributeDefinitionFixture;
import edu.cornell.kfs.krad.fixture.BusinessObjectEntryFixture;
import edu.cornell.kfs.sys.CUKFSConstants.EndpointCodes;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticationMapping;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticator;
import edu.cornell.kfs.sys.businessobject.ApiEndpointDescriptor;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.sys.web.filter.ApiAuthenticationFilter;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class AccountAttachmentControllerTest {

    private static final String TEST_CREDENTIALS = "testuser1:testpass1";
    private static final String BAD_CREDENTIALS = "baduser1:badpass1";
    private static final String BASE_ATTACHMENT_LISTING_URL = "/coa/account-attachments/api/get-attachment-list";
    private static final String ATTACHMENT_LISTING_URL = BASE_ATTACHMENT_LISTING_URL
            + "?chartCode={chartCode}&accountNumber={accountNumber}";
    private static final String BASE_ATTACHMENT_CONTENTS_URL = "/coa/account-attachments/api/get-attachment-contents";
    private static final String ATTACHMENT_CONTENTS_URL = BASE_ATTACHMENT_CONTENTS_URL
            + "?chartCode={chartCode}&accountNumber={accountNumber}&attachmentId={attachmentId}";

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/coa/cu-spring-coa-account-attach-test.xml");

    @RegisterExtension
    static MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private static ClasspathOrFileResourceLoader resourceLoader;
    private static AccountAttachmentController controller;
    private static WebTestClient testClient;

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
        final Map<String, AttributeDefinition> attributeMap = Arrays
                .stream(AccountAttachmentControllerTestBoEntry.values())
                .map(fixture -> FixtureUtils.getAnnotationBasedFixture(fixture, BusinessObjectEntryFixture.class))
                .flatMap(AccountAttachmentControllerTest::createAttributeMappingsFromEntry)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new CuMockBuilder<>(DataDictionaryService.class)
                .withAnswer(
                        service -> service.getAttributeDefinition(Mockito.anyString(), Mockito.anyString()),
                        invocation -> getAttributeDefinition(invocation, attributeMap))
                .build();
    }

    private static Stream<Map.Entry<String, AttributeDefinition>> createAttributeMappingsFromEntry(
            final BusinessObjectEntryFixture boEntryFixture) {
        return Arrays.stream(boEntryFixture.attributes())
                .map(attributeFixture -> {
                    final String attributeKey = createAttributeKey(
                            boEntryFixture.businessObjectClass(), attributeFixture.name());
                    final AttributeDefinition attribute = AttributeDefinitionFixture.Utils
                            .toAttributeDefinition(attributeFixture);
                    return Map.entry(attributeKey, attribute);
                });
    }

    private static AttributeDefinition getAttributeDefinition(final InvocationOnMock invocation,
            final Map<String, AttributeDefinition> attributeMap) {
        final String attributeKey = createAttributeKey(
                invocation.getArgument(0, String.class), invocation.getArgument(1));
        return attributeMap.get(attributeKey);
    }

    private static String createAttributeKey(final Class<? extends BusinessObject> boClass, final String attributeName) {
        return createAttributeKey(boClass.getSimpleName(), attributeName);
    }

    private static String createAttributeKey(final String boEntryName, final String attributeName) {
        return StringUtils.join(boEntryName, KFSConstants.DASH, attributeName);
    }

    @SpringXmlTestBeanFactoryMethod
    public static AttachmentService buildMockAttachmentService() throws Exception {
        return new CuMockBuilder<>(AttachmentService.class)
                .withAnswer(
                        service -> service.retrieveAttachmentContents(Mockito.any()),
                        invocaton -> {
                            final Attachment attachment = invocaton.getArgument(0);
                            final String filePath = CuCoaTestConstants.ACCOUNT_ATTACHMENT_TEST_BASE_PATH
                                    + attachment.getAttachmentFileName();
                            final Resource fileResource = resourceLoader.getResource(filePath);
                            return fileResource.getInputStream();
                        })
                .build();
    }

    @BeforeAll
    static void performFirstTimeInitialization() throws Exception {
        final AccountAttachmentService helperService = springContextExtension.getBean(
                CoaTestBeanNames.ACCOUNT_ATTACHMENT_SERVICE,
                AccountAttachmentService.class);
        final AttachmentService attachmentService = springContextExtension.getBean(
                CoaTestBeanNames.ATTACHMENT_SERVICE, AttachmentService.class);
        final ApiAuthenticationFilter filter = springContextExtension.getBean(
                CoaTestBeanNames.ACCOUNT_ATTACHMENT_AUTH_FILTER, ApiAuthenticationFilter.class);

        resourceLoader = new ClasspathOrFileResourceLoader();
        controller = new AccountAttachmentController(helperService, attachmentService);

        webServerExtension.initializeStandaloneMockMvcWithControllersAndFilters(
                new Object[] {controller}, new Filter[] {filter});

        testClient = webServerExtension.createWebTestClient();
    }

    @AfterAll
    static void performCleanupAfterFinalTestRun() throws Exception {
        testClient = null;
        controller = null;
        resourceLoader = null;
    }

    static Stream<Arguments> accountAttachmentListings() {
        return Arrays.stream(AccountAttachmentControllerTestData.values())
                .map(fixture -> FixtureUtils.createNamedAnnotationFixtureArgument(
                        fixture, AccountAttachmentListingFixture.class));
    }

    @ParameterizedTest
    @MethodSource("accountAttachmentListings")
    void testGetAccountAttachmentListing(final AccountAttachmentListingFixture listingFixture) throws Exception {
        final AccountAttachmentListingDto expectedResponseBody = AccountAttachmentListingFixture.Utils
                .toAccountAttachmentListingDto(listingFixture);

        testClient.get()
                .uri(ATTACHMENT_LISTING_URL, listingFixture.chartOfAccountsCode(), listingFixture.accountNumber())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::setBasicAuthorizationHeaderWithValidCredentials)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(AccountAttachmentListingDto.class).isEqualTo(expectedResponseBody);
    }

    @ParameterizedTest
    @MethodSource("accountAttachmentListings")
    void testCannotGetAccountAttachmentListingWithMissingCredentials(
            final AccountAttachmentListingFixture listingFixture) throws Exception {
        assertInvocationReturns401UnauthorizedError(this::leaveHeadersAsIs,
                ATTACHMENT_LISTING_URL, listingFixture.chartOfAccountsCode(), listingFixture.accountNumber());
    }

    @ParameterizedTest
    @MethodSource("accountAttachmentListings")
    void testCannotGetAccountAttachmentListingWithInvalidCredentials(
            final AccountAttachmentListingFixture listingFixture) throws Exception {
        assertInvocationReturns401UnauthorizedError(this::setBasicAuthorizationHeaderWithInvalidCredentials,
                ATTACHMENT_LISTING_URL, listingFixture.chartOfAccountsCode(), listingFixture.accountNumber());
    }

    @ParameterizedTest
    @MethodSource("accountAttachmentListings")
    void testCannotGetAccountAttachmentListingWhenServerHasBadState(
            final AccountAttachmentListingFixture listingFixture) throws Exception {
        assertInvocationReturns500InternalServerErrorWhenServerHasBadState(
                ATTACHMENT_LISTING_URL, listingFixture.chartOfAccountsCode(), listingFixture.accountNumber());
    }

    @ParameterizedTest
    @CsvSource({
        "IT,ABC6543",
        "ZZ,UNKNOWN",
        "EO,5959595",
        "MC,7575758"
    })
    void testCannotGetAccountAttachmentListingForNonExistentAccounts(
            final String chartOfAccountsCode, final String accountNumber) throws Exception {
        assertInvocationReturnsErrorResponseWithJsonErrorMessageList(HttpStatus.NOT_FOUND,
                ATTACHMENT_LISTING_URL, chartOfAccountsCode, accountNumber);
    }

    @ParameterizedTest
    @CsvSource({
        ",",
        ",G123456",
        "IT,",
        "T,G123456",
        "ITT,G123456",
        "IT,G12345",
        "IT,G1234567",
        "$T,G123456",
        "IT,G12&456",
        "IT$,G******"
    })
    void testCannotGetAccountAttachmentListingForMalformedIdentifiers(
            final String chartOfAccountsCode, final String accountNumber) throws Exception {
        assertInvocationReturnsErrorResponseWithJsonErrorMessageList(HttpStatus.BAD_REQUEST,
                ATTACHMENT_LISTING_URL, chartOfAccountsCode, accountNumber);
    }

    static Stream<Arguments> accountAttachments() {
        return Arrays.stream(AccountAttachmentControllerTestData.values())
                .map(enumFixture -> FixtureUtils.getAnnotationBasedFixture(
                        enumFixture, AccountAttachmentListingFixture.class))
                .flatMap(listingFixture -> {
                    return Arrays.stream(listingFixture.notes())
                            .filter(AccountAttachmentNoteFixture::hasAttachment)
                            .map(noteFixture -> Named.of(
                                    "Attachment[[ID=" + noteFixture.attachmentId() + "]]", noteFixture))
                            .map(wrappedNote -> Arguments.of(
                                    listingFixture.chartOfAccountsCode(), listingFixture.accountNumber(), wrappedNote));
                });
    }

    @ParameterizedTest
    @MethodSource("accountAttachments")
    void testGetAccountAttachmentContents(final String chartOfAccountsCode, final String accountNumber,
            final AccountAttachmentNoteFixture noteFixture) throws Exception {
        final byte[] expectedContents = AccountAttachmentNoteFixture.Utils.getAttachmentContents(noteFixture);
        final ContentDisposition expectedContentDispositionHeader = ContentDisposition.attachment()
                .filename(noteFixture.fileName(), StandardCharsets.UTF_8)
                .build();

        testClient.get()
                .uri(ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, noteFixture.attachmentId())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::setBasicAuthorizationHeaderWithValidCredentials)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM)
                .expectHeader().contentDisposition(expectedContentDispositionHeader)
                .expectBody(byte[].class).isEqualTo(expectedContents);
    }

    @ParameterizedTest
    @MethodSource("accountAttachments")
    void testCannotGetAccountAttachmentContentsWithMissingCredentials(final String chartOfAccountsCode,
            final String accountNumber, final AccountAttachmentNoteFixture noteFixture) throws Exception {
        assertInvocationReturns401UnauthorizedError(this::leaveHeadersAsIs,
                ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, noteFixture.attachmentId());
    }

    @ParameterizedTest
    @MethodSource("accountAttachments")
    void testCannotGetAccountAttachmentContentsWithInvalidCredentials(final String chartOfAccountsCode,
            final String accountNumber, final AccountAttachmentNoteFixture noteFixture) throws Exception {
        assertInvocationReturns401UnauthorizedError(this::setBasicAuthorizationHeaderWithInvalidCredentials,
                ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, noteFixture.attachmentId());
    }

    @ParameterizedTest
    @MethodSource("accountAttachments")
    void testCannotGetAccountAttachmentContentsWhenServerHasBadState(final String chartOfAccountsCode,
            final String accountNumber, final AccountAttachmentNoteFixture noteFixture) throws Exception {
        assertInvocationReturns500InternalServerErrorWhenServerHasBadState(
                ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, noteFixture.attachmentId());
    }

    @ParameterizedTest
    @CsvSource({
        "IT,5656565,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI000,11111112-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EF,EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI001,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c"
    })
    void testCannotGetAccountAttachmentContentsForNonExistentOrMismappedData(final String chartOfAccountsCode,
            final String accountNumber, final String attachmentId) throws Exception {
        assertInvocationReturnsErrorResponseWithJsonErrorMessageList(HttpStatus.NOT_FOUND,
                ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, attachmentId);
    }

    @ParameterizedTest
    @CsvSource({
        ",,",
        ",EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI000,",
        "E,EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EOO,EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI00,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI0000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c3",
        "E#,EIEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,$IEI000,11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        "EO,EIEI000,11111111-\tzzz-2222-yyyy-3c3c3c3c3c3c",
        "E!,$#EI000,11111111-    -2222-yyyy-3c3c3c3c3c3c"
    })
    void testCannotGetAccountAttachmentContentsForMalformedIdentifiers(final String chartOfAccountsCode,
            final String accountNumber, final String attachmentId) throws Exception {
        assertInvocationReturnsErrorResponseWithJsonErrorMessageList(HttpStatus.BAD_REQUEST,
                ATTACHMENT_CONTENTS_URL, chartOfAccountsCode, accountNumber, attachmentId);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        BASE_ATTACHMENT_LISTING_URL,
        BASE_ATTACHMENT_LISTING_URL + "?chartCode=IT",
        BASE_ATTACHMENT_LISTING_URL + "?accountNumber=G123456",
        BASE_ATTACHMENT_LISTING_URL + "?otherParam=TESTING",
        BASE_ATTACHMENT_CONTENTS_URL,
        BASE_ATTACHMENT_CONTENTS_URL + "?chartCode=IT&attachmentId=11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        BASE_ATTACHMENT_CONTENTS_URL + "?accountNumber=G123456&attachmentId=11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
        BASE_ATTACHMENT_CONTENTS_URL + "?chartCode=IT&accountNumber=G123456",
        BASE_ATTACHMENT_CONTENTS_URL + "?otherParam=TESTING"
    })
    void testCannotInvokeEndpointsWithMissingParameters(final String url) throws Exception {
        assertInvocationReturnsErrorResponseWithJsonErrorMessageList(HttpStatus.BAD_REQUEST, url);
    }

    private void assertInvocationReturns500InternalServerErrorWhenServerHasBadState(
            final String uri, final Object... uriVariables) {
        final AccountService accountService = springContextExtension.getBean(
                CoaTestBeanNames.ACCOUNT_SERVICE, AccountService.class);
        final AccountAttachmentServiceImpl accountAttachmentService = springContextExtension.getBean(
                CoaTestBeanNames.ACCOUNT_ATTACHMENT_SERVICE,
                AccountAttachmentServiceImpl.class);
        assertNotNull(accountService, "Could not find AccountService");
        assertNotNull(accountAttachmentService, "Could not find AccountAttachmentServiceImpl");

        try {
            accountAttachmentService.setAccountService(null);
            assertInvocationReturnsErrorResponseWithJsonErrorMessageList(
                    HttpStatus.INTERNAL_SERVER_ERROR, uri, uriVariables);
        } finally {
            accountAttachmentService.setAccountService(accountService);
        }
    }

    private void assertInvocationReturnsErrorResponseWithJsonErrorMessageList(final HttpStatus expectedStatus,
            final String uri, final Object... uriVariables) {
        testClient.get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::setBasicAuthorizationHeaderWithValidCredentials)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(AccountAttachmentErrorResponseDto.class).value(this::assertResponseBodyHasErrorMessages);
    }

    private void assertResponseBodyHasErrorMessages(final AccountAttachmentErrorResponseDto responseBody) {
        assertNotNull(responseBody, "JSON response body should not have been null");
        assertTrue(CollectionUtils.isNotEmpty(responseBody.getErrors()),
                "JSON response body should have contained at least one error message");
        int i = 0;
        for (final String errorMessage : responseBody.getErrors()) {
            assertTrue(StringUtils.isNotBlank(errorMessage),
                    "Found an unexpected blank error message at index " + i);
            i++;
        }
    }

    private void assertInvocationReturns401UnauthorizedError(final Consumer<HttpHeaders> headersInitializer,
            final String uri, final Object... uriVariables) {
        testClient.get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headersInitializer)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class).value(this::assertErrorMessageIsNotBlank);
    }

    private void assertErrorMessageIsNotBlank(final String body) {
        assertTrue(StringUtils.isNotBlank(body), "Error message in response body should not have been blank");
    }

    private void leaveHeadersAsIs(final HttpHeaders headers) {
        // Do nothing.
    }

    private void setBasicAuthorizationHeaderWithValidCredentials(final HttpHeaders headers) {
        final String encodedCredentials = encodeCredentials(TEST_CREDENTIALS);
        headers.setBasicAuth(encodedCredentials);
    }

    private void setBasicAuthorizationHeaderWithInvalidCredentials(final HttpHeaders headers) {
        final String encodedCredentials = encodeCredentials(BAD_CREDENTIALS);
        headers.setBasicAuth(encodedCredentials);
    }

    private String encodeCredentials(final String credentials) {
        final byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
        final byte[] encodedBytes = Base64.encodeBase64(credentialsBytes);
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

}
