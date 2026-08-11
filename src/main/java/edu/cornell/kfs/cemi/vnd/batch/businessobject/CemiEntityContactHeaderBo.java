package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.vnd.businessobject.VendorContact;

public class CemiEntityContactHeaderBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = -6869123386201406230L;

    private List<VendorContact> mergedContacts;
    private List<String> mergedTenantedContactTypes;

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

    public List<VendorContact> getMergedContacts() {
        if (mergedContacts == null) {
            mergedContacts = new ArrayList<>();
        }
        return mergedContacts;
    }

    public void setMergedContacts(final List<VendorContact> mergedContacts) {
        this.mergedContacts = mergedContacts;
    }

    public List<String> getMergedTenantedContactTypes() {
        if (mergedTenantedContactTypes == null) {
            mergedTenantedContactTypes = new ArrayList<>();
        }
        return mergedTenantedContactTypes;
    }

    public void setMergedTenantedContactTypes(final List<String> mergedTenantedContactTypes) {
        this.mergedTenantedContactTypes = mergedTenantedContactTypes;
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

}
