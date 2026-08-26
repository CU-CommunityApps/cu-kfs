package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;

public class CemiSupplierEmailBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorAddress> vendorAddresses;
    private Optional<VendorAddress> firstAddress;
    private String supplierId;
    private boolean primaryEmailAddress;
    private int emailIndex;

    public CemiSupplierEmailBoFactory(final List<VendorAddress> vendorAddresses, final String supplierId,
                final boolean primaryEmailAddress, int emailIndex) {
        Validate.notNull(vendorAddresses, "vendorAddresses list cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(supplierId) || vendorAddresses.isEmpty(),
                "supplierId cannot be blank if vendorAddresses list is non-empty");
        Validate.isTrue(vendorAddresses.isEmpty() == (emailIndex <= 0),
                "emailIndex must be a positive value if, and only if, vendorAddresses list is non-empty");
        this.vendorAddresses = vendorAddresses;
        this.firstAddress = vendorAddresses.isEmpty() ? Optional.empty() : Optional.of(vendorAddresses.get(0));
        this.supplierId = supplierId;
        this.primaryEmailAddress = primaryEmailAddress;
        this.emailIndex = emailIndex;
    }

    public static CemiSupplierEmailBo createEmailBoFrom(final List<VendorAddress> vendorAddresses,
                final String supplierId, final boolean primaryEmailAddress, int emailIndex) {
        final CemiSupplierEmailBoFactory factory = new CemiSupplierEmailBoFactory(
                vendorAddresses, supplierId, primaryEmailAddress, emailIndex);
        return factory.createCemiSupplierEmailBo();
    }

    public CemiSupplierEmailBo createCemiSupplierEmailBo() {
        final CemiSupplierEmailBo emailBo = new CemiSupplierEmailBo();

        final List<String> emailUses = determineEmailUseFor();
        final List<String> emailTenantedUses = determineUseForTenanted();

        emailBo.setEmailId(determineEmailId());
        emailBo.setEmailAddress(determineEmailAddress());
        emailBo.setEmailPrimary(determineEmailPrimary());
        emailBo.setEmailUseFor1(emailUses.get(0));
        emailBo.setEmailUseFor2(emailUses.get(1));
        emailBo.setEmailUseFor3(emailUses.get(2));
        emailBo.setEmailUseFor4(emailUses.get(3));
        emailBo.setUseForTenanted1(emailTenantedUses.get(0));
        emailBo.setUseForTenanted2(emailTenantedUses.get(1));
        emailBo.setUseForTenanted3(emailTenantedUses.get(2));
        emailBo.setUseForTenanted4(emailTenantedUses.get(3));

        return emailBo;
    }

    private String determineEmailId() {
        if (firstAddress.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final Integer vendorAddressGeneratedIdentifier = firstAddress.get().getVendorAddressGeneratedIdentifier();
        return MessageFormat.format(CemiSupplierConstants.EMAIL_ID_FORMAT,
                supplierId, 
                Integer.toString(vendorAddressGeneratedIdentifier),
                Integer.toString(emailIndex));
    }

    private String determineEmailAddress() {
        return firstAddress.map(VendorAddress::getVendorAddressEmailAddress)
                .orElse(CemiBaseConstants.EMPTY_STRING);
    }

    private String determineEmailPrimary() {
        return firstAddress.isPresent()
                ? CemiUtils.convertToBooleanValueForFileExtract(primaryEmailAddress) : CemiBaseConstants.EMPTY_STRING;
    }

    private List<String> determineEmailUseFor() {
        if (vendorAddresses.isEmpty()) {
            return CemiUtils.createListOfEmptyStrings(CemiSupplierConstants.MAX_EMAIL_USES);
        }
        final String[] matchingEmailUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.ADDRESS_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingEmailUses.length > CemiSupplierConstants.MAX_EMAIL_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstAddress.get().getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstAddress.get().getVendorDetailAssignedIdentifier();
            LOG.warn("determineEmailUseFor, Found a total of {} email uses across {} "
                    + "duplicate emails for Vendor {}-{}; only the first {} will be used in the output",
                    matchingEmailUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_EMAIL_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_EMAIL_USES, matchingEmailUses);
    }

    private List<String> determineUseForTenanted() {
        if (vendorAddresses.isEmpty()) {
            return CemiUtils.createListOfEmptyStrings(CemiSupplierConstants.MAX_EMAIL_TENANTED_USES);
        }
        final String[] matchingEmailTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.ADDRESS_TENANTED_USES, vendorAddresses, VendorAddress::getVendorAddressTypeCode);
        if (matchingEmailTenantedUses.length > CemiSupplierConstants.MAX_EMAIL_TENANTED_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstAddress.get().getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstAddress.get().getVendorDetailAssignedIdentifier();
            LOG.warn("determineUseForTenanted, Found a total of {} email tenanted uses across {} "
                    + "duplicate emails for Vendor {}-{}; only the first {} will be used in the output",
                    matchingEmailTenantedUses.length, vendorAddresses.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_EMAIL_TENANTED_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_EMAIL_TENANTED_USES, matchingEmailTenantedUses);
    }

}
