package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

/*
 *
 * Each concrete business object created will:
 *  (1) Require a corresponding OJB table definition in file resources/edu/cornell/kfs/cemi/patterntemplate/batch/cu-ojb-cemi-patterntemplate.xml
 *  (2) Require a corresponding nonprod-sql database table SQL creation script. Details are in that example file.
 *  (3) Be referenced in a Cemi{EXTRACTNAME}ExtractFileOutputDefinition.xml
 *  (4) Be populated by a corresponding business object factory class Cemi{EXTRACTNAME}File{TABNAME}TabRowBoFactory. Details are in that example file.
 */

public class CemiSponsorFileSponsorTabRowBo extends CemiIndexedBusinessObjectBase {

    // Attributes to make the data extract spreadsheet row database searchable and
    // identifiable by data extraction run date and legacy data source.
    // Abstract class attributes that are controlled by the CemiOrmDataBuilderBase
    // class
    // and not the business object factory:
    // private String jobRunDateString;
    // private Long jobRunRowIndex;

    // These items would be legacy system business object keys needed to obtain the
    // data representing
    // the tab row's values held in the remaining attributes in the section below.
//FIXME ADD LEGACY BO KEY attributes and getters/setters

    // Attributes representing a single data extract spreadsheet row
    private String spreadsheetKey;
    private String addOnly;
    private String sponsor;
    private String sponsorId;
    private String sponsorReferenceId;
    private String sponsorName;
    private String sponsorType;
    private String paymentTerms;
    private String defaultPaymentType;

    private String businessEntityRowId;                         // template field name duplicated as rowId
    private String businessEntityName;
    private String businessEntityPhoneticName;
    private String businessEntityTaxId;
    private String externalEntityId;

    private String addressDataRowId;                            // template field name duplicated as rowId
    private String addressLineDataRowId;                        // template field name duplicated as rowId
    private String addressLineDataDescriptor;                   // template field name duplicated as descriptor
    private String addressLineDataType;                         // template field name duplicated as type
    private String addressLineData;
    private String municipality;
    private String countryCity;
    private String iso31661Alpha2Code;

    private String submunicipalityDataRowId;                    // template field name duplicated as rowId
    private String addressComponentName;
    private String submunicipalityDataType;                     // template field name duplicated as type
    private String submunicipalityData;
    private String countryRegion;
    private String countryRegionDescriptor;

    private String subRegionDataRowId;                          // template field name duplicated as rowId
    private String subRegionDataDescriptor;                     // template field name duplicated as descriptor
    private String subregionDataType;                           // template field name duplicated as type
    private String subregionData;
    private String postalCode;

    private String addressDataUsageDataRowId;                   // template field name duplicated as rowId
    private String addressDataUsageDataPublic;                  // template field name duplicated as public
    private String addressDataTypeDataRowId;                    // template field name duplicated as rowId
    private String addressDataTypeDataPrimary;                  // template field name duplicated as primary
    private String addressDataTypeDataType;                     // template field name duplicated as type
    private String useFor1;
    private String useFor2;
    private String useFor3;
    private String useForTenanted1;
    private String useForTenanted2;
    private String useForTenanted3;
    private String addressDataUsageDataComments;                // template field name duplicated as comments
    private String numberOfDays;
    private String municipalityLocal;
    private String address;
    private String addressId;

    private String phoneDataRowId;                              // template field name duplicated as rowId
    private String areaCode;
    private String tenantFormattedPhone;
    private String internationalFormattedPhone;
    private String phoneNumberWithoutAreaCode;
    private String nationalFormattedPhone;
    private String e164FormattedPhone;
    private String workdayTraditionalFormattedPhone;
    private String phoneDataDelete;                             // template field name duplicated as delete
    private String phoneDataDoNotReplaceAll;                    // template field name duplicated as doNotReplaceAll
    private String countryIsoCode;
    private String internationalPhoneCode;
    private String phoneNumber;
    private String phoneExtension;
    private String phoneDeviceType;

    private String phoneDataUsageDataRowId;                     // template field name duplicated as rowId
    private String phoneDataUsageDataPublic;                    // template field name duplicated as public
    private String phoneDataTypeDataRowId;                      // template field name duplicated as rowId
    private String phoneDataTypeDataPrimary;                    // template field name duplicated as primary
    private String phoneDataTypeDataType;                       // template field name duplicated as type
    private String phoneDataUsageDataUseFor;                    // template field name duplicated as useFor
    private String phoneDataUsageDataUseForTenanted;            // template field name duplicated as useForTenanted
    private String phoneDataUsageDataComments;                  // template field name duplicated as comments
    private String phone;
    private String phoneDataId;                                 // template field name duplicated as id

