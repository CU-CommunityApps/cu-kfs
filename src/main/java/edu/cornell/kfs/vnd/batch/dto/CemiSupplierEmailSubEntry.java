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

public class CemiSupplierEmailSubEntry {

    private static final Logger LOG = LogManager.getLogger();

    public static final CemiSupplierEmailSubEntry EMPTY = new CemiSupplierEmailSubEntry();

    private final List<VendorAddress> vendorAddresses;
    private final String supplierId;
    private final String emailId;
    private final String emailAddress;
    private final String emailPrimary;
    private final List<String> emailUseFor;
    private final List<String> useForTenanted;
    
    private CemiSupplierEmailSubEntry() {
        this.vendorAddresses = List.of();
        this.supplierId = CemiVendorConstants.EMPTY_STRING;
        this.emailId = CemiVendorConstants.EMPTY_STRING;
        this.emailAddress = CemiVendorConstants.EMPTY_STRING;
        this.emailPrimary = CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor = CemiUtils.createListOfEmptyStrings(CemiVendorConstants.MAX_EMAIL_USES);
        this.useForTenanted = CemiUtils.createListOfEmptyStrings(CemiVendorConstants.MAX_EMAIL_TENANTED_USES);
    }
    
    public CemiSupplierEmailSubEntry(final List<VendorAddress> vendorAddresses, final String supplierId,
                final boolean primaryAddress, int index) {
        Validate.isTrue(CollectionUtils.isNotEmpty(vendorAddresses), "vendorAddresses cannot be null or empty");

        final VendorAddress firstAddress = vendorAddresses.get(0);
        this.vendorAddresses = vendorAddresses;
        this.supplierId = supplierId;
        this.emailId = determineEmailId(supplierId, firstAddress.getVendorAddressGeneratedIdentifier(), index);
        this.emailAddress = firstAddress.getVendorAddressEmailAddress();
        this.emailPrimary = CemiUtils.convertToBooleanValueForFileExtract(primaryAddress);
        this.emailUseFor = determineEmailUseFor(vendorAddresses,
                firstAddress.getVendorHeaderGeneratedIdentifier(), firstAddress.getVendorDetailAssignedIdentifier());
        this.useForTenanted = determineUsesForTenanted(vendorAddresses,
                firstAddress.getVendorHeaderGeneratedIdentifier(), firstAddress.getVendorDetailAssignedIdentifier());
    }    

    private static List<String> determineUsesForTenanted(final List<VendorAddress> vendorAddresses,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingEmailTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.ADDRESS_TENANTED_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingEmailTenantedUses.length > CemiVendorConstants.MAX_EMAIL_TENANTED_USES) {
            LOG.warn("determineUsesForTenanted, Found a total of {} email tenanted uses across {} "
                    + "duplicate emails for Vendor {}-{}; only the first {} will be used in the output",
                    matchingEmailTenantedUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_EMAIL_TENANTED_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiVendorConstants.MAX_EMAIL_TENANTED_USES, matchingEmailTenantedUses);
    }

    private String determineEmailId(String supplierId, Integer vendorAddressGeneratedIdentifier, int index) {
        return MessageFormat.format(CemiVendorConstants.EMAIL_ID_FORMAT,
                supplierId, 
                Integer.toString(vendorAddressGeneratedIdentifier),
                Integer.toString(index));
    }
    
    private static List<String> determineEmailUseFor(final List<VendorAddress> vendorAddresses,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingEmailUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.ADDRESS_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingEmailUses.length > CemiVendorConstants.MAX_EMAIL_USES) {
            LOG.warn("determineEmailUseFor, Found a total of {} email uses across {} "
                    + "duplicate emails for Vendor {}-{}; only the first {} will be used in the output",
                    matchingEmailUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_EMAIL_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiVendorConstants.MAX_EMAIL_USES, matchingEmailUses);
    }

    public List<VendorAddress> getVendorAddresses() {
        return vendorAddresses;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailPrimary() {
        return emailPrimary;
    }

    public List<String> getEmailUseFor() {
        return emailUseFor;
    }

    public List<String> getUseForTenanted() {
        return useForTenanted;
    }

}
