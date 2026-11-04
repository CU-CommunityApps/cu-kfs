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

    private String businessEntityRowId;                     // template field name duplicated as rowId
    private String businessEntityName;
    private String businessEntityPhoneticName;
    private String businessEntityTaxId;
    private String externalEntityId;

    private String addressDataRowId;                        // template field name duplicated as rowId
    private String addressLineDataRowId;                    // template field name duplicated as rowId
    private String addressLineDataDescriptor;               // template field name duplicated as descriptor
    private String addressLineDataType;                     // template field name duplicated as type
    private String addressLineData;
    private String municipality;
    private String countryCity;
    private String iso31661Alpha2Code;

    private String submunicipalityDataRowId;                // template field name duplicated as rowId
    private String addressComponentName;
    private String submunicipalityDataType;                 // template field name duplicated as type
    private String submunicipalityData;
    private String countryRegion;
    private String countryRegionDescriptor;

    private String subRegionDataRowId;                      // template field name duplicated as rowId
    private String subRegionDataDescriptor;                 // template field name duplicated as descriptor
    private String subregionDataType;                       // template field name duplicated as type
    private String subregionData;
    private String postalCode;

    private String addressDataUsageDataRowId;               // template field name duplicated as rowId
    private String addressDataUsageDataPublic;              // template field name duplicated as public
    private String addressDataTypeDataRowId;                // template field name duplicated as rowId
    private String addressDataTypeDataPrimary;              // template field name duplicated as primary
    private String addressDataTypeDataType;                 // template field name duplicated as type
    private String addressDataUseFor1;
    private String addressDataUseFor2;
    private String addressDataUseFor3;
    private String addressDataUseForTenanted1;
    private String addressDataUseForTenanted2;
    private String addressDataUseForTenanted3;
    private String addressDataUsageDataComments;            // template field name duplicated as comments
    private String addressDataNumberOfDays;
    private String addressDataMunicipalityLocal;
    private String existingAddressId;
    private String newAddressId;

    private String phoneDataRowId;                          // template field name duplicated as rowId
    private String phoneDataAreaCode;
    private String phoneDataTenantFormattedPhone;
    private String phoneDataInternationalFormattedPhone;
    private String phoneDataPhoneNumberWithoutAreaCode;
    private String phoneDataNationalFormattedPhone;
    private String phoneDataE164FormattedPhone;
    private String phoneDataWorkdayTraditionalFormattedPhone;
    private String phoneDataDelete;                         // template field name duplicated as delete
    private String phoneDataDoNotReplaceAll;                // template field name duplicated as doNotReplaceAll
    private String phoneDataCountryIsoCode;
    private String phoneDataInternationalPhoneCode;
    private String phoneDataPhoneNumber;
    private String phoneDataPhoneExtension;
    private String phoneDataPhoneDeviceType;

    private String phoneDataUsageDataRowId;                 // template field name duplicated as rowId
    private String phoneDataUsageDataPublic;                // template field name duplicated as public
    private String phoneDataTypeDataRowId;                  // template field name duplicated as rowId
    private String phoneDataTypeDataPrimary;                // template field name duplicated as primary
    private String phoneDataTypeDataType;                   // template field name duplicated as type
    private String phoneDataUsageDataUseFor;                // template field name duplicated as useFor
    private String phoneDataUsageDataUseForTenanted;        // template field name duplicated as useForTenanted
    private String phoneDataUsageDataComments;              // template field name duplicated as comments
    private String existingPhoneId;
    private String newPhoneId;                              // template field name duplicated as id

    private String emailAddressDataRowId;                   // template field name duplicated as rowId
    private String emailAddressDataDelete;                  // template field name duplicated as delete
    private String emailAddressDataDoNotReplaceAll;         // template field name duplicated as doNotReplaceAll
    private String emailAddressDataEmailAddress;
    private String emailAddressDataEmailComment;

    private String emailAddressDataUsageDataRowId;          // template field name duplicated as rowId
    private String emailAddressDataUsageDataPublic;         // template field name duplicated as public
    private String emailAddressDataTypeDataRowId;           // template field name duplicated as rowId
    private String emailAddressDataTypeDataPrimary;         // template field name duplicated as primary
    private String emailAddressDataTypeDataType;            // template field name duplicated as type
    private String emailAddressDataUsageDataUseFor;         // template field name duplicated as useFor
    private String emailAddressDataUsageDataUseForTenanted; // template field name duplicated as useForTenanted
    private String emailAddressDataUsageDataComments;       // template field name duplicated as comments
    private String existingEmailId;
    private String newEmailId;                              // template field name duplicated as id

    private String webAddressDataRowId;                     // template field name duplicated as rowId
    private String webAddressDataDelete;                    // template field name duplicated as delete
    private String webAddressDataDoNotReplaceAll;           // template field name duplicated as doNotReplaceAll
    private String webAddressDataWebAddress;                // template field name duplicated as webAddress
    private String webAddressComment;

    private String webAddressDataUsageDataRowId;            // template field name duplicated as rowId
    private String webAddressDataUsageDataPublic;           // template field name duplicated as public
    private String webAddressTypeDataRowId;                 // template field name duplicated as rowId
    private String webAddressTypeDataPrimary;               // template field name duplicated as primary
    private String webAddressTypeDataType;                  // template field name duplicated as type
    private String webAddressDataUsageDataUseFor;           // template field name duplicated as useFor
    private String webAddressDataUsageDataUseForTenanted;   // template field name duplicated as useForTenanted
    private String webAddressDataUsageDataComments;         // template field name duplicated as comments
    private String existingWebAddressId;                    // template field name duplicated as webAddress
    private String newWebAddressId;                         // template field name duplicated as id
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

    private String alternateNameDataRowId;                  // template field name duplicated as rowId
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

    public String getAddressDataUseFor1() {
        return addressDataUseFor1;
    }

    public void setAddressDataUseFor1(String addressDataUseFor1) {
        this.addressDataUseFor1 = addressDataUseFor1;
    }

    public String getAddressDataUseFor2() {
        return addressDataUseFor2;
    }

    public void setAddressDataUseFor2(String addressDataUseFor2) {
        this.addressDataUseFor2 = addressDataUseFor2;
    }

    public String getAddressDataUseFor3() {
        return addressDataUseFor3;
    }

    public void setAddressDataUseFor3(String addressDataUseFor3) {
        this.addressDataUseFor3 = addressDataUseFor3;
    }

    public String getAddressDataUseForTenanted1() {
        return addressDataUseForTenanted1;
    }

    public void setAddressDataUseForTenanted1(String addressDataUseForTenanted1) {
        this.addressDataUseForTenanted1 = addressDataUseForTenanted1;
    }

    public String getAddressDataUseForTenanted2() {
        return addressDataUseForTenanted2;
    }

    public void setAddressDataUseForTenanted2(String addressDataUseForTenanted2) {
        this.addressDataUseForTenanted2 = addressDataUseForTenanted2;
    }

    public String getAddressDataUseForTenanted3() {
        return addressDataUseForTenanted3;
    }

    public void setAddressDataUseForTenanted3(String addressDataUseForTenanted3) {
        this.addressDataUseForTenanted3 = addressDataUseForTenanted3;
    }

    public String getAddressDataUsageDataComments() {
        return addressDataUsageDataComments;
    }

    public void setAddressDataUsageDataComments(String addressDataUsageDataComments) {
        this.addressDataUsageDataComments = addressDataUsageDataComments;
    }

    public String getAddressDataNumberOfDays() {
        return addressDataNumberOfDays;
    }

    public void setAddressDataNumberOfDays(String addressDataNumberOfDays) {
        this.addressDataNumberOfDays = addressDataNumberOfDays;
    }

    public String getAddressDataMunicipalityLocal() {
        return addressDataMunicipalityLocal;
    }

    public void setAddressDataMunicipalityLocal(String addressDataMunicipalityLocal) {
        this.addressDataMunicipalityLocal = addressDataMunicipalityLocal;
    }

    public String getExistingAddressId() {
        return existingAddressId;
    }

    public void setExistingAddressId(String existingAddressId) {
        this.existingAddressId = existingAddressId;
    }

    public String getNewAddressId() {
        return newAddressId;
    }

    public void setNewAddressId(String newAddressId) {
        this.newAddressId = newAddressId;
    }

    public String getPhoneDataRowId() {
        return phoneDataRowId;
    }

    public void setPhoneDataRowId(String phoneDataRowId) {
        this.phoneDataRowId = phoneDataRowId;
    }

    public String getPhoneDataAreaCode() {
        return phoneDataAreaCode;
    }

    public void setPhoneDataAreaCode(String phoneDataAreaCode) {
        this.phoneDataAreaCode = phoneDataAreaCode;
    }

    public String getPhoneDataTenantFormattedPhone() {
        return phoneDataTenantFormattedPhone;
    }

    public void setPhoneDataTenantFormattedPhone(String phoneDataTenantFormattedPhone) {
        this.phoneDataTenantFormattedPhone = phoneDataTenantFormattedPhone;
    }

    public String getPhoneDataInternationalFormattedPhone() {
        return phoneDataInternationalFormattedPhone;
    }

    public void setPhoneDataInternationalFormattedPhone(String phoneDataInternationalFormattedPhone) {
        this.phoneDataInternationalFormattedPhone = phoneDataInternationalFormattedPhone;
    }

    public String getPhoneDataPhoneNumberWithoutAreaCode() {
        return phoneDataPhoneNumberWithoutAreaCode;
    }

    public void setPhoneDataPhoneNumberWithoutAreaCode(String phoneDataPhoneNumberWithoutAreaCode) {
        this.phoneDataPhoneNumberWithoutAreaCode = phoneDataPhoneNumberWithoutAreaCode;
    }

    public String getPhoneDataNationalFormattedPhone() {
        return phoneDataNationalFormattedPhone;
    }

    public void setPhoneDataNationalFormattedPhone(String phoneDataNationalFormattedPhone) {
        this.phoneDataNationalFormattedPhone = phoneDataNationalFormattedPhone;
    }

    public String getPhoneDataE164FormattedPhone() {
        return phoneDataE164FormattedPhone;
    }

    public void setPhoneDataE164FormattedPhone(String phoneDataE164FormattedPhone) {
        this.phoneDataE164FormattedPhone = phoneDataE164FormattedPhone;
    }

    public String getPhoneDataWorkdayTraditionalFormattedPhone() {
        return phoneDataWorkdayTraditionalFormattedPhone;
    }

    public void setPhoneDataWorkdayTraditionalFormattedPhone(String phoneDataWorkdayTraditionalFormattedPhone) {
        this.phoneDataWorkdayTraditionalFormattedPhone = phoneDataWorkdayTraditionalFormattedPhone;
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

    public String getPhoneDataCountryIsoCode() {
        return phoneDataCountryIsoCode;
    }

    public void setPhoneDataCountryIsoCode(String phoneDataCountryIsoCode) {
        this.phoneDataCountryIsoCode = phoneDataCountryIsoCode;
    }

    public String getPhoneDataInternationalPhoneCode() {
        return phoneDataInternationalPhoneCode;
    }

    public void setPhoneDataInternationalPhoneCode(String phoneDataInternationalPhoneCode) {
        this.phoneDataInternationalPhoneCode = phoneDataInternationalPhoneCode;
    }

    public String getPhoneDataPhoneNumber() {
        return phoneDataPhoneNumber;
    }

    public void setPhoneDataPhoneNumber(String phoneDataPhoneNumber) {
        this.phoneDataPhoneNumber = phoneDataPhoneNumber;
    }

    public String getPhoneDataPhoneExtension() {
        return phoneDataPhoneExtension;
    }

    public void setPhoneDataPhoneExtension(String phoneDataPhoneExtension) {
        this.phoneDataPhoneExtension = phoneDataPhoneExtension;
    }

    public String getPhoneDataPhoneDeviceType() {
        return phoneDataPhoneDeviceType;
    }

    public void setPhoneDataPhoneDeviceType(String phoneDataPhoneDeviceType) {
        this.phoneDataPhoneDeviceType = phoneDataPhoneDeviceType;
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

    public String getExistingPhoneId() {
        return existingPhoneId;
    }

    public void setExistingPhoneId(String existingPhoneId) {
        this.existingPhoneId = existingPhoneId;
    }

    public String getNewPhoneId() {
        return newPhoneId;
    }

    public void setNewPhoneId(String newPhoneId) {
        this.newPhoneId = newPhoneId;
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

    public String getEmailAddressDataEmailAddress() {
        return emailAddressDataEmailAddress;
    }

    public void setEmailAddressDataEmailAddress(String emailAddressDataEmailAddress) {
        this.emailAddressDataEmailAddress = emailAddressDataEmailAddress;
    }

    public String getEmailAddressDataEmailComment() {
        return emailAddressDataEmailComment;
    }

    public void setEmailAddressDataEmailComment(String emailAddressDataEmailComment) {
        this.emailAddressDataEmailComment = emailAddressDataEmailComment;
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

    public String getExistingEmailId() {
        return existingEmailId;
    }

    public void setExistingEmailId(String existingEmailId) {
        this.existingEmailId = existingEmailId;
    }

    public String getNewEmailId() {
        return newEmailId;
    }

    public void setNewEmailId(String newEmailId) {
        this.newEmailId = newEmailId;
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

    public String getExistingWebAddressId() {
        return existingWebAddressId;
    }

    public void setExistingWebAddressId(String existingWebAddressId) {
        this.existingWebAddressId = existingWebAddressId;
    }

    public String getNewWebAddressId() {
        return newWebAddressId;
    }

    public void setNewWebAddressId(String newWebAddressId) {
        this.newWebAddressId = newWebAddressId;
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
