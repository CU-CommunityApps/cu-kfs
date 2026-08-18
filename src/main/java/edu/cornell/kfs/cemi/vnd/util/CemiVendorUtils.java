package edu.cornell.kfs.cemi.vnd.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.sys.CUKFSConstants;

@SuppressWarnings("deprecation")
public final class CemiVendorUtils {

    private static final Logger LOG = LogManager.getLogger();

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
                    CemiSupplierConstants.AllDefinedAddressTypes.PURCHASE_ORDER, vendorAddress);
        } else {
            return addressTypeIsActiveAndIsDefaultAndMatches(
                    CemiSupplierConstants.AllDefinedAddressTypes.REMIT, vendorAddress);
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

    public static Map<String, List<VendorAddress>> groupKfsVendorAddressesByLineDataThenPrioritizeByType(
            final List<VendorAddress> vendorAddresses, String addressTypeWithHighestPrecedence) {
        final Map<String, List<VendorAddress>> groupedVendorAddresses = new HashMap<>();

        for (final VendorAddress vendorAddress : vendorAddresses) {
            final String addressKey = CemiVendorUtils.generateAddressKey(vendorAddress);
            final List<VendorAddress> subGroup = groupedVendorAddresses.computeIfAbsent(
                    addressKey, key -> new ArrayList<>());
            subGroup.add(vendorAddress);
        }

        final Comparator<VendorAddress> vendorAddressComparator = getVendorAddressPrecedenceComparator(
                addressTypeWithHighestPrecedence);
        for (final List<VendorAddress> subGroup : groupedVendorAddresses.values()) {
            Collections.sort(subGroup, vendorAddressComparator);
        }

        return groupedVendorAddresses;
    }

    private static Comparator<VendorAddress> getVendorAddressPrecedenceComparator(
            final String addressTypeWithHighestPrecedence) {
        return Comparator
                .comparing(VendorAddress::getVendorAddressTypeCode,
                        (addressType1, addressType2) -> compareAddressTypesForPrecedence(
                                addressType1, addressType2, addressTypeWithHighestPrecedence))
                .thenComparing(VendorAddress::isVendorDefaultAddressIndicator, Comparator.reverseOrder())
                .thenComparing(VendorAddress::getVendorAddressGeneratedIdentifier);
    }

    private static int compareAddressTypesForPrecedence(
            final String addressType1, final String addressType2, final String addressTypeWithHighestPrecedence) {
        if (Strings.CS.equals(addressType1, addressTypeWithHighestPrecedence)) {
            return Strings.CS.equals(addressType2, addressTypeWithHighestPrecedence) ? 0 : -1;
        } else if (Strings.CS.equals(addressType2, addressTypeWithHighestPrecedence)) {
            return 1;
        } else {
            return Strings.CS.compare(addressType1, addressType2);
        }
    }

    public static Optional<PhoneNumber> parsePhoneNumberIfPossible(final VendorContactPhoneNumber vendorContactPhone) {
        try {
            final PhoneNumber parsedPhoneNumber = parsePhoneNumber(vendorContactPhone);
            return Optional.of(parsedPhoneNumber);
        } catch (final NumberParseException | RuntimeException e) {
            LOG.error("parsePhoneNumberIfPossible, Could not parse phone number from Phone BO {} for Vendor Contact {}; "
                    + "this phone number will be skipped",
                    vendorContactPhone.getVendorContactPhoneGeneratedIdentifier(),
                    vendorContactPhone.getVendorContactGeneratedIdentifier(), e);
            return Optional.empty();
        }
    }

    public static PhoneNumber parsePhoneNumber(final VendorContactPhoneNumber vendorContactPhone)
            throws NumberParseException {
        final String rawPhoneNumber = vendorContactPhone.getVendorPhoneNumber();
        Validate.validState(StringUtils.isNotBlank(rawPhoneNumber),
                "Phone BO %s for Vendor Contact %s has a blank phone number; this should NEVER happen",
                vendorContactPhone.getVendorContactPhoneGeneratedIdentifier(),
                vendorContactPhone.getVendorContactGeneratedIdentifier());
        final String tentativeExplicitRegion = isPhoneNumberUsingInternationalFormat(rawPhoneNumber)
                ? null : CemiSupplierConstants.COUNTRY_CODE_UNITED_STATES;
        final PhoneNumber parsedPhoneNumber = getPhoneNumberUtil().parseAndKeepRawInput(
                rawPhoneNumber, tentativeExplicitRegion);
        return parsedPhoneNumber;
    }

    public static boolean isPhoneNumberUsingInternationalFormat(final String rawPhoneNumber) {
        return Strings.CI.startsWith(rawPhoneNumber, CUKFSConstants.PLUS_SIGN);
    }

    public static String getRegionCode(final PhoneNumber phoneNumber) {
        return getPhoneNumberUtil().getRegionCodeForNumber(phoneNumber);
    }

    public static PhoneNumberUtil getPhoneNumberUtil() {
        // The method call below should always return the same singleton instance.
        return PhoneNumberUtil.getInstance();
    }

}
