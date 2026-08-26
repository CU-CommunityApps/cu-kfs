package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFilePhonesTabRowBo;

public class CemiSupplierFilePhonesTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorPhoneNumber> matchingPhoneNumbers;
    private VendorPhoneNumber firstPhoneNumber;
    private String supplierId;
    private int phoneIndex;

    public CemiSupplierFilePhonesTabRowBoFactory(final List<VendorPhoneNumber> matchingPhoneNumbers,
            final String supplierId, final int phoneIndex) {
        Validate.isTrue(CollectionUtils.isNotEmpty(matchingPhoneNumbers),
                "matchingPhoneNumbers cannot be null or empty");
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.isTrue(phoneIndex > 0, "phoneIndex must be a positive integer");
        this.matchingPhoneNumbers = matchingPhoneNumbers;
        this.firstPhoneNumber = matchingPhoneNumbers.get(0);
        this.supplierId = supplierId;
        this.phoneIndex = phoneIndex;
    }

    public static CemiSupplierFilePhonesTabRowBo createTabRowBoFrom(
            final List<VendorPhoneNumber> matchingPhoneNumbers, final String supplierId, final int phoneIndex) {
        final CemiSupplierFilePhonesTabRowBoFactory factory = new CemiSupplierFilePhonesTabRowBoFactory(
                matchingPhoneNumbers, supplierId, phoneIndex);
        return factory.createCemiSupplierFilePhonesTabRowBo();
    }

    public CemiSupplierFilePhonesTabRowBo createCemiSupplierFilePhonesTabRowBo() {
        final CemiSupplierFilePhonesTabRowBo phoneRowBo = new CemiSupplierFilePhonesTabRowBo();

        final List<String> phoneUses = determinePhoneUseValuesBasedOnPhoneTypes();
        final List<String> phoneTenantedUses = determinePhoneTenantedUseValuesBasedOnPhoneTypes();

        phoneRowBo.setSupplierId(supplierId);
        phoneRowBo.setPhoneId(determinePhoneId());
        phoneRowBo.setCountryIsoCode(CemiSupplierConstants.COUNTRY_CODE_UNITED_STATES);
        phoneRowBo.setInternationalPhoneCode(CemiSupplierConstants.DEFAULT_INTERNATIONAL_PHONE_TYPE);
        phoneRowBo.setPhoneNumber(firstPhoneNumber.getVendorPhoneNumber());
        phoneRowBo.setPhoneExtension(firstPhoneNumber.getVendorPhoneExtensionNumber());
        phoneRowBo.setPhoneDeviceType(CemiSupplierConstants.DEFAULT_PHONE_DEVICE_TYPE);
        phoneRowBo.setPhonePrimary(determinePhonePrimary());
        phoneRowBo.setUseForPhone(phoneUses.get(0));
        phoneRowBo.setUseForPhone2(phoneUses.get(1));
        phoneRowBo.setUseForPhone3(phoneUses.get(2));
        phoneRowBo.setUseForPhone4(phoneUses.get(3));
        phoneRowBo.setUseForTenantedPhone(phoneTenantedUses.get(0));
        phoneRowBo.setUseForTenantedPhone2(phoneTenantedUses.get(1));
        phoneRowBo.setUseForTenantedPhone3(phoneTenantedUses.get(2));
        phoneRowBo.setUseForTenantedPhone4(phoneTenantedUses.get(3));
        phoneRowBo.setPhoneComments(CemiSupplierConstants.EMPTY_STRING);

        return phoneRowBo;
    }

    private String determinePhoneId() {
        return MessageFormat.format(CemiSupplierConstants.PHONE_ID_FORMAT,
                supplierId,
                Integer.toString(firstPhoneNumber.getVendorPhoneGeneratedIdentifier()),
                Integer.toString(phoneIndex));
    }

    private String determinePhonePrimary() {
        final boolean isPrimary = (phoneIndex == 1);
        return CemiUtils.convertToBooleanValueForFileExtract(isPrimary);
    }

    private List<String> determinePhoneUseValuesBasedOnPhoneTypes() {
        final String[] matchingPhoneUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.PHONE_USES, matchingPhoneNumbers, VendorPhoneNumber::getVendorPhoneTypeCode);
        if (matchingPhoneUses.length > CemiSupplierConstants.MAX_PHONE_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstPhoneNumber.getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstPhoneNumber.getVendorDetailAssignedIdentifier();
            LOG.warn("determinePhoneUseValuesBasedOnPhoneTypes, Found a total of {} phone uses across {} "
                    + "duplicate phones for Vendor {}-{}; only the first {} will be used in the output",
                    matchingPhoneUses.length, matchingPhoneNumbers.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_PHONE_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_PHONE_USES, matchingPhoneUses);
    }

    private List<String> determinePhoneTenantedUseValuesBasedOnPhoneTypes() {
        final String[] matchingPhoneTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiSupplierConstants.PHONE_TENANTED_USES, matchingPhoneNumbers, VendorPhoneNumber::getVendorPhoneTypeCode);
        if (matchingPhoneTenantedUses.length > CemiSupplierConstants.MAX_PHONE_TENANTED_USES) {
            final Integer vendorHeaderGeneratedIdentifier = firstPhoneNumber.getVendorHeaderGeneratedIdentifier();
            final Integer vendorDetailAssignedIdentifier = firstPhoneNumber.getVendorDetailAssignedIdentifier();
            LOG.warn("determinePhoneUseTenantedValuesBasedOnPhoneTypes, Found a total of {} phone tenanted uses across {} "
                    + "duplicate phones for Vendor {}-{}; only the first {} will be used in the output",
                    matchingPhoneTenantedUses.length, matchingPhoneNumbers.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiSupplierConstants.MAX_PHONE_TENANTED_USES);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_PHONE_TENANTED_USES, matchingPhoneTenantedUses);
    }

}