    private String emailAddressDataRowId;                       // template field name duplicated as rowId
    private String emailAddressDataDelete;                      // template field name duplicated as delete
    private String emailAddressDataDoNotReplaceAll;             // template field name duplicated as doNotReplaceAll
    private String emailAddress;
    private String emailComment;

    private String emailAddressDataUsageDataRowId;              // template field name duplicated as rowId
    private String emailAddressDataUsageDataPublic;             // template field name duplicated as public
    private String emailAddressDataTypeDataRowId;               // template field name duplicated as rowId
    private String emailAddressDataTypeDataPrimary;             // template field name duplicated as primary
    private String emailAddressDataTypeDataType;                // template field name duplicated as type
    private String emailAddressDataUsageDataUseFor;             // template field name duplicated as useFor
    private String emailAddressDataUsageDataUseForTenanted;     // template field name duplicated as useForTenanted
    private String emailAddressDataUsageDataComments;           // template field name duplicated as comments
    private String email;
    private String emailAddressDataId;                          // template field name duplicated as id

    private String webAddressDataRowId;                         // template field name duplicated as rowId
    private String webAddressDataDelete;                        // template field name duplicated as delete
    private String webAddressDataDoNotReplaceAll;               // template field name duplicated as doNotReplaceAll
    private String webAddressDataWebAddress;                    // template field name duplicated as webAddress
    private String webAddressComment;

    private String webAddressDataUsageDataRowId;                // template field name duplicated as rowId
    private String webAddressDataUsageDataPublic;               // template field name duplicated as public
    private String webAddressTypeDataRowId;                     // template field name duplicated as rowId
    private String webAddressTypeDataPrimary;                   // template field name duplicated as primary
    private String webAddressTypeDataType;                      // template field name duplicated as type
    private String webAddressDataUsageDataUseFor;               // template field name duplicated as useFor
    private String webAddressDataUsageDataUseForTenanted;       // template field name duplicated as useForTenanted
    private String webAddressDataUsageDataComments;             // template field name duplicated as comments
    private String webAddressDataUsageDataWebAddress;           // template field name duplicated as webAddress
    private String webAddressDataUsageDataId;                   // template field name duplicated as id
    private String fileName;
    private String image;
    private String remitFromSponsor;
    private String currency;
    private String acceptedCurrencies;
    private String sponsorPreferredLocale;
    private String invoiceDeliveryType;
    private String invoiceNotificationEmailRecipients;
    private String statementDeliveryType;
    private String statementNotificationEmailRecipients;
    private String includedChildren;

    private String alternateNameDataRowId;                      // template field name duplicated as rowId
    private String alternateName;
    private String alternateNameUsage;
    private String replaceAll;

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAddOnly() {
        return addOnly;
    }

