package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiEntityContactPhoneBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private Integer vendorContactGeneratedIdentifier;
    private Integer vendorContactPhoneGeneratedIdentifier;

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
    private String existingPhoneId;
    private String newPhoneId;

    private List<CemiEntityContactGenericUsageBo> phoneUsages;

    public Integer getVendorContactGeneratedIdentifier() {
        return vendorContactGeneratedIdentifier;
    }

    public void setVendorContactGeneratedIdentifier(final Integer vendorContactGeneratedIdentifier) {
        this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
    }

    public Integer getVendorContactPhoneGeneratedIdentifier() {
        return vendorContactPhoneGeneratedIdentifier;
    }

    public void setVendorContactPhoneGeneratedIdentifier(final Integer vendorContactPhoneGeneratedIdentifier) {
        this.vendorContactPhoneGeneratedIdentifier = vendorContactPhoneGeneratedIdentifier;
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

    public List<CemiEntityContactGenericUsageBo> getPhoneUsages() {
        if (phoneUsages == null) {
            phoneUsages = new ArrayList<>();
        }
        return phoneUsages;
    }

    public void setPhoneUsages(final List<CemiEntityContactGenericUsageBo> phoneUsages) {
        this.phoneUsages = phoneUsages;
    }

    public void addPhoneUsage(final CemiEntityContactGenericUsageBo phoneUsage) {
        getPhoneUsages().add(phoneUsage);
    }

}
