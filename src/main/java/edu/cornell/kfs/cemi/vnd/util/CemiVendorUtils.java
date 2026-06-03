package edu.cornell.kfs.cemi.vnd.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

@SuppressWarnings("deprecation")
public final class CemiVendorUtils {

    public static List<List<VendorAddress>> reorderAddressGroupsToPutPrimaryGroupFirst(
            final String vendorTypeCode, final Collection<List<VendorAddress>> addressGroups) {
        final Stream.Builder<List<VendorAddress>> nonPrimaryGroups = Stream.builder();
        List<VendorAddress> primaryGroup = null;
        for (final List<VendorAddress> addressGroup : addressGroups) {
            if (primaryGroup == null && containsPrimaryVendorAddress(vendorTypeCode, addressGroup)) {
                primaryGroup = addressGroup;
            } else {
                nonPrimaryGroups.add(addressGroup);
            }
        }

        final Stream<List<VendorAddress>> reorderedGroups;
        if (primaryGroup != null) {
            reorderedGroups = Stream.of(Stream.of(primaryGroup), nonPrimaryGroups.build())
                    .flatMap(Function.identity());
        } else {
            reorderedGroups = nonPrimaryGroups.build();
        }
        return reorderedGroups.collect(Collectors.toUnmodifiableList());
    }

    public static boolean containsPrimaryVendorAddress(final String vendorTypeCode,
            final List<VendorAddress> vendorAddresses) {
        return vendorAddresses.stream()
                .anyMatch(vendorAddress -> isPrimaryVendorAddress(vendorTypeCode, vendorAddress));
    }

    // For PO vendors, mark default PO address as Primary
    // For non-PO vendors, mark default Remit address as Primary
    public static boolean isPrimaryVendorAddress(final String vendorTypeCode, final VendorAddress vendorAddress) {
        if (isPurchaseOrderVendor(vendorTypeCode)) {
            return addressTypeIsActiveAndIsDefaultAndMatches(
                    CemiVendorConstants.AllDefinedAddressTypes.PURCHASE_ORDER, vendorAddress);
        } else {
            return addressTypeIsActiveAndIsDefaultAndMatches(
                    CemiVendorConstants.AllDefinedAddressTypes.REMIT, vendorAddress);
        }
    }

    private static boolean isPurchaseOrderVendor(final String vendorTypeCode) {
        return StringUtils.equals(vendorTypeCode, VendorConstants.VendorTypes.PURCHASE_ORDER);
    }

    public static boolean addressTypeIsActiveAndIsDefaultAndMatches(
            final String vendorAddressType, final VendorAddress vendorAddress) {
        return vendorAddress.getVendorAddressTypeCode().equalsIgnoreCase(vendorAddressType)
                && vendorAddress.isActive()
                && vendorAddress.isVendorDefaultAddressIndicator();
    }

    public static String generateAddressKey(final VendorAddress vendorAddress) {
        return CemiUtils.generateConcatenatedKey(
                    vendorAddress.getVendorLine1Address(), vendorAddress.getVendorLine2Address(),
                    vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                    vendorAddress.getVendorZipCode(), vendorAddress.getVendorCountryCode());
    }

    public static String generateAddressKey(final CemiSupplierAddressBo supplierAddress) {
        return CemiUtils.generateConcatenatedKey(
                    supplierAddress.getAddressLine1(), supplierAddress.getAddressLine2(),
                    supplierAddress.getCity(), supplierAddress.getState(),
                    supplierAddress.getZipCode(), supplierAddress.getCountryForAddress());
    }

}
