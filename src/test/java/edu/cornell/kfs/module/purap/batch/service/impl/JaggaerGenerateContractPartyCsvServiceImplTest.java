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

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressHeader;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerPartyHeader;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.impl.fixcture.VendorAddressFixture;
import edu.cornell.kfs.module.purap.batch.service.impl.fixcture.VendorFixture;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvServiceImplTest {
    private JaggaerGenerateContractPartyCsvServiceImpl jaggaerGenerateContractPartyCsvServiceImpl;
    
    private static final String PROCESS_DATE = "2022-08-16";

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
        dtos.add(VendorFixture.VENDOR1.toJaggaerContractPartyUploadDto());
        dtos.add(VendorFixture.VENDOR2.toJaggaerContractPartyUploadDto());
        return dtos;
    }
    
    private List<JaggaerContractAddressUploadDto> buildMockAddressDtos() {
        List<JaggaerContractAddressUploadDto> dtos = new ArrayList<>();
        dtos.add(VendorAddressFixture.VENDOR1_ADDRESS1.toJaggaerContractAddressUploadDto());
        dtos.add(VendorAddressFixture.VENDOR1_ADDRESS2.toJaggaerContractAddressUploadDto());
        dtos.add(buildNoVendorAddressDto());
        return dtos;
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
        assertEquals(VendorFixture.VENDOR1.toJaggaerContractPartyUploadDto(), actualFirstElement, "The first element should be vendor 1");
        
        JaggaerContractAddressUploadDto actualSecondElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(1);
        JaggaerContractAddressUploadDto expectedAddress1 = VendorAddressFixture.VENDOR1_ADDRESS1.toJaggaerContractAddressUploadDto();
        expectedAddress1.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress1, actualSecondElement, "The second element should be vendor 1 address 1");
        
        JaggaerContractAddressUploadDto actualThirdElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(2);
        JaggaerContractAddressUploadDto expectedAddress2 = VendorAddressFixture.VENDOR1_ADDRESS2.toJaggaerContractAddressUploadDto();
        expectedAddress2.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress2, actualThirdElement, "The third element should be vendor 1 address 2");
        
        JaggaerContractPartyUploadDto actualFourthElement = (JaggaerContractPartyUploadDto) jaggaerUploadDtos.get(3);
        assertEquals(VendorFixture.VENDOR2.toJaggaerContractPartyUploadDto(), actualFourthElement, "THe fourth element should be vendor 2");
    }
    
    @ParameterizedTest
    @MethodSource("buildJaggaerContractPartyUploadDtoToStringParameters")
    void testJaggaerContractPartyUploadDtoToString(JaggaerContractPartyUploadDto contractPartyDto, String searchString) {
        String actualToString = contractPartyDto.toString();
        assertTrue(StringUtils.contains(actualToString, searchString));
    }
    
    static Stream<Arguments> buildJaggaerContractPartyUploadDtoToStringParameters() {
        return Stream.of(
                Arguments.of(VendorFixture.VENDOR2.toJaggaerContractPartyUploadDto(), "taxIdentificationNumber=restricted tax id number"),
                Arguments.of(VendorFixture.VENDOR1.toJaggaerContractPartyUploadDto(),  "taxIdentificationNumber=,")
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
                Arguments.of(buildJaggaerContractPartyUploadDto("sciquestID"),
                        buildJaggaerContractPartyUploadDtoCsvData("sciquestID")),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.EMPTY),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(null),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)),
                Arguments.of(buildJaggaerContractPartyUploadDto(StringUtils.SPACE),
                        buildJaggaerContractPartyUploadDtoCsvData(StringUtils.EMPTY)));
    }

    private static JaggaerContractPartyUploadDto buildJaggaerContractPartyUploadDto(String sciQuestId) {
        JaggaerContractPartyUploadDto dto = VendorFixture.VENDOR3.toJaggaerContractPartyUploadDto();
        dto.setSciQuestID(sciQuestId);
        return dto;
    }

    private static String[] buildJaggaerContractPartyUploadDtoCsvData(String sciQuestId) {
        VendorFixture vendor = VendorFixture.VENDOR3;
        String[] record = { vendor.rowType.rowType, vendor.overrideDupError, vendor.ERPNumber, sciQuestId,
                vendor.contractPartyName, vendor.doingBusinessAs, vendor.otherNames, vendor.countryOfOrigin, vendor.active,
                vendor.contractPartyType.partyTypeName, vendor.primary,
                vendor.legalStructure.jaggaerLegalStructureName, vendor.taxIDType, vendor.taxIdentificationNumber,
                vendor.VATRegistrationNumber, vendor.websiteURL };
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
        JaggaerContractAddressUploadDto dto = VendorAddressFixture.VENDOR3_ADDRESS1.toJaggaerContractAddressUploadDto();
        dto.setNotes(notes);
        return dto;
    }

    static String[] buildJaggaerContractAddressUploadDtoData(String notes) {
        VendorAddressFixture address = VendorAddressFixture.VENDOR3_ADDRESS1;
        String[] record = { address.rowType.name(), address.addressID,
                address.sciQuestID, address.name, address.addressType.jaggaerAddressType, address.primaryType, 
                address.active, address.country, address.streetLine1,
                address.streetLine2, address.streetLine3, address.city, address.state,
                address.postalCode, address.phone, address.tollFreeNumber, address.fax,
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
