package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;

import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

@Execution(ExecutionMode.SAME_THREAD)
public class SupplierSyncMessageJaggaerTest extends SupplierSyncMessageTestBase {
    private static final String TEXT_OF_ERROR_MESSAGE = "Text of error message";
    private static final String REQUEST_FILE_EXAMPLE = "SupplierSyncMessage-RequestMessage-JaggaerTestData.xml";
    private static final String RESPONSE_FILE_EXAMPLE = "SupplierSyncMessage-ResponseMessage-JaggaerTestData.xml";
    
    @ParameterizedTest
    @MethodSource("testSupplierSyncMessageArguments")
    void testSupplierSyncMessage(String fileName, boolean requestTest) throws JAXBException, IOException, SAXException {
        File expectedRequestXmlFile = new File(INPUT_FILE_PATH + fileName);

        SupplierSyncMessage supplierSyncMessage = buildSupplierSyncMessageBase();
        supplierSyncMessage.setHeader(buildHeader());
        
        if (requestTest) {
            SupplierRequestMessage srm = new SupplierRequestMessage();
            srm.getSuppliers().add(buildSupplier());
            supplierSyncMessage.getSupplierSyncMessageItems().add(srm);
        } else {
            supplierSyncMessage.getSupplierSyncMessageItems().add(buildSupplierResponseMessage());
        }

        File actualXmlFile = marshalService.marshalObjectToXMLFragment(supplierSyncMessage, OUTPUT_FILE_PATH + "test.xml");
        CuXMLUnitTestUtils.compareXML(expectedRequestXmlFile, actualXmlFile);
        validateFileContainsExpectedHeader(actualXmlFile);
    }
    
    static Stream<Arguments> testSupplierSyncMessageArguments() {
        return Stream.of(Arguments.of(REQUEST_FILE_EXAMPLE, true), 
                Arguments.of(RESPONSE_FILE_EXAMPLE, false));
    }
    
    private Header buildHeader() {
        Header header = new Header();
        header.setMessageId("f54311e2-364b-4cf4-8942-016e5ad308d9");
        header.setTimestamp("2023-10-30T09:06:42.209-04:00");
        Authentication auth = new Authentication();
        auth.setIdentity("OrgID");
        auth.setSharedSecret("password");
        header.setAuthentication(auth);
        return header;
    }
    
    private SupplierRequestMessage buildSupplierRequestMessage() {
        SupplierRequestMessage message = new SupplierRequestMessage();
        message.getSuppliers().add(buildSupplier());
        return message;
    }
    
