package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

public class JaggaerExampleTest {
    
    private static final Logger LOG = LogManager.getLogger();

    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    private static final String OUTPUT_FILE_PATH = INPUT_FILE_PATH + "jaggaertemp/";
    private static final String BASIC_FILE_EXAMPLE = "JaggaerExample.xml";

    private File outputFileDirectory;

    private CUMarshalService marshalService;

    @BeforeEach
    public void setUp() throws Exception {
        marshalService = new CUMarshalServiceImpl();
        outputFileDirectory = new File(OUTPUT_FILE_PATH);
        outputFileDirectory.mkdir();
    }

    @AfterEach
    public void tearDown() throws Exception {
        marshalService = null;
        FileUtils.deleteDirectory(outputFileDirectory);
    }

    @Test
    void testBuildingJaggaerExample() throws JAXBException, IOException, SAXException {
        File expectedXmlFile = new File(INPUT_FILE_PATH + BASIC_FILE_EXAMPLE);

        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        supplierSyncMessage.setVersion(JaggaerConstants.SUPPLIER_SYNCH_MESSAGE_XML_VERSION);
        supplierSyncMessage.setHeader(buildHeader());
        supplierSyncMessage.getSupplierRequestMessageItems().add(buildSupplierRequestMessage());


        logActualXmlIfNeeded(supplierSyncMessage);
        File actualXmlFile = marshalService.marshalObjectToXML(supplierSyncMessage, OUTPUT_FILE_PATH + "testJaggaerExample.xml");
        CuXMLUnitTestUtils.compareXML(expectedXmlFile, actualXmlFile);
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
        message.getSupplier().add(buildSupplier());
        return message;
    }
    