    public void setAddOnly(String addOnly) {
        this.addOnly = addOnly;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(String sponsorId) {
        this.sponsorId = sponsorId;
    }

    public String getSponsorReferenceId() {
        return sponsorReferenceId;
    }

    public void setSponsorReferenceId(String sponsorReferenceId) {
        this.sponsorReferenceId = sponsorReferenceId;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getSponsorType() {
        return sponsorType;
    }

    public void setSponsorType(String sponsorType) {
        this.sponsorType = sponsorType;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDefaultPaymentType() {
        return defaultPaymentType;
    }

    public void setDefaultPaymentType(String defaultPaymentType) {
        this.defaultPaymentType = defaultPaymentType;
    }

    public String getBusinessEntityRowId() {
        return businessEntityRowId;
    }

    public void setBusinessEntityRowId(String businessEntityRowId) {
        this.businessEntityRowId = businessEntityRowId;
    }

    public String getBusinessEntityName() {
        return businessEntityName;
    }

    public void setBusinessEntityName(String businessEntityName) {
        this.businessEntityName = businessEntityName;
    }

    public String getBusinessEntityPhoneticName() {
        return businessEntityPhoneticName;
    }

    public void setBusinessEntityPhoneticName(String businessEntityPhoneticName) {
        this.businessEntityPhoneticName = businessEntityPhoneticName;
    }

    public String getBusinessEntityTaxId() {
        return businessEntityTaxId;
    }

    public void setBusinessEntityTaxId(String businessEntityTaxId) {
        this.businessEntityTaxId = businessEntityTaxId;
    }

    public String getExternalEntityId() {
        return externalEntityId;
    }

    public void setExternalEntityId(String externalEntityId) {
        this.externalEntityId = externalEntityId;
    }

    public String getAddressDataRowId() {
        return addressDataRowId;
    }

    public void setAddressDataRowId(String addressDataRowId) {
        this.addressDataRowId = addressDataRowId;
    }

    public String getAddressLineDataRowId() {
        return addressLineDataRowId;
    }

    public void setAddressLineDataRowId(String addressLineDataRowId) {
        this.addressLineDataRowId = addressLineDataRowId;
    }

    public String getAddressLineDataDescriptor() {
        return addressLineDataDescriptor;
    }

    public void setAddressLineDataDescriptor(String addressLineDataDescriptor) {
        this.addressLineDataDescriptor = addressLineDataDescriptor;
    }

    public String getAddressLineDataType() {
        return addressLineDataType;
    }

    public void setAddressLineDataType(String addressLineDataType) {
        this.addressLineDataType = addressLineDataType;
    }

    public String getAddressLineData() {
        return addressLineData;
    }

    public void setAddressLineData(String addressLineData) {
        this.addressLineData = addressLineData;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getCountryCity() {
        return countryCity;
    }

    public void setCountryCity(String countryCity) {
        this.countryCity = countryCity;
    }

    public String getIso31661Alpha2Code() {
        return iso31661Alpha2Code;
    }

    public void setIso31661Alpha2Code(String iso31661Alpha2Code) {
        this.iso31661Alpha2Code = iso31661Alpha2Code;
    }

    public String getSubmunicipalityDataRowId() {
        return submunicipalityDataRowId;
    }

    public void setSubmunicipalityDataRowId(String submunicipalityDataRowId) {
        this.submunicipalityDataRowId = submunicipalityDataRowId;
    }

    public String getAddressComponentName() {
        return addressComponentName;
    }

    public void setAddressComponentName(String addressComponentName) {
        this.addressComponentName = addressComponentName;
    }

    public String getSubmunicipalityDataType() {
        return submunicipalityDataType;
    }

    public void setSubmunicipalityDataType(String submunicipalityDataType) {
        this.submunicipalityDataType = submunicipalityDataType;
    }

    public String getSubmunicipalityData() {
        return submunicipalityData;
    }

    public void setSubmunicipalityData(String submunicipalityData) {
        this.submunicipalityData = submunicipalityData;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public String getCountryRegionDescriptor() {
        return countryRegionDescriptor;
    }

    public void setCountryRegionDescriptor(String countryRegionDescriptor) {
        this.countryRegionDescriptor = countryRegionDescriptor;
    }

    public String getSubRegionDataRowId() {
        return subRegionDataRowId;
    }

    public void setSubRegionDataRowId(String subRegionDataRowId) {
        this.subRegionDataRowId = subRegionDataRowId;
    }

    public String getSubRegionDataDescriptor() {
        return subRegionDataDescriptor;
    }

    public void setSubRegionDataDescriptor(String subRegionDataDescriptor) {
        this.subRegionDataDescriptor = subRegionDataDescriptor;
    }

    public String getSubregionDataType() {
        return subregionDataType;
    }

    public void setSubregionDataType(String subregionDataType) {
        this.subregionDataType = subregionDataType;
    }

    public String getSubregionData() {
        return subregionData;
    }

    public void setSubregionData(String subregionData) {
        this.subregionData = subregionData;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressDataUsageDataRowId() {
        return addressDataUsageDataRowId;
    }

    public void setAddressDataUsageDataRowId(String addressDataUsageDataRowId) {
        this.addressDataUsageDataRowId = addressDataUsageDataRowId;
    }

    public String getAddressDataUsageDataPublic() {
        return addressDataUsageDataPublic;
    }

    public void setAddressDataUsageDataPublic(String addressDataUsageDataPublic) {
        this.addressDataUsageDataPublic = addressDataUsageDataPublic;
    }

    public String getAddressDataTypeDataRowId() {
        return addressDataTypeDataRowId;
    }

    public void setAddressDataTypeDataRowId(String addressDataTypeDataRowId) {
        this.addressDataTypeDataRowId = addressDataTypeDataRowId;
    }

    public String getAddressDataTypeDataPrimary() {
        return addressDataTypeDataPrimary;
    }

    public void setAddressDataTypeDataPrimary(String addressDataTypeDataPrimary) {
        this.addressDataTypeDataPrimary = addressDataTypeDataPrimary;
    }

    public String getAddressDataTypeDataType() {
        return addressDataTypeDataType;
    }

    public void setAddressDataTypeDataType(String addressDataTypeDataType) {
        this.addressDataTypeDataType = addressDataTypeDataType;
    }

    public String getUseFor1() {
        return useFor1;
    }

    public void setUseFor1(String useFor1) {
        this.useFor1 = useFor1;
    }

    public String getUseFor2() {
        return useFor2;
    }

    public void setUseFor2(String useFor2) {
        this.useFor2 = useFor2;
    }

    public String getUseFor3() {
        return useFor3;
    }

    public void setUseFor3(String useFor3) {
        this.useFor3 = useFor3;
    }

    public String getUseForTenanted1() {
        return useForTenanted1;
    }

    public void setUseForTenanted1(String useForTenanted1) {
        this.useForTenanted1 = useForTenanted1;
    }

    public String getUseForTenanted2() {
        return useForTenanted2;
    }

    public void setUseForTenanted2(String useForTenanted2) {
        this.useForTenanted2 = useForTenanted2;
    }

    public String getUseForTenanted3() {
        return useForTenanted3;
    }

    public void setUseForTenanted3(String useForTenanted3) {
        this.useForTenanted3 = useForTenanted3;
    }

    public String getAddressDataUsageDataComments() {
        return addressDataUsageDataComments;
    }

    public void setAddressDataUsageDataComments(String addressDataUsageDataComments) {
        this.addressDataUsageDataComments = addressDataUsageDataComments;
    }

    public String getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(String numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getMunicipalityLocal() {
        return municipalityLocal;
    }

    public void setMunicipalityLocal(String municipalityLocal) {
        this.municipalityLocal = municipalityLocal;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getPhoneDataRowId() {
        return phoneDataRowId;
    }

    public void setPhoneDataRowId(String phoneDataRowId) {
        this.phoneDataRowId = phoneDataRowId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getTenantFormattedPhone() {
        return tenantFormattedPhone;
    }

    public void setTenantFormattedPhone(String tenantFormattedPhone) {
        this.tenantFormattedPhone = tenantFormattedPhone;
    }

    public String getInternationalFormattedPhone() {
        return internationalFormattedPhone;
    }

    public void setInternationalFormattedPhone(String internationalFormattedPhone) {
        this.internationalFormattedPhone = internationalFormattedPhone;
    }

    public String getPhoneNumberWithoutAreaCode() {
        return phoneNumberWithoutAreaCode;
    }

    public void setPhoneNumberWithoutAreaCode(String phoneNumberWithoutAreaCode) {
        this.phoneNumberWithoutAreaCode = phoneNumberWithoutAreaCode;
    }

    public String getNationalFormattedPhone() {
        return nationalFormattedPhone;
    }

    public void setNationalFormattedPhone(String nationalFormattedPhone) {
        this.nationalFormattedPhone = nationalFormattedPhone;
    }

    public String getE164FormattedPhone() {
        return e164FormattedPhone;
    }

    public void setE164FormattedPhone(String e164FormattedPhone) {
        this.e164FormattedPhone = e164FormattedPhone;
    }

    public String getWorkdayTraditionalFormattedPhone() {
        return workdayTraditionalFormattedPhone;
    }

    public void setWorkdayTraditionalFormattedPhone(String workdayTraditionalFormattedPhone) {
        this.workdayTraditionalFormattedPhone = workdayTraditionalFormattedPhone;
    }

    public String getPhoneDataDelete() {
        return phoneDataDelete;
    }

    public void setPhoneDataDelete(String phoneDataDelete) {
        this.phoneDataDelete = phoneDataDelete;
    }

    public String getPhoneDataDoNotReplaceAll() {
        return phoneDataDoNotReplaceAll;
    }

    public void setPhoneDataDoNotReplaceAll(String phoneDataDoNotReplaceAll) {
        this.phoneDataDoNotReplaceAll = phoneDataDoNotReplaceAll;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    public String getInternationalPhoneCode() {
        return internationalPhoneCode;
    }

    public void setInternationalPhoneCode(String internationalPhoneCode) {
        this.internationalPhoneCode = internationalPhoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneDeviceType() {
        return phoneDeviceType;
    }

    public void setPhoneDeviceType(String phoneDeviceType) {
        this.phoneDeviceType = phoneDeviceType;
    }

    public String getPhoneDataUsageDataRowId() {
        return phoneDataUsageDataRowId;
    }

    public void setPhoneDataUsageDataRowId(String phoneDataUsageDataRowId) {
        this.phoneDataUsageDataRowId = phoneDataUsageDataRowId;
    }

    public String getPhoneDataUsageDataPublic() {
        return phoneDataUsageDataPublic;
    }

    public void setPhoneDataUsageDataPublic(String phoneDataUsageDataPublic) {
        this.phoneDataUsageDataPublic = phoneDataUsageDataPublic;
    }

    public String getPhoneDataTypeDataRowId() {
        return phoneDataTypeDataRowId;
    }

    public void setPhoneDataTypeDataRowId(String phoneDataTypeDataRowId) {
        this.phoneDataTypeDataRowId = phoneDataTypeDataRowId;
    }

    public String getPhoneDataTypeDataPrimary() {
        return phoneDataTypeDataPrimary;
    }

    public void setPhoneDataTypeDataPrimary(String phoneDataTypeDataPrimary) {
        this.phoneDataTypeDataPrimary = phoneDataTypeDataPrimary;
    }

    public String getPhoneDataTypeDataType() {
        return phoneDataTypeDataType;
    }

    public void setPhoneDataTypeDataType(String phoneDataTypeDataType) {
        this.phoneDataTypeDataType = phoneDataTypeDataType;
    }

    public String getPhoneDataUsageDataUseFor() {
        return phoneDataUsageDataUseFor;
    }

    public void setPhoneDataUsageDataUseFor(String phoneDataUsageDataUseFor) {
        this.phoneDataUsageDataUseFor = phoneDataUsageDataUseFor;
    }

    public String getPhoneDataUsageDataUseForTenanted() {
        return phoneDataUsageDataUseForTenanted;
    }

    public void setPhoneDataUsageDataUseForTenanted(String phoneDataUsageDataUseForTenanted) {
        this.phoneDataUsageDataUseForTenanted = phoneDataUsageDataUseForTenanted;
    }

    public String getPhoneDataUsageDataComments() {
        return phoneDataUsageDataComments;
    }

    public void setPhoneDataUsageDataComments(String phoneDataUsageDataComments) {
        this.phoneDataUsageDataComments = phoneDataUsageDataComments;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneDataId() {
        return phoneDataId;
    }

    public void setPhoneDataId(String phoneDataId) {
        this.phoneDataId = phoneDataId;
    }

    public String getEmailAddressDataRowId() {
        return emailAddressDataRowId;
    }

    public void setEmailAddressDataRowId(String emailAddressDataRowId) {
        this.emailAddressDataRowId = emailAddressDataRowId;
    }

    public String getEmailAddressDataDelete() {
        return emailAddressDataDelete;
    }

    public void setEmailAddressDataDelete(String emailAddressDataDelete) {
        this.emailAddressDataDelete = emailAddressDataDelete;
    }

    public String getEmailAddressDataDoNotReplaceAll() {
        return emailAddressDataDoNotReplaceAll;
    }

    public void setEmailAddressDataDoNotReplaceAll(String emailAddressDataDoNotReplaceAll) {
        this.emailAddressDataDoNotReplaceAll = emailAddressDataDoNotReplaceAll;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailComment() {
        return emailComment;
    }

    public void setEmailComment(String emailComment) {
        this.emailComment = emailComment;
    }

    public String getEmailAddressDataUsageDataRowId() {
        return emailAddressDataUsageDataRowId;
    }

    public void setEmailAddressDataUsageDataRowId(String emailAddressDataUsageDataRowId) {
        this.emailAddressDataUsageDataRowId = emailAddressDataUsageDataRowId;
    }

    public String getEmailAddressDataUsageDataPublic() {
        return emailAddressDataUsageDataPublic;
    }

    public void setEmailAddressDataUsageDataPublic(String emailAddressDataUsageDataPublic) {
        this.emailAddressDataUsageDataPublic = emailAddressDataUsageDataPublic;
    }

    public String getEmailAddressDataTypeDataRowId() {
        return emailAddressDataTypeDataRowId;
    }

    public void setEmailAddressDataTypeDataRowId(String emailAddressDataTypeDataRowId) {
        this.emailAddressDataTypeDataRowId = emailAddressDataTypeDataRowId;
    }

    public String getEmailAddressDataTypeDataPrimary() {
        return emailAddressDataTypeDataPrimary;
    }

    public void setEmailAddressDataTypeDataPrimary(String emailAddressDataTypeDataPrimary) {
        this.emailAddressDataTypeDataPrimary = emailAddressDataTypeDataPrimary;
    }

    public String getEmailAddressDataTypeDataType() {
        return emailAddressDataTypeDataType;
    }

    public void setEmailAddressDataTypeDataType(String emailAddressDataTypeDataType) {
        this.emailAddressDataTypeDataType = emailAddressDataTypeDataType;
    }

    public String getEmailAddressDataUsageDataUseFor() {
        return emailAddressDataUsageDataUseFor;
    }

    public void setEmailAddressDataUsageDataUseFor(String emailAddressDataUsageDataUseFor) {
        this.emailAddressDataUsageDataUseFor = emailAddressDataUsageDataUseFor;
    }

    public String getEmailAddressDataUsageDataUseForTenanted() {
        return emailAddressDataUsageDataUseForTenanted;
    }

    public void setEmailAddressDataUsageDataUseForTenanted(String emailAddressDataUsageDataUseForTenanted) {
        this.emailAddressDataUsageDataUseForTenanted = emailAddressDataUsageDataUseForTenanted;
    }

    public String getEmailAddressDataUsageDataComments() {
        return emailAddressDataUsageDataComments;
    }

    public void setEmailAddressDataUsageDataComments(String emailAddressDataUsageDataComments) {
        this.emailAddressDataUsageDataComments = emailAddressDataUsageDataComments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailAddressDataId() {
        return emailAddressDataId;
    }

    public void setEmailAddressDataId(String emailAddressDataId) {
        this.emailAddressDataId = emailAddressDataId;
    }

    public String getWebAddressDataRowId() {
        return webAddressDataRowId;
    }

    public void setWebAddressDataRowId(String webAddressDataRowId) {
        this.webAddressDataRowId = webAddressDataRowId;
    }

    public String getWebAddressDataDelete() {
        return webAddressDataDelete;
    }

    public void setWebAddressDataDelete(String webAddressDataDelete) {
        this.webAddressDataDelete = webAddressDataDelete;
    }

    public String getWebAddressDataDoNotReplaceAll() {
        return webAddressDataDoNotReplaceAll;
    }

    public void setWebAddressDataDoNotReplaceAll(String webAddressDataDoNotReplaceAll) {
        this.webAddressDataDoNotReplaceAll = webAddressDataDoNotReplaceAll;
    }

    public String getWebAddressDataWebAddress() {
        return webAddressDataWebAddress;
    }

    public void setWebAddressDataWebAddress(String webAddressDataWebAddress) {
        this.webAddressDataWebAddress = webAddressDataWebAddress;
    }

    public String getWebAddressComment() {
        return webAddressComment;
    }

    public void setWebAddressComment(String webAddressComment) {
        this.webAddressComment = webAddressComment;
    }

    public String getWebAddressDataUsageDataRowId() {
        return webAddressDataUsageDataRowId;
    }

    public void setWebAddressDataUsageDataRowId(String webAddressDataUsageDataRowId) {
        this.webAddressDataUsageDataRowId = webAddressDataUsageDataRowId;
    }

    public String getWebAddressDataUsageDataPublic() {
        return webAddressDataUsageDataPublic;
    }

    public void setWebAddressDataUsageDataPublic(String webAddressDataUsageDataPublic) {
        this.webAddressDataUsageDataPublic = webAddressDataUsageDataPublic;
    }

    public String getWebAddressTypeDataRowId() {
        return webAddressTypeDataRowId;
    }

    public void setWebAddressTypeDataRowId(String webAddressTypeDataRowId) {
        this.webAddressTypeDataRowId = webAddressTypeDataRowId;
    }

    public String getWebAddressTypeDataPrimary() {
        return webAddressTypeDataPrimary;
    }

    public void setWebAddressTypeDataPrimary(String webAddressTypeDataPrimary) {
        this.webAddressTypeDataPrimary = webAddressTypeDataPrimary;
    }

    public String getWebAddressTypeDataType() {
        return webAddressTypeDataType;
    }

    public void setWebAddressTypeDataType(String webAddressTypeDataType) {
        this.webAddressTypeDataType = webAddressTypeDataType;
    }

    public String getWebAddressDataUsageDataUseFor() {
        return webAddressDataUsageDataUseFor;
    }

    public void setWebAddressDataUsageDataUseFor(String webAddressDataUsageDataUseFor) {
        this.webAddressDataUsageDataUseFor = webAddressDataUsageDataUseFor;
    }

    public String getWebAddressDataUsageDataUseForTenanted() {
        return webAddressDataUsageDataUseForTenanted;
    }

    public void setWebAddressDataUsageDataUseForTenanted(String webAddressDataUsageDataUseForTenanted) {
        this.webAddressDataUsageDataUseForTenanted = webAddressDataUsageDataUseForTenanted;
    }

    public String getWebAddressDataUsageDataComments() {
        return webAddressDataUsageDataComments;
    }

    public void setWebAddressDataUsageDataComments(String webAddressDataUsageDataComments) {
        this.webAddressDataUsageDataComments = webAddressDataUsageDataComments;
    }

    public String getWebAddressDataUsageDataWebAddress() {
        return webAddressDataUsageDataWebAddress;
    }

    public void setWebAddressDataUsageDataWebAddress(String webAddressDataUsageDataWebAddress) {
        this.webAddressDataUsageDataWebAddress = webAddressDataUsageDataWebAddress;
    }

    public String getWebAddressDataUsageDataId() {
        return webAddressDataUsageDataId;
    }

    public void setWebAddressDataUsageDataId(String webAddressDataUsageDataId) {
        this.webAddressDataUsageDataId = webAddressDataUsageDataId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRemitFromSponsor() {
        return remitFromSponsor;
    }

    public void setRemitFromSponsor(String remitFromSponsor) {
        this.remitFromSponsor = remitFromSponsor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAcceptedCurrencies() {
        return acceptedCurrencies;
    }

    public void setAcceptedCurrencies(String acceptedCurrencies) {
        this.acceptedCurrencies = acceptedCurrencies;
    }

    public String getSponsorPreferredLocale() {
        return sponsorPreferredLocale;
    }

    public void setSponsorPreferredLocale(String sponsorPreferredLocale) {
        this.sponsorPreferredLocale = sponsorPreferredLocale;
    }

    public String getInvoiceDeliveryType() {
        return invoiceDeliveryType;
    }

    public void setInvoiceDeliveryType(String invoiceDeliveryType) {
        this.invoiceDeliveryType = invoiceDeliveryType;
    }

    public String getInvoiceNotificationEmailRecipients() {
        return invoiceNotificationEmailRecipients;
    }

    public void setInvoiceNotificationEmailRecipients(String invoiceNotificationEmailRecipients) {
        this.invoiceNotificationEmailRecipients = invoiceNotificationEmailRecipients;
    }

    public String getStatementDeliveryType() {
        return statementDeliveryType;
    }

    public void setStatementDeliveryType(String statementDeliveryType) {
        this.statementDeliveryType = statementDeliveryType;
    }

    public String getStatementNotificationEmailRecipients() {
        return statementNotificationEmailRecipients;
    }

    public void setStatementNotificationEmailRecipients(String statementNotificationEmailRecipients) {
        this.statementNotificationEmailRecipients = statementNotificationEmailRecipients;
    }

    public String getIncludedChildren() {
        return includedChildren;
    }

    public void setIncludedChildren(String includedChildren) {
        this.includedChildren = includedChildren;
    }

    public String getAlternateNameDataRowId() {
        return alternateNameDataRowId;
    }

    public void setAlternateNameDataRowId(String alternateNameDataRowId) {
        this.alternateNameDataRowId = alternateNameDataRowId;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }

    public String getAlternateNameUsage() {
        return alternateNameUsage;
    }

    public void setAlternateNameUsage(String alternateNameUsage) {
        this.alternateNameUsage = alternateNameUsage;
    }

    public String getReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(String replaceAll) {
        this.replaceAll = replaceAll;
    }
    
}