    private Supplier buildSupplier() {
        Supplier supplier = new Supplier();
        supplier.setErpNumber(JaggaerBuilderTest.buildErpNumber("als12345"));
        supplier.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511507"));
        supplier.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber("ALS0001"));
        supplier.setName(JaggaerBuilderTest.buildName("Affordable Lab Supplies"));
        supplier.setDoingBusinessAs(JaggaerBuilderTest.buildJaggaerBasicValue("ALS"));
        supplier.setOtherNames(JaggaerBuilderTest.buildJaggaerBasicValue("AffLabs"));
        supplier.setJaSupplierId("9142");
        supplier.setCountryOfOrigin(JaggaerBuilderTest.buildJaggaerBasicValue("US"));
        supplier.setParentSupplier(buildParentSupplier());
        supplier.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.NO));
        supplier.setBusinessUnitVendorNumberList(buildBusinessUnitVendorNumberList());
        supplier.setWebSiteURL(JaggaerBuilderTest.buildJaggaerBasicValue("www.affordablelabsupplies.com"));
        supplier.setDuns(JaggaerBuilderTest.buildJaggaerBasicValue("123456789"));
        supplier.setLegalStructure(JaggaerBuilderTest.buildJaggaerBasicValue("LimitedLiabilityCompany_Partnership"));
        supplier.setTaxIdentificationNumber(JaggaerBuilderTest.buildJaggaerBasicValue("123456789"));
        supplier.setVatIdentificationNumber(JaggaerBuilderTest.buildJaggaerBasicValue("123456789"));
        supplier.setExemptFromBackupWithholding(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.YES));
        supplier.setNumberOfEmployees(JaggaerBuilderTest.buildJaggaerBasicValue("400"));
        supplier.setYearEstablished(JaggaerBuilderTest.buildJaggaerBasicValue("1983-06-04"));
        supplier.setAnnualSalesList(buildAnnualSalesList());
        supplier.setServiceAreaList(buildServiceAreaList());
        supplier.setSupplierTaxRepresentativeId(JaggaerBuilderTest.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegCourt(JaggaerBuilderTest.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegNumber(JaggaerBuilderTest.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegSeat(JaggaerBuilderTest.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierShareholders(JaggaerBuilderTest.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setVatExempt(JaggaerBuilderTest.buildJaggaerBasicValue("false", JaggaerConstants.NO));
        supplier.setCommodityCodeList(buildCommodityCodeList());
        supplier.setShoppingCommodityCode(JaggaerBuilderTest.buildJaggaerBasicValue("SC01"));
        supplier.setAustinTetra(JaggaerBuilderTest.buildJaggaerBasicValue("Austin-Tetra Code"));
        supplier.setSic(JaggaerBuilderTest.buildJaggaerBasicValue("SIC Code"));
        supplier.setNaicsCodeList(buildNaicsCodes());
        supplier.setSupportedCurrencyList(buildCurrencyList("USD", "CAD", "GBP", "EUR", "CHF", "MXN"));
        supplier.setEnabledCurrencyList(buildCurrencyList("USD", "MXN"));
        supplier.setSupplierKeywords(JaggaerBuilderTest.buildJaggaerBasicValue("Paper Products, Filters, Drinking Straws"));
        supplier.setAddressList(buildAddressList());
        supplier.setPrimaryAddressList(buildPrimaryAddressList());
        supplier.setContactList(buildContactList());
        supplier.setPrimaryContactList(buildPrimaryContactList());
        supplier.setClassificationList(buildClassificationList());
        supplier.setDiversityClassificationList(buildDiversityClassificationList());
        supplier.setInsuranceInformationList(buildInsuranceInformationList());
        supplier.setLocationList(buildLocationList());
        supplier.setRestrictFulfillmentLocationsByBusinessUnit(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        supplier.setCustomElementList(buildSupplierCustomElementList());
        supplier.setAccountsPayableList(buildAccountsPayableList());
        supplier.setTaxInformationList(buildTaxInformationList());
        return supplier;
    }
    
    private ParentSupplier buildParentSupplier() {
        ParentSupplier parent = new ParentSupplier();
        parent.setErpNumber(JaggaerBuilderTest.buildErpNumber("als12343"));
        parent.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511504"));
        return parent;
    }
    
    private BusinessUnitVendorNumberList buildBusinessUnitVendorNumberList() {
        BusinessUnitVendorNumberList list = new BusinessUnitVendorNumberList();
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("NC1", "NCCBO0001"));
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("VA1", "VACBO0001"));
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("NC2", "NCCBO0002"));
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("VA2", "VACBO0002"));
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("NC3", "NCCBO0003"));
        list.getBusinessUnitVendorNumbers().add(JaggaerBuilderTest.buildBusinessUnitVendorNumber("VA3", "VACBO0003"));
        return list;
    }
    
    private AnnualSalesList buildAnnualSalesList() {
        AnnualSalesList list = new AnnualSalesList();
        list.getAnnualSalesItems().add(buildAnnualSales("60,000.00", "2024", "USD"));
        list.getAnnualSalesItems().add(buildAnnualSales("50,000.00", "2024", "USD"));
        list.getAnnualSalesItems().add(buildAnnualSales("40,000.00", "2024", "USD"));
        return list;
    }
    
    private AnnualSalesItem buildAnnualSales(String amountString, String salesYear, String currencyCode) {
        AnnualSalesItem sale = new AnnualSalesItem();
        sale.setAnnualSalesAmount(JaggaerBuilderTest.buildAmount(amountString));
        sale.setAnnualSalesYear(JaggaerBuilderTest.buildJaggaerBasicValue(salesYear));
        sale.setIsoCurrencyCode(JaggaerBuilderTest.buildIsoCurrencyCode(currencyCode));
        return sale;
    }
    
    private ServiceAreaList buildServiceAreaList() {
        ServiceAreaList serviceAreaList = new ServiceAreaList();
        ServiceArea serviceArea = new ServiceArea();
        serviceArea.setServiceAreaInternalName(JaggaerBuilderTest.buildJaggaerBasicValue("US"));
        
        StateServiceAreaList stateServiceAreaList = new StateServiceAreaList();
        stateServiceAreaList.getStateServiceAreaInternalNames().add(JaggaerBuilderTest.buildStateServiceAreaInternalName("US-NC"));
        stateServiceAreaList.getStateServiceAreaInternalNames().add(JaggaerBuilderTest.buildStateServiceAreaInternalName("US-CA"));
        
        serviceArea.getStateServiceAreaList().add(stateServiceAreaList);
        serviceAreaList.getServiceAreas().add(serviceArea);
        return serviceAreaList;
    }
    
    private CommodityCodeList buildCommodityCodeList() {
        CommodityCodeList list = new CommodityCodeList();
        list.getCommodityCodes().add(JaggaerBuilderTest.buildJaggaerBasicValue("OM01"));
        list.getCommodityCodes().add(JaggaerBuilderTest.buildJaggaerBasicValue("OM02"));
        return list;
    }
    
    private NaicsCodeList buildNaicsCodes() {
        NaicsCodeList naicsCodes = new NaicsCodeList();
        naicsCodes.getNaicsCodeListItems().add(JaggaerBuilderTest.buildPrimaryNaicsItem("424120"));
        
        SecondaryNaicsList secondaryList = new SecondaryNaicsList();
        secondaryList.getSecondaryNaicsItems().add(JaggaerBuilderTest.buildSecondaryNaicsItem("424121"));
        secondaryList.getSecondaryNaicsItems().add(JaggaerBuilderTest.buildSecondaryNaicsItem("524120"));
        secondaryList.getSecondaryNaicsItems().add(JaggaerBuilderTest.buildSecondaryNaicsItem("524121"));
        naicsCodes.getNaicsCodeListItems().add(secondaryList);
        
        return naicsCodes;
    }
    
    private CurrencyList buildCurrencyList(String... currencies) {
        CurrencyList list = new CurrencyList();
        list.setIsChanged(JaggaerConstants.NO);
        for (String currency : currencies) {
            list.getIsoCurrencyCodes().add(JaggaerBuilderTest.buildIsoCurrencyCode(currency, JaggaerConstants.NO));
        }
        return list;
    }
    
    private AddressList buildAddressList() {
        AddressList list = new AddressList();
        list.getAddresses().add(buildRemitToAddress());
        list.getAddresses().add(buildFulfillmentAddress());
        return list;
    }
    
    private Address buildRemitToAddress() {
        Address remit = new Address();
        remit.setType("remitto");
        remit.setErpNumber(JaggaerBuilderTest.buildErpNumber("add123"));
        remit.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u15174772"));
        remit.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        remit.setName(JaggaerBuilderTest.buildName("California Regional Office"));
        remit.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.NO));
        remit.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("accountspayable@affordablelabsupplies.com"));  
        remit.setAddressLine1(JaggaerBuilderTest.buildAddressLine("309 Pattern Ave."));
        remit.setAddressLine2(new AddressLine());
        remit.setAddressLine3(new AddressLine());
        remit.setCity(JaggaerBuilderTest.buildCity("San Diego"));
        remit.setState(JaggaerBuilderTest.buildState("CA"));
        remit.setPostalCode(JaggaerBuilderTest.buildPostalCode("92093"));
        remit.setIsoCountryCode(JaggaerBuilderTest.buildIsoCountryCode("USA"));
        remit.setPhone(buildPhone("1", "619", "1234567", null));
        remit.setTollFreePhone(buildTollFreePhone("1", "800", "8901234", null));
        remit.setFax(buildFax("1", "619", "5678901", null));
        remit.setNotes(new JaggaerBasicValue());
        remit.setAssignedBusinessUnitsList(buildAssignedBusinessUnitsList("California Regional Office", "West Coast Distribution Center"));
        return remit;
    }
    
    private PrefPurchaseOrderDeliveryMethod buildPrefPurchaseOrderDeliveryMethod(String emailAddress) {
        PrefPurchaseOrderDeliveryMethod method = new PrefPurchaseOrderDeliveryMethod();
        method.setType("Email");
        method.getDeliveryMethodTypes().add(JaggaerBuilderTest.buildEmail(emailAddress));
        return method;
    }
    
    private Phone buildPhone(String countryCode, String areaCode, String number, String extension) {
        Phone phone = new Phone();
        phone.setTelephoneNumber(JaggaerBuilderTest.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private TollFreePhone buildTollFreePhone(String countryCode, String areaCode, String number, String extension) {
        TollFreePhone phone = new TollFreePhone();
        phone.setTelephoneNumber(JaggaerBuilderTest.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private Fax buildFax(String countryCode, String areaCode, String number, String extension) {
        Fax fax = new Fax();
        fax.setTelephoneNumber(JaggaerBuilderTest.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return fax;
    }
    
    private AssignedBusinessUnitsList buildAssignedBusinessUnitsList(String... names) {
        AssignedBusinessUnitsList list = new AssignedBusinessUnitsList();
        for (String name : names) {
            list.getBusinessUnitInternalNames().add(JaggaerBuilderTest.buildBusinessUnitInternalName(name, null));
        }
        return list;
    }
    
    private Address buildFulfillmentAddress() {
        Address fulfill = new Address();
        fulfill.setType("fulfillment");
        fulfill.setErpNumber(JaggaerBuilderTest.buildErpNumber("add123"));
        fulfill.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511560"));
        fulfill.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        fulfill.setName(JaggaerBuilderTest.buildName("West Coast Distribution Center"));
        fulfill.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.YES));
        fulfill.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("fulfillment@affordablelabsupplies.com"));  
        fulfill.setAddressLine1(JaggaerBuilderTest.buildAddressLine("123 Park Ave."));
        fulfill.setAddressLine2(new AddressLine());
        fulfill.setAddressLine3(new AddressLine());
        fulfill.setCity(JaggaerBuilderTest.buildCity("San Diego"));
        fulfill.setState(JaggaerBuilderTest.buildState("CA"));
        fulfill.setPostalCode(JaggaerBuilderTest.buildPostalCode("92093"));
        fulfill.setIsoCountryCode(JaggaerBuilderTest.buildIsoCountryCode("USA"));
        fulfill.setPhone(buildPhone("1", "619", "2345678", null));
        fulfill.setTollFreePhone(buildTollFreePhone("1", "800", "9012345", null));
        fulfill.setFax(buildFax("1", "619", "6789012", null));
        fulfill.setNotes(new JaggaerBasicValue());
        fulfill.setAssignedBusinessUnitsList(buildAssignedBusinessUnitsList("California Regional Office", "West Coast Distribution Center"));
        return fulfill;
    }
    
    private PrimaryAddressList buildPrimaryAddressList() {
        PrimaryAddressList primaryAddredssList = new PrimaryAddressList();
        primaryAddredssList.getAssociatedAddresses().add(buildAssociatedAddress("remitto", "ADDR1"));
        primaryAddredssList.getAssociatedAddresses().add(buildAssociatedAddress("fulfillment", "ADDR2"));
        return primaryAddredssList;
    }
    
    private AssociatedAddress buildAssociatedAddress(String addressType, String erpNumber) {
        AssociatedAddress address = new AssociatedAddress();
        address.setType(addressType);
        
        AddressRef ref = new AddressRef();
        ref.setErpNumber(JaggaerBuilderTest.buildErpNumber(erpNumber));
        address.setAddressRef(ref);
        
        return address;
    }
    
    private ContactList buildContactList() {
        ContactList contactList = new ContactList();
        contactList.getContacts().add(buildRemitContact());
        contactList.getContacts().add(buildFulfillmentContact());
        contactList.getContacts().add(buildTechnicalContact());
        return contactList;
    }
    
    private Contact buildRemitContact() {
        Contact remit = new Contact();
        remit.setType("remitto");
        remit.setErpNumber(JaggaerBuilderTest.buildErpNumber("CONTACT1"));
        remit.setOldERPNumber("CONTACT1");
        remit.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("SQ_1237"));
        remit.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber("3pID1"));
        remit.setName(JaggaerBuilderTest.buildName("Primary ALS RemitTo and Fulfillment Contact"));
        remit.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        remit.setFirstName(JaggaerBuilderTest.buildJaggaerBasicValue("John"));
        remit.setLastName(JaggaerBuilderTest.buildJaggaerBasicValue("Smith"));
        remit.setTitle(JaggaerBuilderTest.buildJaggaerBasicValue("Procurement Manager"));
        remit.setEmail(JaggaerBuilderTest.buildEmail("jsmith@affordablelabsupplies.com"));
        remit.setPhone(buildPhone("1", "619", "3456789", "0123"));
        remit.setMobilePhone(buildMobilePhone("1", "619", "2468642", null));
        remit.setTollFreePhone(new TollFreePhone());
        remit.setFax(buildFax("1", "619", "9876543", null));
        remit.setNotes(new JaggaerBasicValue());
        remit.setAssociatedAddress(buildAssociatedAddress("remitto", "CONTACT1"));
        return remit;
    }
    
    private MobilePhone buildMobilePhone(String countryCode, String areaCode, String number, String extension) {
        MobilePhone phone = new MobilePhone();
        phone.setTelephoneNumber(JaggaerBuilderTest.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private Contact buildFulfillmentContact() {
        Contact fulfillment = new Contact();
        fulfillment.setType("fulfillment");
        fulfillment.setErpNumber(JaggaerBuilderTest.buildErpNumber("CONTACT1"));
        fulfillment.setOldERPNumber("CONTACT1");
        fulfillment.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("SQ_1236"));
        fulfillment.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber("3pID1"));
        fulfillment.setName(JaggaerBuilderTest.buildName("Primary ALS RemitTo and Fulfillment Contact"));
        fulfillment.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        fulfillment.setFirstName(JaggaerBuilderTest.buildJaggaerBasicValue("John"));
        fulfillment.setLastName(JaggaerBuilderTest.buildJaggaerBasicValue("Smith"));
        fulfillment.setTitle(JaggaerBuilderTest.buildJaggaerBasicValue("Procurement Manager"));
        fulfillment.setEmail(JaggaerBuilderTest.buildEmail("jsmith@affordablelabsupplies.com"));
        fulfillment.setPhone(buildPhone("1", "619", "3456789", "0123"));
        fulfillment.setMobilePhone(buildMobilePhone("1", "619", "2468642", null));
        fulfillment.setTollFreePhone(new TollFreePhone());
        fulfillment.setFax(buildFax("1", "619", "9876543", null));
        fulfillment.setNotes(new JaggaerBasicValue());
        fulfillment.setAssociatedAddress(buildAssociatedAddress("fulfillment", "CONTACT1"));
        return fulfillment;
    }
    
    private Contact buildTechnicalContact() {
        Contact tech = new Contact();
        tech.setType("technical");
        tech.setErpNumber(JaggaerBuilderTest.buildErpNumber("CONTACT2"));
        tech.setOldERPNumber("CONTACT2");
        tech.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("SQ_1240"));
        tech.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber("3pID2"));
        tech.setName(JaggaerBuilderTest.buildName("Primary ALS Technical Contact"));
        tech.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        tech.setFirstName(JaggaerBuilderTest.buildJaggaerBasicValue("Ann"));
        tech.setLastName(JaggaerBuilderTest.buildJaggaerBasicValue("Jones"));
        tech.setTitle(JaggaerBuilderTest.buildJaggaerBasicValue("Technical Consultant"));
        tech.setEmail(JaggaerBuilderTest.buildEmail("ajones@affordablelabsupplies.com"));
        tech.setPhone(buildPhone("1", "619", "3456789", "4567"));
        tech.setMobilePhone(buildMobilePhone("1", "619", "2268644", null));
        tech.setTollFreePhone(new TollFreePhone());
        tech.setFax(new Fax());
        tech.setNotes(new JaggaerBasicValue());
        tech.setAssociatedAddress(new AssociatedAddress());
        return tech;
    }
    
    private PrimaryContactList buildPrimaryContactList() {
        PrimaryContactList primaryContactList = new PrimaryContactList();
        primaryContactList.getAssociatedContacts().add(buildAssociatedContact("remitto", "CONTACT1"));
        primaryContactList.getAssociatedContacts().add(buildAssociatedContact("fulfillment", "CONTACT1"));
        return primaryContactList;
    }
    
    private AssociatedContact buildAssociatedContact(String contactType, String erpNumber) {
        AssociatedContact contact = new AssociatedContact();
        contact.setType(contactType);
        
        ContactRef ref = new ContactRef();
        ref.setErpNumber(JaggaerBuilderTest.buildErpNumber(erpNumber));
        contact.setContactRef(ref);
        
        return contact;
    }
    
    private ClassificationList buildClassificationList() {
        ClassificationList classificationList = new ClassificationList();
        
        Classification classification = new Classification();
        classification.setInternalName(JaggaerBuilderTest.buildJaggaerBasicValue("SupplierClassMinorityDisabled"));
        
        classificationList.getClassifications().add(classification);
        return classificationList;
    }
    
    private DiversityClassificationList buildDiversityClassificationList() {
        DiversityClassificationList classList = new DiversityClassificationList();
        
        DiversityClassification div = new DiversityClassification();
        div.setInternalName(JaggaerBuilderTest.buildJaggaerBasicValue("DOBE"));
        div.setDd214Certificate(new DD214Certificate());
        div.setDiversityCertificate(new DiversityCertificate());
        
        AdditionalDataList dataList = new AdditionalDataList();
        
        AdditionalDataItem datum = new AdditionalDataItem();
        datum.setName("ethnicity");
        datum.getContents().add("NATAM");
        
        dataList.getAdditionalDataItems().add(datum);
        div.setAdditionalDataList(dataList);
        
        classList.getDiversityClassifications().add(div);
        return classList;
    }
    
    private InsuranceInformationList buildInsuranceInformationList() {
        InsuranceInformationList infoList = new InsuranceInformationList();
        InsuranceInformation info = new InsuranceInformation();
        
        info.setType("AutomobileLiability");
        info.setPolicyNumber(JaggaerBuilderTest.buildJaggaerBasicValue("34576789"));
        info.setInsuranceLimit(JaggaerBuilderTest.buildJaggaerBasicValue("INSURANCE_LIMIT_RANGE1"));
        info.setExpirationDate(JaggaerBuilderTest.buildJaggaerBasicValue("2024-11-12"));
        info.setInsuranceProvider(JaggaerBuilderTest.buildJaggaerBasicValue("ACME Insurance Co."));
        info.setAgent(JaggaerBuilderTest.buildJaggaerBasicValue("Ned Ryerson"));
        
        InsuranceProviderPhone providerPhone = new InsuranceProviderPhone();
        providerPhone.setTelephoneNumber(JaggaerBuilderTest.buildTelephoneNumber("1", "704", "1234567", null));
        info.setInsuranceProviderPhone(providerPhone);
        
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setAttachmentList(buildAttachmentList());
        certificate.getAttachmentList().getAttachments().add(JaggaerBuilderTest.buildAttachment("42ins", "file", "Auto", "1180", null));
        info.setInsuranceCertificate(certificate);
        
        infoList.getInsuranceInformationDetails().add(info);
        return infoList;
    }
    
    private AttachmentList buildAttachmentList() {
        AttachmentList attachments = new AttachmentList();
        return attachments;
    }
    
    private LocationList buildLocationList() {
        LocationList locationList = new LocationList();
        locationList.getLocations().add(buildFulfillLocation1());
        locationList.getLocations().add(buildFulfillLocation2());
        locationList.getLocations().add(buildLocation2());
        return locationList;
    }
    
    private Location buildFulfillLocation1() {
        Location location = new Location();
        location.setSupportsOrderFulfillment(JaggaerConstants.YES);
        location.setErpNumber(JaggaerBuilderTest.buildErpNumber("loc123"));
        location.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511493"));
        location.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilderTest.buildName("Fulfillment Center 1"));
        location.setDescription(JaggaerBuilderTest.buildJaggaerBasicValue(null));
        location.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        location.setPrimary(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.YES));
        location.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("orders@affordablelabsupplies.com"));
        location.setLocationEffectiveDate(JaggaerBuilderTest.buildJaggaerBasicValue("2024-10-20"));
        location.setPaymentMethod(buildPaymentMethod());
        location.setShipping(buildShipping());
        location.setHandling(buildHandling());
        location.setTaxInfo(buildTaxInfo());
        location.setTermsAndCondition(buildTermsAndCondition());
        location.setOrderDistributionList(buildOrderDistributionList());
        location.setAssociatedAddressList(buildAssociatedAddressList());
        location.setAssociatedContactList( new AssociatedContactList());
        location.setCustomElementList(buildLocationCustomElementList("xxxxxxxxx", "222-222"));
        return location;
    }
    
    private PaymentMethod buildPaymentMethod() {
        PaymentMethod method = new PaymentMethod();
        method.setType("po");
        
        POPaymentMethod poPaymentMethod = new POPaymentMethod();
        poPaymentMethod.setPoPayment(buildPOPayment());
        poPaymentMethod.setpCardPayment(buildPCardPayment());
        
        method.setPoPaymentMethod(poPaymentMethod);
        return method;
    }
    
    private POPayment buildPOPayment() {
        POPayment poPayment = new POPayment();
        poPayment.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        poPayment.setPoNumberSelection(new PONumberSelection());
        poPayment.setAllowFreeForm(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        return poPayment;
    }
    
    private PCardPayment buildPCardPayment() {
        PCardPayment pcard = new PCardPayment();
        pcard.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.FALSE));
        pcard.setPoNumberSelection(new PONumberSelection());
        pcard.setRequireCardSecurityCode(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        return pcard;
    }
    
    private Shipping buildShipping() {
        Shipping shipping = new Shipping();
        shipping.setSurchargeConfiguration(buildSurchargeConfiguration());
        return shipping;
    }
    
    private SurchargeConfiguration buildSurchargeConfiguration() {
        SurchargeConfiguration config = new SurchargeConfiguration();
        config.setFee(buildFee());
        config.setOrderThreshold(JaggaerBuilderTest.buildJaggaerBasicValue("0"));
        config.setUseOrderThreshold(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        
        return config;
    }
    
    private Fee buildFee() {
        Fee fee = new Fee();
        fee.setFeeType(JaggaerBuilderTest.buildJaggaerBasicValue("FlatFee"));
        fee.setPercentage(JaggaerBuilderTest.buildJaggaerBasicValue("0.000"));
        fee.setAmount(JaggaerBuilderTest.buildAmount("0.00"));
        fee.setFeeScope(JaggaerBuilderTest.buildJaggaerBasicValue("ByOrder"));
        return fee;
    }
    
    private Handling buildHandling() {
        Handling handling = new Handling();
        handling.setSurchargeConfiguration(buildSurchargeConfiguration());
        return handling;
    }
    
    private TaxInfo buildTaxInfo() {
        TaxInfo info = new TaxInfo();
        info.setTaxableByDefault(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.TRUE));
        info.setTax1(JaggaerBuilderTest.buildJaggaerBasicValue("0.000"));
        info.setTax1Active(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTax2(JaggaerBuilderTest.buildJaggaerBasicValue("0.000"));
        info.setTax2Active(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTaxShipping(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTaxHandling(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        return info;
    }
    
    private TermsAndCondition buildTermsAndCondition() {
        TermsAndCondition terms = new TermsAndCondition();
        
        PaymentTerms paymentTerms = new PaymentTerms();
        paymentTerms.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        paymentTerms.setDiscount(buildDiscount());
        paymentTerms.setDays(JaggaerBuilderTest.buildJaggaerBasicValue("0"));
        paymentTerms.setFob(JaggaerBuilderTest.buildJaggaerBasicValue("notApplicable"));
        paymentTerms.setTermsType(JaggaerBuilderTest.buildJaggaerBasicValue("Net"));
        paymentTerms.setDaysAfter(JaggaerBuilderTest.buildJaggaerBasicValue("30"));
        
        terms.setPaymentTerms(paymentTerms);
        
        return terms;
    }
    
    private Discount buildDiscount() {
        Discount discount = new Discount();
        discount.setUnit("percent");
        DiscountPercent percent = new DiscountPercent();
        percent.setValue("0.0000");
        discount.getDiscountItems().add(percent);
        return discount;
    }
    
    private OrderDistributionList buildOrderDistributionList() {
        OrderDistributionList orderList = new OrderDistributionList();
        
        DistributionMethod faxMethod = buildDistributionMethod("fax", JaggaerConstants.FALSE, null);
        faxMethod.setFax(buildFax("1", "619", "5773360", null));
        orderList.getDistributionMethods().add(faxMethod);
        
        orderList.getDistributionMethods().add(buildDistributionMethod("emailplain", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethods().add(buildDistributionMethod("emailbody", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethods().add(buildDistributionMethod("emailattach", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethods().add(buildDistributionMethod("manual", JaggaerConstants.TRUE, null));
        
        return orderList;
    }
    
    private DistributionMethod buildDistributionMethod(String type, String active, String email) {
        DistributionMethod method = new DistributionMethod();
        method.setType(type);
        method.setActive(JaggaerBuilderTest.buildActive(active));
        if (StringUtils.isNotBlank(email)) {
            method.setEmail(JaggaerBuilderTest.buildEmail(email));
        }
        return method;
    }
    
    private AssociatedAddressList buildAssociatedAddressList() {
        AssociatedAddressList addressList = new AssociatedAddressList();
        
        AssociatedAddress address = buildAssociatedAddress("fulfillment", "add123");
        address.getAddressRef().setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511560"));
        address.getAddressRef().setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        addressList.getAssociatedAddresses().add(address);
        
        return addressList;
    }
    
    private CustomElementList buildLocationCustomElementList(String taxPayerId, String taxTypeCode) {
        CustomElementList elementList = new CustomElementList();
        elementList.getCustomElements().add(buildCustomElement(JaggaerConstants.YES, "TXPID", "TXP Taxpayer ID", taxPayerId));
        elementList.getCustomElements().add(buildCustomElement(JaggaerConstants.YES, "TXPTC", "TXP Type Code", taxTypeCode));
        elementList.getCustomElements().add(buildCustomElement(JaggaerConstants.YES, "WHT", "Withholding Tax", JaggaerConstants.NO));
        elementList.getCustomElements().add(buildCustomElement(JaggaerConstants.YES, "ATD", "Always Take Discount", JaggaerConstants.YES));
        elementList.getCustomElements().add(buildCustomElement(JaggaerConstants.YES, "HFP", "Hold Future Payments", JaggaerConstants.NO));
        return elementList;
    }
    
    private static CustomElement buildCustomElement(String active, String elementIdentifier, String displayName, String... values) {
        CustomElement element = new CustomElement();
        if (StringUtils.isNoneBlank(active)) {
            element.setIsActive(active);
        }
        
        element.setCustomElementIdentifier(JaggaerBuilderTest.buildJaggaerBasicValue(elementIdentifier));
        
        if (StringUtils.isNotBlank(displayName)) {
            element.setDisplayName(JaggaerBuilderTest.buildDisplayName(displayName));
        }
        
        CustomElementValueList valueList = new CustomElementValueList();
        for (String value : values) {
            CustomElementValue elementValue = new CustomElementValue();
            elementValue.setValue(value);
            valueList.getCustomElementValues().add(elementValue);
        }
        element.getCustomElementDetails().add(valueList);
        
        return element;
    }
    
    private Location buildFulfillLocation2() {
        Location location = new Location();
        location.setSupportsOrderFulfillment(JaggaerConstants.YES);
        location.setErpNumber(JaggaerBuilderTest.buildErpNumber(null));
        location.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1514101"));
        location.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilderTest.buildName("Fulfillment Center 2"));
        location.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        location.setPrimary(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.NO));
        location.setPaymentMethod(buildPaymentMethod());
        location.setShipping(buildShipping());
        location.setHandling(buildHandling());
        location.setTaxInfo(buildTaxInfo());
        location.setTermsAndCondition(buildTermsAndCondition());
        location.setOrderDistributionList(buildOrderDistributionList());
        location.setAssociatedAddressList(buildAssociatedAddressList());
        location.setCustomElementList(buildLocationCustomElementList("yyyyyyyyy", "555-555"));
        return location;
    }
    
    private Location buildLocation2() {
        Location location = new Location();
        location.setSupportsOrderFulfillment(JaggaerConstants.NO);
        location.setErpNumber(JaggaerBuilderTest.buildErpNumber(null));
        location.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511564"));
        location.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilderTest.buildName("Location 2"));
        location.setDescription(JaggaerBuilderTest.buildJaggaerBasicValue(null));
        location.setPrimary(JaggaerBuilderTest.buildJaggaerBasicValue(JaggaerConstants.YES));
        location.setLocationEffectiveDate(JaggaerBuilderTest.buildJaggaerBasicValue(null));
        location.setAssociatedAddressList(new AssociatedAddressList());
        location.setAssociatedContactList(new AssociatedContactList());
        return location;
    }
    
    private CustomElementList buildSupplierCustomElementList() {
        CustomElementList elementList = new CustomElementList();
        elementList.getCustomElements().add(buildCustomElement(null, "CustomElementID1", null, "20"));
        elementList.getCustomElements().add(buildCustomElement(null, "CustomElementID2", null, "Brake Inspection", "Tire Change", "Oil Change"));
        return elementList;
    }
    
    private AccountsPayableList buildAccountsPayableList() {
        AccountsPayableList apList = new AccountsPayableList();
        AccountsPayable ap = new AccountsPayable();
        ap.setType("Check");
        ap.setErpNumber(JaggaerBuilderTest.buildErpNumber("ap123"));;
        ap.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("u1511565"));
        ap.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        ap.setName(JaggaerBuilderTest.buildName("ap 1"));
        ap.setActive(JaggaerBuilderTest.buildActive(JaggaerConstants.TRUE));
        
        AssociatedAddress address = buildAssociatedAddress("remitto", "apremit123");
        address.getAddressRef().setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber(null));
        address.getAddressRef().setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber(null));
        ap.getAssociatedAddresses().add(address);
        
        ap.setEmail(new Email());
        ap.setIsoCurrencyCode(JaggaerBuilderTest.buildIsoCurrencyCode("USD"));
        ap.setBankAccount(buildBankAccount());
        ap.setFlexFields(buildFlexFields());
        
        apList.getAccountsPayableDetails().add(ap);
        return apList;
    }
    
    private BankAccount buildBankAccount() {
        BankAccount bank = new BankAccount();
        bank.setType("Checking");
        bank.setBankName(JaggaerBuilderTest.buildJaggaerBasicValue("Third National Bank of Cary"));
        bank.setAccountHoldersName(JaggaerBuilderTest.buildJaggaerBasicValue(null));
        bank.setAccountNumberType(JaggaerBuilderTest.buildJaggaerBasicValue("Account Number"));
        bank.setRoutingNumber(JaggaerBuilderTest.buildJaggaerBasicValue("234765198"));
        bank.setBankAccountNumber(JaggaerBuilderTest.buildJaggaerBasicValue("093827541899"));
        bank.setIsoCountryCode(JaggaerBuilderTest.buildIsoCountryCode("USA"));
        bank.setAddressLine1(JaggaerBuilderTest.buildAddressLine("Austin Bldg, Suite 717"));
        bank.setAddressLine2(JaggaerBuilderTest.buildAddressLine("3400 Main Street"));
        bank.setCity(JaggaerBuilderTest.buildCity("Cary"));
        bank.setState(JaggaerBuilderTest.buildState("NC"));
        bank.setPostalCode(JaggaerBuilderTest.buildPostalCode("27511"));
        return bank;
    }
    
    private FlexFields buildFlexFields() {
        FlexFields fields = new FlexFields();
        fields.setFlexField1(JaggaerBuilderTest.buildJaggaerBasicValue("NFS 12"));
        fields.setFlexField2(JaggaerBuilderTest.buildJaggaerBasicValue("NFS 11"));
        fields.setFlexField3(JaggaerBuilderTest.buildJaggaerBasicValue("NFS 34"));
        fields.setFlexField4(JaggaerBuilderTest.buildJaggaerBasicValue("90"));
        fields.setFlexField5(JaggaerBuilderTest.buildJaggaerBasicValue("-0"));
        return fields;
    }
    
    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        
        TaxInformation info = new TaxInformation();
        info.setType("TAX_TYPE_W9");
        info.setTaxDocumentName(JaggaerBuilderTest.buildJaggaerBasicValue("W9"));
        info.setTaxDocumentYear(JaggaerBuilderTest.buildJaggaerBasicValue("2022"));
        
        TaxDocument doc = new TaxDocument();
        doc.setAttachmentList(buildAttachmentList());
        doc.getAttachmentList().getAttachments().add(JaggaerBuilderTest.buildAttachment("42d", "file", "Tax", "883", null));
        
        info.setTaxDocument(doc);
        
        taxList.getTaxInformationDetails().add(info);
        return taxList;
    }
    
    private SupplierResponseMessage buildSupplierResponseMessage() {
        SupplierResponseMessage srm = new SupplierResponseMessage();
        srm.setStatus(buildStatus());
        srm.getSupplierErrors().add(buildSupplierErrors());
        return srm;
    }
    
    private Status buildStatus() {
        Status status = new Status();
        status.setStatusCode("406");
        status.setStatusText("Text of status message");
        status.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return status;
    }
    
    private SupplierError buildSupplierErrors() {
        SupplierError se = new SupplierError();
        se.setSupplierRef(buildReferenceObject(SupplierRef.class));
        se.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        se.getAddressErrors().add(buildAddressErrors());
        se.getContactErrors().add(buildContactErrors());
        se.getLocationErrors().add(buildLocationErrors());
        se.getAccountsPayableErrors().add(buildAccountsPayableErrors());
        se.getCustomElementErrors().add(buildCustomElementErrors());
        return se;
    }
    
    public <T extends JaggaerRef> T buildReferenceObject(Class<T> clazz) {
        T ref = null;
        try {
            ref = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ref.setErpNumber(JaggaerBuilderTest.buildErpNumber("1111111"));
        ref.setSqIntegrationNumber(JaggaerBuilderTest.buildSQIntegrationNumber("2222222"));
        ref.setThirdPartyRefNumber(JaggaerBuilderTest.buildThirdPartyRefNumber("3333333"));
        return ref;
    }

    
    private AddressError buildAddressErrors() {
        AddressError ae = new AddressError();
        ae.setAddressRef(buildReferenceObject(AddressRef.class));
        ae.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return ae;
    }
    
    private ContactError buildContactErrors() {
        ContactError ce = new ContactError();
        ce.setContactRef(buildReferenceObject(ContactRef.class));
        ce.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return ce;
    }
    
    private LocationError buildLocationErrors() {
        LocationError le = new LocationError();
        le.setAddressRef(buildReferenceObject(AddressRef.class));
        le.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return le;
    }
    
    private AccountsPayableError buildAccountsPayableErrors() {
        AccountsPayableError ape = new AccountsPayableError();
        ape.setAccountsPayableRef(buildReferenceObject(AccountsPayableRef.class));
        ape.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return ape;
    }
    
    private CustomElementError buildCustomElementErrors() {
        CustomElementError cee = new CustomElementError();
        cee.setCustomElementIdentifier(JaggaerBuilderTest.buildJaggaerBasicValue("CustomElementID1"));
        cee.getErrorMessages().add(buildErrorMessage(TEXT_OF_ERROR_MESSAGE));
        return cee;
    }

}
