package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
        CuXMLUnitTestUtils.compareXML(actualXmlFile, expectedXmlFile);
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
    
    private void logActualXmlIfNeeded(SupplierSyncMessage supplierSyncMessage) throws JAXBException, IOException {
        if (true) {
            String actualResults = marshalService.marshalObjectToXmlString(supplierSyncMessage);
            LOG.info("logActualXmlIfNeeded, actualResults: " + actualResults);
        }
    }

}
