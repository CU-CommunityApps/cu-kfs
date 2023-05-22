package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

public class SupplierSyncMessageTest {
    private static final String US_DOLLAR_CURRENCY_CODE = "usd";

    private static final String F_FALSE = "F";

    private static final String T_TRUE = "T";

    private static final Logger LOG = LogManager.getLogger();

    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    private static final String OUTPUT_FILE_PATH = INPUT_FILE_PATH + "outputtemp/";
    private static final String BASIC_FILE_EXAMPLE = "SupplierSyncMessageBasic.xml";

    private File outputFileDirectory;

    private CUMarshalService marshalService;

    @BeforeEach
    void setUpBeforeClass() throws Exception {
        marshalService = new CUMarshalServiceImpl();
        outputFileDirectory = new File(OUTPUT_FILE_PATH);
        outputFileDirectory.mkdir();
    }

    @AfterEach
    void tearDownAfterClass() throws Exception {
        marshalService = null;
        FileUtils.deleteDirectory(outputFileDirectory);
    }

    @Test
    void testSupplierSyncMessage() throws JAXBException, IOException, SAXException  {
        File expectedXmlFile = new File(INPUT_FILE_PATH + BASIC_FILE_EXAMPLE);

        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        supplierSyncMessage.setVersion("1.0");
        supplierSyncMessage.setHeader(buildHeader());

        SupplierRequestMessage srm = new SupplierRequestMessage();
        srm.getSupplier().add(buildSupplier());
        supplierSyncMessage.getSupplierRequestMessageItems().add(srm);

        logActualXmlIfNeeded(supplierSyncMessage);
        File actualXmlFile = marshalService.marshalObjectToXML(supplierSyncMessage, OUTPUT_FILE_PATH + "test.xml");
        CuXMLUnitTestUtils.compareXML(expectedXmlFile, actualXmlFile);
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
        supplier.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", F_FALSE));
        supplier.setName(JaggaerBuilder.buildName("Acme Test Company", null));

        supplier.setRestrictFulfillmentLocationsByBusinessUnit(JaggaerBuilder.buildJaggaerBasicValue("restrict", T_TRUE));
        supplier.setSic(JaggaerBuilder.buildJaggaerBasicValue("SIC", F_FALSE));
        supplier.setSupplierKeywords(JaggaerBuilder.buildJaggaerBasicValue("keyword"));
        supplier.setEnablePaymentProvisioning(JaggaerBuilder.buildJaggaerBasicValue("prov"));
        supplier.setAustinTetra(JaggaerBuilder.buildJaggaerBasicValue("austin"));
        supplier.setShoppingCommodityCode(JaggaerBuilder.buildJaggaerBasicValue("commodity"));
        supplier.setVatExempt(JaggaerBuilder.buildJaggaerBasicValue("VAT", null));
        supplier.setVatIdentificationNumber(JaggaerBuilder.buildJaggaerBasicValue("VAT ID"));
        supplier.setSupplierShareholders(JaggaerBuilder.buildJaggaerBasicValue("holders"));
        supplier.setSupplierRegNumber(JaggaerBuilder.buildJaggaerBasicValue("reg number"));
        supplier.setSupplierRegSeat(JaggaerBuilder.buildJaggaerBasicValue("regular seat"));
        supplier.setSupplierRegCourt(JaggaerBuilder.buildJaggaerBasicValue("regular court"));
        supplier.setSupplierTaxRepresentativeId(JaggaerBuilder.buildJaggaerBasicValue("tax rep ID", T_TRUE));
        supplier.setRegistrationProfileStatus(JaggaerBuilder.buildJaggaerBasicValue("profile status"));
        supplier.setRegistrationProfileType(JaggaerBuilder.buildJaggaerBasicValue("profile tyoe"));
        supplier.setYearEstablished(JaggaerBuilder.buildJaggaerBasicValue("1977"));
        supplier.setNumberOfEmployees(JaggaerBuilder.buildJaggaerBasicValue("69"));
        supplier.setExemptFromBackupWithholding(JaggaerBuilder.buildJaggaerBasicValue("back holding"));
        supplier.setTaxIdentificationNumber(JaggaerBuilder.buildJaggaerBasicValue("tax id"));
        supplier.setTaxIdentificationType(JaggaerBuilder.buildJaggaerBasicValue("tax type"));
        supplier.setLegalStructure(JaggaerBuilder.buildJaggaerBasicValue("legal structure"));
        supplier.setDuns(JaggaerBuilder.buildJaggaerBasicValue("duns"));
        supplier.setWebSiteURL(JaggaerBuilder.buildJaggaerBasicValue("www.cornell.edu", null));
        supplier.setActive(buildActive());
        supplier.setCountryOfOrigin(JaggaerBuilder.buildJaggaerBasicValue("USA", null));
        supplier.setOtherNames(JaggaerBuilder.buildJaggaerBasicValue("other name", null));
        supplier.setDoingBusinessAs(JaggaerBuilder.buildJaggaerBasicValue("doing business os", null));
        supplier.setThirdPartyRefNumber(buildThirdPartyRefNumber());

        SupplierSQId sqId = new SupplierSQId();
        sqId.setValue("SO ID");
        supplier.setSupplierSQId(sqId);

        supplier.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("sqIntegrationNumber"));

