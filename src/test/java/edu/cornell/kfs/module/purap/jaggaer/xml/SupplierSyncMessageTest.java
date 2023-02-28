package edu.cornell.kfs.module.purap.jaggaer.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

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
import org.xml.sax.SAXException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

public class SupplierSyncMessageTest {
    private static final String US_DOLLAR_CURRENCY_CODE = "usd";

    private static final String F_FALSE = "F";

    private static final String T_TRUE = "T";

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
    void test() throws JAXBException, IOException, SAXException {
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
        supplier.setIsChanged(T_TRUE);
        supplier.setApprovedForERPSync(T_TRUE);
        supplier.setRequiresERP(T_TRUE);
        supplier.setOldERPNumber("old erp number");
        supplier.setErpNumber(buildERPNumber("erp number", F_FALSE));
        supplier.setName(buildName("Acme Test Company", null));

        supplier.setRestrictFulfillmentLocationsByBusinessUnit(new JaggaerBasicValue("restrict"));
        supplier.getRestrictFulfillmentLocationsByBusinessUnit().setIsChanged(T_TRUE);

        supplier.setSic(new JaggaerBasicValue("SIC"));
        supplier.getSic().setIsChanged(F_FALSE);

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
        repId.setIsChanged(T_TRUE);
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
        supplier.setActive(buildActive());
        supplier.setCountryOfOrigin(new JaggaerBasicValue("USA"));
        supplier.setOtherNames(new JaggaerBasicValue("other name"));
        supplier.setDoingBusinessAs(new JaggaerBasicValue("doing business os"));
        supplier.setThirdPartyRefNumber(buildThirdPartyRefNumber());

        SupplierSQId sqId = new SupplierSQId();
        sqId.setvalue("SO ID");
        supplier.setSupplierSQId(sqId);

        supplier.setSqIntegrationNumber(buildSQIntegrationNumber("sqIntegrationNumber"));

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
        supplier.setCustomElementList(buildCustomElementList());
        supplier.setLocationList(buildLocationList());

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

        return supplier;
    }
    
    private ERPNumber buildERPNumber(String erpNumber, String changed) {
        ERPNumber erp = new ERPNumber();
        erp.setIsChanged(changed);
        erp.setvalue(erpNumber);
        return erp;
    }
    
    private Name buildName(String nameString, String changed) {
        Name name = new Name();
        name.setvalue(nameString);
        name.setIsChanged(changed);
        return name;
    }
    
    private Active buildActive() {
        Active active = new Active();
        active.setvalue("active");
        return active;
    }
    
    private ThirdPartyRefNumber buildThirdPartyRefNumber() {
        ThirdPartyRefNumber refNumber = new ThirdPartyRefNumber();
        refNumber.setvalue("3rd party ref number");
        return refNumber;
    }
    
    private SQIntegrationNumber buildSQIntegrationNumber(String number) {
        SQIntegrationNumber sqIntegrationNumber = new SQIntegrationNumber();
        sqIntegrationNumber.setvalue(number);
        return sqIntegrationNumber;
    }

    private Brands buildBrands() {
        Brands brands = new Brands();

        JaggaerBasicValue brand1 = new JaggaerBasicValue("brand 1");
        brand1.setIsChanged(T_TRUE);
        brands.getBrand().add(brand1);

        JaggaerBasicValue brand2 = new JaggaerBasicValue("brand 2");
        brand2.setIsChanged(F_FALSE);
        brands.getBrand().add(brand2);

        return brands;
    }

