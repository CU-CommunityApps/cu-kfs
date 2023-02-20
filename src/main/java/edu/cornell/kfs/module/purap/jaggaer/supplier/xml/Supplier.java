
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "ownerOrganization", "organization", "name", "doingBusinessAs", "countryOfOrigin",
        "active", "comingSoon", "newSupplier", "supplierNumber", "baselineSupplierNumber", "thirdPartyRefNumber",
        "baselineThirdPartyRefNumber", "vendorId", "webSiteURL", "duns", "legalStructure", "taxIdentificationType",
        "federalIDNumber", "taxIdentificationNumber", "numberOfEmployees", "dateEstablished", "annualSalesList",
        "geographicServiceArea", "customerCommodityCodeList", "commodityCode", "austinTetra", "sic", "naicsCodes",
        "naics", "promotionalText", "preferredFulfillmentCenterId", "preferredRemitToId", "preferredTechnicalContactId",
        "preferredCatalogContactId", "preferredCustomerCareContactId", "preferredCorporateContactId",
        "preferredPOFailureContactId", "sciCatSupplier", "exposeListPrice", "useListPriceRule", "allowNonCatalogItems",
        "allowFormPurchases", "supportedCurrencies", "enabledCurrencies", "supplierKeywords", "supplierContactInfo",
        "classification", "defaultFCConfig", "fulfillmentCenter", "hiddenConfiguration", "cxmlSettings",
        "restrictFulfillmentCentersByBusinessUnit", "diversityClassificationList" })
