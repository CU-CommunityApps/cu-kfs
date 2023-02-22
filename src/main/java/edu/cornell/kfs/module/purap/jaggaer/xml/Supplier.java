package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "erpNumber", "oldERPNumber", "supplierSQId", "sqIntegrationNumber",
        "thirdPartyRefNumber", "jaSupplierId", "name", "doingBusinessAs", "otherNames", "countryOfOrigin",
        "parentSupplier", "active", "businessUnitVendorNumberList", "webSiteURL", "duns", "legalStructure",
        "taxIdentificationType", "taxIdentificationNumber", "vatIdentificationNumber", "exemptFromBackupWithholding",
        "numberOfEmployees", "yearEstablished", "annualSalesList", "serviceAreaList", "registrationProfileType",
        "registrationProfileStatus", "supplierTaxRepresentativeId", "supplierRegCourt", "supplierRegSeat",
        "supplierRegNumber", "supplierCapital", "supplierShareholders", "vatExempt", "commodityCodeList", "brands",
        "shoppingCommodityCode", "austinTetra", "sic", "naicsCodes", "supportedCurrencyList", "enabledCurrencyList",
        "supplierKeywords", "addressList", "primaryAddressList", "contactList", "primaryContactList",
        "classificationList", "diversityClassificationList", "locationList",
        "restrictFulfillmentLocationsByBusinessUnit", "customElementList", "accountsPayableList", "taxInformationList",
        "insuranceInformationList", "enablePaymentProvisioning" })
