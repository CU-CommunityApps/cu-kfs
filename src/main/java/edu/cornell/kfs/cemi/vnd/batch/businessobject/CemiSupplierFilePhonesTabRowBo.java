package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiSupplierFilePhonesTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 7856069668191813164L;

    private String supplierId;
    private String phoneId;
    private String countryIsoCode;
    private String internationalPhoneCode;
    private String phoneNumber;
    private String phoneExtension;
    private String phoneDeviceType;
    private String phonePrimary;
    private String useForPhone;
    private String useForPhone2;
    private String useForPhone3;
    private String useForPhone4;
    private String useForTenantedPhone;
    private String useForTenantedPhone2;
    private String useForTenantedPhone3;
    private String useForTenantedPhone4;
    private String phoneComments;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(final String phoneId) {
        this.phoneId = phoneId;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(final String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
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

    public String getPhonePrimary() {
        return phonePrimary;
    }

    public void setPhonePrimary(final String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }

    public String getUseForPhone() {
        return useForPhone;
    }

    public void setUseForPhone(final String useForPhone) {
        this.useForPhone = useForPhone;
    }

    public String getUseForPhone2() {
        return useForPhone2;
    }

    public void setUseForPhone2(final String useForPhone2) {
        this.useForPhone2 = useForPhone2;
    }

    public String getUseForPhone3() {
        return useForPhone3;
    }

    public void setUseForPhone3(final String useForPhone3) {
        this.useForPhone3 = useForPhone3;
    }

    public String getUseForPhone4() {
        return useForPhone4;
    }

    public void setUseForPhone4(final String useForPhone4) {
        this.useForPhone4 = useForPhone4;
    }

    public String getUseForTenantedPhone() {
        return useForTenantedPhone;
    }

    public void setUseForTenantedPhone(final String useForTenantedPhone) {
        this.useForTenantedPhone = useForTenantedPhone;
    }

    public String getUseForTenantedPhone2() {
        return useForTenantedPhone2;
    }

    public void setUseForTenantedPhone2(final String useForTenantedPhone2) {
        this.useForTenantedPhone2 = useForTenantedPhone2;
    }

    public String getUseForTenantedPhone3() {
        return useForTenantedPhone3;
    }

    public void setUseForTenantedPhone3(final String useForTenantedPhone3) {
        this.useForTenantedPhone3 = useForTenantedPhone3;
    }

    public String getUseForTenantedPhone4() {
        return useForTenantedPhone4;
    }

    public void setUseForTenantedPhone4(final String useForTenantedPhone4) {
        this.useForTenantedPhone4 = useForTenantedPhone4;
    }

    public String getPhoneComments() {
        return phoneComments;
    }

    public void setPhoneComments(final String phoneComments) {
        this.phoneComments = phoneComments;
    }

}
