package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.util.CemiVendorUtils;

public class CemiSupplierAddress {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorAddress> matchingVendorAddresses;
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
    private List<String> addressUses;
    private List<String> addressTenantedUses;
    private String comments;

    public CemiSupplierAddress(final String vendorTypeCode, final List<VendorAddress> matchingVendorAddresses,
            final String supplierId, int addressCount) {
        Validate.isTrue(CollectionUtils.isNotEmpty(matchingVendorAddresses),
                "matchingVendorAddresses cannot be null or empty");

        final VendorAddress firstAddress = matchingVendorAddresses.get(0);
        this.matchingVendorAddresses = matchingVendorAddresses;
        this.supplierId = supplierId;
        this.addressId = buildSupplierAddressId(firstAddress, supplierId, addressCount);
        this.country = firstAddress.getVendorCountryCode();
        this.line1 = firstAddress.getVendorLine1Address();
        this.line2 = firstAddress.getVendorLine2Address();
        this.city = firstAddress.getVendorCityName();
        this.stateCode = firstAddress.getVendorStateCode();
        this.zipCode = firstAddress.getVendorZipCode();
        this.addressPrimary = determineWhetherAtLeastOneAddressIsPrimary(vendorTypeCode, matchingVendorAddresses);
        this.addressType = CemiVendorConstants.DEFAULT_ADDRESS_TYPE;
        this.addressUses = determineAddressUseValuesBasedOnAddressTypes(matchingVendorAddresses,
                firstAddress.getVendorHeaderGeneratedIdentifier(), firstAddress.getVendorDetailAssignedIdentifier());
        this.addressTenantedUses = determineAddressTenantedUseValuesBasedOnAddressTypes(matchingVendorAddresses,
                firstAddress.getVendorHeaderGeneratedIdentifier(), firstAddress.getVendorDetailAssignedIdentifier());
        
        //columns not populated with this load
        this.comments = CemiVendorConstants.EMPTY_STRING;
    }
    
    private static String buildSupplierAddressId(final VendorAddress vendorAddress, String supplierId, int addressCount) {
        return MessageFormat.format(CemiVendorConstants.ADDRESS_ID_FORMAT,
                supplierId,
                Integer.toString(vendorAddress.getVendorAddressGeneratedIdentifier()),
                Integer.toString(addressCount));
    }

    private static String determineWhetherAtLeastOneAddressIsPrimary(final String vendorTypeCode,
            final List<VendorAddress> vendorAddresses) {
        final boolean atLeastOneAddressIsPrimary = CemiVendorUtils.containsPrimaryVendorAddress(
                vendorTypeCode, vendorAddresses);
        return CemiUtils.convertToBooleanValueForFileExtract(atLeastOneAddressIsPrimary);
    }

    private static List<String> determineAddressUseValuesBasedOnAddressTypes(final List<VendorAddress> vendorAddresses,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingAddressUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.ADDRESS_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingAddressUses.length > CemiVendorConstants.MAX_ADDRESS_USES) {
            LOG.warn("determineAddressUseValuesBasedOnAddressTypes, Found a total of {} address uses across {} "
                    + "duplicate addresses for Vendor {}-{}; only the first {} will be used in the output",
                    matchingAddressUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_ADDRESS_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiVendorConstants.MAX_ADDRESS_USES, matchingAddressUses);
    }

    private static List<String> determineAddressTenantedUseValuesBasedOnAddressTypes(final List<VendorAddress> vendorAddresses,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingAddressTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.ADDRESS_TENANTED_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingAddressTenantedUses.length > CemiVendorConstants.MAX_ADDRESS_TENANTED_USES) {
            LOG.warn("determineAddressUseValuesBasedOnAddressTypes, Found a total of {} address tenanted uses across {} "
                    + "duplicate addresses for Vendor {}-{}; only the first {} will be used in the output",
                    matchingAddressTenantedUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_ADDRESS_TENANTED_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiVendorConstants.MAX_ADDRESS_TENANTED_USES, matchingAddressTenantedUses);
    }

    public List<VendorAddress> getMatchingVendorAddresses() {
        return matchingVendorAddresses;
    }

    public void setMatchingVendorAddresses(List<VendorAddress> matchingVendorAddresses) {
        this.matchingVendorAddresses = matchingVendorAddresses;
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

    public List<String> getAddressUses() {
        return addressUses;
    }

    public void setAddressUses(final List<String> addressUses) {
        this.addressUses = addressUses;
    }

    public List<String> getAddressTenantedUses() {
        return addressTenantedUses;
    }

    public void setAddressTenantedUses(final List<String> addressTenantedUses) {
        this.addressTenantedUses = addressTenantedUses;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
