package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
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
    private Date csvTestDate;
    
    private static final String PROCESS_DATE = "2022-08-16";
    private static final String BASE_CSV_DIRECTORY = "src/test/resources/edu/cornell/kfs/module/purap/batch/service/impl/fixture/";
    //private static final String EXPECTED_JAGGAER_FILENAME = "expectedJaggaerUpload.csv";
    //private static final String ACTUAL_JAGGAER_FILENAME = "testJaggaerUploadFile.csv";;

    @BeforeEach
    void setUp() throws Exception {
        jaggaerGenerateContractPartyCsvServiceImpl = new JaggaerGenerateContractPartyCsvServiceImpl();
        jaggaerGenerateContractPartyCsvServiceImpl.setJaggaerUploadCreationDirectory(BASE_CSV_DIRECTORY);
        
        Calendar cal = Calendar.getInstance();
        cal.set(1996, 10, 30, 23, 31, 0);
        csvTestDate = cal.getTime();
        jaggaerGenerateContractPartyCsvServiceImpl.setDateTimeService(buildMockDateTimeService());
        
    }
    
    private DateTimeService buildMockDateTimeService() {
        DateTimeService service = Mockito.mock(DateTimeService.class);
        Mockito.when(service.getCurrentDate()).thenReturn(csvTestDate);
        return service;
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
        dtos.add(VendorFixture.BASIC_VENDOR.toJaggaerContractPartyUploadDto());
        dtos.add(VendorFixture.BASIC_VENDOR_WITH_TAX_ID.toJaggaerContractPartyUploadDto());
        return dtos;
    }
    
    private List<JaggaerContractAddressUploadDto> buildMockAddressDtos() {
        List<JaggaerContractAddressUploadDto> dtos = new ArrayList<>();
        dtos.add(VendorAddressFixture.BASIC_VENDOR_ADDRESS_1.toJaggaerContractAddressUploadDto());
        dtos.add(VendorAddressFixture.BASIC_VENDOR_ADDRESS_2.toJaggaerContractAddressUploadDto());
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
        assertEquals(VendorFixture.BASIC_VENDOR.toJaggaerContractPartyUploadDto(), actualFirstElement, "The first element should be vendor 1");
        
        JaggaerContractAddressUploadDto actualSecondElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(1);
        JaggaerContractAddressUploadDto expectedAddress1 = VendorAddressFixture.BASIC_VENDOR_ADDRESS_1.toJaggaerContractAddressUploadDto();
        expectedAddress1.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress1, actualSecondElement, "The second element should be vendor 1 address 1");
        
        JaggaerContractAddressUploadDto actualThirdElement = (JaggaerContractAddressUploadDto) jaggaerUploadDtos.get(2);
        JaggaerContractAddressUploadDto expectedAddress2 = VendorAddressFixture.BASIC_VENDOR_ADDRESS_2.toJaggaerContractAddressUploadDto();
        expectedAddress2.setName(actualFirstElement.getContractPartyName());
        assertEquals(expectedAddress2, actualThirdElement, "The third element should be vendor 1 address 2");
        
        JaggaerContractPartyUploadDto actualFourthElement = (JaggaerContractPartyUploadDto) jaggaerUploadDtos.get(3);
        assertEquals(VendorFixture.BASIC_VENDOR_WITH_TAX_ID.toJaggaerContractPartyUploadDto(), actualFourthElement, "THe fourth element should be vendor 2");
    }
    
    @ParameterizedTest
    @MethodSource("buildJaggaerContractPartyUploadDtoToStringParameters")
    void testJaggaerContractPartyUploadDtoToString(JaggaerContractPartyUploadDto contractPartyDto, String searchString) {
        String actualToString = contractPartyDto.toString();
        assertTrue(StringUtils.contains(actualToString, searchString));
    }
    
    static Stream<Arguments> buildJaggaerContractPartyUploadDtoToStringParameters() {
        return Stream.of(
                Arguments.of(VendorFixture.BASIC_VENDOR_WITH_TAX_ID.toJaggaerContractPartyUploadDto(), "taxIdentificationNumber=restricted tax id number"),
                Arguments.of(VendorFixture.BASIC_VENDOR.toJaggaerContractPartyUploadDto(),  "taxIdentificationNumber=,")
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
        JaggaerContractPartyUploadDto dto = VendorFixture.FULL_VENOOR.toJaggaerContractPartyUploadDto();
        dto.setSciQuestID(sciQuestId);
        return dto;
    }

    private static String[] buildJaggaerContractPartyUploadDtoCsvData(String sciQuestId) {
        VendorFixture vendor = VendorFixture.FULL_VENOOR;
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
        JaggaerContractAddressUploadDto dto = VendorAddressFixture.FULL_VENDOR_FULL_ADDRSS.toJaggaerContractAddressUploadDto();
        dto.setNotes(notes);
        return dto;
    }

    static String[] buildJaggaerContractAddressUploadDtoData(String notes) {
        VendorAddressFixture address = VendorAddressFixture.FULL_VENDOR_FULL_ADDRSS;
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
    
    @Test
    void testGenerateCsvFile() throws IOException {
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = new ArrayList<>();
        jaggaerUploadDtos.add(VendorFixture.FULL_VENOOR_FOR_CSV.toJaggaerContractPartyUploadDto());
        jaggaerUploadDtos.add(VendorAddressFixture.FULL_VENDOR_FULL_ADDRSS_FOR_CSV.toJaggaerContractAddressUploadDto());
        
        jaggaerGenerateContractPartyCsvServiceImpl.generateCsvFile(jaggaerUploadDtos, JaggaerContractUploadProcessingMode.PO);
        
        
        File expectedJaggaerFile = new File(BASE_CSV_DIRECTORY + "expectedJaggaerUpload.csv");
        File actualJaggaerFile = new File(BASE_CSV_DIRECTORY + "JaggaerUpload_found_by_PO_search_19961130_233100.csv");
        
        FileUtils.readFileToString(expectedJaggaerFile, StandardCharsets.UTF_8);
        assertEquals(FileUtils.readFileToString(expectedJaggaerFile, StandardCharsets.UTF_8), 
                FileUtils.readFileToString(actualJaggaerFile, StandardCharsets.UTF_8));
        
        FileUtils.forceDelete(actualJaggaerFile);
        
    }

}
