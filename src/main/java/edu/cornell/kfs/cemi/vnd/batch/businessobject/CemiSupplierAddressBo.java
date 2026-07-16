package edu.cornell.kfs.cemi.vnd.batch.businessobject;

// CHANGE extends TO USE edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
// MAKE ALL RELATED CODING CHANGES NEEDED TO MAKE BO FOLLOW PATTERN. 
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiSupplierAddressBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 3111569769858194235L;

    private String supplierId;
    private String addressId;
    private String countryForAddress;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String addressPrimary;
    private String addressType;
    private String addressUse1;
    private String addressUse2;
    private String addressUse3;
    private String addressUse4;
    private String addressUseTenanted1;
    private String addressUseTenanted2;
    private String addressUseTenanted3;
    private String addressUseTenanted4;
    private String comments;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(final String addressId) {
        this.addressId = addressId;
    }

    public String getCountryForAddress() {
        return countryForAddress;
    }

    public void setCountryForAddress(final String countryForAddress) {
        this.countryForAddress = countryForAddress;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddressPrimary() {
        return addressPrimary;
    }

    public void setAddressPrimary(final String addressPrimary) {
        this.addressPrimary = addressPrimary;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(final String addressType) {
        this.addressType = addressType;
    }

    public String getAddressUse1() {
        return addressUse1;
    }

    public void setAddressUse1(final String addressUse1) {
        this.addressUse1 = addressUse1;
    }

    public String getAddressUse2() {
        return addressUse2;
    }

    public void setAddressUse2(final String addressUse2) {
        this.addressUse2 = addressUse2;
    }

    public String getAddressUse3() {
        return addressUse3;
    }

    public void setAddressUse3(final String addressUse3) {
        this.addressUse3 = addressUse3;
    }

    public String getAddressUse4() {
        return addressUse4;
    }

    public void setAddressUse4(final String addressUse4) {
        this.addressUse4 = addressUse4;
    }

    public String getAddressUseTenanted1() {
        return addressUseTenanted1;
    }

    public void setAddressUseTenanted1(final String addressUseTenanted1) {
        this.addressUseTenanted1 = addressUseTenanted1;
    }

    public String getAddressUseTenanted2() {
        return addressUseTenanted2;
    }

    public void setAddressUseTenanted2(final String addressUseTenanted2) {
        this.addressUseTenanted2 = addressUseTenanted2;
    }

    public String getAddressUseTenanted3() {
        return addressUseTenanted3;
    }

    public void setAddressUseTenanted3(final String addressUseTenanted3) {
        this.addressUseTenanted3 = addressUseTenanted3;
    }

    public String getAddressUseTenanted4() {
        return addressUseTenanted4;
    }

    public void setAddressUseTenanted4(final String addressUseTenanted4) {
        this.addressUseTenanted4 = addressUseTenanted4;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

}
