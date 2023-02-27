package edu.cornell.kfs.module.purap.jaggaer.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.types.Expiration;
import org.xml.sax.SAXException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

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

        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();

        supplierSyncMessage.setVersion("1.0");
        supplierSyncMessage.setHeader(buildHeader());

        SupplierRequestMessage srm = new SupplierRequestMessage();
        srm.getSupplier().add(buildSupplier());

        supplierSyncMessage
                .getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage()
                .add(srm);

        String actualResults = marshalService.marshalObjectToXmlString(supplierSyncMessage);
        LOG.info("actualResults: " + actualResults);

        File actualXmlFile = marshalService.marshalObjectToXML(supplierSyncMessage, BATCH_DIRECTORY + "test.xml");

        FileInputStream actualFileInputStream = new FileInputStream(actualXmlFile);
        FileInputStream expectedFIleInputStream = new FileInputStream(basicFileExample);

        BufferedReader actualBufferedReader = new BufferedReader(new InputStreamReader(actualFileInputStream));
        BufferedReader expectedBufferedReader = new BufferedReader(new InputStreamReader(expectedFIleInputStream));

        compareXML(actualBufferedReader, expectedBufferedReader);

    }

    private Header buildHeader() {
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

        supplier.setSupplierKeywords(new JaggaerBasicValue("keyword"));

        supplier.setEnablePaymentProvisioning(new JaggaerBasicValue("prov"));

        supplier.setAustinTetra(new JaggaerBasicValue("austin"));

        supplier.setShoppingCommodityCode(new JaggaerBasicValue("commodity"));

        supplier.setVatExempt(new JaggaerBasicValue("VAT"));

        supplier.setVatIdentificationNumber(new JaggaerBasicValue("VAT ID"));

        supplier.setSupplierShareholders(new JaggaerBasicValue("holders"));

        supplier.setSupplierRegNumber(new JaggaerBasicValue("reg number"));

        supplier.setSupplierRegSeat(new JaggaerBasicValue("regular seat"));

        supplier.setSupplierRegCourt(new JaggaerBasicValue("regular court"));

        JaggaerBasicValue repId = new JaggaerBasicValue("tax rep ID");
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

        supplier.setWebSiteURL(new JaggaerBasicValue("www.cornell.edu"));

        Active active = new Active();
        active.setvalue("active");
        supplier.setActive(active);

        supplier.setCountryOfOrigin(new JaggaerBasicValue("USA"));

        supplier.setOtherNames(new JaggaerBasicValue("other name"));

        supplier.setDoingBusinessAs(new JaggaerBasicValue("doing business os"));

        ThirdPartyRefNumber refNumber = new ThirdPartyRefNumber();
        refNumber.setvalue("3rd party ref number");
        supplier.setThirdPartyRefNumber(refNumber);

        SupplierSQId sqId = new SupplierSQId();
        sqId.setvalue("SO ID");
        supplier.setSupplierSQId(sqId);

        SQIntegrationNumber sqIntegrationNumber = new SQIntegrationNumber();
        sqIntegrationNumber.setvalue("sqIntegrationNumber");
        supplier.setSqIntegrationNumber(sqIntegrationNumber);

        supplier.setBrands(buildBrands());
        supplier.setNaicsCodes(buildNaicsCodes());
        supplier.setCommodityCodeList(buildCommodityCodeList());
        supplier.setSupportedCurrencyList(buildCurrencyList(false, true, true, true));
        supplier.setEnabledCurrencyList(buildCurrencyList(true, true, false, false));
        supplier.setBusinessUnitVendorNumberList(buildBusinessUnitVendorNumberList());
        supplier.setSupplierCapital(buildSupplierCapital());
        supplier.setAnnualSalesList(buildAnnualSalesList());
        supplier.setServiceAreaList(buildServiceAreaList());
        supplier.setParentSupplier(buildParentSupplier());
        supplier.setInsuranceInformationList(buildInsuranceInformationList());
        supplier.setTaxInformationList(buildTaxInformationList());
        supplier.setAccountsPayableList(buildAccountsPayableList());

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

        return supplier;
    }

    private Brands buildBrands() {
        Brands brands = new Brands();

        JaggaerBasicValue brand1 = new JaggaerBasicValue("brand 1");
        brand1.setIsChanged("T");

        JaggaerBasicValue brand2 = new JaggaerBasicValue("brand 2");
        brand2.setIsChanged("F");

        brands.getBrand().add(brand1);
        brands.getBrand().add(brand2);

        return brands;
    }

    private NaicsCodes buildNaicsCodes() {
        NaicsCodes codes = new NaicsCodes();
        codes.setIsChanged("T");
        PrimaryNaics primary = new PrimaryNaics();
        primary.setvalue("primary code");
        primary.setIsChanged("T");
        codes.getPrimaryNaicsOrSecondaryNaicsList().add(primary);

        SecondaryNaicsList secondaryList = new SecondaryNaicsList();

        SecondaryNaics second = new SecondaryNaics();
        second.setvalue("second");
        second.setIsChanged("F");
        secondaryList.getSecondaryNaics().add(second);

        SecondaryNaics third = new SecondaryNaics();
        third.setvalue("third");
        third.setIsChanged("T");
        secondaryList.getSecondaryNaics().add(third);

        codes.getPrimaryNaicsOrSecondaryNaicsList().add(secondaryList);
        return codes;
    }

    private CommodityCodeList buildCommodityCodeList() {
        CommodityCodeList commodityCodeList = new CommodityCodeList();

        JaggaerBasicValue code1 = new JaggaerBasicValue("Commodity Code 1");
        code1.setIsChanged("T");
        commodityCodeList.getCommodityCode().add(code1);

        JaggaerBasicValue code2 = new JaggaerBasicValue("Commodity Code 2");
        code2.setIsChanged("F");
        commodityCodeList.getCommodityCode().add(code2);

        return commodityCodeList;
    }

    private CurrencyList buildCurrencyList(boolean includeListChanged, boolean includeUSD, boolean includePeso,
            boolean includeEuro) {
        CurrencyList currencyList = new CurrencyList();

        if (includeListChanged) {
            currencyList.setIsChanged("T");
        }

        if (includeUSD) {
            IsoCurrencyCode usd = new IsoCurrencyCode("usd");
            usd.setIsChanged("T");
            currencyList.getIsoCurrencyCode().add(usd);
        }

        if (includePeso) {
            IsoCurrencyCode peso = new IsoCurrencyCode("peso");
            peso.setIsChanged("F");
            currencyList.getIsoCurrencyCode().add(peso);
        }

        if (includeEuro) {
            currencyList.getIsoCurrencyCode().add(new IsoCurrencyCode("euro"));
        }

        return currencyList;
    }

    private BusinessUnitVendorNumberList buildBusinessUnitVendorNumberList() {
        BusinessUnitVendorNumberList unitNumberList = new BusinessUnitVendorNumberList();
        unitNumberList.setIsChanged("T");

        BusinessUnitVendorNumber unit1 = new BusinessUnitVendorNumber();
        unit1.setBusinessUnitInternalName("vendor number 1");
        unit1.setvalue("1232");
        unit1.setIsChanged("T");
        unitNumberList.getBusinessUnitVendorNumber().add(unit1);

        BusinessUnitVendorNumber unit2 = new BusinessUnitVendorNumber();
        unit2.setBusinessUnitInternalName("vendor number 2");
        unit2.setvalue("56464");
        unit2.setIsChanged("F");
        unitNumberList.getBusinessUnitVendorNumber().add(unit2);

        return unitNumberList;

    }

    private SupplierCapital buildSupplierCapital() {
        SupplierCapital capital = new SupplierCapital();
        Amount ammount = new Amount();
        ammount.setIsChanged("T");
        ammount.setvalue("50.00");
        capital.setAmount(ammount);
        capital.setIsChanged("T");
        IsoCurrencyCode usd = new IsoCurrencyCode("usd");
        usd.setIsChanged("F");
        capital.setIsoCurrencyCode(usd);
        return capital;
    }

    private AnnualSalesList buildAnnualSalesList() {
        AnnualSalesList salesList = new AnnualSalesList();
        salesList.setIsChanged("T");

        AnnualSales sale = new AnnualSales();
        sale.setIsChanged("F");

        IsoCurrencyCode usd = new IsoCurrencyCode("usd");
        usd.setIsChanged("F");
        sale.setIsoCurrencyCode(usd);

        JaggaerBasicValue year = new JaggaerBasicValue();
        year.setIsChanged("T");
        year.setvalue("2023");
        sale.setAnnualSalesYear(year);

        Amount ammount = new Amount();
        ammount.setIsChanged("T");
        ammount.setvalue("6900.00");
        sale.setAnnualSalesAmount(ammount);

        salesList.getAnnualSales().add(sale);

        return salesList;
    }

    private ServiceAreaList buildServiceAreaList() {
        ServiceAreaList areaList = new ServiceAreaList();
        areaList.setIsChanged("T");

        ServiceArea area1 = new ServiceArea();
        area1.setIsChanged("T");
        area1.setServiceAreaInternalName(buildServiceAreaInternalName("internal name"));
        area1.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 1", "internal name 2"));
        areaList.getServiceArea().add(area1);

        ServiceArea area2 = new ServiceArea();
        area2.setIsChanged("F");
        area2.setServiceAreaInternalName(buildServiceAreaInternalName("a different internal name"));
        area2.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 3", "internal name 4"));
        areaList.getServiceArea().add(area2);

        return areaList;
    }

    private ServiceAreaInternalName buildServiceAreaInternalName(String internalName) {
        ServiceAreaInternalName sain = new ServiceAreaInternalName();
        sain.setvalue(internalName);
        sain.setIsChanged("T");
        return sain;
    }

    private StateServiceAreaList buildStateServiceAreaList(String... names) {
        StateServiceAreaList stateServiceAreaList = new StateServiceAreaList();
        stateServiceAreaList.setIsChanged("T");

        for (String name : names) {
            StateServiceAreaInternalName internalName = new StateServiceAreaInternalName();
            internalName.setIsChanged("T");
            internalName.setvalue(name);
            stateServiceAreaList.getStateServiceAreaInternalName().add(internalName);
        }

        return stateServiceAreaList;
    }

    private ParentSupplier buildParentSupplier() {
        ParentSupplier parent = new ParentSupplier();

        ERPNumber erpNumber = new ERPNumber();
        erpNumber.setvalue("parent erp number");
        erpNumber.setIsChanged("F");
        parent.setERPNumber(erpNumber);

        parent.setIsChanged("F");

        SQIntegrationNumber sqIntegrationNumber = new SQIntegrationNumber();
        sqIntegrationNumber.setvalue("parent integration number");
        parent.setSQIntegrationNumber(sqIntegrationNumber);

        return parent;

    }

    private InsuranceInformationList buildInsuranceInformationList() {
        InsuranceInformationList insurance = new InsuranceInformationList();
        insurance.setIsChanged("T");

        InsuranceInformation info = new InsuranceInformation();
        info.setType("auto");
        info.setIsChanged("T");

        JaggaerBasicValue policyNumber = new JaggaerBasicValue("XYZ321");
        policyNumber.setIsChanged("T");
        info.setPolicyNumber(policyNumber);

        JaggaerBasicValue limit = new JaggaerBasicValue("100");
        limit.setIsChanged("T");
        info.setInsuranceLimit(limit);

        JaggaerBasicValue provider = new JaggaerBasicValue("USAA");
        provider.setIsChanged("T");
        info.setInsuranceProvider(provider);

        JaggaerBasicValue agent = new JaggaerBasicValue("Agent Name");
        agent.setIsChanged("T");
        info.setAgent(agent);
        
        JaggaerBasicValue expiration = new JaggaerBasicValue("12/31/2069");
        expiration.setIsChanged("T");
        info.setExpirationDate(expiration);
        

        info.setInsuranceProviderPhone(buildInsuranceProviderPhone());

        info.setInsuranceCertificate(buildInsuranceCertificate());

        JaggaerBasicValue other = new JaggaerBasicValue("some other name");
        other.setIsChanged("T");
        info.setOtherTypeName(other);

        insurance.getInsuranceInformation().add(info);
        return insurance;
    }

    private InsuranceCertificate buildInsuranceCertificate() {
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setIsChanged("T");
        Attachments attachments = new Attachments();
        attachments.setXmlnsXop("test.dtd");
        Attachment attach = new Attachment();
        attach.setAttachmentName("attachment name");
        attach.setAttachmentSize("5000");
        attach.setAttachmentURL("http://www.cornell.edu");
        attach.setId("666");
        attach.setType("HTML");
        XopInclude include = new XopInclude();
        include.setHref("http://www.google.com");
        attach.setXopInclude(include);
        attachments.getAttachment().add(attach);
        certificate.setAttachments(attachments);
        return certificate;
    }

    private InsuranceProviderPhone buildInsuranceProviderPhone() {
        InsuranceProviderPhone phone = new InsuranceProviderPhone();
        phone.setIsChanged("T");
        TelephoneNumber telephoneNumber = new TelephoneNumber();
        telephoneNumber.setIsChanged("T");
        
        CountryCode country = new CountryCode();
        country.setIsChanged("T");
        country.setvalue("USA");
        telephoneNumber.setCountryCode(country);
        
        JaggaerBasicValue area = new JaggaerBasicValue("607");
        area.setIsChanged("T");
        telephoneNumber.setAreaCode(area);
        
        JaggaerBasicValue number = new JaggaerBasicValue("255*9900");
        number.setIsChanged("T");
        telephoneNumber.setNumber(number);
        
        JaggaerBasicValue extension = new JaggaerBasicValue("987");
        extension.setIsChanged("T");
        telephoneNumber.setExtension(extension);
        
        phone.setTelephoneNumber(telephoneNumber);
        return phone;
    }
    
    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        taxList.setIsChanged("T");
        
        TaxInformation info = new TaxInformation();
        info.setIsChanged("T");
        info.setType("information type");
        
        JaggaerBasicValue documentName = new JaggaerBasicValue("document name");
        documentName.setIsChanged("F");
        info.setTaxDocumentName(documentName);;
        
        JaggaerBasicValue year = new JaggaerBasicValue("2023");
        year.setIsChanged("T");
        info.setTaxDocumentYear(year);
        
        TaxDocument document = new TaxDocument();
        document.setIsChanged("Y");
        
        Attachments attachments = new Attachments();
        attachments.setXmlnsXop("taxDocument.dtd");
        Attachment attach = new Attachment();
        attach.setAttachmentName("super cool tax document");
        attach.setAttachmentSize("5000");
        attach.setAttachmentURL("http://www.cornell.edu");
        attach.setId("1000");
        attach.setType("pdf");
        XopInclude include = new XopInclude();
        include.setHref("http://www.google.com");
        attach.setXopInclude(include);
        attachments.getAttachment().add(attach);
        
        document.setAttachments(attachments);
        
        info.setTaxDocument(document);
        
        
        taxList.getTaxInformation().add(info);
        return taxList;
    }
    
    private AccountsPayableList buildAccountsPayableList() {
        AccountsPayableList apList = new AccountsPayableList();
        apList.setIsChanged("T");
        
        AccountsPayable ap = new AccountsPayable();
        ap.setIsChanged("T");
        ap.setType("accounts payable type");
        ap.setOldERPNumber("old erp number");
        
        ERPNumber erpNumber = new ERPNumber();
        erpNumber.setvalue("erp number");
        erpNumber.setIsChanged("F");
        ap.setERPNumber(erpNumber);
        
        SQIntegrationNumber sqIntegrationNumber = new SQIntegrationNumber();
        sqIntegrationNumber.setvalue("sqIntegrationNumber");
        ap.setSQIntegrationNumber(sqIntegrationNumber);
        
        ThirdPartyRefNumber refNumber = new ThirdPartyRefNumber();
        refNumber.setvalue("3rd party ref number");
        refNumber.setIsChanged("T");
        ap.setThirdPartyRefNumber(refNumber);
        
        Name name = new Name();
        name.setvalue("accounts payable name");
        name.setIsChanged("T");
        ap.setName(name);
        
        Active active = new Active();
        active.setvalue("active");
        active.setIsChanged("T");
        ap.setActive(active);
        
        AssociatedAddress address = new AssociatedAddress();
        
        AddressRef ref = new AddressRef();
        address.setIsChanged("T");
        address.setType("type");
        
        ref.setERPNumber(erpNumber);
        ref.setSQIntegrationNumber(sqIntegrationNumber);
        ref.setThirdPartyRefNumber(refNumber);
        address.setAddressRef(ref);
        
        ap.getAssociatedAddress().add(address);
        
        Email email = new Email();
        email.setIsChanged("T");
        email.setvalue("user@cornell.edu");
        ap.setEmail(email);
        
        IsoCurrencyCode usd = new IsoCurrencyCode("usd");
        usd.setIsChanged("T");
        ap.setIsoCurrencyCode(usd);
        
        JaggaerBasicValue contact = new JaggaerBasicValue("contact name");
        contact.setIsChanged("T");
        ap.setContactName(contact);
        
        JaggaerBasicValue purpose = new JaggaerBasicValue("testing is the only purpose");
        purpose.setIsChanged("T");
        ap.setPurpose(purpose);;
        
        JaggaerBasicValue accountid = new JaggaerBasicValue("G234715");
        accountid.setIsChanged("T");
        ap.setAccountId(accountid);
        
        JaggaerBasicValue holderName = new JaggaerBasicValue("John Doe");
        holderName.setIsChanged("T");
        ap.setAccountHolderName(holderName);
        
        JaggaerBasicValue accountType = new JaggaerBasicValue("account type");
        accountType.setIsChanged("T");
        ap.setAccountType(accountType);
        
        CountryCode country = new CountryCode();
        country.setIsChanged("T");
        country.setvalue("USA");
        ap.setCountryCode(country);
        
        BankAccount bankAccount = new BankAccount();
        /*
         * @todo finish this
         */
        ap.setBankAccount(bankAccount);
        
        FlexFields flexFields = new FlexFields();
        
        JaggaerBasicValue field1 = new JaggaerBasicValue("flex field 1");
        field1.setIsChanged("T");
        flexFields.setFlexField1(field1);
        
        JaggaerBasicValue field2 = new JaggaerBasicValue("flex field 2");
        field2.setIsChanged("T");
        flexFields.setFlexField2(field2);
        
        JaggaerBasicValue field3 = new JaggaerBasicValue("flex field 3");
        field3.setIsChanged("T");
        flexFields.setFlexField3(field3);
        
        JaggaerBasicValue field4 = new JaggaerBasicValue("flex field 4");
        field4.setIsChanged("T");
        flexFields.setFlexField4(field4);
        
        JaggaerBasicValue field5 = new JaggaerBasicValue("flex field 5");
        field5.setIsChanged("T");
        flexFields.setFlexField5(field5);
        
        ap.setFlexFields(flexFields);
        
        
        apList.getAccountsPayable().add(ap);
        return apList;
    }

    private void compareXML(Reader control, Reader test) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        
        Diff xmlDiff = new Diff(control, test);
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);
        List<Difference> differences = detailXmlDiff.getAllDifferences();
        for (Difference difference : differences) {
            LOG.info("compareXML, difference: " + difference);
        }
        assertEquals(0, differences.size());
        
    }

}
