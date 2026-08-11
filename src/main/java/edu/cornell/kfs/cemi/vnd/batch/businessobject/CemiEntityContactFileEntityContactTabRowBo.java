package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiEntityContactFileEntityContactTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = -9044387307952799994L;

    private Integer vendorContactGeneratedIdentifier;
    private Integer vendorContactGeneratedIdentifierForEmail;
    private Integer vendorContactGeneratedIdentifierForPhone;
    private Integer vendorContactPhoneGeneratedIdentifier;

    private String spreadsheetKey;
    private String addOnly;
    private String existingBusinessEntityContactId;
    private String primaryBillToContact;
    private String defaultBillToContact;
    private String newBusinessEntityContactId;
    private String supplier;
    private String billableEntity;
    private String financialInstitution;
    private String taxAuthority;
    private String nameRowId;
    private String formattedName;
    private String reportingName;
    private String country;
    private String title;
    private String titleDescriptor;
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondaryLastName;
    private String tertiaryLastName;
    private String localName;
    private String localScript;
    private String localFirstName;
    private String localMiddleName;
    private String localLastName;
    private String localSecondaryLastName;
    private String localFirstName2;
    private String localMiddleName2;
    private String localLastName2;
    private String localSecondaryLastName2;
    private String socialSuffix;
    private String socialSuffixDescriptor;
    private String academicSuffix;
    private String hereditarySuffix;
    private String honorarySuffix;
    private String professionalSuffix;
    private String religiousSuffix;
    private String royalSuffix;
    private String fullNameForSingaporeAndMalaysia;
    private String addressRowId;
    private String formattedAddress;
    private String addressFormatType;
    private String defaultedBusinessSiteAddress;
    private String deleteAddress;
    private String doNotReplaceAllAddress;
    private String addressEffectiveDate;
    private String addressCountry;
    private String addressLastModified;
    private String addressLineRowId;
    private String addressLineDescriptor;
    private String addressLineType;
    private String addressLineData;
    private String addressMunicipality;
    private String addressCountryCity;
    private String addressIso31661Alpha2Code;
    private String submunicipalityRowId;
    private String submunicipalityAddressComponentName;
    private String submunicipalityType;
    private String submunicipalityData;
    private String countryRegion;
    private String countryRegionDescriptor;
    private String subregionRowId;
    private String subregionDescriptor;
    private String subregionType;
    private String subregionData;
    private String postalCode;
    private String addressUsageRowId;
    private String addressUsagePublic;
    private String addressUsageTypeRowId;
    private String addressUsageTypePrimary;
    private String addressUsageType;
    private String addressUseFor;
    private String addressUseForTenanted;
    private String addressUsageComments;
    private String addressNumberOfDays;
    private String addressMunicipalityLocal;
    private String existingAddressId;
    private String newAddressId;
    private String phoneRowId;
    private String phoneAreaCode;
    private String tenantFormattedPhone;
    private String internationalFormattedPhone;
    private String phoneNumberWithoutAreaCode;
    private String nationalFormattedPhone;
    private String e164FormattedPhone;
    private String workdayTraditionalFormattedPhone;
    private String deletePhone;
    private String doNotReplaceAllPhone;
    private String phoneCountryIsoCode;
    private String internationalPhoneCode;
    private String phoneNumber;
    private String phoneExtension;
    private String phoneDeviceType;
    private String phoneUsageRowId;
    private String phoneUsagePublic;
    private String phoneUsageTypeRowId;
    private String phoneUsageTypePrimary;
    private String phoneUsageType;
    private String phoneUseFor;
    private String phoneUseForTenanted;
    private String phoneUsageComments;
    private String existingPhoneId;
    private String newPhoneId;
    private String emailRowId;
    private String deleteEmail;
    private String doNotReplaceAllEmail;
    private String emailAddress;
    private String emailComment;
    private String emailUsageRowId;
    private String emailUsagePublic;
    private String emailUsageTypeRowId;
    private String emailUsageTypePrimary;
    private String emailUsageType;
    private String emailUseFor;
    private String emailUseForTenanted;
    private String emailUsageComments;
    private String existingEmailId;
    private String newEmailId;
    private String instantMessengerRowId;
    private String deleteInstantMessenger;
    private String doNotReplaceAllInstantMessenger;
    private String instantMessengerAddress;
    private String instantMessengerType;
    private String instantMessengerComment;
    private String instantMessengerUsageRowId;
    private String instantMessengerUsagePublic;
    private String instantMessengerUsageTypeRowId;
    private String instantMessengerUsageTypePrimary;
    private String instantMessengerUsageType;
    private String instantMessengerUseFor;
    private String instantMessengerUseForTenanted;
    private String instantMessengerUsageComments;
    private String existingInstantMessengerId;
    private String newInstantMessengerId;
    private String webAddressRowId;
    private String deleteWebAddress;
    private String doNotReplaceAllWebAddress;
    private String webAddress;
    private String webAddressComment;
    private String webAddressUsageRowId;
    private String webAddressUsagePublic;
    private String webAddressUsageTypeRowId;
    private String webAddressUsageTypePrimary;
    private String webAddressUsageType;
    private String webAddressUseFor;
    private String webAddressUseForTenanted;
    private String webAddressUsageComments;
    private String existingWebAddressId;
    private String newWebAddressId;
    private String contactTypeRowId;
    private String contactTypeTenanted1;
    private String contactTypeTenanted2;
    private String contactTypeTenanted3;
    private String contactTypeTenanted4;
    private String contactTypeTenanted5;
    private String contactTypeTenanted6;
    private String contactTypeTenanted7;
    private String contactTypeTenanted8;
    private String contactTypeTenanted9;
    private String contactTypeTenanted10;
    private String businessEntityContactType;
    private String externalSystemId;
    private String externalId;

    public Integer getVendorContactGeneratedIdentifier() {
        return vendorContactGeneratedIdentifier;
    }

    public void setVendorContactGeneratedIdentifier(final Integer vendorContactGeneratedIdentifier) {
        this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
    }

    public Integer getVendorContactGeneratedIdentifierForEmail() {
        return vendorContactGeneratedIdentifierForEmail;
    }

    public void setVendorContactGeneratedIdentifierForEmail(final Integer vendorContactGeneratedIdentifierForEmail) {
        this.vendorContactGeneratedIdentifierForEmail = vendorContactGeneratedIdentifierForEmail;
    }

    public Integer getVendorContactGeneratedIdentifierForPhone() {
        return vendorContactGeneratedIdentifierForPhone;
    }

    public void setVendorContactGeneratedIdentifierForPhone(final Integer vendorContactGeneratedIdentifierForPhone) {
        this.vendorContactGeneratedIdentifierForPhone = vendorContactGeneratedIdentifierForPhone;
    }

    public Integer getVendorContactPhoneGeneratedIdentifier() {
        return vendorContactPhoneGeneratedIdentifier;
    }

    public void setVendorContactPhoneGeneratedIdentifier(final Integer vendorContactPhoneGeneratedIdentifier) {
        this.vendorContactPhoneGeneratedIdentifier = vendorContactPhoneGeneratedIdentifier;
    }

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(final String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAddOnly() {
        return addOnly;
    }

    public void setAddOnly(final String addOnly) {
        this.addOnly = addOnly;
    }

    public String getExistingBusinessEntityContactId() {
        return existingBusinessEntityContactId;
    }

    public void setExistingBusinessEntityContactId(final String existingBusinessEntityContactId) {
        this.existingBusinessEntityContactId = existingBusinessEntityContactId;
    }

    public String getPrimaryBillToContact() {
        return primaryBillToContact;
    }

    public void setPrimaryBillToContact(final String primaryBillToContact) {
        this.primaryBillToContact = primaryBillToContact;
    }

    public String getDefaultBillToContact() {
        return defaultBillToContact;
    }

    public void setDefaultBillToContact(final String defaultBillToContact) {
        this.defaultBillToContact = defaultBillToContact;
    }

    public String getNewBusinessEntityContactId() {
        return newBusinessEntityContactId;
    }

    public void setNewBusinessEntityContactId(final String newBusinessEntityContactId) {
        this.newBusinessEntityContactId = newBusinessEntityContactId;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(final String supplier) {
        this.supplier = supplier;
    }

    public String getBillableEntity() {
        return billableEntity;
    }

    public void setBillableEntity(final String billableEntity) {
        this.billableEntity = billableEntity;
    }

    public String getFinancialInstitution() {
        return financialInstitution;
    }

    public void setFinancialInstitution(final String financialInstitution) {
        this.financialInstitution = financialInstitution;
    }

    public String getTaxAuthority() {
        return taxAuthority;
    }

    public void setTaxAuthority(final String taxAuthority) {
        this.taxAuthority = taxAuthority;
    }

    public String getNameRowId() {
        return nameRowId;
    }

    public void setNameRowId(final String nameRowId) {
        this.nameRowId = nameRowId;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public void setFormattedName(final String formattedName) {
        this.formattedName = formattedName;
    }

    public String getReportingName() {
        return reportingName;
    }

    public void setReportingName(final String reportingName) {
        this.reportingName = reportingName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getTitleDescriptor() {
        return titleDescriptor;
    }

    public void setTitleDescriptor(final String titleDescriptor) {
        this.titleDescriptor = titleDescriptor;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(final String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getSecondaryLastName() {
        return secondaryLastName;
    }

    public void setSecondaryLastName(final String secondaryLastName) {
        this.secondaryLastName = secondaryLastName;
    }

    public String getTertiaryLastName() {
        return tertiaryLastName;
    }

    public void setTertiaryLastName(final String tertiaryLastName) {
        this.tertiaryLastName = tertiaryLastName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(final String localName) {
        this.localName = localName;
    }

    public String getLocalScript() {
        return localScript;
    }

    public void setLocalScript(final String localScript) {
        this.localScript = localScript;
    }

    public String getLocalFirstName() {
        return localFirstName;
    }

    public void setLocalFirstName(final String localFirstName) {
        this.localFirstName = localFirstName;
    }

    public String getLocalMiddleName() {
        return localMiddleName;
    }

    public void setLocalMiddleName(final String localMiddleName) {
        this.localMiddleName = localMiddleName;
    }

    public String getLocalLastName() {
        return localLastName;
    }

    public void setLocalLastName(final String localLastName) {
        this.localLastName = localLastName;
    }

    public String getLocalSecondaryLastName() {
        return localSecondaryLastName;
    }

    public void setLocalSecondaryLastName(final String localSecondaryLastName) {
        this.localSecondaryLastName = localSecondaryLastName;
    }

    public String getLocalFirstName2() {
        return localFirstName2;
    }

    public void setLocalFirstName2(final String localFirstName2) {
        this.localFirstName2 = localFirstName2;
    }

    public String getLocalMiddleName2() {
        return localMiddleName2;
    }

    public void setLocalMiddleName2(final String localMiddleName2) {
        this.localMiddleName2 = localMiddleName2;
    }

    public String getLocalLastName2() {
        return localLastName2;
    }

    public void setLocalLastName2(final String localLastName2) {
        this.localLastName2 = localLastName2;
    }

    public String getLocalSecondaryLastName2() {
        return localSecondaryLastName2;
    }

    public void setLocalSecondaryLastName2(final String localSecondaryLastName2) {
        this.localSecondaryLastName2 = localSecondaryLastName2;
    }

    public String getSocialSuffix() {
        return socialSuffix;
    }

    public void setSocialSuffix(final String socialSuffix) {
        this.socialSuffix = socialSuffix;
    }

    public String getSocialSuffixDescriptor() {
        return socialSuffixDescriptor;
    }

    public void setSocialSuffixDescriptor(final String socialSuffixDescriptor) {
        this.socialSuffixDescriptor = socialSuffixDescriptor;
    }

    public String getAcademicSuffix() {
        return academicSuffix;
    }

    public void setAcademicSuffix(final String academicSuffix) {
        this.academicSuffix = academicSuffix;
    }

    public String getHereditarySuffix() {
        return hereditarySuffix;
    }

    public void setHereditarySuffix(final String hereditarySuffix) {
        this.hereditarySuffix = hereditarySuffix;
    }

    public String getHonorarySuffix() {
        return honorarySuffix;
    }

    public void setHonorarySuffix(final String honorarySuffix) {
        this.honorarySuffix = honorarySuffix;
    }

    public String getProfessionalSuffix() {
        return professionalSuffix;
    }

    public void setProfessionalSuffix(final String professionalSuffix) {
        this.professionalSuffix = professionalSuffix;
    }

    public String getReligiousSuffix() {
        return religiousSuffix;
    }

    public void setReligiousSuffix(final String religiousSuffix) {
        this.religiousSuffix = religiousSuffix;
    }

    public String getRoyalSuffix() {
        return royalSuffix;
    }

    public void setRoyalSuffix(final String royalSuffix) {
        this.royalSuffix = royalSuffix;
    }

    public String getFullNameForSingaporeAndMalaysia() {
        return fullNameForSingaporeAndMalaysia;
    }

    public void setFullNameForSingaporeAndMalaysia(final String fullNameForSingaporeAndMalaysia) {
        this.fullNameForSingaporeAndMalaysia = fullNameForSingaporeAndMalaysia;
    }

    public String getAddressRowId() {
        return addressRowId;
    }

    public void setAddressRowId(final String addressRowId) {
        this.addressRowId = addressRowId;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(final String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getAddressFormatType() {
        return addressFormatType;
    }

    public void setAddressFormatType(final String addressFormatType) {
        this.addressFormatType = addressFormatType;
    }

    public String getDefaultedBusinessSiteAddress() {
        return defaultedBusinessSiteAddress;
    }

    public void setDefaultedBusinessSiteAddress(final String defaultedBusinessSiteAddress) {
        this.defaultedBusinessSiteAddress = defaultedBusinessSiteAddress;
    }

    public String getDeleteAddress() {
        return deleteAddress;
    }

    public void setDeleteAddress(final String deleteAddress) {
        this.deleteAddress = deleteAddress;
    }

    public String getDoNotReplaceAllAddress() {
        return doNotReplaceAllAddress;
    }

    public void setDoNotReplaceAllAddress(final String doNotReplaceAllAddress) {
        this.doNotReplaceAllAddress = doNotReplaceAllAddress;
    }

    public String getAddressEffectiveDate() {
        return addressEffectiveDate;
    }

    public void setAddressEffectiveDate(final String addressEffectiveDate) {
        this.addressEffectiveDate = addressEffectiveDate;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(final String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressLastModified() {
        return addressLastModified;
    }

    public void setAddressLastModified(final String addressLastModified) {
        this.addressLastModified = addressLastModified;
    }

    public String getAddressLineRowId() {
        return addressLineRowId;
    }

    public void setAddressLineRowId(final String addressLineRowId) {
        this.addressLineRowId = addressLineRowId;
    }

    public String getAddressLineDescriptor() {
        return addressLineDescriptor;
    }

    public void setAddressLineDescriptor(final String addressLineDescriptor) {
        this.addressLineDescriptor = addressLineDescriptor;
    }

    public String getAddressLineType() {
        return addressLineType;
    }

    public void setAddressLineType(final String addressLineType) {
        this.addressLineType = addressLineType;
    }

    public String getAddressLineData() {
        return addressLineData;
    }

    public void setAddressLineData(final String addressLineData) {
        this.addressLineData = addressLineData;
    }

    public String getAddressMunicipality() {
        return addressMunicipality;
    }

    public void setAddressMunicipality(final String addressMunicipality) {
        this.addressMunicipality = addressMunicipality;
    }

    public String getAddressCountryCity() {
        return addressCountryCity;
    }

    public void setAddressCountryCity(final String addressCountryCity) {
        this.addressCountryCity = addressCountryCity;
    }

    public String getAddressIso31661Alpha2Code() {
        return addressIso31661Alpha2Code;
    }

    public void setAddressIso31661Alpha2Code(final String addressIso31661Alpha2Code) {
        this.addressIso31661Alpha2Code = addressIso31661Alpha2Code;
    }

    public String getSubmunicipalityRowId() {
        return submunicipalityRowId;
    }

    public void setSubmunicipalityRowId(final String submunicipalityRowId) {
        this.submunicipalityRowId = submunicipalityRowId;
    }

    public String getSubmunicipalityAddressComponentName() {
        return submunicipalityAddressComponentName;
    }

    public void setSubmunicipalityAddressComponentName(final String submunicipalityAddressComponentName) {
        this.submunicipalityAddressComponentName = submunicipalityAddressComponentName;
    }

    public String getSubmunicipalityType() {
        return submunicipalityType;
    }

    public void setSubmunicipalityType(final String submunicipalityType) {
        this.submunicipalityType = submunicipalityType;
    }

    public String getSubmunicipalityData() {
        return submunicipalityData;
    }

    public void setSubmunicipalityData(final String submunicipalityData) {
        this.submunicipalityData = submunicipalityData;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(final String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public String getCountryRegionDescriptor() {
        return countryRegionDescriptor;
    }

    public void setCountryRegionDescriptor(final String countryRegionDescriptor) {
        this.countryRegionDescriptor = countryRegionDescriptor;
    }

    public String getSubregionRowId() {
        return subregionRowId;
    }

    public void setSubregionRowId(final String subregionRowId) {
        this.subregionRowId = subregionRowId;
    }

    public String getSubregionDescriptor() {
        return subregionDescriptor;
    }

    public void setSubregionDescriptor(final String subregionDescriptor) {
        this.subregionDescriptor = subregionDescriptor;
    }

    public String getSubregionType() {
        return subregionType;
    }

    public void setSubregionType(final String subregionType) {
        this.subregionType = subregionType;
    }

    public String getSubregionData() {
        return subregionData;
    }

    public void setSubregionData(final String subregionData) {
        this.subregionData = subregionData;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressUsageRowId() {
        return addressUsageRowId;
    }

    public void setAddressUsageRowId(final String addressUsageRowId) {
        this.addressUsageRowId = addressUsageRowId;
    }

    public String getAddressUsagePublic() {
        return addressUsagePublic;
    }

    public void setAddressUsagePublic(final String addressUsagePublic) {
        this.addressUsagePublic = addressUsagePublic;
    }

    public String getAddressUsageTypeRowId() {
        return addressUsageTypeRowId;
    }

    public void setAddressUsageTypeRowId(final String addressUsageTypeRowId) {
        this.addressUsageTypeRowId = addressUsageTypeRowId;
    }

    public String getAddressUsageTypePrimary() {
        return addressUsageTypePrimary;
    }

    public void setAddressUsageTypePrimary(final String addressUsageTypePrimary) {
        this.addressUsageTypePrimary = addressUsageTypePrimary;
    }

    public String getAddressUsageType() {
        return addressUsageType;
    }

    public void setAddressUsageType(final String addressUsageType) {
        this.addressUsageType = addressUsageType;
    }

    public String getAddressUseFor() {
        return addressUseFor;
    }

    public void setAddressUseFor(final String addressUseFor) {
        this.addressUseFor = addressUseFor;
    }

    public String getAddressUseForTenanted() {
        return addressUseForTenanted;
    }

    public void setAddressUseForTenanted(final String addressUseForTenanted) {
        this.addressUseForTenanted = addressUseForTenanted;
    }

    public String getAddressUsageComments() {
        return addressUsageComments;
    }

    public void setAddressUsageComments(final String addressUsageComments) {
        this.addressUsageComments = addressUsageComments;
    }

    public String getAddressNumberOfDays() {
        return addressNumberOfDays;
    }

    public void setAddressNumberOfDays(final String addressNumberOfDays) {
        this.addressNumberOfDays = addressNumberOfDays;
    }

    public String getAddressMunicipalityLocal() {
        return addressMunicipalityLocal;
    }

    public void setAddressMunicipalityLocal(final String addressMunicipalityLocal) {
        this.addressMunicipalityLocal = addressMunicipalityLocal;
    }

    public String getExistingAddressId() {
        return existingAddressId;
    }

    public void setExistingAddressId(final String existingAddressId) {
        this.existingAddressId = existingAddressId;
    }

    public String getNewAddressId() {
        return newAddressId;
    }

    public void setNewAddressId(final String newAddressId) {
        this.newAddressId = newAddressId;
    }

    public String getPhoneRowId() {
        return phoneRowId;
    }

    public void setPhoneRowId(final String phoneRowId) {
        this.phoneRowId = phoneRowId;
    }

    public String getPhoneAreaCode() {
        return phoneAreaCode;
    }

    public void setPhoneAreaCode(final String phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }

    public String getTenantFormattedPhone() {
        return tenantFormattedPhone;
    }

    public void setTenantFormattedPhone(final String tenantFormattedPhone) {
        this.tenantFormattedPhone = tenantFormattedPhone;
    }

    public String getInternationalFormattedPhone() {
        return internationalFormattedPhone;
    }

    public void setInternationalFormattedPhone(final String internationalFormattedPhone) {
        this.internationalFormattedPhone = internationalFormattedPhone;
    }

    public String getPhoneNumberWithoutAreaCode() {
        return phoneNumberWithoutAreaCode;
    }

    public void setPhoneNumberWithoutAreaCode(final String phoneNumberWithoutAreaCode) {
        this.phoneNumberWithoutAreaCode = phoneNumberWithoutAreaCode;
    }

    public String getNationalFormattedPhone() {
        return nationalFormattedPhone;
    }

    public void setNationalFormattedPhone(final String nationalFormattedPhone) {
        this.nationalFormattedPhone = nationalFormattedPhone;
    }

    public String getE164FormattedPhone() {
        return e164FormattedPhone;
    }

    public void setE164FormattedPhone(final String e164FormattedPhone) {
        this.e164FormattedPhone = e164FormattedPhone;
    }

    public String getWorkdayTraditionalFormattedPhone() {
        return workdayTraditionalFormattedPhone;
    }

    public void setWorkdayTraditionalFormattedPhone(final String workdayTraditionalFormattedPhone) {
        this.workdayTraditionalFormattedPhone = workdayTraditionalFormattedPhone;
    }

    public String getDeletePhone() {
        return deletePhone;
    }

    public void setDeletePhone(final String deletePhone) {
        this.deletePhone = deletePhone;
    }

    public String getDoNotReplaceAllPhone() {
        return doNotReplaceAllPhone;
    }

    public void setDoNotReplaceAllPhone(final String doNotReplaceAllPhone) {
        this.doNotReplaceAllPhone = doNotReplaceAllPhone;
    }

    public String getPhoneCountryIsoCode() {
        return phoneCountryIsoCode;
    }

    public void setPhoneCountryIsoCode(final String phoneCountryIsoCode) {
        this.phoneCountryIsoCode = phoneCountryIsoCode;
    }

    public String getInternationalPhoneCode() {
        return internationalPhoneCode;
    }

    public void setInternationalPhoneCode(final String internationalPhoneCode) {
        this.internationalPhoneCode = internationalPhoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(final String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneDeviceType() {
        return phoneDeviceType;
    }

    public void setPhoneDeviceType(final String phoneDeviceType) {
        this.phoneDeviceType = phoneDeviceType;
    }

    public String getPhoneUsageRowId() {
        return phoneUsageRowId;
    }

    public void setPhoneUsageRowId(final String phoneUsageRowId) {
        this.phoneUsageRowId = phoneUsageRowId;
    }

    public String getPhoneUsagePublic() {
        return phoneUsagePublic;
    }

    public void setPhoneUsagePublic(final String phoneUsagePublic) {
        this.phoneUsagePublic = phoneUsagePublic;
    }

    public String getPhoneUsageTypeRowId() {
        return phoneUsageTypeRowId;
    }

    public void setPhoneUsageTypeRowId(final String phoneUsageTypeRowId) {
        this.phoneUsageTypeRowId = phoneUsageTypeRowId;
    }

    public String getPhoneUsageTypePrimary() {
        return phoneUsageTypePrimary;
    }

    public void setPhoneUsageTypePrimary(final String phoneUsageTypePrimary) {
        this.phoneUsageTypePrimary = phoneUsageTypePrimary;
    }

    public String getPhoneUsageType() {
        return phoneUsageType;
    }

    public void setPhoneUsageType(final String phoneUsageType) {
        this.phoneUsageType = phoneUsageType;
    }

    public String getPhoneUseFor() {
        return phoneUseFor;
    }

    public void setPhoneUseFor(final String phoneUseFor) {
        this.phoneUseFor = phoneUseFor;
    }

    public String getPhoneUseForTenanted() {
        return phoneUseForTenanted;
    }

    public void setPhoneUseForTenanted(final String phoneUseForTenanted) {
        this.phoneUseForTenanted = phoneUseForTenanted;
    }

    public String getPhoneUsageComments() {
        return phoneUsageComments;
    }

    public void setPhoneUsageComments(final String phoneUsageComments) {
        this.phoneUsageComments = phoneUsageComments;
    }

    public String getExistingPhoneId() {
        return existingPhoneId;
    }

    public void setExistingPhoneId(final String existingPhoneId) {
        this.existingPhoneId = existingPhoneId;
    }

    public String getNewPhoneId() {
        return newPhoneId;
    }

    public void setNewPhoneId(final String newPhoneId) {
        this.newPhoneId = newPhoneId;
    }

    public String getEmailRowId() {
        return emailRowId;
    }

    public void setEmailRowId(final String emailRowId) {
        this.emailRowId = emailRowId;
    }

    public String getDeleteEmail() {
        return deleteEmail;
    }

    public void setDeleteEmail(final String deleteEmail) {
        this.deleteEmail = deleteEmail;
    }

    public String getDoNotReplaceAllEmail() {
        return doNotReplaceAllEmail;
    }

    public void setDoNotReplaceAllEmail(final String doNotReplaceAllEmail) {
        this.doNotReplaceAllEmail = doNotReplaceAllEmail;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailComment() {
        return emailComment;
    }

    public void setEmailComment(final String emailComment) {
        this.emailComment = emailComment;
    }

    public String getEmailUsageRowId() {
        return emailUsageRowId;
    }

    public void setEmailUsageRowId(final String emailUsageRowId) {
        this.emailUsageRowId = emailUsageRowId;
    }

    public String getEmailUsagePublic() {
        return emailUsagePublic;
    }

    public void setEmailUsagePublic(final String emailUsagePublic) {
        this.emailUsagePublic = emailUsagePublic;
    }

    public String getEmailUsageTypeRowId() {
        return emailUsageTypeRowId;
    }

    public void setEmailUsageTypeRowId(final String emailUsageTypeRowId) {
        this.emailUsageTypeRowId = emailUsageTypeRowId;
    }

    public String getEmailUsageTypePrimary() {
        return emailUsageTypePrimary;
    }

    public void setEmailUsageTypePrimary(final String emailUsageTypePrimary) {
        this.emailUsageTypePrimary = emailUsageTypePrimary;
    }

    public String getEmailUsageType() {
        return emailUsageType;
    }

    public void setEmailUsageType(final String emailUsageType) {
        this.emailUsageType = emailUsageType;
    }

    public String getEmailUseFor() {
        return emailUseFor;
    }

    public void setEmailUseFor(final String emailUseFor) {
        this.emailUseFor = emailUseFor;
    }

    public String getEmailUseForTenanted() {
        return emailUseForTenanted;
    }

    public void setEmailUseForTenanted(final String emailUseForTenanted) {
        this.emailUseForTenanted = emailUseForTenanted;
    }

    public String getEmailUsageComments() {
        return emailUsageComments;
    }

    public void setEmailUsageComments(final String emailUsageComments) {
        this.emailUsageComments = emailUsageComments;
    }

    public String getExistingEmailId() {
        return existingEmailId;
    }

    public void setExistingEmailId(final String existingEmailId) {
        this.existingEmailId = existingEmailId;
    }

    public String getNewEmailId() {
        return newEmailId;
    }

    public void setNewEmailId(final String newEmailId) {
        this.newEmailId = newEmailId;
    }

    public String getInstantMessengerRowId() {
        return instantMessengerRowId;
    }

    public void setInstantMessengerRowId(final String instantMessengerRowId) {
        this.instantMessengerRowId = instantMessengerRowId;
    }

    public String getDeleteInstantMessenger() {
        return deleteInstantMessenger;
    }

    public void setDeleteInstantMessenger(final String deleteInstantMessenger) {
        this.deleteInstantMessenger = deleteInstantMessenger;
    }

    public String getDoNotReplaceAllInstantMessenger() {
        return doNotReplaceAllInstantMessenger;
    }

    public void setDoNotReplaceAllInstantMessenger(final String doNotReplaceAllInstantMessenger) {
        this.doNotReplaceAllInstantMessenger = doNotReplaceAllInstantMessenger;
    }

    public String getInstantMessengerAddress() {
        return instantMessengerAddress;
    }

    public void setInstantMessengerAddress(final String instantMessengerAddress) {
        this.instantMessengerAddress = instantMessengerAddress;
    }

    public String getInstantMessengerType() {
        return instantMessengerType;
    }

    public void setInstantMessengerType(final String instantMessengerType) {
        this.instantMessengerType = instantMessengerType;
    }

    public String getInstantMessengerComment() {
        return instantMessengerComment;
    }

    public void setInstantMessengerComment(final String instantMessengerComment) {
        this.instantMessengerComment = instantMessengerComment;
    }

    public String getInstantMessengerUsageRowId() {
        return instantMessengerUsageRowId;
    }

    public void setInstantMessengerUsageRowId(final String instantMessengerUsageRowId) {
        this.instantMessengerUsageRowId = instantMessengerUsageRowId;
    }

    public String getInstantMessengerUsagePublic() {
        return instantMessengerUsagePublic;
    }

    public void setInstantMessengerUsagePublic(final String instantMessengerUsagePublic) {
        this.instantMessengerUsagePublic = instantMessengerUsagePublic;
    }

    public String getInstantMessengerUsageTypeRowId() {
        return instantMessengerUsageTypeRowId;
    }

    public void setInstantMessengerUsageTypeRowId(final String instantMessengerUsageTypeRowId) {
        this.instantMessengerUsageTypeRowId = instantMessengerUsageTypeRowId;
    }

    public String getInstantMessengerUsageTypePrimary() {
        return instantMessengerUsageTypePrimary;
    }

    public void setInstantMessengerUsageTypePrimary(final String instantMessengerUsageTypePrimary) {
        this.instantMessengerUsageTypePrimary = instantMessengerUsageTypePrimary;
    }

    public String getInstantMessengerUsageType() {
        return instantMessengerUsageType;
    }

    public void setInstantMessengerUsageType(final String instantMessengerUsageType) {
        this.instantMessengerUsageType = instantMessengerUsageType;
    }

    public String getInstantMessengerUseFor() {
        return instantMessengerUseFor;
    }

    public void setInstantMessengerUseFor(final String instantMessengerUseFor) {
        this.instantMessengerUseFor = instantMessengerUseFor;
    }

    public String getInstantMessengerUseForTenanted() {
        return instantMessengerUseForTenanted;
    }

    public void setInstantMessengerUseForTenanted(final String instantMessengerUseForTenanted) {
        this.instantMessengerUseForTenanted = instantMessengerUseForTenanted;
    }

    public String getInstantMessengerUsageComments() {
        return instantMessengerUsageComments;
    }

    public void setInstantMessengerUsageComments(final String instantMessengerUsageComments) {
        this.instantMessengerUsageComments = instantMessengerUsageComments;
    }

    public String getExistingInstantMessengerId() {
        return existingInstantMessengerId;
    }

    public void setExistingInstantMessengerId(final String existingInstantMessengerId) {
        this.existingInstantMessengerId = existingInstantMessengerId;
    }

    public String getNewInstantMessengerId() {
        return newInstantMessengerId;
    }

    public void setNewInstantMessengerId(final String newInstantMessengerId) {
        this.newInstantMessengerId = newInstantMessengerId;
    }

    public String getWebAddressRowId() {
        return webAddressRowId;
    }

    public void setWebAddressRowId(final String webAddressRowId) {
        this.webAddressRowId = webAddressRowId;
    }

    public String getDeleteWebAddress() {
        return deleteWebAddress;
    }

    public void setDeleteWebAddress(final String deleteWebAddress) {
        this.deleteWebAddress = deleteWebAddress;
    }

    public String getDoNotReplaceAllWebAddress() {
        return doNotReplaceAllWebAddress;
    }

    public void setDoNotReplaceAllWebAddress(final String doNotReplaceAllWebAddress) {
        this.doNotReplaceAllWebAddress = doNotReplaceAllWebAddress;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(final String webAddress) {
        this.webAddress = webAddress;
    }

    public String getWebAddressComment() {
        return webAddressComment;
    }

    public void setWebAddressComment(final String webAddressComment) {
        this.webAddressComment = webAddressComment;
    }

    public String getWebAddressUsageRowId() {
        return webAddressUsageRowId;
    }

    public void setWebAddressUsageRowId(final String webAddressUsageRowId) {
        this.webAddressUsageRowId = webAddressUsageRowId;
    }

    public String getWebAddressUsagePublic() {
        return webAddressUsagePublic;
    }

    public void setWebAddressUsagePublic(final String webAddressUsagePublic) {
        this.webAddressUsagePublic = webAddressUsagePublic;
    }

    public String getWebAddressUsageTypeRowId() {
        return webAddressUsageTypeRowId;
    }

    public void setWebAddressUsageTypeRowId(final String webAddressUsageTypeRowId) {
        this.webAddressUsageTypeRowId = webAddressUsageTypeRowId;
    }

    public String getWebAddressUsageTypePrimary() {
        return webAddressUsageTypePrimary;
    }

    public void setWebAddressUsageTypePrimary(final String webAddressUsageTypePrimary) {
        this.webAddressUsageTypePrimary = webAddressUsageTypePrimary;
    }

    public String getWebAddressUsageType() {
        return webAddressUsageType;
    }

    public void setWebAddressUsageType(final String webAddressUsageType) {
        this.webAddressUsageType = webAddressUsageType;
    }

    public String getWebAddressUseFor() {
        return webAddressUseFor;
    }

    public void setWebAddressUseFor(final String webAddressUseFor) {
        this.webAddressUseFor = webAddressUseFor;
    }

    public String getWebAddressUseForTenanted() {
        return webAddressUseForTenanted;
    }

    public void setWebAddressUseForTenanted(final String webAddressUseForTenanted) {
        this.webAddressUseForTenanted = webAddressUseForTenanted;
    }

    public String getWebAddressUsageComments() {
        return webAddressUsageComments;
    }

    public void setWebAddressUsageComments(final String webAddressUsageComments) {
        this.webAddressUsageComments = webAddressUsageComments;
    }

    public String getExistingWebAddressId() {
        return existingWebAddressId;
    }

    public void setExistingWebAddressId(final String existingWebAddressId) {
        this.existingWebAddressId = existingWebAddressId;
    }

    public String getNewWebAddressId() {
        return newWebAddressId;
    }

    public void setNewWebAddressId(final String newWebAddressId) {
        this.newWebAddressId = newWebAddressId;
    }

    public String getContactTypeRowId() {
        return contactTypeRowId;
    }

    public void setContactTypeRowId(final String contactTypeRowId) {
        this.contactTypeRowId = contactTypeRowId;
    }

    public String getContactTypeTenanted1() {
        return contactTypeTenanted1;
    }

    public void setContactTypeTenanted1(final String contactTypeTenanted1) {
        this.contactTypeTenanted1 = contactTypeTenanted1;
    }

    public String getContactTypeTenanted2() {
        return contactTypeTenanted2;
    }

    public void setContactTypeTenanted2(final String contactTypeTenanted2) {
        this.contactTypeTenanted2 = contactTypeTenanted2;
    }

    public String getContactTypeTenanted3() {
        return contactTypeTenanted3;
    }

    public void setContactTypeTenanted3(final String contactTypeTenanted3) {
        this.contactTypeTenanted3 = contactTypeTenanted3;
    }

    public String getContactTypeTenanted4() {
        return contactTypeTenanted4;
    }

    public void setContactTypeTenanted4(final String contactTypeTenanted4) {
        this.contactTypeTenanted4 = contactTypeTenanted4;
    }

    public String getContactTypeTenanted5() {
        return contactTypeTenanted5;
    }

    public void setContactTypeTenanted5(final String contactTypeTenanted5) {
        this.contactTypeTenanted5 = contactTypeTenanted5;
    }

    public String getContactTypeTenanted6() {
        return contactTypeTenanted6;
    }

    public void setContactTypeTenanted6(final String contactTypeTenanted6) {
        this.contactTypeTenanted6 = contactTypeTenanted6;
    }

    public String getContactTypeTenanted7() {
        return contactTypeTenanted7;
    }

    public void setContactTypeTenanted7(final String contactTypeTenanted7) {
        this.contactTypeTenanted7 = contactTypeTenanted7;
    }

    public String getContactTypeTenanted8() {
        return contactTypeTenanted8;
    }

    public void setContactTypeTenanted8(final String contactTypeTenanted8) {
        this.contactTypeTenanted8 = contactTypeTenanted8;
    }

    public String getContactTypeTenanted9() {
        return contactTypeTenanted9;
    }

    public void setContactTypeTenanted9(final String contactTypeTenanted9) {
        this.contactTypeTenanted9 = contactTypeTenanted9;
    }

    public String getContactTypeTenanted10() {
        return contactTypeTenanted10;
    }

    public void setContactTypeTenanted10(final String contactTypeTenanted10) {
        this.contactTypeTenanted10 = contactTypeTenanted10;
    }

    public String getBusinessEntityContactType() {
        return businessEntityContactType;
    }

    public void setBusinessEntityContactType(final String businessEntityContactType) {
        this.businessEntityContactType = businessEntityContactType;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(final String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

}
