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
import liquibase.pro.packaged.F;
import liquibase.pro.packaged.T;

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

        compareXML(expectedBufferedReader, actualBufferedReader);

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

        supplier.setRestrictFulfillmentLocationsByBusinessUnit(new JaggaerBasicValue("restrict", T_TRUE));
        supplier.setSic(new JaggaerBasicValue("SIC", F_FALSE));
        supplier.setSupplierKeywords(new JaggaerBasicValue("keyword", null));
        supplier.setEnablePaymentProvisioning(new JaggaerBasicValue("prov", null));
        supplier.setAustinTetra(new JaggaerBasicValue("austin", null));
        supplier.setShoppingCommodityCode(new JaggaerBasicValue("commodity", null));
        supplier.setVatExempt(new JaggaerBasicValue("VAT", null));
        supplier.setVatIdentificationNumber(new JaggaerBasicValue("VAT ID", null));
        supplier.setSupplierShareholders(new JaggaerBasicValue("holders", null));
        supplier.setSupplierRegNumber(new JaggaerBasicValue("reg number", null));
        supplier.setSupplierRegSeat(new JaggaerBasicValue("regular seat", null));
        supplier.setSupplierRegCourt(new JaggaerBasicValue("regular court", null));
        supplier.setSupplierTaxRepresentativeId(new JaggaerBasicValue("tax rep ID", T_TRUE));
        supplier.setRegistrationProfileStatus(new JaggaerBasicValue("profile status", null));
        supplier.setRegistrationProfileType(new JaggaerBasicValue("profile tyoe", null));
        supplier.setYearEstablished(new JaggaerBasicValue("1977", null));
        supplier.setNumberOfEmployees(new JaggaerBasicValue("69", null));
        supplier.setExemptFromBackupWithholding(new JaggaerBasicValue("back holding", null));
        supplier.setTaxIdentificationNumber(new JaggaerBasicValue("tax id", null));
        supplier.setTaxIdentificationType(new JaggaerBasicValue("tax type", null));
        supplier.setLegalStructure(new JaggaerBasicValue("legal structure", null));

        DUNS duns = new DUNS();
        duns.setvalue("duns");
        supplier.setDuns(duns);

        supplier.setWebSiteURL(new JaggaerBasicValue("www.cornell.edu", null));
        supplier.setActive(buildActive());
        supplier.setCountryOfOrigin(new JaggaerBasicValue("USA", null));
        supplier.setOtherNames(new JaggaerBasicValue("other name", null));
        supplier.setDoingBusinessAs(new JaggaerBasicValue("doing business os", null));
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
        supplier.setDiversityClassificationList(buildDiversityClassificationList());
        supplier.setClassificationList(buildClassificationList());
        supplier.setPrimaryContactList(buildPrimaryContactList());
        supplier.setContactList(buildContactList());
        supplier.setPrimaryAddressList(buildPrimaryAddressList());
        supplier.setAddressList(buildAddressList());

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
        brands.getBrand().add(new JaggaerBasicValue("brand 1", T_TRUE));
        brands.getBrand().add(new JaggaerBasicValue("brand 2", F_FALSE));
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
        commodityCodeList.getCommodityCode().add(new JaggaerBasicValue("Commodity Code 1", T_TRUE));
        commodityCodeList.getCommodityCode().add(new JaggaerBasicValue("Commodity Code 2", F_FALSE));
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

        info.setPolicyNumber(new JaggaerBasicValue("XYZ321", T_TRUE));
        info.setInsuranceLimit(new JaggaerBasicValue("100", T_TRUE));
        info.setInsuranceProvider(new JaggaerBasicValue("USAA", T_TRUE));
        info.setAgent(new JaggaerBasicValue("Agent Name", T_TRUE));
        info.setExpirationDate(new JaggaerBasicValue("12/31/2069", T_TRUE));
        info.setInsuranceProviderPhone(buildInsuranceProviderPhone());
        info.setInsuranceCertificate(buildInsuranceCertificate());
        info.setOtherTypeName(new JaggaerBasicValue("some other name", T_TRUE));
        insurance.getInsuranceInformation().add(info);
        return insurance;
    }

    private InsuranceCertificate buildInsuranceCertificate() {
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setIsChanged(T_TRUE);
        certificate.setAttachments(buildAttachments("attachment name"));
        return certificate;
    }

    private InsuranceProviderPhone buildInsuranceProviderPhone() {
        InsuranceProviderPhone phone = new InsuranceProviderPhone();
        phone.setIsChanged(T_TRUE);
        phone.setTelephoneNumber(buildBasicTelephoneNumber());
        return phone;
    }

    private TelephoneNumber buildBasicTelephoneNumber() {
        TelephoneNumber telephoneNumber = new TelephoneNumber();
        telephoneNumber.setIsChanged(T_TRUE);
        
        CountryCode country = new CountryCode();
        country.setIsChanged(T_TRUE);
        country.setvalue("USA");
        telephoneNumber.setCountryCode(country);

        telephoneNumber.setAreaCode(new JaggaerBasicValue("607", T_TRUE));
        telephoneNumber.setNumber(new JaggaerBasicValue("255*9900", T_TRUE));
        telephoneNumber.setExtension(new JaggaerBasicValue("987", T_TRUE));
        return telephoneNumber;
    }
    
    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        taxList.setIsChanged(T_TRUE);
        
        TaxInformation info = new TaxInformation();
        info.setIsChanged(T_TRUE);
        info.setType("information type");
        
        info.setTaxDocumentName(new JaggaerBasicValue("document name", F_FALSE));;
        info.setTaxDocumentYear(new JaggaerBasicValue("2023", T_TRUE));
        
        TaxDocument document = new TaxDocument();
        document.setIsChanged(T_TRUE);
        
        document.setAttachments(buildAttachments("super cool tax document"));
        
        info.setTaxDocument(document);
        
        taxList.getTaxInformation().add(info);
        return taxList;
    }
    
    private Attachments buildAttachments(String attachmentName) {
        Attachments attachments = new Attachments();
        attachments.setXmlnsXop("test.dtd");
        Attachment attach = new Attachment();
        attach.setAttachmentName(attachmentName);
        attach.setAttachmentSize("5000");
        attach.setAttachmentURL("http://www.cornell.edu");
        attach.setId("1000");
        attach.setType("pdf");
        XopInclude include = new XopInclude();
        include.setHref("http://www.google.com");
        attach.setXopInclude(include);
        attachments.getAttachment().add(attach);
        return attachments;
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
        
        ap.setEmail(buildEmail("user@cornell.edu", T_TRUE));
        
        ap.setIsoCurrencyCode(buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, T_TRUE));
        ap.setContactName(new JaggaerBasicValue("contact name", T_TRUE));
        ap.setPurpose(new JaggaerBasicValue("testing is the only purpose", T_TRUE));;
        ap.setAccountId(new JaggaerBasicValue("G234715", T_TRUE));
        ap.setAccountHolderName(new JaggaerBasicValue("John Doe", T_TRUE));
        
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
        
        bankAccount.setAddressLine1(new AddressLine("Address Line 1", T_TRUE));
        bankAccount.setAddressLine2(new AddressLine("Address Line 2", T_TRUE));
        bankAccount.setAddressLine3(new AddressLine("Address Line 3", T_TRUE));
        
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
        
        element1.setDisplayName(buildDisplayName("a cool display name", T_TRUE));
        
        customList.getCustomElement().add(element1);
        
        CustomElement element2 = new CustomElement();
        element2.setIsActive(T_TRUE);
        element2.setIsChanged(F_FALSE);
        element2.setType("custom type2");
        
        JaggaerBasicValue identifer2 = new JaggaerBasicValue("a  different custom identifer");
        identifer2.setIsChanged(T_TRUE);
        element2.setCustomElementIdentifier(identifer2);
        
        element2.setDisplayName(buildDisplayName("a lame display name", T_TRUE));
        
        customList.getCustomElement().add(element2);
        
        return customList;
    }
    
    private DisplayName buildDisplayName(String name, String changed) {
        DisplayName displayName = new DisplayName();
        displayName.setIsChanged(changed);
        displayName.setvalue(name);
        return displayName;
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
        
        JaggaerBasicValue description = new JaggaerBasicValue("description value");
        description.setIsChanged(T_TRUE);
        location.setDescription(description);
        
        location.setActive(buildActive());
        
        JaggaerBasicValue locationActive = new JaggaerBasicValue("location is active");
        locationActive.setIsChanged(T_TRUE);
        location.setLocationActive(locationActive);
        
        JaggaerBasicValue primary = new JaggaerBasicValue("primary");
        primary.setIsChanged(T_TRUE);
        location.setPrimary(primary);
        
        location.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod());
        
        LocationEffectiveDate effectiveDate = new LocationEffectiveDate();
        effectiveDate.setIsChanged(T_TRUE);
        effectiveDate.setvalue("02/28/2023");
        location.setLocationEffectiveDate(effectiveDate);
        
        location.setPaymentMethod(buildPaymentMethod());
        
        locationList.getLocation().add(location);
        return locationList;
    }

    private PrefPurchaseOrderDeliveryMethod buildPrefPurchaseOrderDeliveryMethod() {
        PrefPurchaseOrderDeliveryMethod method = new PrefPurchaseOrderDeliveryMethod();
        method.setIsChanged(T_TRUE);
        method.setType("delivery method");
        method.getEmailOrFax().add(buildEmail("foo@bar.com", T_TRUE));
        Fax fax = new Fax();
        fax.setIsChanged(F_FALSE);
        fax.setTelephoneNumber(buildBasicTelephoneNumber());
        method.getEmailOrFax().add(fax);
        return method;
    }
    
    private Email buildEmail(String emailAddress, String changed) {
        Email email = new Email();
        email.setIsChanged(changed);
        email.setvalue(emailAddress);
        return email;
    }

    private PaymentMethod buildPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType("payment type");
        paymentMethod.setIsChanged(T_TRUE);
        paymentMethod.setPOPaymentMethod(buildPOPaymentMethod());
        
        BlanketPOPaymentMethod blanketMethod = new BlanketPOPaymentMethod();
        blanketMethod.setIsChanged(T_TRUE);
        
        JaggaerBasicValue blanketNumber = new JaggaerBasicValue("blanket number");
        blanketNumber.setIsChanged(T_TRUE);
        blanketMethod.setBlanketPONumber(blanketNumber);
        
        paymentMethod.setBlanketPOPaymentMethod(blanketMethod);
        
        return paymentMethod;
    }

    private POPaymentMethod buildPOPaymentMethod() {
        POPaymentMethod poPaymentMethod = new POPaymentMethod();
        poPaymentMethod.setIsChanged(T_TRUE);
        
        POPayment poPayment = new POPayment();
        poPayment.setIsChanged(T_TRUE);
        poPayment.setActive(buildActive());
        
        PONumberSelection selection = new PONumberSelection();
        selection.setIsChanged(T_TRUE);
        selection.setType("selection type");
        
        JaggaerBasicValue wheel = new JaggaerBasicValue("wheel");
        wheel.setIsChanged(T_TRUE);
        selection.setNumberWheel(wheel);
        
        poPayment.setPONumberSelection(selection);
        
        JaggaerBasicValue form = new JaggaerBasicValue("allow free form");
        form.setIsChanged(T_TRUE);
        poPayment.setAllowFreeForm(form);
        
        
        poPaymentMethod.setPOPayment(poPayment);
        
        return poPaymentMethod;
    }
    
    private DiversityClassificationList buildDiversityClassificationList() {
        DiversityClassificationList diversityList = new DiversityClassificationList();
        diversityList.setIsChanged(T_TRUE);
        
        DiversityClassification diversity = new DiversityClassification();
        diversity.setIsChanged(T_TRUE);
        
        JaggaerBasicValue name = new JaggaerBasicValue("internal name");
        name.setIsChanged(T_TRUE);
        diversity.setInternalName(name);;
        
        diversity.setDisplayName(buildDisplayName("display name", F_FALSE));
        
        DD214Certificate ddCertificate = new DD214Certificate();
        ddCertificate.setIsChanged(T_TRUE);
        ddCertificate.setAttachments(buildAttachments("DD 214 Certificate Attachments"));
        diversity.setDD214Certificate(ddCertificate);
        
        DiversityCertificate certificate = new DiversityCertificate();
        certificate.setIsChanged(T_TRUE);
        certificate.setAttachments(buildAttachments("diversity certificate attachment"));
        diversity.setDiversityCertificate(certificate);
        
        AdditionalDataList dataList = new AdditionalDataList();
        dataList.setIsChanged(T_TRUE);
        
        AdditionalData datum = new AdditionalData();
        datum.setIsChanged(T_TRUE);
        datum.setName("datum name");
        datum.getContent().add("some additional piece of information");
        dataList.getAdditionalData().add(datum);
        
        diversity.setAdditionalDataList(dataList);
        
        diversityList.getDiversityClassification().add(diversity);
        return diversityList;
    }
    
    private ClassificationList buildClassificationList() {
        ClassificationList classificationList = new ClassificationList();
        classificationList.setIsChanged(T_TRUE);
        
        Classification classification1 = new Classification();
        classification1.setIsChanged(T_TRUE);
        classification1.setDisplayName(buildDisplayName("classification 1 name", T_TRUE));
        
        JaggaerBasicValue name1 = new JaggaerBasicValue("internal name for classification 1");
        name1.setIsChanged(T_TRUE);
        classification1.setInternalName(name1);
        classificationList.getClassification().add(classification1);
        
        Classification classification2 = new Classification();
        classification2.setIsChanged(F_FALSE);
        classification2.setDisplayName(buildDisplayName("classification 2 name", F_FALSE));
        
        JaggaerBasicValue name2 = new JaggaerBasicValue("internal name for classification 2");
        name2.setIsChanged(F_FALSE);
        classification2.setInternalName(name2);
        classificationList.getClassification().add(classification2);
        
        return classificationList;
    }
    
    private PrimaryContactList buildPrimaryContactList() {
        PrimaryContactList primaryContactList = new PrimaryContactList();
        primaryContactList.setIsChanged(T_TRUE);
        
        AssociatedContact contact = new AssociatedContact();
        contact.setIsChanged(T_TRUE);
        contact.setType("contact type");
        
        ContactRef ref = new ContactRef();
        ref.setERPNumber(buildERPNumber("erp number", T_TRUE));
        ref.setSQIntegrationNumber(buildSQIntegrationNumber("SQ number"));
        ref.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        contact.setContactRef(ref);
        primaryContactList.getAssociatedContact().add(contact);
        
        return primaryContactList;
    }
    
    private ContactList buildContactList() {
        ContactList contactList = new ContactList();
        contactList.setIsChanged(T_TRUE);
        
        Contact contact = new Contact();
        contact.setIsChanged(T_TRUE);
        contact.setType("contact type");
        contact.setERPNumber(buildERPNumber("erp number", T_TRUE));
        contact.setOldERPNumber("old erp number");
        contact.setSQIntegrationNumber(buildSQIntegrationNumber("sq integration number"));
        contact.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        contact.setName(buildName("contact name", T_TRUE));
        contact.setActive(buildActive());
        
        JaggaerBasicValue firstName = new JaggaerBasicValue("Jane");
        firstName.setIsChanged(T_TRUE);
        contact.setFirstName(firstName);
        
        JaggaerBasicValue lastName = new JaggaerBasicValue("Doe");
        lastName.setIsChanged(T_TRUE);
        contact.setLastName(lastName);
        
        JaggaerBasicValue title = new JaggaerBasicValue("cool title");
        title.setIsChanged(T_TRUE);
        contact.setTitle(title);
        
        contact.setEmail(buildEmail("test@foo.com", T_TRUE));
        
        Phone phone = new Phone();
        phone.setIsChanged(T_TRUE);
        phone.setTelephoneNumber(buildBasicTelephoneNumber());
        contact.setPhone(phone);
        
        MobilePhone mobile = new MobilePhone();
        mobile.setIsChanged(F_FALSE);
        mobile.setTelephoneNumber(buildBasicTelephoneNumber());
        contact.setMobilePhone(mobile);
        
        TollFreePhone tollFree = new TollFreePhone();
        tollFree.setIsChanged(T_TRUE);
        tollFree.setTelephoneNumber(buildBasicTelephoneNumber());
        contact.setTollFreePhone(tollFree);
        
        Fax fax = new Fax();
        fax.setIsChanged(T_TRUE);
        fax.setTelephoneNumber(buildBasicTelephoneNumber());
        contact.setFax(fax);
        
        Notes notes = new Notes();
        notes.setIsChanged(T_TRUE);
        notes.setvalue("just a simple note");
        contact.setNotes(notes);
        
        contactList.getContact().add(contact);
        return contactList;
    }
    
    private PrimaryAddressList buildPrimaryAddressList() {
        PrimaryAddressList addressList = new PrimaryAddressList();
        addressList.setIsChanged(T_TRUE);
        
        AssociatedAddress address1 = new AssociatedAddress();
        address1.setIsChanged(T_TRUE);
        address1.setType("adddress type");
        
        AddressRef ref = new AddressRef();
        ref.setERPNumber(buildERPNumber("erp number", T_TRUE));
        ref.setSQIntegrationNumber(buildSQIntegrationNumber("sq integration number"));
        ref.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        address1.setAddressRef(ref);
        
        addressList.getAssociatedAddress().add(address1);        
        return addressList;
    }
    
    private AddressList buildAddressList() {
        AddressList addressList = new AddressList();
        addressList.setIsChanged(T_TRUE);        
        
        Address address = new Address();
        address.setIsChanged(T_TRUE);
        address.setType("home address");
        address.setERPNumber(buildERPNumber("erp number", T_TRUE));
        address.setOldERPNumber("old erp number");
        address.setSQIntegrationNumber(buildSQIntegrationNumber("sq integration number"));
        address.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        address.setName(buildName("address name", T_TRUE));
        address.setActive(buildActive());
        address.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod());
        
        address.setAddressLine1(new AddressLine("line 1", T_TRUE));
        address.setAddressLine2(new AddressLine("line 2", T_TRUE));
        address.setAddressLine3(new AddressLine("line 3", T_TRUE));
        
        City city = new City();
        city.setIsChanged(T_TRUE);
        city.setvalue("Ithaca");
        address.setCity(city);
        
        State state = new State();
        state.setIsChanged(T_TRUE);
        state.setvalue("NY");
        address.setState(state);
        
        PostalCode postal = new PostalCode();
        postal.setIsChanged(T_TRUE);
        postal.setvalue("14850");
        address.setPostalCode(postal);
        
        IsoCountryCode country = new IsoCountryCode();
        country.setIsChanged(T_TRUE);
        country.setvalue("USA");
        address.setIsoCountryCode(country);
        
        Phone phone = new Phone();
        phone.setIsChanged(T_TRUE);
        phone.setTelephoneNumber(buildBasicTelephoneNumber());
        address.setPhone(phone);
        
        TollFreePhone tollFree = new TollFreePhone();
        tollFree.setIsChanged(T_TRUE);
        tollFree.setTelephoneNumber(buildBasicTelephoneNumber());
        address.setTollFreePhone(tollFree);
        
        Fax fax = new Fax();
        fax.setIsChanged(T_TRUE);
        fax.setTelephoneNumber(buildBasicTelephoneNumber());
        address.setFax(fax);
        
        Notes notes = new Notes();
        notes.setIsChanged(T_TRUE);
        notes.setvalue("just a simple note");
        address.setNotes(notes);
        
        AssignedBusinessUnitsList businessList = new AssignedBusinessUnitsList();
        businessList.setIsChanged(T_TRUE);
        
        BusinessUnitInternalName internalName = new BusinessUnitInternalName();
        internalName.setIsChanged(T_TRUE);
        internalName.setPreferredForThisBusinessUnit("is preferred");
        internalName.setvalue("testing name");
        
        businessList.getBusinessUnitInternalName().add(internalName);
        
        address.setAssignedBusinessUnitsList(businessList);
        addressList.getAddress().add(address);
        
        return addressList;
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
