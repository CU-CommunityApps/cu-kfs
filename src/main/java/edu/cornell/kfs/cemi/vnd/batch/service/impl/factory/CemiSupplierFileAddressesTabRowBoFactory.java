package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;

public class CemiSupplierFileAddressesTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorAddress> matchingAddresses;
    private VendorAddress firstAddress;
    private String supplierId;
    private String vendorTypeCode;
    private int addressIndex;

    public CemiSupplierFileAddressesTabRowBoFactory(final List<VendorAddress> matchingAddresses,
            final String supplierId, final String vendorTypeCode, final int addressIndex) {
        Validate.isTrue(CollectionUtils.isNotEmpty(matchingAddresses), "matchingAddresses cannot be null or empty");
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.notBlank(vendorTypeCode, "vendorTypeCode cannot be blank");
        Validate.isTrue(addressIndex > 0, "addressIndex must be a positive integer");
        this.matchingAddresses = matchingAddresses;
        this.firstAddress = matchingAddresses.get(0);
        this.supplierId = supplierId;
        this.vendorTypeCode = vendorTypeCode;
        this.addressIndex = addressIndex;
    }

    public static CemiSupplierFileAddressesTabRowBo createTabRowBoFrom(final List<VendorAddress> matchingAddresses,
            final String supplierId, final String vendorTypeCode, final int addressIndex) {
        final CemiSupplierFileAddressesTabRowBoFactory factory = new CemiSupplierFileAddressesTabRowBoFactory(
                matchingAddresses, supplierId, vendorTypeCode, addressIndex);
        return factory.createCemiSupplierFileAddressesTabRowBo();
    }

    public CemiSupplierFileAddressesTabRowBo createCemiSupplierFileAddressesTabRowBo() {
        final CemiSupplierFileAddressesTabRowBo addressRowBo = new CemiSupplierFileAddressesTabRowBo();

        final List<String> addressUses = determineAddressUseValuesBasedOnAddressTypes();
        final List<String> addressTenantedUses = determineAddressTenantedUseValuesBasedOnAddressTypes();

        addressRowBo.setSupplierId(supplierId);
        addressRowBo.setAddressId(determineAddressId());
        addressRowBo.setCountryForAddress(firstAddress.getVendorCountryCode());
        addressRowBo.setAddressLine1(firstAddress.getVendorLine1Address());
        addressRowBo.setAddressLine2(firstAddress.getVendorLine2Address());
        addressRowBo.setCity(firstAddress.getVendorCityName());
        addressRowBo.setState(firstAddress.getVendorStateCode());
        addressRowBo.setZipCode(firstAddress.getVendorZipCode());
        addressRowBo.setAddressPrimary(determineWhetherAtLeastOneAddressIsPrimary());
        addressRowBo.setAddressType(CemiSupplierConstants.DEFAULT_ADDRESS_TYPE);
        addressRowBo.setAddressUse1(addressUses.get(0));
        addressRowBo.setAddressUse2(addressUses.get(1));
        addressRowBo.setAddressUse3(addressUses.get(2));
        addressRowBo.setAddressUse4(addressUses.get(3));
        addressRowBo.setAddressUseTenanted1(addressTenantedUses.get(0));
        addressRowBo.setAddressUseTenanted2(addressTenantedUses.get(1));
        addressRowBo.setAddressUseTenanted3(addressTenantedUses.get(2));
        addressRowBo.setAddressUseTenanted4(addressTenantedUses.get(3));
        addressRowBo.setComments(CemiBaseConstants.EMPTY_STRING);

        return addressRowBo;
    }

    private String determineAddressId() {
        return MessageFormat.format(CemiSupplierConstants.ADDRESS_ID_FORMAT,
                supplierId,
                Integer.toString(firstAddress.getVendorAddressGeneratedIdentifier()),
                Integer.toString(addressIndex));
    }

    private String determineWhetherAtLeastOneAddressIsPrimary() {
        final boolean atLeastOneAddressIsPrimary = CemiVendorUtils.containsPrimaryVendorAddress(
                vendorTypeCode, matchingAddresses);
        return CemiUtils.convertToBooleanValueForFileExtract(atLeastOneAddressIsPrimary);
    }

    private List<String> determineAddressUseValuesBasedOnAddressTypes() {
        final String[] matchingAddressUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.ADDRESS_USES, matchingAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingAddressUses.length > CemiSupplierConstants.MAX_ADDRESS_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstAddress.getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstAddress.getVendorDetailAssignedIdentifier();
            LOG.warn("determineAddressUseValuesBasedOnAddressTypes, Found a total of {} address uses across {} "
                    + "duplicate addresses for Vendor {}-{}; only the first {} will be used in the output",
                    matchingAddressUses.length, matchingAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_ADDRESS_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_ADDRESS_USES, matchingAddressUses);
    }

    private List<String> determineAddressTenantedUseValuesBasedOnAddressTypes() {
        final String[] matchingAddressTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.ADDRESS_TENANTED_USES, matchingAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingAddressTenantedUses.length > CemiSupplierConstants.MAX_ADDRESS_TENANTED_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstAddress.getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstAddress.getVendorDetailAssignedIdentifier();
            LOG.warn("determineAddressUseValuesBasedOnAddressTypes, Found a total of {} address tenanted uses across {} "
                    + "duplicate addresses for Vendor {}-{}; only the first {} will be used in the output",
                    matchingAddressTenantedUses.length, matchingAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_ADDRESS_TENANTED_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_ADDRESS_TENANTED_USES, matchingAddressTenantedUses);
    }

}