    private NaicsCodes buildNaicsCodes() {
        NaicsCodes codes = new NaicsCodes();
        codes.setIsChanged(T_TRUE);
        PrimaryNaics primary = new PrimaryNaics();
        primary.setvalue("primary code");
        primary.setIsChanged(T_TRUE);
        codes.getPrimaryNaicsOrSecondaryNaicsList().add(primary);

        SecondaryNaicsList secondaryList = new SecondaryNaicsList();

        SecondaryNaics second = new SecondaryNaics();
        second.setvalue("second");
        second.setIsChanged(F_FALSE);
        secondaryList.getSecondaryNaics().add(second);

        SecondaryNaics third = new SecondaryNaics();
        third.setvalue("third");
        third.setIsChanged(T_TRUE);
        secondaryList.getSecondaryNaics().add(third);

        codes.getPrimaryNaicsOrSecondaryNaicsList().add(secondaryList);
        return codes;
    }

    private CommodityCodeList buildCommodityCodeList() {
        CommodityCodeList commodityCodeList = new CommodityCodeList();

        JaggaerBasicValue code1 = new JaggaerBasicValue("Commodity Code 1");
        code1.setIsChanged(T_TRUE);
        commodityCodeList.getCommodityCode().add(code1);

        JaggaerBasicValue code2 = new JaggaerBasicValue("Commodity Code 2");
        code2.setIsChanged(F_FALSE);
        commodityCodeList.getCommodityCode().add(code2);

        return commodityCodeList;
    }

    private CurrencyList buildCurrencyList(boolean includeListChanged, boolean includeUSD, boolean includePeso,
            boolean includeEuro) {
        CurrencyList currencyList = new CurrencyList();

        if (includeListChanged) {
            currencyList.setIsChanged(T_TRUE);
        }

        if (includeUSD) {
            currencyList.getIsoCurrencyCode().add(buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, T_TRUE));
        }

        if (includePeso) {
            currencyList.getIsoCurrencyCode().add(buildIsoCurrencyCode("peso", F_FALSE));
        }

        if (includeEuro) {
            currencyList.getIsoCurrencyCode().add(buildIsoCurrencyCode("euro", null));
        }

