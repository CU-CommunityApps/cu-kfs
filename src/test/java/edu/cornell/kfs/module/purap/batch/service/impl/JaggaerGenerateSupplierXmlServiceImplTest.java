package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.impl.fixture.JaggaerVendorAddressFixture;
import edu.cornell.kfs.module.purap.batch.service.impl.fixture.JaggaerVendorDetailFixture;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Address;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.JaggaerBuilderTest;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Supplier;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierRequestMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;

@Execution(ExecutionMode.SAME_THREAD)
public class JaggaerGenerateSupplierXmlServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private JaggaerGenerateSupplierXmlServiceImpl jaggaerGenerateSupplierXmlServiceImpl;
    
    private static final String OUTPUT_FILE_PATH = "test/jaggaer/JaggaerGenerateSupplierXmlServiceImplTest/";
    private File outputFileDirectory;
    private TestDateTimeServiceImpl dateTimeService;
    

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(JaggaerGenerateSupplierXmlServiceImpl.class.getName(), Level.DEBUG);
        jaggaerGenerateSupplierXmlServiceImpl = new JaggaerGenerateSupplierXmlServiceImpl();
        jaggaerGenerateSupplierXmlServiceImpl.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        outputFileDirectory = new File(OUTPUT_FILE_PATH);
        outputFileDirectory.mkdir();
    }
    
    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService service = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(service.getWebServiceCredentialValue(CUPurapParameterConstants.JAGGAER_WEBSERVICE_GROUP_CODE,
                CUPurapParameterConstants.JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_NAME)).thenReturn("Cornell");
        Mockito.when(service.getWebServiceCredentialValue(CUPurapParameterConstants.JAGGAER_WEBSERVICE_GROUP_CODE,
                CUPurapParameterConstants.JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_PASSWORD)).thenReturn("test password");
        return service;
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerGenerateSupplierXmlServiceImpl = null;
        FileUtils.deleteDirectory(outputFileDirectory);
    }

    @ParameterizedTest
    @MethodSource("provideForTestBuildSupplierSyncMessageList")
    void testBuildSupplierSyncMessageList(int numberOfSuppliers, int maximumNumberOfSuppliersPerListItem, int expectedNumberOfSupplierSyncMessage) {
        List<Supplier> suppliers = buildListOfSuppliers(numberOfSuppliers);
        List<SupplierSyncMessage> messages  = jaggaerGenerateSupplierXmlServiceImpl.buildSupplierSyncMessageList(suppliers, maximumNumberOfSuppliersPerListItem);
        assertEquals(expectedNumberOfSupplierSyncMessage, messages.size());
    }
    
    private static Stream<Arguments> provideForTestBuildSupplierSyncMessageList() {
        return Stream.of(
          Arguments.of(1, 1, 1),
          Arguments.of(2, 1, 2),
          Arguments.of(2, 2, 1),
          Arguments.of(100, 30, 4),
          Arguments.of(100, 500, 1),
          Arguments.of(0, 1, 0)
        );
    }
    
    private List<Supplier> buildListOfSuppliers(int numberOfSuppliers) {
        List<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < numberOfSuppliers; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(JaggaerBuilderTest.buildName("Test supplier " + i));
            suppliers.add(supplier);
        }
        return suppliers;
    }
    
    private Instant getCurrentDateTimeAsUtcInstant() {
        return Instant.now();
    }
    
    private ZonedDateTime convertInstantToSystemDefaultZone(Instant pointInTime) {
        return pointInTime.atZone(ZoneId.systemDefault());
    }
    
    private ZonedDateTime convertInstantToUtcZone(Instant pointInTime) {
        return pointInTime.atZone(ZoneOffset.UTC);
    }
    
    @Test
    void testDateFormatterFileName() {
        Instant currentDateTimeUtcInstant = getCurrentDateTimeAsUtcInstant();
        String manuallyFormattedFileNameDateTimeString = buildExpectedFileNameLocaleDateTimeString(convertInstantToSystemDefaultZone(currentDateTimeUtcInstant));
        String formattedFileNameDateTimeString = convertInstantToSystemDefaultZone(currentDateTimeUtcInstant).format(JaggaerGenerateSupplierXmlServiceImpl.DATE_TIME_ZONE_DEFAULT_FORMATTER_yyyyMMdd_HHmmssSSS);
        LOG.info("");
        LOG.info("testDateFormatterLocaleFileName::: File Name should have local time zone (NOT UTC): currentDateTimeUtcInstant={}  manuallyFormattedFileNameDateTimeString={}  formattedFileNameDateTimeString={}", currentDateTimeUtcInstant, manuallyFormattedFileNameDateTimeString, formattedFileNameDateTimeString);
        LOG.info("");
        assertEquals(manuallyFormattedFileNameDateTimeString, formattedFileNameDateTimeString);
    }
    
    @Test
    void testDateFormatterFileHeader() {
        Instant currentDateTimeAsUtcInstant = getCurrentDateTimeAsUtcInstant();
        String manuallyFormattedHeaderDateTimeString = buildExpectedHeaderUtcDateTimeString(convertInstantToUtcZone(currentDateTimeAsUtcInstant));
        String formattedHeaderDateTimeString = convertInstantToUtcZone(currentDateTimeAsUtcInstant).format(JaggaerGenerateSupplierXmlServiceImpl.DATE_TIME_ZONE_UTC_FORMATTER_yyyy_MM_dd_T_HH_mm_ss_SSS_Z);
        LOG.info("");
        LOG.info("testDateFormatterUtcFileHeader: File Header should have UTC time zone:  currentDateTimeAsInstant={}  manuallyFormattedHeaderDateTimeString={}  formattedHeaderDateTimeString={}", currentDateTimeAsUtcInstant, manuallyFormattedHeaderDateTimeString, formattedHeaderDateTimeString);
        LOG.info("");
        assertEquals(manuallyFormattedHeaderDateTimeString, formattedHeaderDateTimeString);
    }
    
    private String buildExpectedFileNameLocaleDateTimeString(ZonedDateTime preZonedDateTime) {
        String manualFileNameDateString = new String();
        int myNanoSeconds = preZonedDateTime.getNano() / 1000000;
        manualFileNameDateString = manualFileNameDateString + preZonedDateTime.getYear() 
            + (preZonedDateTime.getMonthValue() < 10 ? "0" + preZonedDateTime.getMonthValue() : preZonedDateTime.getMonthValue())
            + (preZonedDateTime.getDayOfMonth() < 10 ? "0" + preZonedDateTime.getDayOfMonth() : preZonedDateTime.getDayOfMonth())
            + "_"
            + (preZonedDateTime.getHour() < 10 ? "0" + preZonedDateTime.getHour() : preZonedDateTime.getHour())
            + (preZonedDateTime.getMinute() < 10 ? "0" + preZonedDateTime.getMinute() : preZonedDateTime.getMinute())
            + (preZonedDateTime.getSecond() < 10 ? "0" + preZonedDateTime.getSecond() : preZonedDateTime.getSecond())
            + (myNanoSeconds < 10 ? "00" + myNanoSeconds : (myNanoSeconds < 100 ? "0" + myNanoSeconds : myNanoSeconds));
        return manualFileNameDateString;
    }
    
    private String buildExpectedHeaderUtcDateTimeString(ZonedDateTime utcZonedDateTime) {
        String manualHeaderDateString = new String();
        int myNanoSeconds = utcZonedDateTime.getNano() / 1000000;
        manualHeaderDateString = manualHeaderDateString + utcZonedDateTime.getYear() + "-"
                + (utcZonedDateTime.getMonthValue() < 10 ? "0" + utcZonedDateTime.getMonthValue() : utcZonedDateTime.getMonthValue()) + "-"
                + (utcZonedDateTime.getDayOfMonth() < 10 ? "0" + utcZonedDateTime.getDayOfMonth() : utcZonedDateTime.getDayOfMonth())
                + "T"
                + (utcZonedDateTime.getHour() < 10 ? "0" + utcZonedDateTime.getHour() : utcZonedDateTime.getHour()) + ":"
                + (utcZonedDateTime.getMinute() < 10 ? "0" + utcZonedDateTime.getMinute() : utcZonedDateTime.getMinute()) + ":"
                + (utcZonedDateTime.getSecond() < 10 ? "0" + utcZonedDateTime.getSecond() : utcZonedDateTime.getSecond()) + "."
                + (myNanoSeconds < 10 ? "00" + myNanoSeconds : (myNanoSeconds < 100 ? "0" + myNanoSeconds : myNanoSeconds)) + "Z";
        return manualHeaderDateString;
    }
    
    @ParameterizedTest
    @EnumSource(JaggaerVendorDetailFixture.class)
    void testGetSupplierSyncMessages(JaggaerVendorDetailFixture vendorDetailFixture) {
        JaggaerUploadSuppliersProcessingMode processingMode = JaggaerUploadSuppliersProcessingMode.VENDOR;
        Date processDate = dateTimeService.getCurrentSqlDate();
        jaggaerGenerateSupplierXmlServiceImpl.setJaggaerUploadDao(buildBockJaggaerUploadDao(processingMode, processDate, vendorDetailFixture));
        jaggaerGenerateSupplierXmlServiceImpl.setIsoFipsConversionService(buildMockISOFIPSConversionService());
        jaggaerGenerateSupplierXmlServiceImpl.setParameterService(buildMockParameterService());
        jaggaerGenerateSupplierXmlServiceImpl.setConfigurationService(buildMockConfigurationService());
        
        List<SupplierSyncMessage> supplierSyncMessages = jaggaerGenerateSupplierXmlServiceImpl.getSupplierSyncMessages(processingMode, processDate, 1);
        validateSupplierSynchMessage(supplierSyncMessages, vendorDetailFixture);
    }
    
    private JaggaerUploadDao buildBockJaggaerUploadDao(JaggaerUploadSuppliersProcessingMode processingMode, Date processDate, JaggaerVendorDetailFixture vendorDetailFixture) {
        JaggaerUploadDao dao = Mockito.mock(JaggaerUploadDao.class);        
        List<VendorDetail> vendorDetails = new ArrayList<>();
        vendorDetails.add(vendorDetailFixture.toVendorDetail());
        Mockito.when(dao.findVendors(processingMode, processDate)).thenReturn(vendorDetails);
        return dao;
    }
    
    private ISOFIPSConversionService buildMockISOFIPSConversionService() {
        ISOFIPSConversionService service = Mockito.mock(ISOFIPSConversionService.class);
        Mockito.when(service.convertFIPSCountryCodeToActiveISOCountryCode(Mockito.anyString())).thenAnswer(invocation -> 
        invocation.getArgument(0, String.class));
        return service;
    }
    
    private ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_ADDRESS_NOTE_TEXT)).thenReturn("The KFS vendor addres type is");
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG)).thenReturn(CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_DTD_TAG);
        return service;
    }
    
    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configService.getPropertyValueAsString(CUPurapKeyConstants.JAGGAER_XML_LOCATION_NAME_FORMAT)).thenReturn("{0} - {1}");
        return configService;
    }
    
    private void validateSupplierSynchMessage(List<SupplierSyncMessage> supplierSyncMessages, JaggaerVendorDetailFixture vendorDetailFixture) {
        assertEquals(1, supplierSyncMessages.size());
        SupplierSyncMessage supplierSyncMessage = supplierSyncMessages.get(0);
        assertEquals(1, supplierSyncMessage.getSupplierSyncMessageItems().size());
        SupplierRequestMessage requestMessage = (SupplierRequestMessage) supplierSyncMessage.getSupplierSyncMessageItems().get(0);
        assertEquals(1, requestMessage.getSuppliers().size());
        Supplier supplier = requestMessage.getSuppliers().get(0);
        
        String expectedVendorNumber = vendorDetailFixture.generatedIdentifier + "-" + vendorDetailFixture.assignedIdentifier;
        assertEquals(expectedVendorNumber, supplier.getErpNumber().getValue());
        
        assertEquals(vendorDetailFixture.name, supplier.getName().getValue());
        assertEquals(vendorDetailFixture.countryOfOrigin, supplier.getCountryOfOrigin().getValue());
        assertEquals(vendorDetailFixture.url, supplier.getWebSiteURL().getValue());
        
        String expectedLegalStructureCode = JaggaerLegalStructure.findJaggaerLegalStructureByKfsOwnershipCode(vendorDetailFixture.ownershipCode).jaggaerLegalStructureName;
        assertEquals(expectedLegalStructureCode, supplier.getLegalStructure().getValue());
        
        assertEquals(vendorDetailFixture.expectedVendorAdressIds.size(), supplier.getAddressList().getAddresses().size());
        
        for (JaggaerVendorAddressFixture addressFixture : vendorDetailFixture.addresses) {
            if (vendorDetailFixture.expectedVendorAdressIds.contains(String.valueOf(addressFixture.addressId))) {
                Address supplierAddress = supplier.getAddressList().getAddresses().stream().filter(address -> StringUtils
                        .equalsIgnoreCase(String.valueOf(addressFixture.addressId), address.getErpNumber().getValue()))
                        .findFirst().get();
                assertEquals(addressFixture.countryCode, supplierAddress.getIsoCountryCode().getValue());
                assertEquals(addressFixture.addressLine1, supplierAddress.getAddressLine1().getValue());
                assertEquals(addressFixture.addressLine2, supplierAddress.getAddressLine2().getValue());
                assertEquals(addressFixture.city, supplierAddress.getCity().getValue());
                assertEquals(addressFixture.zip, supplierAddress.getPostalCode().getValue());
                
                String expectedState = StringUtils.isNotBlank(addressFixture.state) ? addressFixture.state : addressFixture.internationalState;
                assertEquals(expectedState, supplierAddress.getState().getValue());
            }
        }
    }
    
    @ParameterizedTest
    @MethodSource("provideForTestIsValidUrl")
    void testIsValidUrl(String url,  String vendorNumber, boolean expectedResults) {
        boolean actualResults = jaggaerGenerateSupplierXmlServiceImpl.isValidUrl(url, vendorNumber);
        assertEquals(expectedResults, actualResults);
    }
    
    private static Stream<Arguments> provideForTestIsValidUrl() {
        return Stream.of(
          Arguments.of(StringUtils.EMPTY, "empty", false),
          Arguments.of(StringUtils.SPACE, "space", false),
          Arguments.of(null, "nullVendor", false),
          Arguments.of("824910376", "numbers", false),
          Arguments.of("foobar", "words", false),
          Arguments.of("foo bar", "wordsAndSpace", false),
          Arguments.of("www.somedumbwebsite101.com", "badDomain", false),
          Arguments.of("somedumbwebsite101.com", "badDomain2", false),
          Arguments.of("13.107.213.35", "CornellIP", true),
          Arguments.of("cornell.edu", "domain", true),
          Arguments.of("www.google.com", "wwwDomain", true),
          Arguments.of("http://www.google.com", "httpDomain", true),
          Arguments.of("https://www.google.com", "httpsDomain", true),
          Arguments.of("https://www.cornell.edu/subFolder", "httpsDomainSub", true),
          Arguments.of("https://www.cornell.edu/index.jsp", "httpsDomainPage", true),
          Arguments.of("https://www.cornell.edu/index.jsp?foo=bar", "httpsDomainPageParam", true),
          Arguments.of("https://www.cornell.edu/index.jsp#section1", "httpsDomainPageFragment", true),
          Arguments.of("http:443//www.google.com", "httpPortDomain", true),
          Arguments.of("http://128.253.173.243", "httpIp", true),
          Arguments.of("https://128.253.173.243", "httpsIp", true),
          Arguments.of("https://128.253.173.243/index.aspx", "httpsIpPage", true)
        );
    }
}
