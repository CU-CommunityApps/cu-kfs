
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
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

    /**
     * Gets the value of the isChanged property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsChanged() {
        return isChanged;
    }

    /**
     * Sets the value of the isChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    /**
     * Gets the value of the approvedForERPSync property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApprovedForERPSync() {
        return approvedForERPSync;
    }

    /**
     * Sets the value of the approvedForERPSync property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApprovedForERPSync(String value) {
        this.approvedForERPSync = value;
    }

    /**
     * Gets the value of the requiresERP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequiresERP() {
        return requiresERP;
    }

    /**
     * Sets the value of the requiresERP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequiresERP(String value) {
        this.requiresERP = value;
    }

    /**
     * Gets the value of the erpNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ERPNumber }
     *     
     */
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    /**
     * Sets the value of the erpNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERPNumber }
     *     
     */
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    /**
     * Gets the value of the oldERPNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldERPNumber() {
        return oldERPNumber;
    }

    /**
     * Sets the value of the oldERPNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    /**
     * Gets the value of the supplierSQId property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierSQId }
     *     
     */
    public SupplierSQId getSupplierSQId() {
        return supplierSQId;
    }

    /**
     * Sets the value of the supplierSQId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierSQId }
     *     
     */
    public void setSupplierSQId(SupplierSQId value) {
        this.supplierSQId = value;
    }

    /**
     * Gets the value of the sqIntegrationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    /**
     * Sets the value of the sqIntegrationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    /**
     * Gets the value of the thirdPartyRefNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    /**
     * Sets the value of the thirdPartyRefNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    /**
     * Gets the value of the jaSupplierId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJASupplierId() {
        return jaSupplierId;
    }

    /**
     * Sets the value of the jaSupplierId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJASupplierId(String value) {
        this.jaSupplierId = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the doingBusinessAs property.
     * 
     * @return
     *     possible object is
     *     {@link DoingBusinessAs }
     *     
     */
    public DoingBusinessAs getDoingBusinessAs() {
        return doingBusinessAs;
    }

    /**
     * Sets the value of the doingBusinessAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoingBusinessAs }
     *     
     */
    public void setDoingBusinessAs(DoingBusinessAs value) {
        this.doingBusinessAs = value;
    }

    /**
     * Gets the value of the otherNames property.
     * 
     * @return
     *     possible object is
     *     {@link OtherNames }
     *     
     */
    public OtherNames getOtherNames() {
        return otherNames;
    }

    /**
     * Sets the value of the otherNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link OtherNames }
     *     
     */
    public void setOtherNames(OtherNames value) {
        this.otherNames = value;
    }

    /**
     * Gets the value of the countryOfOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link CountryOfOrigin }
     *     
     */
    public CountryOfOrigin getCountryOfOrigin() {
        return countryOfOrigin;
    }

    /**
     * Sets the value of the countryOfOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CountryOfOrigin }
     *     
     */
    public void setCountryOfOrigin(CountryOfOrigin value) {
        this.countryOfOrigin = value;
    }

    /**
     * Gets the value of the parentSupplier property.
     * 
     * @return
     *     possible object is
     *     {@link ParentSupplier }
     *     
     */
    public ParentSupplier getParentSupplier() {
        return parentSupplier;
    }

    /**
     * Sets the value of the parentSupplier property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParentSupplier }
     *     
     */
    public void setParentSupplier(ParentSupplier value) {
        this.parentSupplier = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Active }
     *     
     */
    public Active getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Active }
     *     
     */
    public void setActive(Active value) {
        this.active = value;
    }

    /**
     * Gets the value of the businessUnitVendorNumberList property.
     * 
     * @return
     *     possible object is
     *     {@link BusinessUnitVendorNumberList }
     *     
     */
    public BusinessUnitVendorNumberList getBusinessUnitVendorNumberList() {
        return businessUnitVendorNumberList;
    }

    /**
     * Sets the value of the businessUnitVendorNumberList property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessUnitVendorNumberList }
     *     
     */
    public void setBusinessUnitVendorNumberList(BusinessUnitVendorNumberList value) {
        this.businessUnitVendorNumberList = value;
    }

    /**
     * Gets the value of the webSiteURL property.
     * 
     * @return
     *     possible object is
     *     {@link WebSiteURL }
     *     
     */
    public WebSiteURL getWebSiteURL() {
        return webSiteURL;
    }

    /**
     * Sets the value of the webSiteURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebSiteURL }
     *     
     */
    public void setWebSiteURL(WebSiteURL value) {
        this.webSiteURL = value;
    }

    /**
     * Gets the value of the duns property.
     * 
     * @return
     *     possible object is
     *     {@link DUNS }
     *     
     */
    public DUNS getDUNS() {
        return duns;
    }

    /**
     * Sets the value of the duns property.
     * 
     * @param value
     *     allowed object is
     *     {@link DUNS }
     *     
     */
    public void setDUNS(DUNS value) {
        this.duns = value;
    }

    /**
     * Gets the value of the legalStructure property.
     * 
     * @return
     *     possible object is
     *     {@link LegalStructure }
     *     
     */
    public LegalStructure getLegalStructure() {
        return legalStructure;
    }

    /**
     * Sets the value of the legalStructure property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegalStructure }
     *     
     */
    public void setLegalStructure(LegalStructure value) {
        this.legalStructure = value;
    }

    /**
     * Gets the value of the taxIdentificationType property.
     * 
     * @return
     *     possible object is
     *     {@link TaxIdentificationType }
     *     
     */
    public TaxIdentificationType getTaxIdentificationType() {
        return taxIdentificationType;
    }

    /**
     * Sets the value of the taxIdentificationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxIdentificationType }
     *     
     */
    public void setTaxIdentificationType(TaxIdentificationType value) {
        this.taxIdentificationType = value;
    }

    /**
     * Gets the value of the taxIdentificationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link TaxIdentificationNumber }
     *     
     */
    public TaxIdentificationNumber getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    /**
     * Sets the value of the taxIdentificationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxIdentificationNumber }
     *     
     */
    public void setTaxIdentificationNumber(TaxIdentificationNumber value) {
        this.taxIdentificationNumber = value;
    }

    /**
     * Gets the value of the vatIdentificationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link VatIdentificationNumber }
     *     
     */
    public VatIdentificationNumber getVatIdentificationNumber() {
        return vatIdentificationNumber;
    }

    /**
     * Sets the value of the vatIdentificationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link VatIdentificationNumber }
     *     
     */
    public void setVatIdentificationNumber(VatIdentificationNumber value) {
        this.vatIdentificationNumber = value;
    }

    /**
     * Gets the value of the exemptFromBackupWithholding property.
     * 
     * @return
     *     possible object is
     *     {@link ExemptFromBackupWithholding }
     *     
     */
    public ExemptFromBackupWithholding getExemptFromBackupWithholding() {
        return exemptFromBackupWithholding;
    }

    /**
     * Sets the value of the exemptFromBackupWithholding property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExemptFromBackupWithholding }
     *     
     */
    public void setExemptFromBackupWithholding(ExemptFromBackupWithholding value) {
        this.exemptFromBackupWithholding = value;
    }

    /**
     * Gets the value of the numberOfEmployees property.
     * 
     * @return
     *     possible object is
     *     {@link NumberOfEmployees }
     *     
     */
    public NumberOfEmployees getNumberOfEmployees() {
        return numberOfEmployees;
    }

    /**
     * Sets the value of the numberOfEmployees property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberOfEmployees }
     *     
     */
    public void setNumberOfEmployees(NumberOfEmployees value) {
        this.numberOfEmployees = value;
    }

    /**
     * Gets the value of the yearEstablished property.
     * 
     * @return
     *     possible object is
     *     {@link YearEstablished }
     *     
     */
    public YearEstablished getYearEstablished() {
        return yearEstablished;
    }

    /**
     * Sets the value of the yearEstablished property.
     * 
     * @param value
     *     allowed object is
     *     {@link YearEstablished }
     *     
     */
    public void setYearEstablished(YearEstablished value) {
        this.yearEstablished = value;
    }

    /**
     * Gets the value of the annualSalesList property.
     * 
     * @return
     *     possible object is
     *     {@link AnnualSalesList }
     *     
     */
    public AnnualSalesList getAnnualSalesList() {
        return annualSalesList;
    }

    /**
     * Sets the value of the annualSalesList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnnualSalesList }
     *     
     */
    public void setAnnualSalesList(AnnualSalesList value) {
        this.annualSalesList = value;
    }

    /**
     * Gets the value of the serviceAreaList property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceAreaList }
     *     
     */
    public ServiceAreaList getServiceAreaList() {
        return serviceAreaList;
    }

    /**
     * Sets the value of the serviceAreaList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceAreaList }
     *     
     */
    public void setServiceAreaList(ServiceAreaList value) {
        this.serviceAreaList = value;
    }

    /**
     * Gets the value of the registrationProfileType property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationProfileType }
     *     
     */
    public RegistrationProfileType getRegistrationProfileType() {
        return registrationProfileType;
    }

    /**
     * Sets the value of the registrationProfileType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationProfileType }
     *     
     */
    public void setRegistrationProfileType(RegistrationProfileType value) {
        this.registrationProfileType = value;
    }

    /**
     * Gets the value of the registrationProfileStatus property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationProfileStatus }
     *     
     */
    public RegistrationProfileStatus getRegistrationProfileStatus() {
        return registrationProfileStatus;
    }

    /**
     * Sets the value of the registrationProfileStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationProfileStatus }
     *     
     */
    public void setRegistrationProfileStatus(RegistrationProfileStatus value) {
        this.registrationProfileStatus = value;
    }

    /**
     * Gets the value of the supplierTaxRepresentativeId property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierTaxRepresentativeId }
     *     
     */
    public SupplierTaxRepresentativeId getSupplierTaxRepresentativeId() {
        return supplierTaxRepresentativeId;
    }

    /**
     * Sets the value of the supplierTaxRepresentativeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierTaxRepresentativeId }
     *     
     */
    public void setSupplierTaxRepresentativeId(SupplierTaxRepresentativeId value) {
        this.supplierTaxRepresentativeId = value;
    }

    /**
     * Gets the value of the supplierRegCourt property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierRegCourt }
     *     
     */
    public SupplierRegCourt getSupplierRegCourt() {
        return supplierRegCourt;
    }

    /**
     * Sets the value of the supplierRegCourt property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierRegCourt }
     *     
     */
    public void setSupplierRegCourt(SupplierRegCourt value) {
        this.supplierRegCourt = value;
    }

    /**
     * Gets the value of the supplierRegSeat property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierRegSeat }
     *     
     */
    public SupplierRegSeat getSupplierRegSeat() {
        return supplierRegSeat;
    }

    /**
     * Sets the value of the supplierRegSeat property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierRegSeat }
     *     
     */
    public void setSupplierRegSeat(SupplierRegSeat value) {
        this.supplierRegSeat = value;
    }

    /**
     * Gets the value of the supplierRegNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierRegNumber }
     *     
     */
    public SupplierRegNumber getSupplierRegNumber() {
        return supplierRegNumber;
    }

    /**
     * Sets the value of the supplierRegNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierRegNumber }
     *     
     */
    public void setSupplierRegNumber(SupplierRegNumber value) {
        this.supplierRegNumber = value;
    }

    /**
     * Gets the value of the supplierCapital property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierCapital }
     *     
     */
    public SupplierCapital getSupplierCapital() {
        return supplierCapital;
    }

    /**
     * Sets the value of the supplierCapital property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierCapital }
     *     
     */
    public void setSupplierCapital(SupplierCapital value) {
        this.supplierCapital = value;
    }

    /**
     * Gets the value of the supplierShareholders property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierShareholders }
     *     
     */
    public SupplierShareholders getSupplierShareholders() {
        return supplierShareholders;
    }

    /**
     * Sets the value of the supplierShareholders property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierShareholders }
     *     
     */
    public void setSupplierShareholders(SupplierShareholders value) {
        this.supplierShareholders = value;
    }

    /**
     * Gets the value of the vatExempt property.
     * 
     * @return
     *     possible object is
     *     {@link VATExempt }
     *     
     */
    public VATExempt getVATExempt() {
        return vatExempt;
    }

    /**
     * Sets the value of the vatExempt property.
     * 
     * @param value
     *     allowed object is
     *     {@link VATExempt }
     *     
     */
    public void setVATExempt(VATExempt value) {
        this.vatExempt = value;
    }

    /**
     * Gets the value of the commodityCodeList property.
     * 
     * @return
     *     possible object is
     *     {@link CommodityCodeList }
     *     
     */
    public CommodityCodeList getCommodityCodeList() {
        return commodityCodeList;
    }

    /**
     * Sets the value of the commodityCodeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommodityCodeList }
     *     
     */
    public void setCommodityCodeList(CommodityCodeList value) {
        this.commodityCodeList = value;
    }

    /**
     * Gets the value of the brands property.
     * 
     * @return
     *     possible object is
     *     {@link Brands }
     *     
     */
    public Brands getBrands() {
        return brands;
    }

    /**
     * Sets the value of the brands property.
     * 
     * @param value
     *     allowed object is
     *     {@link Brands }
     *     
     */
    public void setBrands(Brands value) {
        this.brands = value;
    }

    /**
     * Gets the value of the shoppingCommodityCode property.
     * 
     * @return
     *     possible object is
     *     {@link ShoppingCommodityCode }
     *     
     */
    public ShoppingCommodityCode getShoppingCommodityCode() {
        return shoppingCommodityCode;
    }

    /**
     * Sets the value of the shoppingCommodityCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ShoppingCommodityCode }
     *     
     */
    public void setShoppingCommodityCode(ShoppingCommodityCode value) {
        this.shoppingCommodityCode = value;
    }

    /**
     * Gets the value of the austinTetra property.
     * 
     * @return
     *     possible object is
     *     {@link AustinTetra }
     *     
     */
    public AustinTetra getAustinTetra() {
        return austinTetra;
    }

    /**
     * Sets the value of the austinTetra property.
     * 
     * @param value
     *     allowed object is
     *     {@link AustinTetra }
     *     
     */
    public void setAustinTetra(AustinTetra value) {
        this.austinTetra = value;
    }

    /**
     * Gets the value of the sic property.
     * 
     * @return
     *     possible object is
     *     {@link SIC }
     *     
     */
    public SIC getSIC() {
        return sic;
    }

    /**
     * Sets the value of the sic property.
     * 
     * @param value
     *     allowed object is
     *     {@link SIC }
     *     
     */
    public void setSIC(SIC value) {
        this.sic = value;
    }

    /**
     * Gets the value of the naicsCodes property.
     * 
     * @return
     *     possible object is
     *     {@link NaicsCodes }
     *     
     */
    public NaicsCodes getNaicsCodes() {
        return naicsCodes;
    }

    /**
     * Sets the value of the naicsCodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link NaicsCodes }
     *     
     */
    public void setNaicsCodes(NaicsCodes value) {
        this.naicsCodes = value;
    }

    /**
     * Gets the value of the supportedCurrencyList property.
     * 
     * @return
     *     possible object is
     *     {@link SupportedCurrencyList }
     *     
     */
    public SupportedCurrencyList getSupportedCurrencyList() {
        return supportedCurrencyList;
    }

    /**
     * Sets the value of the supportedCurrencyList property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupportedCurrencyList }
     *     
     */
    public void setSupportedCurrencyList(SupportedCurrencyList value) {
        this.supportedCurrencyList = value;
    }

    /**
     * Gets the value of the enabledCurrencyList property.
     * 
     * @return
     *     possible object is
     *     {@link EnabledCurrencyList }
     *     
     */
    public EnabledCurrencyList getEnabledCurrencyList() {
        return enabledCurrencyList;
    }

    /**
     * Sets the value of the enabledCurrencyList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnabledCurrencyList }
     *     
     */
    public void setEnabledCurrencyList(EnabledCurrencyList value) {
        this.enabledCurrencyList = value;
    }

    /**
     * Gets the value of the supplierKeywords property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierKeywords }
     *     
     */
    public SupplierKeywords getSupplierKeywords() {
        return supplierKeywords;
    }

    /**
     * Sets the value of the supplierKeywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierKeywords }
     *     
     */
    public void setSupplierKeywords(SupplierKeywords value) {
        this.supplierKeywords = value;
    }

    /**
     * Gets the value of the addressList property.
     * 
     * @return
     *     possible object is
     *     {@link AddressList }
     *     
     */
    public AddressList getAddressList() {
        return addressList;
    }

    /**
     * Sets the value of the addressList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressList }
     *     
     */
    public void setAddressList(AddressList value) {
        this.addressList = value;
    }

    /**
     * Gets the value of the primaryAddressList property.
     * 
     * @return
     *     possible object is
     *     {@link PrimaryAddressList }
     *     
     */
    public PrimaryAddressList getPrimaryAddressList() {
        return primaryAddressList;
    }

    /**
     * Sets the value of the primaryAddressList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrimaryAddressList }
     *     
     */
    public void setPrimaryAddressList(PrimaryAddressList value) {
        this.primaryAddressList = value;
    }

    /**
     * Gets the value of the contactList property.
     * 
     * @return
     *     possible object is
     *     {@link ContactList }
     *     
     */
    public ContactList getContactList() {
        return contactList;
    }

    /**
     * Sets the value of the contactList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactList }
     *     
     */
    public void setContactList(ContactList value) {
        this.contactList = value;
    }

    /**
     * Gets the value of the primaryContactList property.
     * 
     * @return
     *     possible object is
     *     {@link PrimaryContactList }
     *     
     */
    public PrimaryContactList getPrimaryContactList() {
        return primaryContactList;
    }

    /**
     * Sets the value of the primaryContactList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrimaryContactList }
     *     
     */
    public void setPrimaryContactList(PrimaryContactList value) {
        this.primaryContactList = value;
    }

    /**
     * Gets the value of the classificationList property.
     * 
     * @return
     *     possible object is
     *     {@link ClassificationList }
     *     
     */
    public ClassificationList getClassificationList() {
        return classificationList;
    }

    /**
     * Sets the value of the classificationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassificationList }
     *     
     */
    public void setClassificationList(ClassificationList value) {
        this.classificationList = value;
    }

    /**
     * Gets the value of the diversityClassificationList property.
     * 
     * @return
     *     possible object is
     *     {@link DiversityClassificationList }
     *     
     */
    public DiversityClassificationList getDiversityClassificationList() {
        return diversityClassificationList;
    }

    /**
     * Sets the value of the diversityClassificationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiversityClassificationList }
     *     
     */
    public void setDiversityClassificationList(DiversityClassificationList value) {
        this.diversityClassificationList = value;
    }

    /**
     * Gets the value of the locationList property.
     * 
     * @return
     *     possible object is
     *     {@link LocationList }
     *     
     */
    public LocationList getLocationList() {
        return locationList;
    }

    /**
     * Sets the value of the locationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationList }
     *     
     */
    public void setLocationList(LocationList value) {
        this.locationList = value;
    }

    /**
     * Gets the value of the restrictFulfillmentLocationsByBusinessUnit property.
     * 
     * @return
     *     possible object is
     *     {@link RestrictFulfillmentLocationsByBusinessUnit }
     *     
     */
    public RestrictFulfillmentLocationsByBusinessUnit getRestrictFulfillmentLocationsByBusinessUnit() {
        return restrictFulfillmentLocationsByBusinessUnit;
    }

    /**
     * Sets the value of the restrictFulfillmentLocationsByBusinessUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestrictFulfillmentLocationsByBusinessUnit }
     *     
     */
    public void setRestrictFulfillmentLocationsByBusinessUnit(RestrictFulfillmentLocationsByBusinessUnit value) {
        this.restrictFulfillmentLocationsByBusinessUnit = value;
    }

    /**
     * Gets the value of the customElementList property.
     * 
     * @return
     *     possible object is
     *     {@link CustomElementList }
     *     
     */
    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    /**
     * Sets the value of the customElementList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomElementList }
     *     
     */
    public void setCustomElementList(CustomElementList value) {
        this.customElementList = value;
    }

    /**
     * Gets the value of the accountsPayableList property.
     * 
     * @return
     *     possible object is
     *     {@link AccountsPayableList }
     *     
     */
    public AccountsPayableList getAccountsPayableList() {
        return accountsPayableList;
    }

    /**
     * Sets the value of the accountsPayableList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountsPayableList }
     *     
     */
    public void setAccountsPayableList(AccountsPayableList value) {
        this.accountsPayableList = value;
    }

    /**
     * Gets the value of the taxInformationList property.
     * 
     * @return
     *     possible object is
     *     {@link TaxInformationList }
     *     
     */
    public TaxInformationList getTaxInformationList() {
        return taxInformationList;
    }

    /**
     * Sets the value of the taxInformationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxInformationList }
     *     
     */
    public void setTaxInformationList(TaxInformationList value) {
        this.taxInformationList = value;
    }

    /**
     * Gets the value of the insuranceInformationList property.
     * 
     * @return
     *     possible object is
     *     {@link InsuranceInformationList }
     *     
     */
    public InsuranceInformationList getInsuranceInformationList() {
        return insuranceInformationList;
    }

    /**
     * Sets the value of the insuranceInformationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsuranceInformationList }
     *     
     */
    public void setInsuranceInformationList(InsuranceInformationList value) {
        this.insuranceInformationList = value;
    }

    /**
     * Gets the value of the enablePaymentProvisioning property.
     * 
     * @return
     *     possible object is
     *     {@link EnablePaymentProvisioning }
     *     
     */
    public EnablePaymentProvisioning getEnablePaymentProvisioning() {
        return enablePaymentProvisioning;
    }

    /**
     * Sets the value of the enablePaymentProvisioning property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnablePaymentProvisioning }
     *     
     */
    public void setEnablePaymentProvisioning(EnablePaymentProvisioning value) {
        this.enablePaymentProvisioning = value;
    }

}
