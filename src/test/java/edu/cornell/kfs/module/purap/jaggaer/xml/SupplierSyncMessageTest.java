package edu.cornell.kfs.module.purap.jaggaer.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class SupplierSyncMessageTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String BATCH_DIRECTORY = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/outputtemp/";
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    private static final String BASIC_FILE_EXAMPLE = "SupplierSyncMessageBasic.xml";
    
    private File batchDirectoryFile;
    
    private CUMarshalService marshalService;

    @BeforeEach
    void setUpBeforeClass() throws Exception {
        marshalService = new CUMarshalServiceImpl();
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @AfterEach
    void tearDownAfterClass() throws Exception {
        marshalService = null;
        FileUtils.deleteDirectory(batchDirectoryFile);
    }

    @Test
    void test() throws JAXBException, IOException, SAXException, ParserConfigurationException {
        File basicFileExample = new File(INPUT_FILE_PATH + BASIC_FILE_EXAMPLE);
        //String expectedXml = FileUtils.readFileToString(basicFileExample, StandardCharsets.UTF_8);
        //LOG.info("expectedXML: " + expectedXml);
        
        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        
        supplierSyncMessage.setVersion("1.0");
        supplierSyncMessage.setHeader(builderHeader());
        
        SupplierRequestMessage srm = new SupplierRequestMessage();
        srm.getSupplier().add(buildSupplier());
        
        supplierSyncMessage.getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage().add(srm);
        
        String actualResults  = marshalService.marshalObjectToXmlString(supplierSyncMessage);
        LOG.info("actualResults: " + actualResults);
        
        File actualXmlFile = marshalService.marshalObjectToXML(supplierSyncMessage, BATCH_DIRECTORY + "test.xml");
        
        assertXMLFilesEqual(actualXmlFile, basicFileExample);
        
        
    }

    private Header builderHeader() {
        Header header = new Header();
        
        Authentication auth = new Authentication();
        auth.setIdentity("Cornell");
        auth.setSharedSecret("SuperCoolPassword");
        header.setAuthentication(auth);
        
        header.setMessageId("message id");
        header.setRelatedMessageId("related id");
        header.setTimestamp("20210218");
        return header;
    }
    
    private Supplier buildSupplier() {
        Supplier supplier = new Supplier();
        supplier.setJaSupplierId("ja supplier id");
        supplier.setIsChanged("T");
        supplier.setApprovedForERPSync("T");
        supplier.setRequiresERP("T");
        supplier.setOldERPNumber("old erp number");
        
        ERPNumber erpNumber = new ERPNumber();
        erpNumber.setvalue("erp number");
        erpNumber.setIsChanged("F");
        supplier.setErpNumber(erpNumber);
        
        Name name = new Name();
        name.setvalue("Acme Test Company");
        supplier.setName(name);
        
        supplier.setRestrictFulfillmentLocationsByBusinessUnit(new JaggaerBasicValue("restrict"));
        supplier.getRestrictFulfillmentLocationsByBusinessUnit().setIsChanged("T");
        
        supplier.setSic(new JaggaerBasicValue("SIC"));
        supplier.getSic().setIsChanged("F");
        
        supplier.setSupplierKeywords(new JaggaerBasicValue("keyward"));
        
        supplier.setEnablePaymentProvisioning(new JaggaerBasicValue("prov"));
        
        supplier.setAustinTetra(new JaggaerBasicValue("austin"));
        
        supplier.setShoppingCommodityCode(new JaggaerBasicValue("commodity"));
        
        supplier.setVatExempt(new JaggaerBasicValue("VAT"));
        
        supplier.setVatIdentificationNumber(new JaggaerBasicValue("VAT ID"));
        
        supplier.setSupplierShareholders(new JaggaerBasicValue("holders"));
        
        supplier.setSupplierRegNumber(new JaggaerBasicValue("reg number"));
        
        supplier.setSupplierRegSeat(new JaggaerBasicValue("regular seat"));
        
        supplier.setSupplierRegCourt(new JaggaerBasicValue("regular court"));
        
        JaggaerBasicValue repId = new JaggaerBasicValue();
        repId.setvalue("tax rep ID");
        repId.setIsChanged("T");
        supplier.setSupplierTaxRepresentativeId(repId);
        
        supplier.setRegistrationProfileStatus(new JaggaerBasicValue("profile status"));
        
        supplier.setRegistrationProfileType(new JaggaerBasicValue("profile tyoe"));
        
        supplier.setYearEstablished(new JaggaerBasicValue("1977"));
        
        supplier.setNumberOfEmployees(new JaggaerBasicValue("69"));
        
        supplier.setExemptFromBackupWithholding(new JaggaerBasicValue("back holding"));
        
        supplier.setTaxIdentificationNumber(new JaggaerBasicValue("tax id"));
        
        supplier.setTaxIdentificationType(new JaggaerBasicValue("tax type"));
        
        supplier.setLegalStructure(new JaggaerBasicValue("legal structure"));
        
        DUNS duns = new DUNS();
        duns.setvalue("duns");
        supplier.setDuns(duns);
        
        WebSiteURL url = new WebSiteURL();
        url.setvalue("www.cornell.edu");
        supplier.setWebSiteURL(url);
        
        Active active = new Active();
        active.setvalue("active");
        supplier.setActive(active);
        
        CountryOfOrigin country = new CountryOfOrigin();
        country.setvalue("USA");
        supplier.setCountryOfOrigin(country);
        
        OtherNames other = new OtherNames();
        other.setvalue("other name");
        supplier.setOtherNames(other);
        
        DoingBusinessAs dba = new DoingBusinessAs();
        dba.setvalue("doing business os");
        supplier.setDoingBusinessAs(dba);
        
        ThirdPartyRefNumber refNumber = new ThirdPartyRefNumber();
        refNumber.setvalue("3rd party ref number");
        supplier.setThirdPartyRefNumber(refNumber);
        
        SupplierSQId sqId = new SupplierSQId();
        sqId.setvalue("SO ID");
        supplier.setSupplierSQId(sqId);
        
        SQIntegrationNumber sqIntegrationNumber = new SQIntegrationNumber();
        sqIntegrationNumber.setvalue("sqIntegrationNumber");
        supplier.setSqIntegrationNumber(sqIntegrationNumber);
        
        
        
        Brands brands = new Brands();
        supplier.setBrands(brands);
        
        NaicsCodes codes = new NaicsCodes();
        supplier.setNaicsCodes(codes);
        
        SupportedCurrencyList supportedCurrency = new SupportedCurrencyList();
        supplier.setSupportedCurrencyList(supportedCurrency);
        
        EnabledCurrencyList currencyList = new EnabledCurrencyList();
        supplier.setEnabledCurrencyList(currencyList);
        
        AddressList addressList = new AddressList();
        supplier.setAddressList(addressList);
        
        PrimaryAddressList addresses = new PrimaryAddressList();
        supplier.setPrimaryAddressList(addresses);
        
        ContactList contactList = new ContactList();
        supplier.setContactList(contactList);
        
        PrimaryContactList primaryContact = new PrimaryContactList();
        supplier.setPrimaryContactList(primaryContact);
        
        ClassificationList classificiationList = new ClassificationList();
        supplier.setClassificationList(classificiationList);
        
        DiversityClassificationList diversity = new DiversityClassificationList();
        supplier.setDiversityClassificationList(diversity);
        
        LocationList location = new LocationList();
        supplier.setLocationList(location);
        
        CustomElementList custom = new CustomElementList();
        supplier.setCustomElementList(custom);
        
        AccountsPayableList ap = new AccountsPayableList();
        supplier.setAccountsPayableList(ap);
        
        TaxInformationList tax = new TaxInformationList();
        supplier.setTaxInformationList(tax);
        
        InsuranceInformationList insurance = new InsuranceInformationList();
        supplier.setInsuranceInformationList(insurance);
        
        CommodityCodeList commodityCodeList = new CommodityCodeList();
        supplier.setCommodityCodeList(commodityCodeList);
        
        SupplierCapital capital = new SupplierCapital();
        supplier.setSupplierCapital(capital);
        
        ServiceAreaList areaList = new ServiceAreaList();
        supplier.setServiceAreaList(areaList);
        
        AnnualSalesList salesList = new AnnualSalesList();
        supplier.setAnnualSalesList(salesList);
        
        BusinessUnitVendorNumberList unitNumberList = new BusinessUnitVendorNumberList();
        supplier.setBusinessUnitVendorNumberList(unitNumberList);
        
        ParentSupplier parent = new ParentSupplier();
        supplier.setParentSupplier(parent);
        
        return supplier;
    }
    
    private void assertXMLFilesEqual(File actualXmlFile, File expectedXmlFile) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document actualDocument = db.parse(actualXmlFile);
        actualDocument.normalizeDocument();

        Document expectedDocument = db.parse(expectedXmlFile);
        expectedDocument.normalizeDocument();

        assertTrue(actualDocument.isEqualNode(expectedDocument));
    }

}
