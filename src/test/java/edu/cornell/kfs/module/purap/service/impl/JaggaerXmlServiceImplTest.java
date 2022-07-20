package edu.cornell.kfs.module.purap.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.CoreUtilities;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.module.purap.businessobject.B2BInformation;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.CuPurapTestConstants.TestB2BInformation;
import edu.cornell.kfs.module.purap.service.JaggaerRoleService;
import edu.cornell.kfs.module.purap.service.JaggaerXmlService;
import edu.cornell.kfs.module.purap.service.impl.fixture.JaggaerPersonFixture;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.xmladapters.DateTimeUTCOffsetStringToJavaDateAdapter;

public class JaggaerXmlServiceImplTest {

    private static final String BASE_CXML_FILE_PATH = "classpath:edu/cornell/kfs/module/purap/service/impl/";
    private static final String MOCK_CURRENT_DATE = "2022-07-15T11:30:55.000-04:00";

    private Map<String, Map<JaggaerRoleSet, List<String>>> jaggaerRoleMappings;
    private JaggaerXmlServiceImpl jaggaerXmlService;
    private B2BInformation b2bInformation;

    @BeforeEach
    void setUp() throws Exception {
        jaggaerRoleMappings = createMockJaggaerRoleMappings();

        jaggaerXmlService = new JaggaerXmlServiceImpl();
        jaggaerXmlService.setJaggaerRoleService(createMockJaggaerRoleService());
        jaggaerXmlService.setCuMarshalService(new CUMarshalServiceImpl());
        jaggaerXmlService.setDateTimeService(createMockDateTimeService());
        jaggaerXmlService.setInternalSupplierId(CuPurapTestConstants.TEST_INTERNAL_SUPPLIER_ID);

        b2bInformation = createTestB2BInformation();
    }

    private Map<String, Map<JaggaerRoleSet, List<String>>> createMockJaggaerRoleMappings() {
        return Map.ofEntries(
                Map.entry(JaggaerPersonFixture.JOHN_DOE.principalName, Map.ofEntries(
                        Map.entry(JaggaerRoleSet.ESHOP, List.of(CUPurapConstants.SCIQUEST_ROLE_SHOPPER)),
                        Map.entry(JaggaerRoleSet.CONTRACTS_PLUS, List.of(
                                CUPurapConstants.SCIQUEST_ROLE_OFFICE, CUPurapConstants.SCIQUEST_ROLE_LAB)),
                        Map.entry(JaggaerRoleSet.ADMINISTRATOR, List.of())
                ))
        );
    }

    private JaggaerRoleService createMockJaggaerRoleService() {
        JaggaerRoleService jaggaerRoleService = Mockito.mock(JaggaerRoleService.class);
        Mockito.when(jaggaerRoleService.getJaggaerRoles(Mockito.any(), Mockito.any()))
                .then(this::getJaggaerRolesFromMap);
        return jaggaerRoleService;
    }

    private List<String> getJaggaerRolesFromMap(InvocationOnMock invocation) {
        Person user = invocation.getArgument(0);
        JaggaerRoleSet roleSet = invocation.getArgument(1);
        List<String> roles = null;
        Map<JaggaerRoleSet, List<String>> subMapping = jaggaerRoleMappings.get(user.getPrincipalName());
        if (subMapping != null) {
            roles = subMapping.get(roleSet);
        }
        return (roles != null) ? roles : List.of();
    }

    private DateTimeService createMockDateTimeService() {
        Date mockCurrentDate = DateTimeUTCOffsetStringToJavaDateAdapter.parseDateString(MOCK_CURRENT_DATE);
        DateTimeService dateTimeService = Mockito.mock(DateTimeService.class);
        Mockito.when(dateTimeService.getCurrentDate())
                .thenReturn(mockCurrentDate);
        return dateTimeService;
    }

    private B2BInformation createTestB2BInformation() {
        B2BInformation b2bInfo = new B2BInformation();
        b2bInfo.setPunchoutURL(TestB2BInformation.PUNCHOUT_URL);
        b2bInfo.setPunchbackURL(TestB2BInformation.PUNCHBACK_URL);
        b2bInfo.setEnvironment(TestB2BInformation.ENVIRONMENT);
        b2bInfo.setUserAgent(TestB2BInformation.USER_AGENT);
        b2bInfo.setIdentity(TestB2BInformation.SHOPPING_IDENTITY);
        b2bInfo.setPassword(TestB2BInformation.SHOPPING_PASSWORD);
        return b2bInfo;
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerRoleMappings = null;
        jaggaerXmlService = null;
        b2bInformation = null;
    }

    static Stream<Arguments> cxmlTestCases() {
        return Stream.of(
                Arguments.of("cxml_john_doe_eshop.xml", JaggaerPersonFixture.JOHN_DOE,
                        serviceMethod(JaggaerXmlService::getJaggaerLoginXmlForEShop))
        );
    } 

    private static <T, U, R> JaggaerXmlServiceFunction<T, U, R> serviceMethod(
            JaggaerXmlServiceFunction<T, U, R> value) {
        return value;
    }

    @ParameterizedTest
    @MethodSource("cxmlTestCases")
    void testGenerateSuccessfulCxml(String expectedCxmlFileLocalName, JaggaerPersonFixture userFixture,
            JaggaerXmlServiceFunction<Person, B2BInformation, String> jaggaerXmlServiceMethod) throws Exception {
        String expectedCxml = readExpectedCxmlFromFile(expectedCxmlFileLocalName);
        Person user = userFixture.toKimPerson();
        assertServiceGeneratesCorrectCxml(expectedCxml, user, jaggaerXmlServiceMethod);
    }

    private void assertServiceGeneratesCorrectCxml(String expectedCxml, Person user,
            JaggaerXmlServiceFunction<Person, B2BInformation, String> jaggaerXmlServiceMethod) throws Exception {
        String actualCxml = jaggaerXmlServiceMethod.apply(jaggaerXmlService, user, b2bInformation);
        String expectedNormalizedCxml = StringUtils.normalizeSpace(expectedCxml);
        String actualNormalizedCxml = StringUtils.normalizeSpace(actualCxml);
        assertEquals(expectedNormalizedCxml, actualNormalizedCxml, "Wrong CXML content was generated");
    }

    private String readExpectedCxmlFromFile(String fileLocalName) throws Exception {
        try (
            InputStream fileStream = CoreUtilities.getResourceAsStream(BASE_CXML_FILE_PATH + fileLocalName);
            InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
            StringBuilderWriter writer = new StringBuilderWriter();
        ) {
            IOUtils.copy(reader, writer);
            return writer.getBuilder().toString();
        }
    }

    // TODO: Replace this with Commons Lang3 TriFunction usage when we upgrade to Commons Lang3 3.12.0 or higher.
    @FunctionalInterface
    private static interface JaggaerXmlServiceFunction<T, U, R> {
        R apply(JaggaerXmlService jaggaerXmlService, T arg1, U arg2);
    }

}