@XmlRootElement(name = "Supplier")
public class Supplier {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "approvedForERPSync")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String approvedForERPSync;
    @XmlAttribute(name = "requiresERP")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String requiresERP;
    @XmlElement(name = "ERPNumber")
    protected ERPNumber erpNumber;
    @XmlElement(name = "OldERPNumber")
    protected String oldERPNumber;
    @XmlElement(name = "SupplierSQId")
    protected SupplierSQId supplierSQId;
    @XmlElement(name = "SQIntegrationNumber")
    protected SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "JASupplierId")
    protected String jaSupplierId;
    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "DoingBusinessAs")
    protected JaggaerBasicValue doingBusinessAs;
    @XmlElement(name = "OtherNames")
    protected JaggaerBasicValue otherNames;
    @XmlElement(name = "CountryOfOrigin")
    protected JaggaerBasicValue countryOfOrigin;
    @XmlElement(name = "ParentSupplier")
    protected ParentSupplier parentSupplier;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "BusinessUnitVendorNumberList")
    protected BusinessUnitVendorNumberList businessUnitVendorNumberList;
    @XmlElement(name = "WebSiteURL")
    protected JaggaerBasicValue webSiteURL;
    @XmlElement(name = "DUNS")
    protected DUNS duns;
    @XmlElement(name = "LegalStructure")
    protected JaggaerBasicValue legalStructure;
    @XmlElement(name = "TaxIdentificationType")
    protected JaggaerBasicValue taxIdentificationType;
    @XmlElement(name = "TaxIdentificationNumber")
    protected JaggaerBasicValue taxIdentificationNumber;
    @XmlElement(name = "VatIdentificationNumber")
    protected JaggaerBasicValue vatIdentificationNumber;
    @XmlElement(name = "ExemptFromBackupWithholding")
    protected JaggaerBasicValue exemptFromBackupWithholding;
    @XmlElement(name = "NumberOfEmployees")
    protected JaggaerBasicValue numberOfEmployees;
    @XmlElement(name = "YearEstablished")
    protected JaggaerBasicValue yearEstablished;
    @XmlElement(name = "AnnualSalesList")
    protected AnnualSalesList annualSalesList;
    @XmlElement(name = "ServiceAreaList")
    protected ServiceAreaList serviceAreaList;
    @XmlElement(name = "RegistrationProfileType")
    protected JaggaerBasicValue registrationProfileType;
    @XmlElement(name = "RegistrationProfileStatus")
    protected JaggaerBasicValue registrationProfileStatus;
    @XmlElement(name = "SupplierTaxRepresentativeId")
    protected JaggaerBasicValue supplierTaxRepresentativeId;
    @XmlElement(name = "SupplierRegCourt")
    protected JaggaerBasicValue supplierRegCourt;
    @XmlElement(name = "SupplierRegSeat")
    protected JaggaerBasicValue supplierRegSeat;
    @XmlElement(name = "SupplierRegNumber")
    protected JaggaerBasicValue supplierRegNumber;
    @XmlElement(name = "SupplierCapital")
    protected SupplierCapital supplierCapital;
    @XmlElement(name = "SupplierShareholders")
    protected JaggaerBasicValue supplierShareholders;
    @XmlElement(name = "VATExempt")
    protected JaggaerBasicValue vatExempt;
    @XmlElement(name = "CommodityCodeList")
    protected CommodityCodeList commodityCodeList;
    @XmlElement(name = "Brands")
    protected Brands brands;
    @XmlElement(name = "ShoppingCommodityCode")
    protected JaggaerBasicValue shoppingCommodityCode;
    @XmlElement(name = "AustinTetra")
    protected JaggaerBasicValue austinTetra;
    @XmlElement(name = "SIC")
    protected JaggaerBasicValue sic;
    @XmlElement(name = "NaicsCodes")
    protected NaicsCodes naicsCodes;
    @XmlElement(name = "SupportedCurrencyList")
    protected SupportedCurrencyList supportedCurrencyList;
    @XmlElement(name = "EnabledCurrencyList")
    protected EnabledCurrencyList enabledCurrencyList;
    @XmlElement(name = "SupplierKeywords")
    protected JaggaerBasicValue supplierKeywords;
    @XmlElement(name = "AddressList")
    protected AddressList addressList;
    @XmlElement(name = "PrimaryAddressList")
    protected PrimaryAddressList primaryAddressList;
    @XmlElement(name = "ContactList")
    protected ContactList contactList;
    @XmlElement(name = "PrimaryContactList")
    protected PrimaryContactList primaryContactList;
    @XmlElement(name = "ClassificationList")
    protected ClassificationList classificationList;
    @XmlElement(name = "DiversityClassificationList")
    protected DiversityClassificationList diversityClassificationList;
    @XmlElement(name = "LocationList")
    protected LocationList locationList;
    @XmlElement(name = "RestrictFulfillmentLocationsByBusinessUnit")
    protected JaggaerBasicValue restrictFulfillmentLocationsByBusinessUnit;
    @XmlElement(name = "CustomElementList")
    protected CustomElementList customElementList;
    @XmlElement(name = "AccountsPayableList")
    protected AccountsPayableList accountsPayableList;
    @XmlElement(name = "TaxInformationList")
    protected TaxInformationList taxInformationList;
    @XmlElement(name = "InsuranceInformationList")
    protected InsuranceInformationList insuranceInformationList;
    @XmlElement(name = "EnablePaymentProvisioning")
    protected JaggaerBasicValue enablePaymentProvisioning;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getApprovedForERPSync() {
        return approvedForERPSync;
    }

    public void setApprovedForERPSync(String approvedForERPSync) {
        this.approvedForERPSync = approvedForERPSync;
    }

    public String getRequiresERP() {
        return requiresERP;
    }

    public void setRequiresERP(String requiresERP) {
        this.requiresERP = requiresERP;
    }

    public ERPNumber getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(ERPNumber erpNumber) {
        this.erpNumber = erpNumber;
    }

    public String getOldERPNumber() {
        return oldERPNumber;
    }

    public void setOldERPNumber(String oldERPNumber) {
        this.oldERPNumber = oldERPNumber;
    }

    public SupplierSQId getSupplierSQId() {
        return supplierSQId;
    }

    public void setSupplierSQId(SupplierSQId supplierSQId) {
        this.supplierSQId = supplierSQId;
    }

    public SQIntegrationNumber getSqIntegrationNumber() {
        return sqIntegrationNumber;
    }

    public void setSqIntegrationNumber(SQIntegrationNumber sqIntegrationNumber) {
        this.sqIntegrationNumber = sqIntegrationNumber;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber thirdPartyRefNumber) {
        this.thirdPartyRefNumber = thirdPartyRefNumber;
    }

    public String getJaSupplierId() {
        return jaSupplierId;
    }

    public void setJaSupplierId(String jaSupplierId) {
        this.jaSupplierId = jaSupplierId;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public JaggaerBasicValue getDoingBusinessAs() {
        return doingBusinessAs;
    }

    public void setDoingBusinessAs(JaggaerBasicValue doingBusinessAs) {
        this.doingBusinessAs = doingBusinessAs;
    }

    public JaggaerBasicValue getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(JaggaerBasicValue otherNames) {
        this.otherNames = otherNames;
    }

    public JaggaerBasicValue getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(JaggaerBasicValue countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public ParentSupplier getParentSupplier() {
        return parentSupplier;
    }

    public void setParentSupplier(ParentSupplier parentSupplier) {
        this.parentSupplier = parentSupplier;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public BusinessUnitVendorNumberList getBusinessUnitVendorNumberList() {
        return businessUnitVendorNumberList;
    }

    public void setBusinessUnitVendorNumberList(BusinessUnitVendorNumberList businessUnitVendorNumberList) {
        this.businessUnitVendorNumberList = businessUnitVendorNumberList;
    }

    public JaggaerBasicValue getWebSiteURL() {
        return webSiteURL;
    }

    public void setWebSiteURL(JaggaerBasicValue webSiteURL) {
        this.webSiteURL = webSiteURL;
    }

    public DUNS getDuns() {
        return duns;
    }

    public void setDuns(DUNS duns) {
        this.duns = duns;
    }

    public JaggaerBasicValue getLegalStructure() {
        return legalStructure;
    }

    public void setLegalStructure(JaggaerBasicValue legalStructure) {
        this.legalStructure = legalStructure;
    }

    public JaggaerBasicValue getTaxIdentificationType() {
        return taxIdentificationType;
    }

    public void setTaxIdentificationType(JaggaerBasicValue taxIdentificationType) {
        this.taxIdentificationType = taxIdentificationType;
    }

    public JaggaerBasicValue getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(JaggaerBasicValue taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    public JaggaerBasicValue getVatIdentificationNumber() {
        return vatIdentificationNumber;
    }

    public void setVatIdentificationNumber(JaggaerBasicValue vatIdentificationNumber) {
        this.vatIdentificationNumber = vatIdentificationNumber;
    }

    public JaggaerBasicValue getExemptFromBackupWithholding() {
        return exemptFromBackupWithholding;
    }

    public void setExemptFromBackupWithholding(JaggaerBasicValue exemptFromBackupWithholding) {
        this.exemptFromBackupWithholding = exemptFromBackupWithholding;
    }

    public JaggaerBasicValue getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(JaggaerBasicValue numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public JaggaerBasicValue getYearEstablished() {
        return yearEstablished;
    }

    public void setYearEstablished(JaggaerBasicValue yearEstablished) {
        this.yearEstablished = yearEstablished;
    }

    public AnnualSalesList getAnnualSalesList() {
        return annualSalesList;
    }

    public void setAnnualSalesList(AnnualSalesList annualSalesList) {
        this.annualSalesList = annualSalesList;
    }

    public ServiceAreaList getServiceAreaList() {
        return serviceAreaList;
    }

    public void setServiceAreaList(ServiceAreaList serviceAreaList) {
        this.serviceAreaList = serviceAreaList;
    }

    public JaggaerBasicValue getRegistrationProfileType() {
        return registrationProfileType;
    }

    public void setRegistrationProfileType(JaggaerBasicValue registrationProfileType) {
        this.registrationProfileType = registrationProfileType;
    }

    public JaggaerBasicValue getRegistrationProfileStatus() {
        return registrationProfileStatus;
    }

    public void setRegistrationProfileStatus(JaggaerBasicValue registrationProfileStatus) {
        this.registrationProfileStatus = registrationProfileStatus;
    }

    public JaggaerBasicValue getSupplierTaxRepresentativeId() {
        return supplierTaxRepresentativeId;
    }

    public void setSupplierTaxRepresentativeId(JaggaerBasicValue supplierTaxRepresentativeId) {
        this.supplierTaxRepresentativeId = supplierTaxRepresentativeId;
    }

    public JaggaerBasicValue getSupplierRegCourt() {
        return supplierRegCourt;
    }

    public void setSupplierRegCourt(JaggaerBasicValue supplierRegCourt) {
        this.supplierRegCourt = supplierRegCourt;
    }

    public JaggaerBasicValue getSupplierRegSeat() {
        return supplierRegSeat;
    }

    public void setSupplierRegSeat(JaggaerBasicValue supplierRegSeat) {
        this.supplierRegSeat = supplierRegSeat;
    }

    public JaggaerBasicValue getSupplierRegNumber() {
        return supplierRegNumber;
    }

    public void setSupplierRegNumber(JaggaerBasicValue supplierRegNumber) {
        this.supplierRegNumber = supplierRegNumber;
    }

    public SupplierCapital getSupplierCapital() {
        return supplierCapital;
    }

    public void setSupplierCapital(SupplierCapital supplierCapital) {
        this.supplierCapital = supplierCapital;
    }

    public JaggaerBasicValue getSupplierShareholders() {
        return supplierShareholders;
    }

    public void setSupplierShareholders(JaggaerBasicValue supplierShareholders) {
        this.supplierShareholders = supplierShareholders;
    }

    public JaggaerBasicValue getVatExempt() {
        return vatExempt;
    }

    public void setVatExempt(JaggaerBasicValue vatExempt) {
        this.vatExempt = vatExempt;
    }

    public CommodityCodeList getCommodityCodeList() {
        return commodityCodeList;
    }

    public void setCommodityCodeList(CommodityCodeList commodityCodeList) {
        this.commodityCodeList = commodityCodeList;
    }

    public Brands getBrands() {
        return brands;
    }

    public void setBrands(Brands brands) {
        this.brands = brands;
    }

    public JaggaerBasicValue getShoppingCommodityCode() {
        return shoppingCommodityCode;
    }

    public void setShoppingCommodityCode(JaggaerBasicValue shoppingCommodityCode) {
        this.shoppingCommodityCode = shoppingCommodityCode;
    }

    public JaggaerBasicValue getAustinTetra() {
        return austinTetra;
    }

    public void setAustinTetra(JaggaerBasicValue austinTetra) {
        this.austinTetra = austinTetra;
    }

    public JaggaerBasicValue getSic() {
        return sic;
    }

    public void setSic(JaggaerBasicValue sic) {
        this.sic = sic;
    }

    public NaicsCodes getNaicsCodes() {
        return naicsCodes;
    }

    public void setNaicsCodes(NaicsCodes naicsCodes) {
        this.naicsCodes = naicsCodes;
    }

    public SupportedCurrencyList getSupportedCurrencyList() {
        return supportedCurrencyList;
    }

    public void setSupportedCurrencyList(SupportedCurrencyList supportedCurrencyList) {
        this.supportedCurrencyList = supportedCurrencyList;
    }

    public EnabledCurrencyList getEnabledCurrencyList() {
        return enabledCurrencyList;
    }

    public void setEnabledCurrencyList(EnabledCurrencyList enabledCurrencyList) {
        this.enabledCurrencyList = enabledCurrencyList;
    }

    public JaggaerBasicValue getSupplierKeywords() {
        return supplierKeywords;
    }

    public void setSupplierKeywords(JaggaerBasicValue supplierKeywords) {
        this.supplierKeywords = supplierKeywords;
    }

    public AddressList getAddressList() {
        return addressList;
    }

    public void setAddressList(AddressList addressList) {
        this.addressList = addressList;
    }

    public PrimaryAddressList getPrimaryAddressList() {
        return primaryAddressList;
    }

    public void setPrimaryAddressList(PrimaryAddressList primaryAddressList) {
        this.primaryAddressList = primaryAddressList;
    }

    public ContactList getContactList() {
        return contactList;
    }

    public void setContactList(ContactList contactList) {
        this.contactList = contactList;
    }

    public PrimaryContactList getPrimaryContactList() {
        return primaryContactList;
    }

    public void setPrimaryContactList(PrimaryContactList primaryContactList) {
        this.primaryContactList = primaryContactList;
    }

    public ClassificationList getClassificationList() {
        return classificationList;
    }

    public void setClassificationList(ClassificationList classificationList) {
        this.classificationList = classificationList;
    }

    public DiversityClassificationList getDiversityClassificationList() {
        return diversityClassificationList;
    }

    public void setDiversityClassificationList(DiversityClassificationList diversityClassificationList) {
        this.diversityClassificationList = diversityClassificationList;
    }

    public LocationList getLocationList() {
        return locationList;
    }

    public void setLocationList(LocationList locationList) {
        this.locationList = locationList;
    }

    public JaggaerBasicValue getRestrictFulfillmentLocationsByBusinessUnit() {
        return restrictFulfillmentLocationsByBusinessUnit;
    }

    public void setRestrictFulfillmentLocationsByBusinessUnit(
            JaggaerBasicValue restrictFulfillmentLocationsByBusinessUnit) {
        this.restrictFulfillmentLocationsByBusinessUnit = restrictFulfillmentLocationsByBusinessUnit;
    }

    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    public void setCustomElementList(CustomElementList customElementList) {
        this.customElementList = customElementList;
    }

    public AccountsPayableList getAccountsPayableList() {
        return accountsPayableList;
    }

    public void setAccountsPayableList(AccountsPayableList accountsPayableList) {
        this.accountsPayableList = accountsPayableList;
    }

    public TaxInformationList getTaxInformationList() {
        return taxInformationList;
    }

    public void setTaxInformationList(TaxInformationList taxInformationList) {
        this.taxInformationList = taxInformationList;
    }

    public InsuranceInformationList getInsuranceInformationList() {
        return insuranceInformationList;
    }

    public void setInsuranceInformationList(InsuranceInformationList insuranceInformationList) {
        this.insuranceInformationList = insuranceInformationList;
    }

    public JaggaerBasicValue getEnablePaymentProvisioning() {
        return enablePaymentProvisioning;
    }

    public void setEnablePaymentProvisioning(JaggaerBasicValue enablePaymentProvisioning) {
        this.enablePaymentProvisioning = enablePaymentProvisioning;
    }

}