    private Supplier buildSupplier() {
        Supplier supplier = new Supplier();
        supplier.setErpNumber(JaggaerBuilder.buildERPNumber("als12345"));
        supplier.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511507"));
        supplier.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber("ALS0001"));
        supplier.setName(JaggaerBuilder.buildName("Affordable Lab Supplies"));
        supplier.setDoingBusinessAs(JaggaerBuilder.buildJaggaerBasicValue("ALS"));
        supplier.setOtherNames(JaggaerBuilder.buildJaggaerBasicValue("AffLabs"));
        supplier.setJaSupplierId("9142");
        supplier.setCountryOfOrigin(JaggaerBuilder.buildJaggaerBasicValue("US"));
        supplier.setParentSupplier(buildParentSupplier());
        supplier.setActive(JaggaerBuilder.buildActive(JaggaerConstants.NO));
        supplier.setBusinessUnitVendorNumberList(buildBusinessUnitVendorNumberList());
        supplier.setWebSiteURL(JaggaerBuilder.buildJaggaerBasicValue("www.affordablelabsupplies.com"));
        supplier.setDuns(JaggaerBuilder.buildJaggaerBasicValue("123456789"));
        supplier.setLegalStructure(JaggaerBuilder.buildJaggaerBasicValue("LimitedLiabilityCompany_Partnership"));
        supplier.setTaxIdentificationNumber(JaggaerBuilder.buildJaggaerBasicValue("123456789"));
        supplier.setVatIdentificationNumber(JaggaerBuilder.buildJaggaerBasicValue("123456789"));
        supplier.setExemptFromBackupWithholding(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.YES));
        supplier.setNumberOfEmployees(JaggaerBuilder.buildJaggaerBasicValue("400"));
        supplier.setYearEstablished(JaggaerBuilder.buildJaggaerBasicValue("1983-06-04"));
        supplier.setAnnualSalesList(buildAnnualSalesList());
        supplier.setServiceAreaList(buildServiceAreaList());
        supplier.setSupplierTaxRepresentativeId(JaggaerBuilder.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegCourt(JaggaerBuilder.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegNumber(JaggaerBuilder.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierRegSeat(JaggaerBuilder.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setSupplierShareholders(JaggaerBuilder.buildJaggaerBasicValue(null, JaggaerConstants.NO));
        supplier.setVatExempt(JaggaerBuilder.buildJaggaerBasicValue("false", JaggaerConstants.NO));
        supplier.setCommodityCodeList(buildCommodityCodeList());
        supplier.setShoppingCommodityCode(JaggaerBuilder.buildJaggaerBasicValue("SC01"));
        supplier.setAustinTetra(JaggaerBuilder.buildJaggaerBasicValue("Austin-Tetra Code"));
        supplier.setSic(JaggaerBuilder.buildJaggaerBasicValue("SIC Code"));
        supplier.setNaicsCodes(buildNaicsCodes());
        supplier.setSupportedCurrencyList(buildCurrencyList("USD", "CAD", "GBP", "EUR", "CHF", "MXN"));
        supplier.setEnabledCurrencyList(buildCurrencyList("USD", "MXN"));
        supplier.setSupplierKeywords(JaggaerBuilder.buildJaggaerBasicValue("Paper Products, Filters, Drinking Straws"));
        supplier.setAddressList(buildAddressList());
        supplier.setPrimaryAddressList(buildPrimaryAddressList());
        supplier.setContactList(buildContactList());
        supplier.setPrimaryContactList(buildPrimaryContactList());
        supplier.setClassificationList(buildClassificationList());
        supplier.setDiversityClassificationList(buildDiversityClassificationList());
        supplier.setInsuranceInformationList(buildInsuranceInformationList());
        supplier.setLocationList(buildLocationList());
        supplier.setRestrictFulfillmentLocationsByBusinessUnit(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        supplier.setCustomElementList(buildSupplierCustomElementList());
        supplier.setAccountsPayableList(buildAccountsPayableList());
        supplier.setTaxInformationList(buildTaxInformationList());
        return supplier;
    }
    
    private ParentSupplier buildParentSupplier() {
        ParentSupplier parent = new ParentSupplier();
        parent.setErpNumber(JaggaerBuilder.buildERPNumber("als12343"));
        parent.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511504"));
        return parent;
    }
    
    private BusinessUnitVendorNumberList buildBusinessUnitVendorNumberList() {
        BusinessUnitVendorNumberList list = new BusinessUnitVendorNumberList();
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("NC1", "NCCBO0001"));
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("VA1", "VACBO0001"));
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("NC2", "NCCBO0002"));
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("VA2", "VACBO0002"));
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("NC3", "NCCBO0003"));
        list.getBusinessUnitVendorNumber().add(JaggaerBuilder.buildBusinessUnitVendorNumber("VA3", "VACBO0003"));
        return list;
    }
    
    private AnnualSalesList buildAnnualSalesList() {
        AnnualSalesList list = new AnnualSalesList();
        list.getAnnualSales().add(buildAnnualSales("60,000.00", "2024", "USD"));
        list.getAnnualSales().add(buildAnnualSales("50,000.00", "2024", "USD"));
        list.getAnnualSales().add(buildAnnualSales("40,000.00", "2024", "USD"));
        return list;
    }
    
    private AnnualSales buildAnnualSales(String amountString, String salesYear, String currencyCode) {
        AnnualSales sale = new AnnualSales();
        sale.setAnnualSalesAmount(JaggaerBuilder.buildAmount(amountString));
        sale.setAnnualSalesYear(JaggaerBuilder.buildJaggaerBasicValue(salesYear));
        sale.setIsoCurrencyCode(JaggaerBuilder.buildIsoCurrencyCode(currencyCode));
        return sale;
    }
    
    private ServiceAreaList buildServiceAreaList() {
        ServiceAreaList serviceAreaList = new ServiceAreaList();
        ServiceArea serviceArea = new ServiceArea();
        serviceArea.setServiceAreaInternalName(JaggaerBuilder.buildJaggaerBasicValue("US"));
        
        StateServiceAreaList stateServiceAreaList = new StateServiceAreaList();
        stateServiceAreaList.getStateServiceAreaInternalName().add(JaggaerBuilder.buildStateServiceAreaInternalName("US-NC"));
        stateServiceAreaList.getStateServiceAreaInternalName().add(JaggaerBuilder.buildStateServiceAreaInternalName("US-CA"));
        
        serviceArea.getStateServiceAreaList().add(stateServiceAreaList);
        serviceAreaList.getServiceArea().add(serviceArea);
        return serviceAreaList;
    }
    
    private CommodityCodeList buildCommodityCodeList() {
        CommodityCodeList list = new CommodityCodeList();
        list.getCommodityCode().add(JaggaerBuilder.buildJaggaerBasicValue("OM01"));
        list.getCommodityCode().add(JaggaerBuilder.buildJaggaerBasicValue("OM02"));
        return list;
    }
    
    private NaicsCodes buildNaicsCodes() {
        NaicsCodes naicsCodes = new NaicsCodes();
        naicsCodes.getPrimaryNaicsOrSecondaryNaicsList().add(JaggaerBuilder.buildPrimaryNaics("424120"));
        
        SecondaryNaicsList secondaryList = new SecondaryNaicsList();
        secondaryList.getSecondaryNaics().add(JaggaerBuilder.buildSecondaryNaics("424121"));
        secondaryList.getSecondaryNaics().add(JaggaerBuilder.buildSecondaryNaics("524120"));
        secondaryList.getSecondaryNaics().add(JaggaerBuilder.buildSecondaryNaics("524121"));
        naicsCodes.getPrimaryNaicsOrSecondaryNaicsList().add(secondaryList);
        
        return naicsCodes;
    }
    
    private CurrencyList buildCurrencyList(String... currencies) {
        CurrencyList list = new CurrencyList();
        list.setIsChanged(JaggaerConstants.NO);
        for (String currency : currencies) {
            list.getIsoCurrencyCode().add(JaggaerBuilder.buildIsoCurrencyCode(currency, JaggaerConstants.NO));
        }
        return list;
    }
    
    private AddressList buildAddressList() {
        AddressList list = new AddressList();
        list.getAddress().add(buildRemitToAddress());
        list.getAddress().add(buildFulfillmentAddress());
        return list;
    }
    
    private Address buildRemitToAddress() {
        Address remit = new Address();
        remit.setType("remitto");
        remit.setErpNumber(JaggaerBuilder.buildERPNumber("add123"));
        remit.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u15174772"));
        remit.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        remit.setName(JaggaerBuilder.buildName("California Regional Office"));
        remit.setActive(JaggaerBuilder.buildActive(JaggaerConstants.NO));
        remit.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("accountspayable@affordablelabsupplies.com"));  
        remit.setAddressLine1(JaggaerBuilder.buildAddressLine("309 Pattern Ave."));
        remit.setAddressLine2(new AddressLine());
        remit.setAddressLine3(new AddressLine());
        remit.setCity(JaggaerBuilder.buildCity("San Diego"));
        remit.setState(JaggaerBuilder.buildState("CA"));
        remit.setPostalCode(JaggaerBuilder.buildPostalCode("92093"));
        remit.setIsoCountryCode(JaggaerBuilder.buildIsoCountryCode("USA"));
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
        method.getEmailOrFax().add(JaggaerBuilder.buildEmail(emailAddress));
        return method;
    }
    
    private Phone buildPhone(String countryCode, String areaCode, String number, String extension) {
        Phone phone = new Phone();
        phone.setTelephoneNumber(JaggaerBuilder.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private TollFreePhone buildTollFreePhone(String countryCode, String areaCode, String number, String extension) {
        TollFreePhone phone = new TollFreePhone();
        phone.setTelephoneNumber(JaggaerBuilder.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private Fax buildFax(String countryCode, String areaCode, String number, String extension) {
        Fax fax = new Fax();
        fax.setTelephoneNumber(JaggaerBuilder.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return fax;
    }
    
    private AssignedBusinessUnitsList buildAssignedBusinessUnitsList(String... names) {
        AssignedBusinessUnitsList list = new AssignedBusinessUnitsList();
        for (String name : names) {
            list.getBusinessUnitInternalName().add(JaggaerBuilder.buildBusinessUnitInternalName(name, null));
        }
        return list;
    }
    
    private Address buildFulfillmentAddress() {
        Address fulfill = new Address();
        fulfill.setType("fulfillment");
        fulfill.setErpNumber(JaggaerBuilder.buildERPNumber("add123"));
        fulfill.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511560"));
        fulfill.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        fulfill.setName(JaggaerBuilder.buildName("West Coast Distribution Center"));
        fulfill.setActive(JaggaerBuilder.buildActive(JaggaerConstants.YES));
        fulfill.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("fulfillment@affordablelabsupplies.com"));  
        fulfill.setAddressLine1(JaggaerBuilder.buildAddressLine("123 Park Ave."));
        fulfill.setAddressLine2(new AddressLine());
        fulfill.setAddressLine3(new AddressLine());
        fulfill.setCity(JaggaerBuilder.buildCity("San Diego"));
        fulfill.setState(JaggaerBuilder.buildState("CA"));
        fulfill.setPostalCode(JaggaerBuilder.buildPostalCode("92093"));
        fulfill.setIsoCountryCode(JaggaerBuilder.buildIsoCountryCode("USA"));
        fulfill.setPhone(buildPhone("1", "619", "2345678", null));
        fulfill.setTollFreePhone(buildTollFreePhone("1", "800", "9012345", null));
        fulfill.setFax(buildFax("1", "619", "6789012", null));
        fulfill.setNotes(new JaggaerBasicValue());
        fulfill.setAssignedBusinessUnitsList(buildAssignedBusinessUnitsList("California Regional Office", "West Coast Distribution Center"));
        return fulfill;
    }
    
    private PrimaryAddressList buildPrimaryAddressList() {
        PrimaryAddressList primaryAddredssList = new PrimaryAddressList();
        primaryAddredssList.getAssociatedAddress().add(buildAssociatedAddress("remitto", "ADDR1"));
        primaryAddredssList.getAssociatedAddress().add(buildAssociatedAddress("fulfillment", "ADDR2"));
        return primaryAddredssList;
    }
    
    private AssociatedAddress buildAssociatedAddress(String addressType, String erpNumber) {
        AssociatedAddress address = new AssociatedAddress();
        address.setType(addressType);
        
        AddressRef ref = new AddressRef();
        ref.setErpNumber(JaggaerBuilder.buildERPNumber(erpNumber));
        address.setAddressRef(ref);
        
        return address;
    }
    
    private ContactList buildContactList() {
        ContactList contactList = new ContactList();
        contactList.getContact().add(buildRemitContact());
        contactList.getContact().add(buildFulfillmentContact());
        contactList.getContact().add(buildTechnicalContact());
        return contactList;
    }
    
    private Contact buildRemitContact() {
        Contact remit = new Contact();
        remit.setType("remitto");
        remit.setErpNumber(JaggaerBuilder.buildERPNumber("CONTACT1"));
        remit.setOldERPNumber("CONTACT1");
        remit.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("SQ_1237"));
        remit.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber("3pID1"));
        remit.setName(JaggaerBuilder.buildName("Primary ALS RemitTo and Fulfillment Contact"));
        remit.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        remit.setFirstName(JaggaerBuilder.buildJaggaerBasicValue("John"));
        remit.setLastName(JaggaerBuilder.buildJaggaerBasicValue("Smith"));
        remit.setTitle(JaggaerBuilder.buildJaggaerBasicValue("Procurement Manager"));
        remit.setEmail(JaggaerBuilder.buildEmail("jsmith@affordablelabsupplies.com"));
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
        phone.setTelephoneNumber(JaggaerBuilder.buildTelephoneNumber(countryCode, areaCode, number, extension));
        return phone;
    }
    
    private Contact buildFulfillmentContact() {
        Contact fulfillment = new Contact();
        fulfillment.setType("fulfillment");
        fulfillment.setErpNumber(JaggaerBuilder.buildERPNumber("CONTACT1"));
        fulfillment.setOldERPNumber("CONTACT1");
        fulfillment.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("SQ_1236"));
        fulfillment.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber("3pID1"));
        fulfillment.setName(JaggaerBuilder.buildName("Primary ALS RemitTo and Fulfillment Contact"));
        fulfillment.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        fulfillment.setFirstName(JaggaerBuilder.buildJaggaerBasicValue("John"));
        fulfillment.setLastName(JaggaerBuilder.buildJaggaerBasicValue("Smith"));
        fulfillment.setTitle(JaggaerBuilder.buildJaggaerBasicValue("Procurement Manager"));
        fulfillment.setEmail(JaggaerBuilder.buildEmail("jsmith@affordablelabsupplies.com"));
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
        tech.setErpNumber(JaggaerBuilder.buildERPNumber("CONTACT2"));
        tech.setOldERPNumber("CONTACT2");
        tech.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("SQ_1240"));
        tech.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber("3pID2"));
        tech.setName(JaggaerBuilder.buildName("Primary ALS Technical Contact"));
        tech.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        tech.setFirstName(JaggaerBuilder.buildJaggaerBasicValue("Ann"));
        tech.setLastName(JaggaerBuilder.buildJaggaerBasicValue("Jones"));
        tech.setTitle(JaggaerBuilder.buildJaggaerBasicValue("Technical Consultant"));
        tech.setEmail(JaggaerBuilder.buildEmail("ajones@affordablelabsupplies.com"));
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
        primaryContactList.getAssociatedContact().add(buildAssociatedContact("remitto", "CONTACT1"));
        primaryContactList.getAssociatedContact().add(buildAssociatedContact("fulfillment", "CONTACT1"));
        return primaryContactList;
    }
    
    private AssociatedContact buildAssociatedContact(String contactType, String erpNumber) {
        AssociatedContact contact = new AssociatedContact();
        contact.setType(contactType);
        
        ContactRef ref = new ContactRef();
        ref.setErpNumber(JaggaerBuilder.buildERPNumber(erpNumber));
        contact.setContactRef(ref);
        
        return contact;
    }
    
    private ClassificationList buildClassificationList() {
        ClassificationList classificationList = new ClassificationList();
        
        Classification classification = new Classification();
        classification.setInternalName(JaggaerBuilder.buildJaggaerBasicValue("SupplierClassMinorityDisabled"));
        
        classificationList.getClassification().add(classification);
        return classificationList;
    }
    
    private DiversityClassificationList buildDiversityClassificationList() {
        DiversityClassificationList classList = new DiversityClassificationList();
        
        DiversityClassification div = new DiversityClassification();
        div.setInternalName(JaggaerBuilder.buildJaggaerBasicValue("DOBE"));
        div.setDd214Certificate(new DD214Certificate());
        div.setDiversityCertificate(new DiversityCertificate());
        
        AdditionalDataList dataList = new AdditionalDataList();
        
        AdditionalData dataum = new AdditionalData();
        dataum.setName("ethnicity");
        dataum.getContent().add("NATAM");
        
        dataList.getAdditionalData().add(dataum);
        div.setAdditionalDataList(dataList);
        
        classList.getDiversityClassification().add(div);
        return classList;
    }
    
    private InsuranceInformationList buildInsuranceInformationList() {
        InsuranceInformationList infoList = new InsuranceInformationList();
        InsuranceInformation info = new InsuranceInformation();
        
        info.setType("AutomobileLiability");
        info.setPolicyNumber(JaggaerBuilder.buildJaggaerBasicValue("34576789"));
        info.setInsuranceLimit(JaggaerBuilder.buildJaggaerBasicValue("INSURANCE_LIMIT_RANGE1"));
        info.setExpirationDate(JaggaerBuilder.buildJaggaerBasicValue("2024-11-12"));
        info.setInsuranceProvider(JaggaerBuilder.buildJaggaerBasicValue("ACME Insurance Co."));
        info.setAgent(JaggaerBuilder.buildJaggaerBasicValue("Ned Ryerson"));
        
        InsuranceProviderPhone providerPhone = new InsuranceProviderPhone();
        providerPhone.setTelephoneNumber(JaggaerBuilder.buildTelephoneNumber("1", "704", "1234567", null));
        info.setInsuranceProviderPhone(providerPhone);
        
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setAttachments(buildAttachments());
        certificate.getAttachments().getAttachment().add(JaggaerBuilder.buildAttachment("42ins", "file", "Auto", "1180", null));
        info.setInsuranceCertificate(certificate);
        
        infoList.getInsuranceInformation().add(info);
        return infoList;
    }
    
    private Attachments buildAttachments() {
        Attachments attachments = new Attachments();
        return attachments;
    }
    
    private LocationList buildLocationList() {
        LocationList locationList = new LocationList();
        locationList.getLocation().add(buildFulfillLocation1());
        locationList.getLocation().add(buildFulfillLocation2());
        locationList.getLocation().add(buildLocation2());
        return locationList;
    }
    
    private Location buildFulfillLocation1() {
        Location location = new Location();
        location.setSupportsOrderFulfillment(JaggaerConstants.YES);
        location.setErpNumber(JaggaerBuilder.buildERPNumber("loc123"));
        location.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511493"));
        location.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilder.buildName("Fulfillment Center 1"));
        location.setDescription(JaggaerBuilder.buildJaggaerBasicValue(null));
        location.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        location.setPrimary(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.YES));
        location.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod("orders@affordablelabsupplies.com"));
        location.setLocationEffectiveDate(JaggaerBuilder.buildJaggaerBasicValue("2024-10-20"));
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
        poPayment.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        poPayment.setPoNumberSelection(new PONumberSelection());
        poPayment.setAllowFreeForm(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        return poPayment;
    }
    
    private PCardPayment buildPCardPayment() {
        PCardPayment pcard = new PCardPayment();
        pcard.setActive(JaggaerBuilder.buildActive(JaggaerConstants.FALSE));
        pcard.setPoNumberSelection(new PONumberSelection());
        pcard.setRequireCardSecurityCode(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
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
        config.setOrderThreshold(JaggaerBuilder.buildJaggaerBasicValue("0"));
        config.setUseOrderThreshold(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        
        return config;
    }
    
    private Fee buildFee() {
        Fee fee = new Fee();
        fee.setFeeType(JaggaerBuilder.buildJaggaerBasicValue("FlatFee"));
        fee.setPercentage(JaggaerBuilder.buildJaggaerBasicValue("0.000"));
        fee.setAmount(JaggaerBuilder.buildAmount("0.00"));
        fee.setFeeScope(JaggaerBuilder.buildJaggaerBasicValue("ByOrder"));
        return fee;
    }
    
    private Handling buildHandling() {
        Handling handling = new Handling();
        handling.setSurchargeConfiguration(buildSurchargeConfiguration());
        return handling;
    }
    
    private TaxInfo buildTaxInfo() {
        TaxInfo info = new TaxInfo();
        info.setTaxableByDefault(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.TRUE));
        info.setTax1(JaggaerBuilder.buildJaggaerBasicValue("0.000"));
        info.setTax1Active(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTax2(JaggaerBuilder.buildJaggaerBasicValue("0.000"));
        info.setTax2Active(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTaxShipping(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        info.setTaxHandling(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
        return info;
    }
    
    private TermsAndCondition buildTermsAndCondition() {
        TermsAndCondition terms = new TermsAndCondition();
        
        PaymentTerms paymentTerms = new PaymentTerms();
        paymentTerms.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        paymentTerms.setDiscount(buildDiscount());
        paymentTerms.setDays(JaggaerBuilder.buildJaggaerBasicValue("0"));
        paymentTerms.setFob(JaggaerBuilder.buildJaggaerBasicValue("notApplicable"));
        paymentTerms.setTermsType(JaggaerBuilder.buildJaggaerBasicValue("Net"));
        paymentTerms.setDaysAfter(JaggaerBuilder.buildJaggaerBasicValue("30"));
        
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
        orderList.getDistributionMethod().add(faxMethod);
        
        orderList.getDistributionMethod().add(buildDistributionMethod("emailplain", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethod().add(buildDistributionMethod("emailbody", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethod().add(buildDistributionMethod("emailattach", JaggaerConstants.FALSE, "support@JAGGAER.com"));
        orderList.getDistributionMethod().add(buildDistributionMethod("manual", JaggaerConstants.TRUE, null));
        
        return orderList;
    }
    
    private DistributionMethod buildDistributionMethod(String type, String active, String email) {
        DistributionMethod method = new DistributionMethod();
        method.setType(type);
        method.setActive(JaggaerBuilder.buildActive(active));
        if (StringUtils.isNotBlank(email)) {
            method.setEmail(JaggaerBuilder.buildEmail(email));
        }
        return method;
    }
    
    private AssociatedAddressList buildAssociatedAddressList() {
        AssociatedAddressList addressList = new AssociatedAddressList();
        
        AssociatedAddress address = buildAssociatedAddress("fulfillment", "add123");
        address.getAddressRef().setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511560"));
        address.getAddressRef().setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        addressList.getAssociatedAddress().add(address);
        
        return addressList;
    }
    
    private CustomElementList buildLocationCustomElementList(String taxPayerId, String taxTypeCode) {
        CustomElementList elementList = new CustomElementList();
        elementList.getCustomElement().add(buildCustomElement(JaggaerConstants.YES, "TXPID", "TXP Taxpayer ID", taxPayerId));
        elementList.getCustomElement().add(buildCustomElement(JaggaerConstants.YES, "TXPTC", "TXP Type Code", taxTypeCode));
        elementList.getCustomElement().add(buildCustomElement(JaggaerConstants.YES, "WHT", "Withholding Tax", JaggaerConstants.NO));
        elementList.getCustomElement().add(buildCustomElement(JaggaerConstants.YES, "ATD", "Always Take Discount", JaggaerConstants.YES));
        elementList.getCustomElement().add(buildCustomElement(JaggaerConstants.YES, "HFP", "Hold Future Payments", JaggaerConstants.NO));
        return elementList;
    }
    
    private static CustomElement buildCustomElement(String active, String elementIdentifier, String displayName, String... values) {
        CustomElement element = new CustomElement();
        if (StringUtils.isNoneBlank(active)) {
            element.setIsActive(active);
        }
        
        element.setCustomElementIdentifier(JaggaerBuilder.buildJaggaerBasicValue(elementIdentifier));
        
        if (StringUtils.isNotBlank(displayName)) {
            element.setDisplayName(JaggaerBuilder.buildDisplayName(displayName));
        }
        
        CustomElementValueList valueList = new CustomElementValueList();
        for (String value : values) {
            CustomElementValue elementValue = new CustomElementValue();
            elementValue.setValue(value);
            valueList.getCustomElementValue().add(elementValue);
        }
        element.getCustomElementValueListOrAttachments().add(valueList);
        
        return element;
    }
    
    private Location buildFulfillLocation2() {
        Location location = new Location();
        location.setSupportsOrderFulfillment(JaggaerConstants.YES);
        location.setErpNumber(JaggaerBuilder.buildERPNumber(null));
        location.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1514101"));
        location.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilder.buildName("Fulfillment Center 2"));
        location.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        location.setPrimary(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.NO));
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
        location.setErpNumber(JaggaerBuilder.buildERPNumber(null));
        location.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511564"));
        location.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        location.setName(JaggaerBuilder.buildName("Location 2"));
        location.setDescription(JaggaerBuilder.buildJaggaerBasicValue(null));
        location.setPrimary(JaggaerBuilder.buildJaggaerBasicValue(JaggaerConstants.YES));
        location.setLocationEffectiveDate(JaggaerBuilder.buildJaggaerBasicValue(null));
        location.setAssociatedAddressList(new AssociatedAddressList());
        location.setAssociatedContactList(new AssociatedContactList());
        return location;
    }
    
    private CustomElementList buildSupplierCustomElementList() {
        CustomElementList elementList = new CustomElementList();
        elementList.getCustomElement().add(buildCustomElement(null, "CustomElementID1", null, "20"));
        elementList.getCustomElement().add(buildCustomElement(null, "CustomElementID2", null, "Brake Inspection", "Tire Change", "Oil Change"));
        return elementList;
    }
    
    private AccountsPayableList buildAccountsPayableList() {
        AccountsPayableList apList = new AccountsPayableList();
        AccountsPayable ap = new AccountsPayable();
        ap.setType("Check");
        ap.setErpNumber(JaggaerBuilder.buildERPNumber("ap123"));;
        ap.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("u1511565"));
        ap.setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        ap.setName(JaggaerBuilder.buildName("ap 1"));
        ap.setActive(JaggaerBuilder.buildActive(JaggaerConstants.TRUE));
        
        AssociatedAddress address = buildAssociatedAddress("remitto", "apremit123");
        address.getAddressRef().setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber(null));
        address.getAddressRef().setThirdPartyRefNumber(JaggaerBuilder.buildThirdPartyRefNumber(null));
        ap.getAssociatedAddress().add(address);
        
        ap.setEmail(new Email());
        ap.setIsoCurrencyCode(JaggaerBuilder.buildIsoCurrencyCode("USD"));
        ap.setBankAccount(buildBankAccount());
        ap.setFlexFields(buildFlexFields());
        
        apList.getAccountsPayable().add(ap);
        return apList;
    }
    
    private BankAccount buildBankAccount() {
        BankAccount bank = new BankAccount();
        bank.setType("Checking");
        bank.setBankName(JaggaerBuilder.buildJaggaerBasicValue("Third National Bank of Cary"));
        bank.setAccountHoldersName(JaggaerBuilder.buildJaggaerBasicValue(null));
        bank.setAccountNumberType(JaggaerBuilder.buildJaggaerBasicValue("Account Number"));
        bank.setRoutingNumber(JaggaerBuilder.buildJaggaerBasicValue("234765198"));
        bank.setBankAccountNumber(JaggaerBuilder.buildJaggaerBasicValue("093827541899"));
        bank.setIsoCountryCode(JaggaerBuilder.buildIsoCountryCode("USA"));
        bank.setAddressLine1(JaggaerBuilder.buildAddressLine("Austin Bldg, Suite 717"));
        bank.setAddressLine2(JaggaerBuilder.buildAddressLine("3400 Main Street"));
        bank.setCity(JaggaerBuilder.buildCity("Cary"));
        bank.setState(JaggaerBuilder.buildState("NC"));
        bank.setPostalCode(JaggaerBuilder.buildPostalCode("27511"));
        return bank;
    }
    
    private FlexFields buildFlexFields() {
        FlexFields fields = new FlexFields();
        fields.setFlexField1(JaggaerBuilder.buildJaggaerBasicValue("NFS 12"));
        fields.setFlexField2(JaggaerBuilder.buildJaggaerBasicValue("NFS 11"));
        fields.setFlexField3(JaggaerBuilder.buildJaggaerBasicValue("NFS 34"));
        fields.setFlexField4(JaggaerBuilder.buildJaggaerBasicValue("90"));
        fields.setFlexField5(JaggaerBuilder.buildJaggaerBasicValue("-0"));
        return fields;
    }
    
    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        
        TaxInformation info = new TaxInformation();
        info.setType("TAX_TYPE_W9");
        info.setTaxDocumentName(JaggaerBuilder.buildJaggaerBasicValue("W9"));
        info.setTaxDocumentYear(JaggaerBuilder.buildJaggaerBasicValue("2022"));
        
        TaxDocument doc = new TaxDocument();
        doc.setAttachments(buildAttachments());
        doc.getAttachments().getAttachment().add(JaggaerBuilder.buildAttachment("42d", "file", "Tax", "883", null));
        
        info.setTaxDocument(doc);
        
        taxList.getTaxInformation().add(info);
        return taxList;
    }
    
    private void logActualXmlIfNeeded(SupplierSyncMessage supplierSyncMessage) throws JAXBException, IOException {
        if (true) {
            String actualResults = marshalService.marshalObjectToXmlString(supplierSyncMessage);
            LOG.info("logActualXmlIfNeeded, actualResults: " + actualResults);
        }
    }

}