        return currencyList;
    }

    private BusinessUnitVendorNumberList buildBusinessUnitVendorNumberList() {
        BusinessUnitVendorNumberList unitNumberList = new BusinessUnitVendorNumberList();
        unitNumberList.setIsChanged(T_TRUE);

        BusinessUnitVendorNumber unit1 = new BusinessUnitVendorNumber();
        unit1.setBusinessUnitInternalName("vendor number 1");
        unit1.setvalue("1232");
        unit1.setIsChanged(T_TRUE);
        unitNumberList.getBusinessUnitVendorNumber().add(unit1);

        BusinessUnitVendorNumber unit2 = new BusinessUnitVendorNumber();
        unit2.setBusinessUnitInternalName("vendor number 2");
        unit2.setvalue("56464");
        unit2.setIsChanged(F_FALSE);
        unitNumberList.getBusinessUnitVendorNumber().add(unit2);

        return unitNumberList;

    }

    private SupplierCapital buildSupplierCapital() {
        SupplierCapital capital = new SupplierCapital();
        Amount ammount = new Amount();
        ammount.setIsChanged(T_TRUE);
        ammount.setvalue("50.00");
        capital.setAmount(ammount);
        capital.setIsChanged(T_TRUE);
        capital.setIsoCurrencyCode(buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, F_FALSE));
        return capital;
    }
    
    private IsoCurrencyCode buildIsoCurrencyCode(String currencyCode, String changed) {
        IsoCurrencyCode code = new IsoCurrencyCode(currencyCode);
        code.setIsChanged(changed);
        return code;
    }

    private AnnualSalesList buildAnnualSalesList() {
        AnnualSalesList salesList = new AnnualSalesList();
        salesList.setIsChanged(T_TRUE);

        AnnualSales sale = new AnnualSales();
        sale.setIsChanged(F_FALSE);

        sale.setIsoCurrencyCode(buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, F_FALSE));

        JaggaerBasicValue year = new JaggaerBasicValue();
        year.setIsChanged(T_TRUE);
        year.setvalue("2023");
        sale.setAnnualSalesYear(year);

        Amount ammount = new Amount();
        ammount.setIsChanged(T_TRUE);
        ammount.setvalue("6900.00");
        sale.setAnnualSalesAmount(ammount);

        salesList.getAnnualSales().add(sale);

        return salesList;
    }

    private ServiceAreaList buildServiceAreaList() {
        ServiceAreaList areaList = new ServiceAreaList();
        areaList.setIsChanged(T_TRUE);

        ServiceArea area1 = new ServiceArea();
        area1.setIsChanged(T_TRUE);
        area1.setServiceAreaInternalName(buildServiceAreaInternalName("internal name"));
        area1.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 1", "internal name 2"));
        areaList.getServiceArea().add(area1);

        ServiceArea area2 = new ServiceArea();
        area2.setIsChanged(F_FALSE);
        area2.setServiceAreaInternalName(buildServiceAreaInternalName("a different internal name"));
        area2.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 3", "internal name 4"));
        areaList.getServiceArea().add(area2);

        return areaList;
    }

    private ServiceAreaInternalName buildServiceAreaInternalName(String internalName) {
        ServiceAreaInternalName sain = new ServiceAreaInternalName();
        sain.setvalue(internalName);
        sain.setIsChanged(T_TRUE);
        return sain;
    }

    private StateServiceAreaList buildStateServiceAreaList(String... names) {
        StateServiceAreaList stateServiceAreaList = new StateServiceAreaList();
        stateServiceAreaList.setIsChanged(T_TRUE);

        for (String name : names) {
            StateServiceAreaInternalName internalName = new StateServiceAreaInternalName();
            internalName.setIsChanged(T_TRUE);
            internalName.setvalue(name);
            stateServiceAreaList.getStateServiceAreaInternalName().add(internalName);
        }

        return stateServiceAreaList;
    }

    private ParentSupplier buildParentSupplier() {
        ParentSupplier parent = new ParentSupplier();
        parent.setERPNumber(buildERPNumber("parent erp number", F_FALSE));
        parent.setIsChanged(F_FALSE);
        parent.setSQIntegrationNumber(buildSQIntegrationNumber("parent integration number"));
        return parent;
    }

    private InsuranceInformationList buildInsuranceInformationList() {
        InsuranceInformationList insurance = new InsuranceInformationList();
        insurance.setIsChanged(T_TRUE);

        InsuranceInformation info = new InsuranceInformation();
        info.setType("auto");
        info.setIsChanged(T_TRUE);

        JaggaerBasicValue policyNumber = new JaggaerBasicValue("XYZ321");
        policyNumber.setIsChanged(T_TRUE);
        info.setPolicyNumber(policyNumber);

        JaggaerBasicValue limit = new JaggaerBasicValue("100");
        limit.setIsChanged(T_TRUE);
        info.setInsuranceLimit(limit);

        JaggaerBasicValue provider = new JaggaerBasicValue("USAA");
        provider.setIsChanged(T_TRUE);
        info.setInsuranceProvider(provider);

        JaggaerBasicValue agent = new JaggaerBasicValue("Agent Name");
        agent.setIsChanged(T_TRUE);
        info.setAgent(agent);
        
        JaggaerBasicValue expiration = new JaggaerBasicValue("12/31/2069");
        expiration.setIsChanged(T_TRUE);
        info.setExpirationDate(expiration);
        

        info.setInsuranceProviderPhone(buildInsuranceProviderPhone());

        info.setInsuranceCertificate(buildInsuranceCertificate());

        JaggaerBasicValue other = new JaggaerBasicValue("some other name");
        other.setIsChanged(T_TRUE);
        info.setOtherTypeName(other);

        insurance.getInsuranceInformation().add(info);
        return insurance;
    }

    private InsuranceCertificate buildInsuranceCertificate() {
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setIsChanged(T_TRUE);
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
        phone.setIsChanged(T_TRUE);
        TelephoneNumber telephoneNumber = new TelephoneNumber();
        telephoneNumber.setIsChanged(T_TRUE);
        
        CountryCode country = new CountryCode();
        country.setIsChanged(T_TRUE);
        country.setvalue("USA");
        telephoneNumber.setCountryCode(country);
        
        JaggaerBasicValue area = new JaggaerBasicValue("607");
        area.setIsChanged(T_TRUE);
        telephoneNumber.setAreaCode(area);
        
        JaggaerBasicValue number = new JaggaerBasicValue("255*9900");
        number.setIsChanged(T_TRUE);
        telephoneNumber.setNumber(number);
        
        JaggaerBasicValue extension = new JaggaerBasicValue("987");
        extension.setIsChanged(T_TRUE);
        telephoneNumber.setExtension(extension);
        
        phone.setTelephoneNumber(telephoneNumber);
        return phone;
    }
    
    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        taxList.setIsChanged(T_TRUE);
        
        TaxInformation info = new TaxInformation();
        info.setIsChanged(T_TRUE);
        info.setType("information type");
        
        JaggaerBasicValue documentName = new JaggaerBasicValue("document name");
        documentName.setIsChanged(F_FALSE);
        info.setTaxDocumentName(documentName);;
        
        JaggaerBasicValue year = new JaggaerBasicValue("2023");
        year.setIsChanged(T_TRUE);
        info.setTaxDocumentYear(year);
        
        TaxDocument document = new TaxDocument();
        document.setIsChanged(T_TRUE);
        
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
        apList.setIsChanged(T_TRUE);
        
        AccountsPayable ap = new AccountsPayable();
        ap.setIsChanged(T_TRUE);
        ap.setType("accounts payable type");
        ap.setOldERPNumber("old erp number");
        
        ap.setERPNumber(buildERPNumber("erp number", F_FALSE));
        
        ap.setSQIntegrationNumber(buildSQIntegrationNumber("sqIntegrationNumber"));
        
        ap.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        
        ap.setName(buildName("accounts payable name", T_TRUE));
        
        ap.setActive(buildActive());
        
        AssociatedAddress address = new AssociatedAddress();
        
        AddressRef ref = new AddressRef();
        address.setIsChanged(T_TRUE);
        address.setType("type");
        
        ref.setERPNumber(ap.getERPNumber());
        ref.setSQIntegrationNumber(ap.getSQIntegrationNumber());
        ref.setThirdPartyRefNumber(ap.getThirdPartyRefNumber());
        address.setAddressRef(ref);
        
        ap.getAssociatedAddress().add(address);
        
        Email email = new Email();
        email.setIsChanged(T_TRUE);
        email.setvalue("user@cornell.edu");
        ap.setEmail(email);
        
        ap.setIsoCurrencyCode(buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, T_TRUE));
        
        JaggaerBasicValue contact = new JaggaerBasicValue("contact name");
        contact.setIsChanged(T_TRUE);
        ap.setContactName(contact);
        
        JaggaerBasicValue purpose = new JaggaerBasicValue("testing is the only purpose");
        purpose.setIsChanged(T_TRUE);
        ap.setPurpose(purpose);;
        
        JaggaerBasicValue accountid = new JaggaerBasicValue("G234715");
        accountid.setIsChanged(T_TRUE);
        ap.setAccountId(accountid);
        
        JaggaerBasicValue holderName = new JaggaerBasicValue("John Doe");
        holderName.setIsChanged(T_TRUE);
        ap.setAccountHolderName(holderName);
        
        JaggaerBasicValue accountType = new JaggaerBasicValue("account type");
        accountType.setIsChanged(T_TRUE);
        ap.setAccountType(accountType);
        
        CountryCode country = new CountryCode();
        country.setIsChanged(T_TRUE);
        country.setvalue("USA");
        ap.setCountryCode(country);
        
        ap.setBankAccount(buildBankAccount());
        ap.setFlexFields(buildFlexFields());
        
        
        apList.getAccountsPayable().add(ap);
        return apList;
    }
    
    private BankAccount buildBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setType("bank account type");
        bankAccount.setIsChanged(T_TRUE);
        
        JaggaerBasicValue bankName = new JaggaerBasicValue("bank name");
        bankName.setIsChanged(T_TRUE);
        bankAccount.setBankName(bankName);
        
        JaggaerBasicValue holder = new JaggaerBasicValue("Jane Doe");
        holder.setIsChanged(T_TRUE);
        bankAccount.setAccountHoldersName(holder);
        
        JaggaerBasicValue accountNumberType = new JaggaerBasicValue("account number type");
        accountNumberType.setIsChanged(T_TRUE);
        bankAccount.setAccountNumberType(accountNumberType);
        
        JaggaerBasicValue routing = new JaggaerBasicValue("routing number");
        routing.setIsChanged(T_TRUE);
        bankAccount.setRoutingNumber(routing);
        
        JaggaerBasicValue accountNumber = new JaggaerBasicValue("bank account number");
        accountNumber.setIsChanged(T_TRUE);
        bankAccount.setBankAccountNumber(accountNumber);
        
        JaggaerBasicValue iban = new JaggaerBasicValue("iban number");
        iban.setIsChanged(T_TRUE);
        bankAccount.setIbanBankAccountNumber(iban);
        
        JaggaerBasicValue depositFormat = new JaggaerBasicValue("Deposit Format");
        depositFormat.setIsChanged(T_TRUE);
        bankAccount.setDirectDepositFormat(depositFormat);
        
        JaggaerBasicValue code = new JaggaerBasicValue("bank identifier code");
        code.setIsChanged(T_TRUE);
        bankAccount.setBankIdentifierCode(code);
        
        JaggaerBasicValue internalRouting = new JaggaerBasicValue("international routing");
        internalRouting.setIsChanged(T_TRUE);
        bankAccount.setInternationalRoutingCode(internalRouting);
        
        IsoCountryCode countryCode = new IsoCountryCode();
        countryCode.setIsChanged(T_TRUE);
        countryCode.setvalue("USA");
        bankAccount.setIsoCountryCode(countryCode);
        
        AddressLine1 line1 = new AddressLine1();
        line1.setIsChanged(T_TRUE);
        line1.setvalue("Address Line 1");
        bankAccount.setAddressLine1(line1);
        
        AddressLine2 line2 = new AddressLine2();
        line2.setIsChanged(T_TRUE);
        line2.setvalue("Address Line 2");
        bankAccount.setAddressLine2(line2);
        
        AddressLine3 line3 = new AddressLine3();
        line3.setIsChanged(T_TRUE);
        line3.setvalue("Address Line 3");
        bankAccount.setAddressLine3(line3);
        
        City city = new City();
        city.setIsChanged(T_TRUE);
        city.setvalue("Ithaca");
        bankAccount.setCity(city);
        
        State state = new State();
        state.setIsChanged(T_TRUE);
        state.setvalue("NY");
        bankAccount.setState(state);
        
        PostalCode postalCode = new PostalCode();
        postalCode.setIsChanged(T_TRUE);
        postalCode.setvalue("14850");
        bankAccount.setPostalCode(postalCode);
        
        
        return bankAccount;
    }

    private FlexFields buildFlexFields() {
        FlexFields flexFields = new FlexFields();
        
        JaggaerBasicValue field1 = new JaggaerBasicValue("flex field 1");
        field1.setIsChanged(T_TRUE);
        flexFields.setFlexField1(field1);
        
        JaggaerBasicValue field2 = new JaggaerBasicValue("flex field 2");
        field2.setIsChanged(T_TRUE);
        flexFields.setFlexField2(field2);
        
        JaggaerBasicValue field3 = new JaggaerBasicValue("flex field 3");
        field3.setIsChanged(T_TRUE);
        flexFields.setFlexField3(field3);
        
        JaggaerBasicValue field4 = new JaggaerBasicValue("flex field 4");
        field4.setIsChanged(T_TRUE);
        flexFields.setFlexField4(field4);
        
        JaggaerBasicValue field5 = new JaggaerBasicValue("flex field 5");
        field5.setIsChanged(T_TRUE);
        flexFields.setFlexField5(field5);
        return flexFields;
    }
    
    private CustomElementList buildCustomElementList() {
        CustomElementList customList = new CustomElementList();
        customList.setIsChanged(T_TRUE);
        
        CustomElement element1 = new CustomElement();
        element1.setIsActive(F_FALSE);
        element1.setIsChanged(T_TRUE);
        element1.setType("custom type");
        
        JaggaerBasicValue identifer1 = new JaggaerBasicValue("a custom identifer");
        identifer1.setIsChanged(T_TRUE);
        element1.setCustomElementIdentifier(identifer1);
        
        DisplayName name1 = new DisplayName();
        name1.setIsChanged(T_TRUE);
        name1.setvalue("a cool display name");
        element1.setDisplayName(name1);
        
        customList.getCustomElement().add(element1);
        
        CustomElement element2 = new CustomElement();
        element2.setIsActive(T_TRUE);
        element2.setIsChanged(F_FALSE);
        element2.setType("custom type2");
        
        JaggaerBasicValue identifer2 = new JaggaerBasicValue("a  different custom identifer");
        identifer2.setIsChanged(T_TRUE);
        element2.setCustomElementIdentifier(identifer2);
        
        DisplayName name2 = new DisplayName();
        name2.setIsChanged(T_TRUE);
        name2.setvalue("a lame display name");
        element2.setDisplayName(name2);
        
        customList.getCustomElement().add(element2);
        
        return customList;
    }
    
    private LocationList buildLocationList() {
        LocationList locationList = new LocationList();
        
        Location location = new Location();
        location.setIsChanged(T_TRUE);
        location.setSupportsOrderFulfillment("Order Fulfillment(");
        location.setOldERPNumber("old erp number");
        
        location.setERPNumber(buildERPNumber("erp number", T_TRUE));
        
        location.setSQIntegrationNumber(buildSQIntegrationNumber("sqIntegrationNumber"));
        
        location.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        
        location.setName(buildName("silly location name", T_TRUE));
        
        Description description = new Description();
        description.setIsChanged(T_TRUE);
        description.setvalue("description value");
        location.setDescription(description);
        
        location.setActive(buildActive());
        
        LocationActive locationActive = new LocationActive();
        locationActive.setIsChanged(T_TRUE);
        locationActive.setvalue("location is active");
        location.setLocationActive(locationActive);
        
        Primary primary = new Primary();
        primary.setIsChanged(T_TRUE);
        primary.setvalue("primary");
        location.setPrimary(primary);
        
        PrefPurchaseOrderDeliveryMethod method = new PrefPurchaseOrderDeliveryMethod();
        method.setIsChanged(T_TRUE);
        method.setType("delivery method");
        location.setPrefPurchaseOrderDeliveryMethod(method);
        
        LocationEffectiveDate effectiveDate = new LocationEffectiveDate();
        effectiveDate.setIsChanged(T_TRUE);
        effectiveDate.setvalue("02/28/2023");
        location.setLocationEffectiveDate(effectiveDate);
        
        PaymentMethod paymentMethod = buildPaymentMethod();
        
        location.setPaymentMethod(paymentMethod);
        
        //locationList.getLocation().add(location);
        return locationList;
    }

    private PaymentMethod buildPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType("payment type");
        paymentMethod.setIsChanged(T_TRUE);
        
        POPaymentMethod poMethod = new POPaymentMethod();
        poMethod.setIsChanged(T_TRUE);
        
        POPayment poPayment = new POPayment();
        poMethod.setPOPayment(poPayment);
        
        paymentMethod.setPOPaymentMethod(poMethod);
        
        return paymentMethod;
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