        supplier.setBrandList(buildBrands());
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

    private Active buildActive() {
        return JaggaerBuilder.buildActive("active");
    }

    private ThirdPartyRefNumber buildThirdPartyRefNumber() {
        return JaggaerBuilder.buildThirdPartyRefNumber("3rd party ref number");
    }


    private BrandList buildBrands() {
        BrandList brands = new BrandList();
        brands.getBrands().add(JaggaerBuilder.buildJaggaerBasicValue("brand 1", T_TRUE));
        brands.getBrands().add(JaggaerBuilder.buildJaggaerBasicValue("brand 2", F_FALSE));
        return brands;
    }

    private NaicsCodeList buildNaicsCodes() {
        NaicsCodeList codes = new NaicsCodeList();
        codes.setIsChanged(T_TRUE);
        codes.getNaicsCodeListItems().add(JaggaerBuilder.buildPrimaryNaics("primary code", T_TRUE));

        SecondaryNaicsList secondaryList = new SecondaryNaicsList();
        secondaryList.getSecondaryNaicItems().add(JaggaerBuilder.buildSecondaryNaics("second", F_FALSE));
        secondaryList.getSecondaryNaicItems().add(JaggaerBuilder.buildSecondaryNaics("third", T_TRUE));

        codes.getNaicsCodeListItems().add(secondaryList);
        return codes;
    }

    private CommodityCodeList buildCommodityCodeList() {
        CommodityCodeList commodityCodeList = new CommodityCodeList();
        commodityCodeList.getCommodityCodes().add(JaggaerBuilder.buildJaggaerBasicValue("Commodity Code 1", T_TRUE));
        commodityCodeList.getCommodityCodes().add(JaggaerBuilder.buildJaggaerBasicValue("Commodity Code 2", F_FALSE));
        return commodityCodeList;
    }

    private CurrencyList buildCurrencyList(boolean includeListChanged, boolean includeUSD, boolean includePeso,
            boolean includeEuro) {
        CurrencyList currencyList = new CurrencyList();

        if (includeListChanged) {
            currencyList.setIsChanged(T_TRUE);
        }

        if (includeUSD) {
            currencyList.getIsoCurrencyCodes().add(JaggaerBuilder.buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, T_TRUE));
        }

        if (includePeso) {
            currencyList.getIsoCurrencyCodes().add(JaggaerBuilder.buildIsoCurrencyCode("peso", F_FALSE));
        }

        if (includeEuro) {
            currencyList.getIsoCurrencyCodes().add(JaggaerBuilder.buildIsoCurrencyCode("euro", null));
        }

