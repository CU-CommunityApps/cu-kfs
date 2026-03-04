package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierAddress {

    private VendorAddress vendorAddress;
    private String supplierId;
    private String addressId;
    private String country;
    private String line1;
    private String line2;
    private String city;
    private String stateCode;
    private String zipCode;
    private String addressPrimary;
    private String addressType;
    private String addressUse;
    private String addressUse2;
    private String addressUse3;
    private String addressUse4;
    private String addressUseTenanted;
    private String addressUseTenanted2;
    private String addressUseTenanted3;
    private String addressUseTenanted4;
    private String comments;

    public CemiSupplierAddress(final String vendorTypeCode, final VendorAddress vendorAddress, final String supplierId, int addressCount) {
        this.vendorAddress = vendorAddress;
        this.supplierId = supplierId;
        this.addressId = buildSupplierAddressId(vendorAddress, supplierId, addressCount);
        this.country = vendorAddress.getVendorCountryCode();
        this.line1 = vendorAddress.getVendorLine1Address();
        this.line2 = vendorAddress.getVendorLine2Address();
        this.city = vendorAddress.getVendorCityName();
        this.stateCode = vendorAddress.getVendorStateCode();
        this.zipCode = vendorAddress.getVendorZipCode();
        this.addressPrimary = CemiUtils.convertToBooleanValueForFileExtract(determineWhetherAddressIsPrimary(vendorTypeCode, vendorAddress));
        this.addressType = CemiVendorConstants.DEFAULT_ADDRESS_TYPE;
        assignAddressUseValuesBasedOnAddressType(vendorAddress.getVendorAddressTypeCode());
        assignAddressTenantedUseValuesBasedOnAddressType(vendorAddress.getVendorAddressTypeCode());
        
        //columns not populated with this load
        this.addressUse3 = CemiVendorConstants.EMPTY_STRING;
        this.addressUse4 = CemiVendorConstants.EMPTY_STRING;
        this.addressUseTenanted3 = CemiVendorConstants.EMPTY_STRING;
        this.addressUseTenanted4 = CemiVendorConstants.EMPTY_STRING;
        this.comments = CemiVendorConstants.EMPTY_STRING;
        
    }
    
    private static String buildSupplierAddressId(final VendorAddress vendorAddress, String supplierId, int addressCount) {
        return MessageFormat.format(CemiVendorConstants.ADDRESS_ID_FORMAT,
                supplierId,
                vendorAddress.getVendorLine1Address(),
                Integer.toString(addressCount));
    }
    
    private static boolean isPurchaseOrderVendor(String vendorTypeCode, VendorAddress vendorAddress) {
        return (StringUtils.equals(vendorTypeCode, VendorConstants.VendorTypes.PURCHASE_ORDER));
    }
    
    private static boolean isRemitVendor(String vendorTypeCode, VendorAddress vendorAddress) {
        return (StringUtils.equals(vendorTypeCode, VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER) 
                || (StringUtils.equals(vendorTypeCode, VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER)));
    }
    
    private static boolean determineWhetherAddressIsPrimary(String vendorTypeCode, VendorAddress vendorAddress) {
        if (isPurchaseOrderVendor(vendorTypeCode, vendorAddress)
                && vendorAddress.isActive()
                && vendorAddress.isVendorDefaultAddressIndicator()) {
            return true;
        } else if (isRemitVendor(vendorTypeCode, vendorAddress)
                && vendorAddress.isActive()
                && vendorAddress.isVendorDefaultAddressIndicator()) {
            return true;
        }
        return false;
    }
    
    private void assignAddressUseValuesBasedOnAddressType(String vendorAddressTypeCode) {
        if (StringUtils.isNotBlank(vendorAddressTypeCode)
                && CemiVendorConstants.ADDRESS_USES.containsKey(vendorAddressTypeCode)) {
            List<String> useValuesList = CemiVendorConstants.ADDRESS_USES.get(vendorAddressTypeCode);
            if (useValuesList.size() == 2) {
                setAddressUse(useValuesList.get(0));
                setAddressUse2(useValuesList.get(1));
            } else if (useValuesList.size() == 1) {
                setAddressUse(useValuesList.get(0));
                setAddressUse2(CemiVendorConstants.EMPTY_STRING);
            }
        } else {
            setAddressUse(CemiVendorConstants.EMPTY_STRING);
            setAddressUse2(CemiVendorConstants.EMPTY_STRING);
        }
    }
    
    private void assignAddressTenantedUseValuesBasedOnAddressType(String vendorAddressTypeCode) {
        if (StringUtils.isNotBlank(vendorAddressTypeCode)
                && CemiVendorConstants.ADDRESS_TENANTED_USES.containsKey(vendorAddressTypeCode)) {
            List<String> useTenantedValuesList = CemiVendorConstants.ADDRESS_TENANTED_USES.get(vendorAddressTypeCode);
            if (useTenantedValuesList.size() == 2) {
                setAddressUseTenanted(useTenantedValuesList.get(0));
                setAddressUseTenanted2(useTenantedValuesList.get(1));
            } else if (useTenantedValuesList.size() == 1) {
                setAddressUseTenanted(useTenantedValuesList.get(0));
                setAddressUseTenanted2(CemiVendorConstants.EMPTY_STRING);
            }
        } else {
            setAddressUseTenanted(CemiVendorConstants.EMPTY_STRING);
            setAddressUseTenanted2(CemiVendorConstants.EMPTY_STRING);
        }
    }

    public VendorAddress getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(VendorAddress vendorAddress) {
        this.vendorAddress = vendorAddress;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }
    
    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddressPrimary() {
        return addressPrimary;
    }

    public void setAddressPrimary(String addressPrimary) {
        this.addressPrimary = addressPrimary;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressUse() {
        return addressUse;
    }

    public void setAddressUse(String addressUse) {
        this.addressUse = addressUse;
    }

    public String getAddressUse2() {
        return addressUse2;
    }

    public void setAddressUse2(String addressUse2) {
        this.addressUse2 = addressUse2;
    }

    public String getAddressUse3() {
        return addressUse3;
    }

    public void setAddressUse3(String addressUse3) {
        this.addressUse3 = addressUse3;
    }

    public String getAddressUse4() {
        return addressUse4;
    }

    public void setAddressUse4(String addressUse4) {
        this.addressUse4 = addressUse4;
    }

    public String getAddressUseTenanted() {
        return addressUseTenanted;
    }

    public void setAddressUseTenanted(String addressUseTenanted) {
        this.addressUseTenanted = addressUseTenanted;
    }

    public String getAddressUseTenanted2() {
        return addressUseTenanted2;
    }

    public void setAddressUseTenanted2(String addressUseTenanted2) {
        this.addressUseTenanted2 = addressUseTenanted2;
    }

    public String getAddressUseTenanted3() {
        return addressUseTenanted3;
    }

    public void setAddressUseTenanted3(String addressUseTenanted3) {
        this.addressUseTenanted3 = addressUseTenanted3;
    }

    public String getAddressUseTenanted4() {
        return addressUseTenanted4;
    }

    public void setAddressUseTenanted4(String addressUseTenanted4) {
        this.addressUseTenanted4 = addressUseTenanted4;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
