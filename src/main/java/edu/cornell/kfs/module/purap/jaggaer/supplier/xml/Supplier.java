
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "erpNumber",
    "oldERPNumber",
    "supplierSQId",
    "sqIntegrationNumber",
    "thirdPartyRefNumber",
    "jaSupplierId",
    "name",
    "doingBusinessAs",
    "otherNames",
    "countryOfOrigin",
    "parentSupplier",
    "active",
    "businessUnitVendorNumberList",
    "webSiteURL",
    "duns",
    "legalStructure",
    "taxIdentificationType",
    "taxIdentificationNumber",
    "vatIdentificationNumber",
    "exemptFromBackupWithholding",
    "numberOfEmployees",
    "yearEstablished",
    "annualSalesList",
    "serviceAreaList",
    "registrationProfileType",
    "registrationProfileStatus",
    "supplierTaxRepresentativeId",
    "supplierRegCourt",
    "supplierRegSeat",
    "supplierRegNumber",
    "supplierCapital",
    "supplierShareholders",
    "vatExempt",
    "commodityCodeList",
    "brands",
    "shoppingCommodityCode",
    "austinTetra",
    "sic",
    "naicsCodes",
    "supportedCurrencyList",
    "enabledCurrencyList",
    "supplierKeywords",
    "addressList",
    "primaryAddressList",
    "contactList",
    "primaryContactList",
    "classificationList",
    "diversityClassificationList",
    "locationList",
    "restrictFulfillmentLocationsByBusinessUnit",
    "customElementList",
    "accountsPayableList",
    "taxInformationList",
    "insuranceInformationList",
    "enablePaymentProvisioning"
})
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
    protected DoingBusinessAs doingBusinessAs;
    @XmlElement(name = "OtherNames")
    protected OtherNames otherNames;
    @XmlElement(name = "CountryOfOrigin")
    protected CountryOfOrigin countryOfOrigin;
    @XmlElement(name = "ParentSupplier")
    protected ParentSupplier parentSupplier;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "BusinessUnitVendorNumberList")
    protected BusinessUnitVendorNumberList businessUnitVendorNumberList;
    @XmlElement(name = "WebSiteURL")
    protected WebSiteURL webSiteURL;
    @XmlElement(name = "DUNS")
    protected DUNS duns;
    @XmlElement(name = "LegalStructure")
    protected LegalStructure legalStructure;
    @XmlElement(name = "TaxIdentificationType")
    protected TaxIdentificationType taxIdentificationType;
    @XmlElement(name = "TaxIdentificationNumber")
    protected TaxIdentificationNumber taxIdentificationNumber;
    @XmlElement(name = "VatIdentificationNumber")
    protected VatIdentificationNumber vatIdentificationNumber;
    @XmlElement(name = "ExemptFromBackupWithholding")
    protected ExemptFromBackupWithholding exemptFromBackupWithholding;
    @XmlElement(name = "NumberOfEmployees")
    protected NumberOfEmployees numberOfEmployees;
    @XmlElement(name = "YearEstablished")
    protected YearEstablished yearEstablished;
    @XmlElement(name = "AnnualSalesList")
    protected AnnualSalesList annualSalesList;
    @XmlElement(name = "ServiceAreaList")
    protected ServiceAreaList serviceAreaList;
    @XmlElement(name = "RegistrationProfileType")
    protected RegistrationProfileType registrationProfileType;
    @XmlElement(name = "RegistrationProfileStatus")
    protected RegistrationProfileStatus registrationProfileStatus;
    @XmlElement(name = "SupplierTaxRepresentativeId")
    protected SupplierTaxRepresentativeId supplierTaxRepresentativeId;
    @XmlElement(name = "SupplierRegCourt")
    protected SupplierRegCourt supplierRegCourt;
    @XmlElement(name = "SupplierRegSeat")
    protected SupplierRegSeat supplierRegSeat;
    @XmlElement(name = "SupplierRegNumber")
    protected SupplierRegNumber supplierRegNumber;
    @XmlElement(name = "SupplierCapital")
    protected SupplierCapital supplierCapital;
    @XmlElement(name = "SupplierShareholders")
    protected SupplierShareholders supplierShareholders;
    @XmlElement(name = "VATExempt")
    protected VATExempt vatExempt;
    @XmlElement(name = "CommodityCodeList")
    protected CommodityCodeList commodityCodeList;
    @XmlElement(name = "Brands")
    protected Brands brands;
    @XmlElement(name = "ShoppingCommodityCode")
    protected ShoppingCommodityCode shoppingCommodityCode;
    @XmlElement(name = "AustinTetra")
    protected AustinTetra austinTetra;
    @XmlElement(name = "SIC")
    protected SIC sic;
    @XmlElement(name = "NaicsCodes")
    protected NaicsCodes naicsCodes;
    @XmlElement(name = "SupportedCurrencyList")
    protected SupportedCurrencyList supportedCurrencyList;
    @XmlElement(name = "EnabledCurrencyList")
    protected EnabledCurrencyList enabledCurrencyList;
    @XmlElement(name = "SupplierKeywords")
    protected SupplierKeywords supplierKeywords;
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
    protected RestrictFulfillmentLocationsByBusinessUnit restrictFulfillmentLocationsByBusinessUnit;
    @XmlElement(name = "CustomElementList")
    protected CustomElementList customElementList;
    @XmlElement(name = "AccountsPayableList")
    protected AccountsPayableList accountsPayableList;
    @XmlElement(name = "TaxInformationList")
    protected TaxInformationList taxInformationList;
    @XmlElement(name = "InsuranceInformationList")
    protected InsuranceInformationList insuranceInformationList;
    @XmlElement(name = "EnablePaymentProvisioning")
    protected EnablePaymentProvisioning enablePaymentProvisioning;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public String getApprovedForERPSync() {
        return approvedForERPSync;
    }

    
    public void setApprovedForERPSync(String value) {
        this.approvedForERPSync = value;
    }

    
    public String getRequiresERP() {
        return requiresERP;
    }

    
    public void setRequiresERP(String value) {
        this.requiresERP = value;
    }

    
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    
    public String getOldERPNumber() {
        return oldERPNumber;
    }

    
    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    
    public SupplierSQId getSupplierSQId() {
        return supplierSQId;
    }

    
    public void setSupplierSQId(SupplierSQId value) {
        this.supplierSQId = value;
    }

    
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    
    public String getJASupplierId() {
        return jaSupplierId;
    }

    
    public void setJASupplierId(String value) {
        this.jaSupplierId = value;
    }

    
    public Name getName() {
        return name;
    }

    
    public void setName(Name value) {
        this.name = value;
    }

    
    public DoingBusinessAs getDoingBusinessAs() {
        return doingBusinessAs;
    }

    
    public void setDoingBusinessAs(DoingBusinessAs value) {
        this.doingBusinessAs = value;
    }

    
    public OtherNames getOtherNames() {
        return otherNames;
    }

    
    public void setOtherNames(OtherNames value) {
        this.otherNames = value;
    }

    
    public CountryOfOrigin getCountryOfOrigin() {
        return countryOfOrigin;
    }

    
    public void setCountryOfOrigin(CountryOfOrigin value) {
        this.countryOfOrigin = value;
    }

    
    public ParentSupplier getParentSupplier() {
        return parentSupplier;
    }

    
    public void setParentSupplier(ParentSupplier value) {
        this.parentSupplier = value;
    }

    
    public Active getActive() {
        return active;
    }

    
    public void setActive(Active value) {
        this.active = value;
    }

    
    public BusinessUnitVendorNumberList getBusinessUnitVendorNumberList() {
        return businessUnitVendorNumberList;
    }

    
    public void setBusinessUnitVendorNumberList(BusinessUnitVendorNumberList value) {
        this.businessUnitVendorNumberList = value;
    }

    
    public WebSiteURL getWebSiteURL() {
        return webSiteURL;
    }

    
    public void setWebSiteURL(WebSiteURL value) {
        this.webSiteURL = value;
    }

    
    public DUNS getDUNS() {
        return duns;
    }

    
    public void setDUNS(DUNS value) {
        this.duns = value;
    }

    
    public LegalStructure getLegalStructure() {
        return legalStructure;
    }

    
    public void setLegalStructure(LegalStructure value) {
        this.legalStructure = value;
    }

    
    public TaxIdentificationType getTaxIdentificationType() {
        return taxIdentificationType;
    }

    
    public void setTaxIdentificationType(TaxIdentificationType value) {
        this.taxIdentificationType = value;
    }

    
    public TaxIdentificationNumber getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    
    public void setTaxIdentificationNumber(TaxIdentificationNumber value) {
        this.taxIdentificationNumber = value;
    }

    
    public VatIdentificationNumber getVatIdentificationNumber() {
        return vatIdentificationNumber;
    }

    
    public void setVatIdentificationNumber(VatIdentificationNumber value) {
        this.vatIdentificationNumber = value;
    }

    
    public ExemptFromBackupWithholding getExemptFromBackupWithholding() {
        return exemptFromBackupWithholding;
    }

    
    public void setExemptFromBackupWithholding(ExemptFromBackupWithholding value) {
        this.exemptFromBackupWithholding = value;
    }

    
    public NumberOfEmployees getNumberOfEmployees() {
        return numberOfEmployees;
    }

    
    public void setNumberOfEmployees(NumberOfEmployees value) {
        this.numberOfEmployees = value;
    }

    
    public YearEstablished getYearEstablished() {
        return yearEstablished;
    }

    
    public void setYearEstablished(YearEstablished value) {
        this.yearEstablished = value;
    }

    
    public AnnualSalesList getAnnualSalesList() {
        return annualSalesList;
    }

    
    public void setAnnualSalesList(AnnualSalesList value) {
        this.annualSalesList = value;
    }

    
    public ServiceAreaList getServiceAreaList() {
        return serviceAreaList;
    }

    
    public void setServiceAreaList(ServiceAreaList value) {
        this.serviceAreaList = value;
    }

    
    public RegistrationProfileType getRegistrationProfileType() {
        return registrationProfileType;
    }

    
    public void setRegistrationProfileType(RegistrationProfileType value) {
        this.registrationProfileType = value;
    }

    
    public RegistrationProfileStatus getRegistrationProfileStatus() {
        return registrationProfileStatus;
    }

    
    public void setRegistrationProfileStatus(RegistrationProfileStatus value) {
        this.registrationProfileStatus = value;
    }

    
    public SupplierTaxRepresentativeId getSupplierTaxRepresentativeId() {
        return supplierTaxRepresentativeId;
    }

    
    public void setSupplierTaxRepresentativeId(SupplierTaxRepresentativeId value) {
        this.supplierTaxRepresentativeId = value;
    }

    
    public SupplierRegCourt getSupplierRegCourt() {
        return supplierRegCourt;
    }

    
    public void setSupplierRegCourt(SupplierRegCourt value) {
        this.supplierRegCourt = value;
    }

    
    public SupplierRegSeat getSupplierRegSeat() {
        return supplierRegSeat;
    }

    
    public void setSupplierRegSeat(SupplierRegSeat value) {
        this.supplierRegSeat = value;
    }

    
    public SupplierRegNumber getSupplierRegNumber() {
        return supplierRegNumber;
    }

    
    public void setSupplierRegNumber(SupplierRegNumber value) {
        this.supplierRegNumber = value;
    }

    
    public SupplierCapital getSupplierCapital() {
        return supplierCapital;
    }

    
    public void setSupplierCapital(SupplierCapital value) {
        this.supplierCapital = value;
    }

    
    public SupplierShareholders getSupplierShareholders() {
        return supplierShareholders;
    }

    
    public void setSupplierShareholders(SupplierShareholders value) {
        this.supplierShareholders = value;
    }

    
    public VATExempt getVATExempt() {
        return vatExempt;
    }

    
    public void setVATExempt(VATExempt value) {
        this.vatExempt = value;
    }

    
    public CommodityCodeList getCommodityCodeList() {
        return commodityCodeList;
    }

    
    public void setCommodityCodeList(CommodityCodeList value) {
        this.commodityCodeList = value;
    }

    
    public Brands getBrands() {
        return brands;
    }

    
    public void setBrands(Brands value) {
        this.brands = value;
    }

    
    public ShoppingCommodityCode getShoppingCommodityCode() {
        return shoppingCommodityCode;
    }

    
    public void setShoppingCommodityCode(ShoppingCommodityCode value) {
        this.shoppingCommodityCode = value;
    }

    
    public AustinTetra getAustinTetra() {
        return austinTetra;
    }

    
    public void setAustinTetra(AustinTetra value) {
        this.austinTetra = value;
    }

    
    public SIC getSIC() {
        return sic;
    }

    
    public void setSIC(SIC value) {
        this.sic = value;
    }

    
    public NaicsCodes getNaicsCodes() {
        return naicsCodes;
    }

    
    public void setNaicsCodes(NaicsCodes value) {
        this.naicsCodes = value;
    }

    
    public SupportedCurrencyList getSupportedCurrencyList() {
        return supportedCurrencyList;
    }

    
    public void setSupportedCurrencyList(SupportedCurrencyList value) {
        this.supportedCurrencyList = value;
    }

    
    public EnabledCurrencyList getEnabledCurrencyList() {
        return enabledCurrencyList;
    }

    
    public void setEnabledCurrencyList(EnabledCurrencyList value) {
        this.enabledCurrencyList = value;
    }

    
    public SupplierKeywords getSupplierKeywords() {
        return supplierKeywords;
    }

    
    public void setSupplierKeywords(SupplierKeywords value) {
        this.supplierKeywords = value;
    }

    
    public AddressList getAddressList() {
        return addressList;
    }

    
    public void setAddressList(AddressList value) {
        this.addressList = value;
    }

    
    public PrimaryAddressList getPrimaryAddressList() {
        return primaryAddressList;
    }

    
    public void setPrimaryAddressList(PrimaryAddressList value) {
        this.primaryAddressList = value;
    }

    
    public ContactList getContactList() {
        return contactList;
    }

    
    public void setContactList(ContactList value) {
        this.contactList = value;
    }

    
    public PrimaryContactList getPrimaryContactList() {
        return primaryContactList;
    }

    
    public void setPrimaryContactList(PrimaryContactList value) {
        this.primaryContactList = value;
    }

    
    public ClassificationList getClassificationList() {
        return classificationList;
    }

    
    public void setClassificationList(ClassificationList value) {
        this.classificationList = value;
    }

    
    public DiversityClassificationList getDiversityClassificationList() {
        return diversityClassificationList;
    }

    
    public void setDiversityClassificationList(DiversityClassificationList value) {
        this.diversityClassificationList = value;
    }

    
    public LocationList getLocationList() {
        return locationList;
    }

    
    public void setLocationList(LocationList value) {
        this.locationList = value;
    }

    
    public RestrictFulfillmentLocationsByBusinessUnit getRestrictFulfillmentLocationsByBusinessUnit() {
        return restrictFulfillmentLocationsByBusinessUnit;
    }

    
    public void setRestrictFulfillmentLocationsByBusinessUnit(RestrictFulfillmentLocationsByBusinessUnit value) {
        this.restrictFulfillmentLocationsByBusinessUnit = value;
    }

    
    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    
    public void setCustomElementList(CustomElementList value) {
        this.customElementList = value;
    }

    
    public AccountsPayableList getAccountsPayableList() {
        return accountsPayableList;
    }

    
    public void setAccountsPayableList(AccountsPayableList value) {
        this.accountsPayableList = value;
    }

    
    public TaxInformationList getTaxInformationList() {
        return taxInformationList;
    }

    
    public void setTaxInformationList(TaxInformationList value) {
        this.taxInformationList = value;
    }

    
    public InsuranceInformationList getInsuranceInformationList() {
        return insuranceInformationList;
    }

    
    public void setInsuranceInformationList(InsuranceInformationList value) {
        this.insuranceInformationList = value;
    }

    
    public EnablePaymentProvisioning getEnablePaymentProvisioning() {
        return enablePaymentProvisioning;
    }

    
    public void setEnablePaymentProvisioning(EnablePaymentProvisioning value) {
        this.enablePaymentProvisioning = value;
    }

}