@XmlRootElement(name = "Supplier")
public class Supplier {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlElement(name = "OwnerOrganization")
    protected OwnerOrganization ownerOrganization;
    @XmlElement(name = "Organization")
    protected Organization organization;
    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "DoingBusinessAs")
    protected DoingBusinessAs doingBusinessAs;
    @XmlElement(name = "CountryOfOrigin")
    protected CountryOfOrigin countryOfOrigin;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "ComingSoon")
    protected ComingSoon comingSoon;
    @XmlElement(name = "NewSupplier")
    protected NewSupplier newSupplier;
    @XmlElement(name = "SupplierNumber")
    protected SupplierNumber supplierNumber;
    @XmlElement(name = "BaselineSupplierNumber")
    protected BaselineSupplierNumber baselineSupplierNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "BaselineThirdPartyRefNumber")
    protected BaselineThirdPartyRefNumber baselineThirdPartyRefNumber;
    @XmlElement(name = "VendorId")
    protected List<VendorId> vendorId;
    @XmlElement(name = "WebSiteURL")
    protected WebSiteURL webSiteURL;
    @XmlElement(name = "DUNS")
    protected DUNS duns;
    @XmlElement(name = "LegalStructure")
    protected LegalStructure legalStructure;
    @XmlElement(name = "TaxIdentificationType")
    protected String taxIdentificationType;
    @XmlElement(name = "FederalIDNumber")
    protected FederalIDNumber federalIDNumber;
    @XmlElement(name = "TaxIdentificationNumber")
    protected String taxIdentificationNumber;
    @XmlElement(name = "NumberOfEmployees")
    protected NumberOfEmployees numberOfEmployees;
    @XmlElement(name = "DateEstablished")
    protected DateEstablished dateEstablished;
    @XmlElement(name = "AnnualSalesList")
    protected AnnualSalesList annualSalesList;
    @XmlElement(name = "GeographicServiceArea")
    protected GeographicServiceArea geographicServiceArea;
    @XmlElement(name = "CustomerCommodityCodeList")
    protected CustomerCommodityCodeList customerCommodityCodeList;
    @XmlElement(name = "CommodityCode")
    protected CommodityCode commodityCode;
    @XmlElement(name = "AustinTetra")
    protected AustinTetra austinTetra;
    @XmlElement(name = "SIC")
    protected SIC sic;
    @XmlElement(name = "NaicsCodes")
    protected NaicsCodes naicsCodes;
    @XmlElement(name = "NAICS")
    protected NAICS naics;
    @XmlElement(name = "PromotionalText")
    protected PromotionalText promotionalText;
    @XmlElement(name = "PreferredFulfillmentCenterId")
    protected PreferredFulfillmentCenterId preferredFulfillmentCenterId;
    @XmlElement(name = "PreferredRemitToId")
    protected PreferredRemitToId preferredRemitToId;
    @XmlElement(name = "PreferredTechnicalContactId")
    protected PreferredTechnicalContactId preferredTechnicalContactId;
    @XmlElement(name = "PreferredCatalogContactId")
    protected PreferredCatalogContactId preferredCatalogContactId;
    @XmlElement(name = "PreferredCustomerCareContactId")
    protected PreferredCustomerCareContactId preferredCustomerCareContactId;
    @XmlElement(name = "PreferredCorporateContactId")
    protected PreferredCorporateContactId preferredCorporateContactId;
    @XmlElement(name = "PreferredPOFailureContactId")
    protected PreferredPOFailureContactId preferredPOFailureContactId;
    @XmlElement(name = "SciCatSupplier")
    protected String sciCatSupplier;
    @XmlElement(name = "ExposeListPrice")
    protected ExposeListPrice exposeListPrice;
    @XmlElement(name = "UseListPriceRule")
    protected UseListPriceRule useListPriceRule;
    @XmlElement(name = "AllowNonCatalogItems")
    protected AllowNonCatalogItems allowNonCatalogItems;
    @XmlElement(name = "AllowFormPurchases")
    protected AllowFormPurchases allowFormPurchases;
    @XmlElement(name = "SupportedCurrencies")
    protected SupportedCurrencies supportedCurrencies;
    @XmlElement(name = "EnabledCurrencies")
    protected EnabledCurrencies enabledCurrencies;
    @XmlElement(name = "SupplierKeywords")
    protected SupplierKeywords supplierKeywords;
    @XmlElement(name = "SupplierContactInfo")
    protected List<SupplierContactInfo> supplierContactInfo;
    @XmlElement(name = "Classification")
    protected List<Classification> classification;
    @XmlElement(name = "DefaultFCConfig")
    protected DefaultFCConfig defaultFCConfig;
    @XmlElement(name = "FulfillmentCenter")
    protected List<FulfillmentCenter> fulfillmentCenter;
    @XmlElement(name = "HiddenConfiguration")
    protected HiddenConfiguration hiddenConfiguration;
    @XmlElement(name = "CXMLSettings")
    protected CXMLSettings cxmlSettings;
    @XmlElement(name = "RestrictFulfillmentCentersByBusinessUnit")
    protected String restrictFulfillmentCentersByBusinessUnit;
    @XmlElement(name = "DiversityClassificationList")
    protected DiversityClassificationList diversityClassificationList;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public OwnerOrganization getOwnerOrganization() {
        return ownerOrganization;
    }

    public void setOwnerOrganization(OwnerOrganization value) {
        this.ownerOrganization = value;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization value) {
        this.organization = value;
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

    public CountryOfOrigin getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(CountryOfOrigin value) {
        this.countryOfOrigin = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public ComingSoon getComingSoon() {
        return comingSoon;
    }

    public void setComingSoon(ComingSoon value) {
        this.comingSoon = value;
    }

    public NewSupplier getNewSupplier() {
        return newSupplier;
    }

    public void setNewSupplier(NewSupplier value) {
        this.newSupplier = value;
    }

    public SupplierNumber getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(SupplierNumber value) {
        this.supplierNumber = value;
    }

    public BaselineSupplierNumber getBaselineSupplierNumber() {
        return baselineSupplierNumber;
    }

    public void setBaselineSupplierNumber(BaselineSupplierNumber value) {
        this.baselineSupplierNumber = value;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    public BaselineThirdPartyRefNumber getBaselineThirdPartyRefNumber() {
        return baselineThirdPartyRefNumber;
    }

    public void setBaselineThirdPartyRefNumber(BaselineThirdPartyRefNumber value) {
        this.baselineThirdPartyRefNumber = value;
    }

    public List<VendorId> getVendorId() {
        if (vendorId == null) {
            vendorId = new ArrayList<VendorId>();
        }
        return this.vendorId;
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

    public String getTaxIdentificationType() {
        return taxIdentificationType;
    }

    public void setTaxIdentificationType(String value) {
        this.taxIdentificationType = value;
    }

    public FederalIDNumber getFederalIDNumber() {
        return federalIDNumber;
    }

    public void setFederalIDNumber(FederalIDNumber value) {
        this.federalIDNumber = value;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String value) {
        this.taxIdentificationNumber = value;
    }

    public NumberOfEmployees getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(NumberOfEmployees value) {
        this.numberOfEmployees = value;
    }

    public DateEstablished getDateEstablished() {
        return dateEstablished;
    }

    public void setDateEstablished(DateEstablished value) {
        this.dateEstablished = value;
    }

    public AnnualSalesList getAnnualSalesList() {
        return annualSalesList;
    }

    public void setAnnualSalesList(AnnualSalesList value) {
        this.annualSalesList = value;
    }

    public GeographicServiceArea getGeographicServiceArea() {
        return geographicServiceArea;
    }

    public void setGeographicServiceArea(GeographicServiceArea value) {
        this.geographicServiceArea = value;
    }

    public CustomerCommodityCodeList getCustomerCommodityCodeList() {
        return customerCommodityCodeList;
    }

    public void setCustomerCommodityCodeList(CustomerCommodityCodeList value) {
        this.customerCommodityCodeList = value;
    }

    public CommodityCode getCommodityCode() {
        return commodityCode;
    }

    public void setCommodityCode(CommodityCode value) {
        this.commodityCode = value;
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

    public NAICS getNAICS() {
        return naics;
    }

    public void setNAICS(NAICS value) {
        this.naics = value;
    }

    public PromotionalText getPromotionalText() {
        return promotionalText;
    }

    public void setPromotionalText(PromotionalText value) {
        this.promotionalText = value;
    }

    public PreferredFulfillmentCenterId getPreferredFulfillmentCenterId() {
        return preferredFulfillmentCenterId;
    }

    public void setPreferredFulfillmentCenterId(PreferredFulfillmentCenterId value) {
        this.preferredFulfillmentCenterId = value;
    }

    public PreferredRemitToId getPreferredRemitToId() {
        return preferredRemitToId;
    }

    public void setPreferredRemitToId(PreferredRemitToId value) {
        this.preferredRemitToId = value;
    }

    public PreferredTechnicalContactId getPreferredTechnicalContactId() {
        return preferredTechnicalContactId;
    }

    public void setPreferredTechnicalContactId(PreferredTechnicalContactId value) {
        this.preferredTechnicalContactId = value;
    }

    public PreferredCatalogContactId getPreferredCatalogContactId() {
        return preferredCatalogContactId;
    }

    public void setPreferredCatalogContactId(PreferredCatalogContactId value) {
        this.preferredCatalogContactId = value;
    }

    public PreferredCustomerCareContactId getPreferredCustomerCareContactId() {
        return preferredCustomerCareContactId;
    }

    public void setPreferredCustomerCareContactId(PreferredCustomerCareContactId value) {
        this.preferredCustomerCareContactId = value;
    }

    public PreferredCorporateContactId getPreferredCorporateContactId() {
        return preferredCorporateContactId;
    }

    public void setPreferredCorporateContactId(PreferredCorporateContactId value) {
        this.preferredCorporateContactId = value;
    }

    public PreferredPOFailureContactId getPreferredPOFailureContactId() {
        return preferredPOFailureContactId;
    }

    public void setPreferredPOFailureContactId(PreferredPOFailureContactId value) {
        this.preferredPOFailureContactId = value;
    }

    public String getSciCatSupplier() {
        return sciCatSupplier;
    }

    public void setSciCatSupplier(String value) {
        this.sciCatSupplier = value;
    }

    public ExposeListPrice getExposeListPrice() {
        return exposeListPrice;
    }

    public void setExposeListPrice(ExposeListPrice value) {
        this.exposeListPrice = value;
    }

    public UseListPriceRule getUseListPriceRule() {
        return useListPriceRule;
    }

    public void setUseListPriceRule(UseListPriceRule value) {
        this.useListPriceRule = value;
    }

    public AllowNonCatalogItems getAllowNonCatalogItems() {
        return allowNonCatalogItems;
    }

    public void setAllowNonCatalogItems(AllowNonCatalogItems value) {
        this.allowNonCatalogItems = value;
    }

    public AllowFormPurchases getAllowFormPurchases() {
        return allowFormPurchases;
    }

    public void setAllowFormPurchases(AllowFormPurchases value) {
        this.allowFormPurchases = value;
    }

    public SupportedCurrencies getSupportedCurrencies() {
        return supportedCurrencies;
    }

    public void setSupportedCurrencies(SupportedCurrencies value) {
        this.supportedCurrencies = value;
    }

    public EnabledCurrencies getEnabledCurrencies() {
        return enabledCurrencies;
    }

    public void setEnabledCurrencies(EnabledCurrencies value) {
        this.enabledCurrencies = value;
    }

    public SupplierKeywords getSupplierKeywords() {
        return supplierKeywords;
    }

    public void setSupplierKeywords(SupplierKeywords value) {
        this.supplierKeywords = value;
    }

    public List<SupplierContactInfo> getSupplierContactInfo() {
        if (supplierContactInfo == null) {
            supplierContactInfo = new ArrayList<SupplierContactInfo>();
        }
        return this.supplierContactInfo;
    }

    public List<Classification> getClassification() {
        if (classification == null) {
            classification = new ArrayList<Classification>();
        }
        return this.classification;
    }

    public DefaultFCConfig getDefaultFCConfig() {
        return defaultFCConfig;
    }

    public void setDefaultFCConfig(DefaultFCConfig value) {
        this.defaultFCConfig = value;
    }

    public List<FulfillmentCenter> getFulfillmentCenter() {
        if (fulfillmentCenter == null) {
            fulfillmentCenter = new ArrayList<FulfillmentCenter>();
        }
        return this.fulfillmentCenter;
    }

    public HiddenConfiguration getHiddenConfiguration() {
        return hiddenConfiguration;
    }

    public void setHiddenConfiguration(HiddenConfiguration value) {
        this.hiddenConfiguration = value;
    }

    public CXMLSettings getCXMLSettings() {
        return cxmlSettings;
    }

    public void setCXMLSettings(CXMLSettings value) {
        this.cxmlSettings = value;
    }

    public String getRestrictFulfillmentCentersByBusinessUnit() {
        return restrictFulfillmentCentersByBusinessUnit;
    }

    public void setRestrictFulfillmentCentersByBusinessUnit(String value) {
        this.restrictFulfillmentCentersByBusinessUnit = value;
    }

    public DiversityClassificationList getDiversityClassificationList() {
        return diversityClassificationList;
    }

    public void setDiversityClassificationList(DiversityClassificationList value) {
        this.diversityClassificationList = value;
    }

}
