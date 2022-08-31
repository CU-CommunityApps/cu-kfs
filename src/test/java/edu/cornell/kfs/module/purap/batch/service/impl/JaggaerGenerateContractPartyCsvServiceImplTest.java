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
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvServiceImplTest {
    
    private JaggaerGenerateContractPartyCsvServiceImpl jaggaerGenerateContractPartyCsvServiceImpl;
    
    private static final String PROCESS_DATE = "2022-08-16";
    
    private static final String VENDOR1_VENDOR_NUMBER = "12345-0";
    private static final String VENDOR1_VENDOR_NAME = "Acme Testing Company";
    private static final String VENDOR1_VENDOR_COUNTRY = "US";
    private static final String VENDOR1_VENDOR_URL = "http://www.google.com";
    
    private static final String VENDOR1_ADDRESS1_ID = "12342";
    private static final String VENDOR1_ADDRESS1_LN1 = "123 Main Street";
    private static final String VENDOR1_ADDRESS1_LN2 = "Apartment 666";
    private static final String VENDOR1_ADDRESS1_CITY = "Freeville";
    private static final String VENDOR1_ADDRESS1_STATE = "NY";
    private static final String VENDOR1_ADDRESS1_ZIP = "13068";
    
    private static final String VENDOR1_ADDRESS2_ID = "98765";
    private static final String VENDOR1_ADDRESS2_LN1 = "45 Palm Street";
    private static final String VENDOR1_ADDRESS2_LN2 = "Apartment 1F";
    private static final String VENDOR1_ADDRESS2_CITY = "Ithaca";
    private static final String VENDOR1_ADDRESS2_STATE = "NY";
    private static final String VENDOR1_ADDRESS2_ZIP = "14850";
    
    private static final String VENDOR2_VENDOR_NUMBER = "98765-0";
    private static final String VENDOR2_VENDOR_NAME = "Jane Doe Paint Services";
    private static final String VENDOR2_VENDOR_COUNTRY = "US";
    private static final String VENDOR2_VENDOR_URL = "http://www.yahoo.com";
    private static final String VENDOR2_TAX_ID  = "65479";

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
    @MethodSource("jaggaerContractPartyUploadDtoToStringParameters")
    void testJaggaerContractPartyUploadDtoToString(JaggaerContractPartyUploadDto contractPartyDto, String searchString) {
        String actualToString = contractPartyDto.toString();
        assertTrue(StringUtils.contains(actualToString, searchString));
    }
    
    static Stream<Arguments> jaggaerContractPartyUploadDtoToStringParameters() {
        return Stream.of(
                Arguments.of(buildVendor2WithTaxIdDto(), "taxIdentificationNumber=restricted tax id number"),
                Arguments.of(buildVendor1Dto(),  "taxIdentificationNumber=,")
                );
    }
    
    @ParameterizedTest
    @MethodSource("builderVendorCSVRowArrayParameters")
    void testBuilderVendorCSVRowArray(JaggaerContractPartyUploadDto vendorDto, String[] expectedCsvData) {

        String[] actualCsvData = jaggaerGenerateContractPartyCsvServiceImpl.builderVendorCSVRowArray(vendorDto);
        assertEquals(16, expectedCsvData.length);
        assertEquals(16, actualCsvData.length);
        for (int i=0; i<expectedCsvData.length; i++) {
            String actualDatum = actualCsvData[i];
            String expectedDataum = expectedCsvData[i];
            assertEquals(expectedDataum, actualDatum);
        }
        
    }
    
    static Stream<Arguments> builderVendorCSVRowArrayParameters() {
        return Stream.of(
                Arguments.of(buildJaggaerContractPartyUploadDto("ABCD"), buildJaggaerContractPartyUploadDtoCsvData("ABCD")),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.EMPTY), buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(null), buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.SPACE), buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY))
                );
    }
    
    private static JaggaerContractPartyUploadDto buildJaggaerContractPartyUploadDto(String sciQuestId) {
        JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
        dto.setOverrideDupError(false);
        dto.setERPNumber("123456-0");
        dto.setSciQuestID(sciQuestId);
        dto.setContractPartyName("Acme Inc");
        dto.setDoingBusinessAs("Acme");
        dto.setOtherNames("other name");
        dto.setCountryOfOrigin("US");
        dto.setActive("active");
        dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
        dto.setPrimary("primary");
        dto.setLegalStructure(JaggaerLegalStructure.C_CORPORATION);
        dto.setTaxIDType("foo type");
        dto.setTaxIdentificationNumber("369852");
        dto.setVATRegistrationNumber("vat number");
        dto.setWebsiteURL("www.google.com");
        return dto;
    }
    
    private static String[] buildJaggaerContractPartyUploadDtoCsvData(String sciQuestId) {
        String[] record = { JaggaerContractPartyUploadRowType.PARTY.rowType, "false", "123456-0", sciQuestId,  "Acme Inc",
                "Acme", "other name", "US", "active", JaggaerContractPartyType.SUPPLIER.partyTypeName, "primary",
                JaggaerLegalStructure.C_CORPORATION.jaggaerLegalStructureName, "foo type", "369852", "vat number",
                "www.google.com" };
        return record;
    }
    
    @ParameterizedTest
    @MethodSource("builderAddressCSVRowArrayParameters")
    void testBuilderAddressCSVRowArray(JaggaerContractAddressUploadDto addressDto, String[] expectedCsvData) {
        String[] actualCsvData = jaggaerGenerateContractPartyCsvServiceImpl.builderAddressCSVRowArray(addressDto);
        assertEquals(18, actualCsvData.length);
        assertEquals(18, expectedCsvData.length);
        for (int i=0; i<expectedCsvData.length; i++) {
            String actualDatum = actualCsvData[i];
            String expectedDataum = expectedCsvData[i];
            assertEquals(expectedDataum, actualDatum);
        }
    }
    
    static Stream<Arguments> builderAddressCSVRowArrayParameters() {
        return Stream.of(
                Arguments.of(buildJaggaerContractAddressUploadDto("cool note"), buildJaggaerContractAddressUploadDtoData("cool note")),
                Arguments.of(buildJaggaerContractAddressUploadDto(StringUtils.EMPTY), buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractAddressUploadDto(null), buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractAddressUploadDto(StringUtils.SPACE), buildJaggaerContractAddressUploadDtoData(StringUtils.EMPTY))
                );
    }
    
    static JaggaerContractAddressUploadDto buildJaggaerContractAddressUploadDto(String notes) {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
        dto.setAddressID("addressId");
        dto.setSciQuestID("ABCD");
        dto.setName("name");
        dto.setAddressType(JaggaerAddressType.FULFILLMENT);
        dto.setPrimaryType("primary type");
        dto.setActive("active");
        dto.setCountry("US");
        dto.setStreetLine1("street line 1");
        dto.setStreetLine2("street line 2");
        dto.setStreetLine3("street line 3");
        dto.setCity("city");
        dto.setState("NY");
        dto.setPostalCode("14850");
        dto.setPhone("6072559900");
        dto.setTollFreeNumber("18002809900");
        dto.setFax("fax");
        dto.setNotes(notes);
        return dto;
    }
    
    static String[] buildJaggaerContractAddressUploadDtoData(String notes) {
        String[] record = { JaggaerContractPartyUploadRowType.ADDRESS.rowType,
                "addressId",
                "ABCD",
                "name",
                JaggaerAddressType.FULFILLMENT.jaggaerAddressType,
                "primary type",
                "active",
                "US",
                "street line 1",
                "street line 2",
                "street line 3",
                "city",
                "NY",
                "14850",
                "6072559900",
                "18002809900",
                "fax",
                notes
                };
        return record;
    }

}
