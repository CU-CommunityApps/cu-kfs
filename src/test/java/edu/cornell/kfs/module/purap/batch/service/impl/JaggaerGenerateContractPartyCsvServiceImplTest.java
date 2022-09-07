package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressHeader;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerPartyHeader;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvServiceImplTest {
    private JaggaerGenerateContractPartyCsvServiceImpl jaggaerGenerateContractPartyCsvServiceImpl;
    
    private static final String COUNTRY_US = "US";
    private static final String STATE_NY = "NY";
    private static final String PROCESS_DATE = "2022-08-16";
    
    private static final String VENDOR1_VENDOR_NUMBER = "12345-0";
    private static final String VENDOR1_VENDOR_NAME = "Acme Testing Company";
    private static final String VENDOR1_VENDOR_COUNTRY = COUNTRY_US;
    private static final String VENDOR1_VENDOR_URL = "http://www.google.com";
    
    private static final String VENDOR1_ADDRESS1_ID = "12342";
    private static final String VENDOR1_ADDRESS1_LN1 = "123 Main Street";
    private static final String VENDOR1_ADDRESS1_LN2 = "Apartment 666";
    private static final String VENDOR1_ADDRESS1_CITY = "Freeville";
    private static final String VENDOR1_ADDRESS1_STATE = STATE_NY;
    private static final String VENDOR1_ADDRESS1_ZIP = "13068";
    
    private static final String VENDOR1_ADDRESS2_ID = "98765";
    private static final String VENDOR1_ADDRESS2_LN1 = "45 Palm Street";
    private static final String VENDOR1_ADDRESS2_LN2 = "Apartment 1F";
    private static final String VENDOR1_ADDRESS2_CITY = "Ithaca";
    private static final String VENDOR1_ADDRESS2_STATE = STATE_NY;
    private static final String VENDOR1_ADDRESS2_ZIP = "14850";
    
    private static final String VENDOR2_VENDOR_NUMBER = "98765-0";
    private static final String VENDOR2_VENDOR_NAME = "Jane Doe Paint Services";
    private static final String VENDOR2_VENDOR_COUNTRY = COUNTRY_US;
    private static final String VENDOR2_VENDOR_URL = "http://www.yahoo.com";
    private static final String VENDOR2_TAX_ID  = "65479";
    
    private static final String VENDOR3_ADDRESS1_FAX = "fax";
    private static final String VENDOR3_ADDRESS1_TOLL_FREE_NUMBER = "18002809900";
    private static final String VENDOR3_ADDRESS1_PHONE = "6072559900";
    private static final String VENDOR3_ADDRESS1_ZIP = "14850";
    private static final String VENDOR3_ADDRESS1_CITY = "city";
    private static final String VENDOR3_ADDRESS1_STREET_LINE_3 = "street line 3";
    private static final String VENDOR3_ADDRESS1_STREET_LINE_2 = "street line 2";
    private static final String VENDOR3_ADDRESS1_STREET_LINE_1 = "street line 1";
    private static final String VENDOR3_ADDRESS1_PRIMARY_TYPE = "primary type";
    private static final String VENDOR3_ADDRESS1_NAME = "name";
    private static final String VENDOR3_ADDRESS1_SCIQUEST_ID = "ABCD";
    private static final String VENDOR3_ADDRESS1_ADDRESS_ID = "addressId";
    private static final String VENDOR3_URL = "www.google.com";
    private static final String VENDOR3_VAT = "vat number";
    private static final String VENDOR3_TAX_ID = "369852";
    private static final String VENDOR3_TAX_TYPE = "foo type";
    private static final String VENDOR3_PRIMARY = "primary";
    private static final String VENDOR3_ACTIVE = "active";
    private static final String VENDOR3_OTHER_NAME = "other name";
    private static final String VENDOR3_DBA = "Acme";
    private static final String VENDOR3_CONTRACT_PARTY_NAME = "Acme Inc";
    private static final String VENDOR3_ERP_NUMBER = "123456-0";

    @BeforeEach
    void setUp() throws Exception {
        jaggaerGenerateContractPartyCsvServiceImpl = new JaggaerGenerateContractPartyCsvServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerGenerateContractPartyCsvServiceImpl = null;
    }
    
    private JaggaerUploadDao buildMockJaggaerUploadDao() {
        JaggaerUploadDao dao = Mockito.mock(JaggaerUploadDao.class);
        Mockito.when(dao.findJaggaerContractParty(JaggaerContractUploadProcessingMode.VENDOR, PROCESS_DATE)).thenReturn(getMockVendorDtos());
        Mockito.when(dao.findJaggaerContractAddress(JaggaerContractUploadProcessingMode.VENDOR, PROCESS_DATE)).thenReturn(buildMockAddressDtos());
        return dao;
    }
    
    private List<JaggaerContractPartyUploadDto> getMockVendorDtos() {
        List<JaggaerContractPartyUploadDto> dtos = new ArrayList<>();
        dtos.add(buildVendor1Dto());
        dtos.add(buildVendor2WithTaxIdDto());
        return dtos;
    }

    private static JaggaerContractPartyUploadDto buildVendor1Dto() {
        JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
        dto.setERPNumber(VENDOR1_VENDOR_NUMBER);
        dto.setContractPartyName(VENDOR1_VENDOR_NAME);
        dto.setCountryOfOrigin(VENDOR1_VENDOR_COUNTRY);
        dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
        dto.setLegalStructure(JaggaerLegalStructure.C_CORPORATION);
        dto.setWebsiteURL(VENDOR1_VENDOR_URL);
        return dto;
    }

    public static JaggaerContractPartyUploadDto buildVendor2WithTaxIdDto() {
        JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
        dto.setERPNumber(VENDOR2_VENDOR_NUMBER);
        dto.setTaxIdentificationNumber(VENDOR2_TAX_ID);
        dto.setContractPartyName(VENDOR2_VENDOR_NAME);
        dto.setCountryOfOrigin(VENDOR2_VENDOR_COUNTRY);
        dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
        dto.setLegalStructure(JaggaerLegalStructure.INDIVIDUAL);
        dto.setWebsiteURL(VENDOR2_VENDOR_URL);
        return dto;
    }
    
    private List<JaggaerContractAddressUploadDto> buildMockAddressDtos() {
        List<JaggaerContractAddressUploadDto> dtos = new ArrayList<>();
        dtos.add(buildVendor1Address1Dto());
        dtos.add(buildVendor1Address2Dto());
        dtos.add(buildNoVendorAddressDto());
        return dtos;
    }

    private JaggaerContractAddressUploadDto buildVendor1Address1Dto() {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
        dto.setAddressID(VENDOR1_ADDRESS1_ID);
        dto.setAddressType(JaggaerAddressType.FULFILLMENT);
        dto.setCountry(VENDOR1_VENDOR_COUNTRY);
        dto.setStreetLine1(VENDOR1_ADDRESS1_LN1);
        dto.setStreetLine2(VENDOR1_ADDRESS1_LN2);
        dto.setCity(VENDOR1_ADDRESS1_CITY);
        dto.setState(VENDOR1_ADDRESS1_STATE);
        dto.setPostalCode(VENDOR1_ADDRESS1_ZIP);
        dto.setERPNumber(VENDOR1_VENDOR_NUMBER);
        return dto;
    }

    private JaggaerContractAddressUploadDto buildVendor1Address2Dto() {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
        dto.setAddressID(VENDOR1_ADDRESS2_ID);
        dto.setAddressType(JaggaerAddressType.REMIT);
        dto.setCountry(VENDOR1_VENDOR_COUNTRY);
        dto.setStreetLine1(VENDOR1_ADDRESS2_LN1);
        dto.setStreetLine2(VENDOR1_ADDRESS2_LN2);
        dto.setCity(VENDOR1_ADDRESS2_CITY);
        dto.setState(VENDOR1_ADDRESS2_STATE);
        dto.setPostalCode(VENDOR1_ADDRESS2_ZIP);
        dto.setERPNumber(VENDOR1_VENDOR_NUMBER);
        return dto;
    }
    
    private JaggaerContractAddressUploadDto buildNoVendorAddressDto() {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
        dto.setAddressID("321");
        dto.setAddressType(JaggaerAddressType.REMIT);
        return dto;
    }

    @Test
    void testGetJaggerContractsDto() {
        jaggaerGenerateContractPartyCsvServiceImpl.setJaggaerUploadDao(buildMockJaggaerUploadDao());
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = jaggaerGenerateContractPartyCsvServiceImpl.getJaggerContractsDto(JaggaerContractUploadProcessingMode.VENDOR, PROCESS_DATE);
        assertEquals(4, jaggaerUploadDtos.size());
        
        JaggaerContractPartyUploadDto actualFirstElement = (JaggaerContractPartyUploadDto) jaggaerUploadDtos.get(0);
        assertEquals(buildVendor1Dto(), actualFirstElement, "The first element should be vendor 1");
        
        JaggaerContractAddressUploadDto actualSecondElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(1);
        JaggaerContractAddressUploadDto expectedAddress1 = buildVendor1Address1Dto();
        expectedAddress1.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress1, actualSecondElement, "The second element should be vendor 1 address 1");
        
        JaggaerContractAddressUploadDto actualThirdElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(2);
        JaggaerContractAddressUploadDto expectedAddress2 = buildVendor1Address2Dto();
        expectedAddress2.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress2, actualThirdElement, "The third element should be vendor 1 address 2");
        
        JaggaerContractPartyUploadDto actualFourthElement = (JaggaerContractPartyUploadDto) jaggaerUploadDtos.get(3);
        assertEquals(buildVendor2WithTaxIdDto(), actualFourthElement, "THe fourth element should be vendor 2");
    }
    
    @ParameterizedTest
    @MethodSource("buildJaggaerContractPartyUploadDtoToStringParameters")
    void testJaggaerContractPartyUploadDtoToString(JaggaerContractPartyUploadDto contractPartyDto, String searchString) {
        String actualToString = contractPartyDto.toString();
        assertTrue(StringUtils.contains(actualToString, searchString));
    }
    
    static Stream<Arguments> buildJaggaerContractPartyUploadDtoToStringParameters() {
        return Stream.of(
                Arguments.of(buildVendor2WithTaxIdDto(), "taxIdentificationNumber=restricted tax id number"),
                Arguments.of(buildVendor1Dto(),  "taxIdentificationNumber=,")
                );
    }
    
    @ParameterizedTest
    @MethodSource("buildVendorCSVRowArrayParameters")
    void testBuilderVendorCSVRowArray(JaggaerContractPartyUploadDto vendorDto, String[] expectedCsvData) {

        String[] actualCsvData = jaggaerGenerateContractPartyCsvServiceImpl.builderVendorCSVRowArray(vendorDto);
        int expectedNumberOfArrayElements = JaggaerPartyHeader.values().length;
        assertEquals(expectedNumberOfArrayElements, expectedCsvData.length);
        assertEquals(expectedNumberOfArrayElements, actualCsvData.length);
        validateCsvRowArray(expectedCsvData, actualCsvData);
    }

    public void validateCsvRowArray(String[] expectedCsvData, String[] actualCsvData) {
        for (int i = 0; i < expectedCsvData.length; i++) {
            String actualDatum = actualCsvData[i];
            String expectedDataum = expectedCsvData[i];
            assertEquals(expectedDataum, actualDatum);
        }
    }

    static Stream<Arguments> buildVendorCSVRowArrayParameters() {
        return Stream.of(
                Arguments.of(buildJaggaerContractPartyUploadDto(VENDOR3_ADDRESS1_SCIQUEST_ID),
                        buildJaggaerContractPartyUploadDtoCsvData(VENDOR3_ADDRESS1_SCIQUEST_ID)),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.EMPTY),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(null),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.SPACE),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)));
    }

    private static JaggaerContractPartyUploadDto buildJaggaerContractPartyUploadDto(String sciQuestId) {
        JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
        dto.setOverrideDupError(CUPurapConstants.FALSE_STRING);
        dto.setERPNumber(VENDOR3_ERP_NUMBER);
        dto.setSciQuestID(sciQuestId);
        dto.setContractPartyName(VENDOR3_CONTRACT_PARTY_NAME);
        dto.setDoingBusinessAs(VENDOR3_DBA);
        dto.setOtherNames(VENDOR3_OTHER_NAME);
        dto.setCountryOfOrigin(COUNTRY_US);
        dto.setActive(VENDOR3_ACTIVE);
        dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
        dto.setPrimary(VENDOR3_PRIMARY);
        dto.setLegalStructure(JaggaerLegalStructure.C_CORPORATION);
        dto.setTaxIDType(VENDOR3_TAX_TYPE);
        dto.setTaxIdentificationNumber(VENDOR3_TAX_ID);
        dto.setVATRegistrationNumber(VENDOR3_VAT);
        dto.setWebsiteURL(VENDOR3_URL);
        return dto;
    }

    private static String[] buildJaggaerContractPartyUploadDtoCsvData(String sciQuestId) {
        String[] record = { JaggaerContractPartyUploadRowType.PARTY.rowType, CUPurapConstants.FALSE_STRING, VENDOR3_ERP_NUMBER, sciQuestId,
                VENDOR3_CONTRACT_PARTY_NAME, VENDOR3_DBA, VENDOR3_OTHER_NAME, COUNTRY_US, VENDOR3_ACTIVE,
                JaggaerContractPartyType.SUPPLIER.partyTypeName, VENDOR3_PRIMARY,
                JaggaerLegalStructure.C_CORPORATION.jaggaerLegalStructureName, VENDOR3_TAX_TYPE, VENDOR3_TAX_ID,
                VENDOR3_VAT, VENDOR3_URL };
        return record;
    }

    @ParameterizedTest
    @MethodSource("buildAddressCSVRowArrayParameters")
    void testBuilderAddressCSVRowArray(JaggaerContractAddressUploadDto addressDto, String[] expectedCsvData) {
        String[] actualCsvData = jaggaerGenerateContractPartyCsvServiceImpl.builderAddressCSVRowArray(addressDto);
        int expectedNumberOfArrayElements = JaggaerAddressHeader.values().length;
        assertEquals(expectedNumberOfArrayElements, actualCsvData.length);
        assertEquals(expectedNumberOfArrayElements, expectedCsvData.length);
        validateCsvRowArray(expectedCsvData, actualCsvData);
    }

    static Stream<Arguments> buildAddressCSVRowArrayParameters() {
        return Stream.of(
                Arguments.of(buildJaggaerContractAddressUploadDto("cool note"),
                        buildJaggaerContractAddressUploadDtoData("cool note")),
                Arguments.of(buildJaggaerContractAddressUploadDto(StringUtils.EMPTY),
                        buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractAddressUploadDto(null),
                        buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractAddressUploadDto(StringUtils.SPACE),
                        buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY)));
    }

    static JaggaerContractAddressUploadDto buildJaggaerContractAddressUploadDto(String notes) {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
        dto.setAddressID(VENDOR3_ADDRESS1_ADDRESS_ID);
        dto.setSciQuestID(VENDOR3_ADDRESS1_SCIQUEST_ID);
        dto.setName(VENDOR3_ADDRESS1_NAME);
        dto.setAddressType(JaggaerAddressType.FULFILLMENT);
        dto.setPrimaryType(VENDOR3_ADDRESS1_PRIMARY_TYPE);
        dto.setActive(VENDOR3_ACTIVE);
        dto.setCountry(COUNTRY_US);
        dto.setStreetLine1(VENDOR3_ADDRESS1_STREET_LINE_1);
        dto.setStreetLine2(VENDOR3_ADDRESS1_STREET_LINE_2);
        dto.setStreetLine3(VENDOR3_ADDRESS1_STREET_LINE_3);
        dto.setCity(VENDOR3_ADDRESS1_CITY);
        dto.setState(STATE_NY);
        dto.setPostalCode(VENDOR3_ADDRESS1_ZIP);
        dto.setPhone(VENDOR3_ADDRESS1_PHONE);
        dto.setTollFreeNumber(VENDOR3_ADDRESS1_TOLL_FREE_NUMBER);
        dto.setFax(VENDOR3_ADDRESS1_FAX);
        dto.setNotes(notes);
        return dto;
    }

    static String[] buildJaggaerContractAddressUploadDtoData(String notes) {
        String[] record = { JaggaerContractPartyUploadRowType.ADDRESS.rowType, VENDOR3_ADDRESS1_ADDRESS_ID,
                VENDOR3_ADDRESS1_SCIQUEST_ID, VENDOR3_ADDRESS1_NAME, JaggaerAddressType.FULFILLMENT.jaggaerAddressType,
                VENDOR3_ADDRESS1_PRIMARY_TYPE, VENDOR3_ACTIVE, COUNTRY_US, VENDOR3_ADDRESS1_STREET_LINE_1,
                VENDOR3_ADDRESS1_STREET_LINE_2, VENDOR3_ADDRESS1_STREET_LINE_3, VENDOR3_ADDRESS1_CITY, STATE_NY,
                VENDOR3_ADDRESS1_ZIP, VENDOR3_ADDRESS1_PHONE, VENDOR3_ADDRESS1_TOLL_FREE_NUMBER, VENDOR3_ADDRESS1_FAX,
                notes };
        return record;
    }
    
    @Test
    void testBuildPartyHeader() {
        String expectedPartyHeader = "PARTY,OverrideDupError,ERPNumber,SciQuestID,ContractPartyName,DoingBusinessAs,OtherNames,CountryOfOrigin,Active,ContractPartyType,Primary,LegalStructure,TaxIDType,TaxIdentificationNumber,VATRegistrationNumber,WebsiteURL";
        String actualPartyHeader = printArrayCommaDelim(jaggaerGenerateContractPartyCsvServiceImpl.buildPartyHeader());
        assertEquals(expectedPartyHeader, actualPartyHeader);
    }

    @Test
    void testBuildAddressHeader() {
        String expectedAddressHeader = "ADDRESS,AddressID,SciQuestID,Name,AddressType,PrimaryType,Active,Country,StreetLine1,StreetLine2,StreetLine3,City/Town,State/Province,PostalCode,Phone,TollFreeNumber,Fax,Notes";
        String actualAddressHeader = printArrayCommaDelim(jaggaerGenerateContractPartyCsvServiceImpl.buildAddressHeader());
        assertEquals(expectedAddressHeader, actualAddressHeader);
    }

    @Test
    void testBuildContactHeader() {
        String expectedContactHeader = "CONTACT,ContactID,SciQuestID,Name,FirstName,LastName,ContactType,PrimaryType,Active,Title,Email,Phone,MobilePhone,TollFreeNumber,Fax,Notes";
        String actualContactHeader = printArrayCommaDelim(jaggaerGenerateContractPartyCsvServiceImpl.buildContactHeader());
        assertEquals(expectedContactHeader, actualContactHeader);
    }

    private String printArrayCommaDelim(String[] data) {
        return StringUtils.join(data, KFSConstants.COMMA);
    }

}