        return currencyList;
    }

    private BusinessUnitVendorNumberList buildBusinessUnitVendorNumberList() {
        BusinessUnitVendorNumberList unitNumberList = new BusinessUnitVendorNumberList();
        unitNumberList.setIsChanged(T_TRUE);
        unitNumberList.getBusinessUnitVendorNumbers().add(JaggaerBuilder.buildBusinessUnitVendorNumber("vendor number 1", "1232", T_TRUE));
        unitNumberList.getBusinessUnitVendorNumbers().add(JaggaerBuilder.buildBusinessUnitVendorNumber("vendor number 2", "56464", F_FALSE));
        return unitNumberList;
    }

    private SupplierCapital buildSupplierCapital() {
        SupplierCapital capital = new SupplierCapital();
        Amount ammount = buildAmount("50.00");
        capital.setAmount(ammount);
        capital.setIsChanged(T_TRUE);
        capital.setIsoCurrencyCode(JaggaerBuilder.buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, F_FALSE));
        return capital;
    }

    private Amount buildAmount(String amountString) {
        return JaggaerBuilder.buildAmount(amountString, T_TRUE);
    }

    private AnnualSalesList buildAnnualSalesList() {
        AnnualSalesList salesList = new AnnualSalesList();
        salesList.setIsChanged(T_TRUE);

        AnnualSalesItem sale = new AnnualSalesItem();
        sale.setIsChanged(F_FALSE);

        sale.setIsoCurrencyCode(JaggaerBuilder.buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, F_FALSE));

        sale.setAnnualSalesYear(JaggaerBuilder.buildJaggaerBasicValue("2023", T_TRUE));

        Amount ammount = new Amount();
        ammount.setIsChanged(T_TRUE);
        ammount.setValue("6900.00");
        sale.setAnnualSalesAmount(ammount);

        salesList.getAnnualSalesItems().add(sale);

        return salesList;
    }

    private ServiceAreaList buildServiceAreaList() {
        ServiceAreaList areaList = new ServiceAreaList();
        areaList.setIsChanged(T_TRUE);

        ServiceArea area1 = new ServiceArea();
        area1.setIsChanged(T_TRUE);
        area1.setServiceAreaInternalName(JaggaerBuilder.buildJaggaerBasicValue("internal name", T_TRUE));
        area1.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 1", "internal name 2"));
        areaList.getServiceArea().add(area1);

        ServiceArea area2 = new ServiceArea();
        area2.setIsChanged(F_FALSE);
        area2.setServiceAreaInternalName(JaggaerBuilder.buildJaggaerBasicValue("a different internal name", T_TRUE));
        area2.getStateServiceAreaList().add(buildStateServiceAreaList("internal name 3", "internal name 4"));
        areaList.getServiceArea().add(area2);

        return areaList;
    }

    private StateServiceAreaList buildStateServiceAreaList(String... names) {
        StateServiceAreaList stateServiceAreaList = new StateServiceAreaList();
        stateServiceAreaList.setIsChanged(T_TRUE);

        for (String name : names) {
            stateServiceAreaList.getStateServiceAreaInternalName().add(JaggaerBuilder.buildStateServiceAreaInternalName(name, T_TRUE));
        }

        return stateServiceAreaList;
    }

    private ParentSupplier buildParentSupplier() {
        ParentSupplier parent = new ParentSupplier();
        parent.setErpNumber(JaggaerBuilder.buildERPNumber("parent erp number", F_FALSE));
        parent.setIsChanged(F_FALSE);
        parent.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("parent integration number"));
        return parent;
    }

    private InsuranceInformationList buildInsuranceInformationList() {
        InsuranceInformationList insurance = new InsuranceInformationList();
        insurance.setIsChanged(T_TRUE);

        InsuranceInformation info = new InsuranceInformation();
        info.setType("auto");
        info.setIsChanged(T_TRUE);

        info.setPolicyNumber(JaggaerBuilder.buildJaggaerBasicValue("XYZ321", T_TRUE));
        info.setInsuranceLimit(JaggaerBuilder.buildJaggaerBasicValue("100", T_TRUE));
        info.setInsuranceProvider(JaggaerBuilder.buildJaggaerBasicValue("USAA", T_TRUE));
        info.setAgent(JaggaerBuilder.buildJaggaerBasicValue("Agent Name", T_TRUE));
        info.setExpirationDate(JaggaerBuilder.buildJaggaerBasicValue("12/31/2069", T_TRUE));
        info.setInsuranceProviderPhone(buildInsuranceProviderPhone());
        info.setInsuranceCertificate(buildInsuranceCertificate());
        info.setOtherTypeName(JaggaerBuilder.buildJaggaerBasicValue("some other name", T_TRUE));
        insurance.getInsuranceInformations().add(info);
        return insurance;
    }

    private InsuranceCertificate buildInsuranceCertificate() {
        InsuranceCertificate certificate = new InsuranceCertificate();
        certificate.setIsChanged(T_TRUE);
        certificate.setAttachmentList(buildAttachments("attachment name"));
        return certificate;
    }

    private InsuranceProviderPhone buildInsuranceProviderPhone() {
        InsuranceProviderPhone phone = new InsuranceProviderPhone();
        phone.setIsChanged(T_TRUE);
        phone.setTelephoneNumber(buildBasicTelephoneNumber());
        return phone;
    }

    private TelephoneNumber buildBasicTelephoneNumber() {
        return JaggaerBuilder.buildTelephoneNumber("USA", "607", "255*9900", "987", T_TRUE);
    }

    private TaxInformationList buildTaxInformationList() {
        TaxInformationList taxList = new TaxInformationList();
        taxList.setIsChanged(T_TRUE);

        TaxInformation info = new TaxInformation();
        info.setIsChanged(T_TRUE);
        info.setType("information type");

        info.setTaxDocumentName(JaggaerBuilder.buildJaggaerBasicValue("document name", F_FALSE));;
        info.setTaxDocumentYear(JaggaerBuilder.buildJaggaerBasicValue("2023", T_TRUE));

        TaxDocument document = new TaxDocument();
        document.setIsChanged(T_TRUE);

        document.setAttachmentList(buildAttachments("super cool tax document"));

        info.setTaxDocument(document);

        taxList.getTaxInformation().add(info);
        return taxList;
    }

    private AttachmentList buildAttachments(String attachmentName) {
        AttachmentList attachments = new AttachmentList();
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
        attachments.getAttachments().add(attach);
        return attachments;
    }


    private AccountsPayableList buildAccountsPayableList() {
        AccountsPayableList apList = new AccountsPayableList();
        apList.setIsChanged(T_TRUE);

        AccountsPayable ap = new AccountsPayable();
        ap.setIsChanged(T_TRUE);
        ap.setType("accounts payable type");
        ap.setOldERPNumber("old erp number");
        ap.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", F_FALSE));
        ap.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("sqIntegrationNumber"));
        ap.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        ap.setName(JaggaerBuilder.buildName("accounts payable name", T_TRUE));
        ap.setActive(buildActive());
        ap.getAssociatedAddresses().add(buildAssociatedAddress("type", ap.getErpNumber().getValue(), ap.getSqIntegrationNumber().getValue()));
        ap.setEmail(buildEmail("user@cornell.edu", T_TRUE));
        ap.setIsoCurrencyCode(JaggaerBuilder.buildIsoCurrencyCode(US_DOLLAR_CURRENCY_CODE, T_TRUE));
        ap.setContactName(JaggaerBuilder.buildJaggaerBasicValue("contact name", T_TRUE));
        ap.setPurpose(JaggaerBuilder.buildJaggaerBasicValue("testing is the only purpose", T_TRUE));;
        ap.setAccountId(JaggaerBuilder.buildJaggaerBasicValue("G234715", T_TRUE));
        ap.setAccountHolderName(JaggaerBuilder.buildJaggaerBasicValue("John Doe", T_TRUE));
        ap.setAccountType(JaggaerBuilder.buildJaggaerBasicValue("account type", T_TRUE));
        ap.setCountryCode(JaggaerBuilder.buildJaggaerBasicValue("USA", T_TRUE));
        ap.setBankAccount(buildBankAccount());
        ap.setFlexFields(buildFlexFields());

        apList.getAccountsPayables().add(ap);
        return apList;
    }

    private BankAccount buildBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setType("bank account type");
        bankAccount.setIsChanged(T_TRUE);

        bankAccount.setBankName(JaggaerBuilder.buildJaggaerBasicValue("bank name", T_TRUE));
        bankAccount.setAccountHoldersName(JaggaerBuilder.buildJaggaerBasicValue("Jane Doe", T_TRUE));
        bankAccount.setAccountNumberType(JaggaerBuilder.buildJaggaerBasicValue("account number type", T_TRUE));
        bankAccount.setRoutingNumber(JaggaerBuilder.buildJaggaerBasicValue("routing number", T_TRUE));
        bankAccount.setBankAccountNumber(JaggaerBuilder.buildJaggaerBasicValue("bank account number", T_TRUE));
        bankAccount.setIbanBankAccountNumber(JaggaerBuilder.buildJaggaerBasicValue("iban number", T_TRUE));
        bankAccount.setDirectDepositFormat(JaggaerBuilder.buildJaggaerBasicValue("Deposit Format", T_TRUE));
        bankAccount.setBankIdentifierCode(JaggaerBuilder.buildJaggaerBasicValue("bank identifier code", T_TRUE));
        bankAccount.setInternationalRoutingCode(JaggaerBuilder.buildJaggaerBasicValue("international routing", T_TRUE));
        bankAccount.setIsoCountryCode(buildIsoCountryCodeUsa());
        bankAccount.setAddressLine1(JaggaerBuilder.buildAddressLine("Address Line 1", T_TRUE));
        bankAccount.setAddressLine2(JaggaerBuilder.buildAddressLine("Address Line 2", T_TRUE));
        bankAccount.setAddressLine3(JaggaerBuilder.buildAddressLine("Address Line 3", T_TRUE));

        City city = new City();
        city.setIsChanged(T_TRUE);
        city.setValue("Ithaca");
        bankAccount.setCity(city);

        State state = new State();
        state.setIsChanged(T_TRUE);
        state.setValue("NY");
        bankAccount.setState(state);

        PostalCode postalCode = new PostalCode();
        postalCode.setIsChanged(T_TRUE);
        postalCode.setValue("14850");
        bankAccount.setPostalCode(postalCode);


        return bankAccount;
    }

    private IsoCountryCode buildIsoCountryCodeUsa() {
        return JaggaerBuilder.buildIsoCountryCode("USA", T_TRUE);
    }

    private FlexFields buildFlexFields() {
        FlexFields flexFields = new FlexFields();
        flexFields.setFlexField1(JaggaerBuilder.buildJaggaerBasicValue("flex field 1", T_TRUE));
        flexFields.setFlexField2(JaggaerBuilder.buildJaggaerBasicValue("flex field 2", T_TRUE));
        flexFields.setFlexField3(JaggaerBuilder.buildJaggaerBasicValue("flex field 3", T_TRUE));
        flexFields.setFlexField4(JaggaerBuilder.buildJaggaerBasicValue("flex field 4", T_TRUE));
        flexFields.setFlexField5(JaggaerBuilder.buildJaggaerBasicValue("flex field 5", T_TRUE));
        return flexFields;
    }

    private CustomElementList buildCustomElementList() {
        CustomElementList customList = new CustomElementList();
        customList.setIsChanged(T_TRUE);

        CustomElement element1 = new CustomElement();
        element1.setIsActive(F_FALSE);
        element1.setIsChanged(T_TRUE);
        element1.setType("custom type");

        CustomElementValueList elementValueList = new CustomElementValueList();
        elementValueList.setIsChanged(T_TRUE);
        CustomElementValue value = new CustomElementValue();
        value.setIsChanged(T_TRUE);
        value.setValue("some custom value");
        elementValueList.getCustomElementValues().add(value);
        element1.getCustomElementValueListOrAttachmentList().add(elementValueList);

        element1.setCustomElementIdentifier(JaggaerBuilder.buildJaggaerBasicValue("a custom identifer", T_TRUE));
        element1.setDisplayName(JaggaerBuilder.buildDisplayName("a cool display name", T_TRUE));
        customList.getCustomElements().add(element1);

        CustomElement element2 = new CustomElement();
        element2.setIsActive(T_TRUE);
        element2.setIsChanged(F_FALSE);
        element2.setType("custom type2");
        element2.getCustomElementValueListOrAttachmentList().add(buildAttachments("custom element attachment"));



        element2.setCustomElementIdentifier(JaggaerBuilder.buildJaggaerBasicValue("a  different custom identifer", T_TRUE));
        element2.setDisplayName(JaggaerBuilder.buildDisplayName("a lame display name", T_TRUE));
        customList.getCustomElements().add(element2);

        return customList;
    }

    private LocationList buildLocationList() {
        LocationList locationList = new LocationList();

        Location location = new Location();
        location.setIsChanged(T_TRUE);
        location.setSupportsOrderFulfillment("Order Fulfillment(");
        location.setOldERPNumber("old erp number");
        location.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", T_TRUE));
        location.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("sqIntegrationNumber"));
        location.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        location.setName(JaggaerBuilder.buildName("silly location name", T_TRUE));
        location.setDescription(JaggaerBuilder.buildJaggaerBasicValue("description value", T_TRUE));
        location.setActive(buildActive());
        location.setLocationActive(JaggaerBuilder.buildJaggaerBasicValue("location is active", T_TRUE));
        location.setPrimary(JaggaerBuilder.buildJaggaerBasicValue("primary", T_TRUE));
        location.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod());
        location.setLocationEffectiveDate(JaggaerBuilder.buildJaggaerBasicValue("02/28/2023", T_TRUE));
        location.setPaymentMethod(buildPaymentMethod());
        location.setShipping(buildShipping());
        location.setHandling(buildHandling());
        location.setTaxInfo(buildTaxInfo());
        location.setTermsAndCondition(buildTermsAndCondition());       
        location.setOrderDistributionList(buildOrderDistributionList());
        location.setAssignedBusinessUnitsList(buildAssignedBusinessUnitsList("is preferred", "testing name"));

        AssociatedAddressList addressList = new AssociatedAddressList();
        addressList.setIsChanged(T_TRUE);
        addressList.getAssociatedAddresses().add(buildAssociatedAddress("address type", "erp number", "sqi number"));
        location.setAssociatedAddressList(addressList);

        location.setCustomElementList(buildCustomElementList());


        AssociatedContactList contactList = new AssociatedContactList();
        contactList.setIsChanged(T_TRUE);
        contactList.getAssociatedContacts().add(buildAssociatedContact());
        location.setAssociatedContactList(contactList);

        locationList.getLocations().add(location);
        return locationList;
    }
    
    private Shipping buildShipping() {
        Shipping shipping = new Shipping();
        shipping.setIsChanged(T_TRUE);
        shipping.setSurchargeConfiguration(buildSurchargeConfiguration("25.00"));
        return shipping;
    }
    
    private SurchargeConfiguration buildSurchargeConfiguration(String feeAmount) {
        SurchargeConfiguration config = new SurchargeConfiguration();
        config.setIsChanged(T_TRUE);
        config.setUseOrderThreshold(JaggaerBuilder.buildJaggaerBasicValue("use threshhold", T_TRUE));
        config.setOrderThreshold(JaggaerBuilder.buildJaggaerBasicValue("order threshhold", T_TRUE));
        
        Fee fee = new Fee();
        fee.setAmount(buildAmount(feeAmount));
        fee.setIsChanged(T_TRUE);
        fee.setFeeType(JaggaerBuilder.buildJaggaerBasicValue("fee type", T_TRUE));
        fee.setPercentage(JaggaerBuilder.buildJaggaerBasicValue("2"));
        fee.setFeeScope(JaggaerBuilder.buildJaggaerBasicValue("scope"));
        config.setFee(fee);
        
        return config;
    }
    
    private Handling buildHandling() {
        Handling handling = new Handling();
        handling.setIsChanged(T_TRUE);
        handling.setSurchargeConfiguration(buildSurchargeConfiguration("2.75"));
        return handling;
    }

    private TermsAndCondition buildTermsAndCondition() {
        TermsAndCondition termsAndConditions = new TermsAndCondition();
        termsAndConditions.setIsChanged(T_TRUE);

        PaymentTerms paymentTerms = new PaymentTerms();
        paymentTerms.setIsChanged(T_TRUE);
        paymentTerms.setActive(buildActive());

        Discount discount = new Discount();
        discount.setIsChanged(T_TRUE);
        discount.setUnit("dollars");

        DiscountAmount amount = new DiscountAmount();
        amount.setValue("10");
        discount.getDiscountItems().add(amount);

        discount.getDiscountItems().add(JaggaerBuilder.buildIsoCurrencyCode("USD", null));

        DiscountPercent percent = new DiscountPercent();
        percent.setValue("5");
        discount.getDiscountItems().add(percent);

        paymentTerms.setDiscount(discount);
        paymentTerms.setDays(JaggaerBuilder.buildJaggaerBasicValue("Monday, Tuesday"));
        paymentTerms.setNet(JaggaerBuilder.buildJaggaerBasicValue("net"));

        CustomPaymentTerm customTerm = new CustomPaymentTerm();
        customTerm.setIsChanged(T_TRUE);
        customTerm.setId("ID field");
        customTerm.setUseCustomPaymentTerm("use custom term");
        customTerm.setValue("custom term");
        paymentTerms.setCustomPaymentTerm(customTerm);

        paymentTerms.setFob(JaggaerBuilder.buildJaggaerBasicValue("FOB", T_TRUE));
        paymentTerms.setStandardPaymentTermsCode(JaggaerBuilder.buildJaggaerBasicValue("standard payment terms"));
        paymentTerms.setTermsType(JaggaerBuilder.buildJaggaerBasicValue("terms type"));
        paymentTerms.setDaysAfter(JaggaerBuilder.buildJaggaerBasicValue("Days After"));

        termsAndConditions.setPaymentTerms(paymentTerms);;
        return termsAndConditions;
    }

    private OrderDistributionList buildOrderDistributionList() {
        OrderDistributionList orderList = new OrderDistributionList();
        orderList.setDistributionLanguage("US English");
        orderList.setIsChanged(T_TRUE);

        DistributionMethod method = new DistributionMethod();
        method.setActive(buildActive());
        method.setEmail(buildEmail("foo@bar.com", T_TRUE));

        Fax fax = new Fax();
        fax.setIsChanged(F_FALSE);
        fax.setTelephoneNumber(buildBasicTelephoneNumber());
        method.setFax(fax);

        orderList.getDistributionMethod().add(method);
        return orderList;
    }

    private TaxInfo buildTaxInfo() {
        TaxInfo taxInfo = new TaxInfo();
        taxInfo.setIsChanged(T_TRUE);
        taxInfo.setTaxableByDefault(JaggaerBuilder.buildJaggaerBasicValue("taxable by default", T_TRUE));
        taxInfo.setTax1Active(JaggaerBuilder.buildJaggaerBasicValue("tax 1 active"));
        taxInfo.setTax1(JaggaerBuilder.buildJaggaerBasicValue("tax 1"));
        taxInfo.setTax2Active(JaggaerBuilder.buildJaggaerBasicValue("tax 2 active"));
        taxInfo.setTax2(JaggaerBuilder.buildJaggaerBasicValue("tax 2"));
        taxInfo.setTaxShipping(JaggaerBuilder.buildJaggaerBasicValue("tax shipping"));
        taxInfo.setTaxHandling(JaggaerBuilder.buildJaggaerBasicValue("tax handling"));

        return taxInfo;
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
        email.setValue(emailAddress);
        return email;
    }

    private PaymentMethod buildPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType("payment type");
        paymentMethod.setIsChanged(T_TRUE);
        paymentMethod.setPoPaymentMethod(buildPOPaymentMethod());

        BlanketPOPaymentMethod blanketMethod = new BlanketPOPaymentMethod();
        blanketMethod.setIsChanged(T_TRUE);
        blanketMethod.setBlanketPONumber(JaggaerBuilder.buildJaggaerBasicValue("blanket number", T_TRUE));

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

        selection.setNumberWheel(JaggaerBuilder.buildJaggaerBasicValue("wheel", T_TRUE));
        poPayment.setPoNumberSelection(selection);
        poPayment.setAllowFreeForm(JaggaerBuilder.buildJaggaerBasicValue("allow free form", T_TRUE));

        poPaymentMethod.setPoPayment(poPayment);

        JPMorganVCardPayment jpMorgan = new JPMorganVCardPayment();
        jpMorgan.setActive(buildActive());
        jpMorgan.setIsChanged(T_TRUE);
        poPaymentMethod.setJpMorganVCardPayment(jpMorgan);

        PCardPayment pCard = new PCardPayment();
        pCard.setActive(buildActive());
        pCard.setIsChanged(T_TRUE);
        pCard.setPoNumberSelection(selection);
        pCard.setRequireCardSecurityCode(JaggaerBuilder.buildJaggaerBasicValue("security code", T_TRUE));

        poPaymentMethod.setpCardPayment(pCard);

        return poPaymentMethod;
    }

    private DiversityClassificationList buildDiversityClassificationList() {
        DiversityClassificationList diversityList = new DiversityClassificationList();
        diversityList.setIsChanged(T_TRUE);

        DiversityClassification diversity = new DiversityClassification();
        diversity.setIsChanged(T_TRUE);

        diversity.setInternalName(JaggaerBuilder.buildJaggaerBasicValue("internal name", T_TRUE));;
        diversity.setDisplayName(JaggaerBuilder.buildDisplayName("display name", F_FALSE));

        DD214Certificate ddCertificate = new DD214Certificate();
        ddCertificate.setIsChanged(T_TRUE);
        ddCertificate.setAttachmentList(buildAttachments("DD 214 Certificate Attachments"));
        diversity.setDd214Certificate(ddCertificate);

        DiversityCertificate certificate = new DiversityCertificate();
        certificate.setIsChanged(T_TRUE);
        certificate.setAttachmentList(buildAttachments("diversity certificate attachment"));
        diversity.setDiversityCertificate(certificate);

        AdditionalDataList dataList = new AdditionalDataList();
        dataList.setIsChanged(T_TRUE);

        AdditionalDataItem datum = new AdditionalDataItem();
        datum.setIsChanged(T_TRUE);
        datum.setName("datum name");
        datum.getContents().add("some additional piece of information");
        dataList.getAdditionalDataItems().add(datum);

        diversity.setAdditionalDataList(dataList);

        diversityList.getDiversityClassifications().add(diversity);
        return diversityList;
    }

    private ClassificationList buildClassificationList() {
        ClassificationList classificationList = new ClassificationList();
        classificationList.setIsChanged(T_TRUE);

        Classification classification1 = new Classification();
        classification1.setIsChanged(T_TRUE);
        classification1.setDisplayName(JaggaerBuilder.buildDisplayName("classification 1 name", T_TRUE));

        classification1.setInternalName(JaggaerBuilder.buildJaggaerBasicValue("internal name for classification 1", T_TRUE));
        classificationList.getClassifications().add(classification1);

        Classification classification2 = new Classification();
        classification2.setIsChanged(F_FALSE);
        classification2.setDisplayName(JaggaerBuilder.buildDisplayName("classification 2 name", F_FALSE));

        classification2.setInternalName(JaggaerBuilder.buildJaggaerBasicValue("internal name for classification 2", F_FALSE));
        classificationList.getClassifications().add(classification2);

        return classificationList;
    }

    private PrimaryContactList buildPrimaryContactList() {
        PrimaryContactList primaryContactList = new PrimaryContactList();
        primaryContactList.setIsChanged(T_TRUE);
        primaryContactList.getAssociatedContact().add(buildAssociatedContact());
        return primaryContactList;
    }

    private AssociatedContact buildAssociatedContact() {
        AssociatedContact contact = new AssociatedContact();
        contact.setIsChanged(T_TRUE);
        contact.setType("contact type");

        ContactRef ref = new ContactRef();
        ref.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", T_TRUE));
        ref.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("SQ number"));
        ref.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        contact.setContactRef(ref);
        return contact;
    }

    private ContactList buildContactList() {
        ContactList contactList = new ContactList();
        contactList.setIsChanged(T_TRUE);

        Contact contact = new Contact();
        contact.setIsChanged(T_TRUE);
        contact.setType("contact type");
        contact.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", T_TRUE));
        contact.setOldERPNumber("old erp number");
        contact.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("sq integration number"));
        contact.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        contact.setName(JaggaerBuilder.buildName("contact name", T_TRUE));
        contact.setActive(buildActive());

        contact.setFirstName(JaggaerBuilder.buildJaggaerBasicValue("Jane", T_TRUE));
        contact.setLastName(JaggaerBuilder.buildJaggaerBasicValue("Doe", T_TRUE));
        contact.setTitle(JaggaerBuilder.buildJaggaerBasicValue("cool title", T_TRUE));

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

        contact.setNotes(JaggaerBuilder.buildJaggaerBasicValue("just a simple note", T_TRUE));

        contactList.getContacts().add(contact);
        return contactList;
    }

    private PrimaryAddressList buildPrimaryAddressList() {
        PrimaryAddressList addressList = new PrimaryAddressList();
        addressList.setIsChanged(T_TRUE);
        addressList.getAssociatedAddress().add(buildAssociatedAddress("adddress type", "erp number", "sq integration number"));        
        return addressList;
    }

    private AssociatedAddress buildAssociatedAddress(String addressType, String erpNumber, String sqiNumber) {
        AssociatedAddress address = new AssociatedAddress();
        address.setIsChanged(T_TRUE);
        address.setType(addressType);

        AddressRef ref = new AddressRef();
        ref.setErpNumber(JaggaerBuilder.buildERPNumber(erpNumber, T_TRUE));
        ref.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber(sqiNumber));
        ref.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        address.setAddressRef(ref);
        return address;
    }

    private AddressList buildAddressList() {
        AddressList addressList = new AddressList();
        addressList.setIsChanged(T_TRUE);        

        Address address = new Address();
        address.setIsChanged(T_TRUE);
        address.setType("home address");
        address.setErpNumber(JaggaerBuilder.buildERPNumber("erp number", T_TRUE));
        address.setOldERPNumber("old erp number");
        address.setSqIntegrationNumber(JaggaerBuilder.buildSQIntegrationNumber("sq integration number"));
        address.setThirdPartyRefNumber(buildThirdPartyRefNumber());
        address.setName(JaggaerBuilder.buildName("address name", T_TRUE));
        address.setActive(buildActive());
        address.setPrefPurchaseOrderDeliveryMethod(buildPrefPurchaseOrderDeliveryMethod());

        address.setAddressLine1(JaggaerBuilder.buildAddressLine("line 1", T_TRUE));
        address.setAddressLine2(JaggaerBuilder.buildAddressLine("line 2", T_TRUE));
        address.setAddressLine3(JaggaerBuilder.buildAddressLine("line 3", T_TRUE));

        City city = new City();
        city.setIsChanged(T_TRUE);
        city.setValue("Ithaca");
        address.setCity(city);

        State state = new State();
        state.setIsChanged(T_TRUE);
        state.setValue("NY");
        address.setState(state);

        PostalCode postal = new PostalCode();
        postal.setIsChanged(T_TRUE);
        postal.setValue("14850");
        address.setPostalCode(postal);

        address.setIsoCountryCode(buildIsoCountryCodeUsa());

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

        address.setNotes(JaggaerBuilder.buildJaggaerBasicValue("just a simple note", T_TRUE));

        address.setAssignedBusinessUnitsList(buildAssignedBusinessUnitsList("is preferred", "testing name"));
        addressList.getAddresses().add(address);

        return addressList;
    }

    private AssignedBusinessUnitsList buildAssignedBusinessUnitsList(String preferredForThisBusinessUnit, String name) {
        AssignedBusinessUnitsList businessList = new AssignedBusinessUnitsList();
        businessList.setIsChanged(T_TRUE);
        businessList.getBusinessUnitInternalNames().add(JaggaerBuilder.buildBusinessUnitInternalName(name, preferredForThisBusinessUnit, T_TRUE));
        return businessList;
    }

    private void logActualXmlIfNeeded(SupplierSyncMessage supplierSyncMessage) throws JAXBException, IOException {
        if (true) {
            String actualResults = marshalService.marshalObjectToXmlString(supplierSyncMessage);
            LOG.info("logActualXmlIfNeeded, actualResults: " + actualResults);
        }
    }

}
