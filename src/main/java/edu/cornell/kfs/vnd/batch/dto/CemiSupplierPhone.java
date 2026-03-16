package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierPhone {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorPhoneNumber> matchingVendorPhoneNumbers;
    private String supplierId;
    private String phoneId;
    private String country;
    private String internationalPhoneCode;
    private String phoneNumber;
    private String phoneExtension;
    private String phoneDeviceType;
    private String phonePrimary;
    private List<String> phoneUses;
    private List<String> phoneTenantedUses;
    private String comments;

    public CemiSupplierPhone(final List<VendorPhoneNumber> matchingVendorPhoneNumbers,
            final String supplierId, int phoneNumberCount) {
        Validate.isTrue(CollectionUtils.isNotEmpty(matchingVendorPhoneNumbers),
                "matchingVendorPhoneNumbers cannot be null or empty");

        final VendorPhoneNumber firstPhoneNumber = matchingVendorPhoneNumbers.get(0);
        this.matchingVendorPhoneNumbers = matchingVendorPhoneNumbers;
        this.supplierId = supplierId;
        this.phoneId = buildSupplierPhoneId(firstPhoneNumber, supplierId, phoneNumberCount);
        this.country = CemiVendorConstants.COUNTRY_CODE_UNITED_STATES;
        this.internationalPhoneCode = CemiVendorConstants.DEFAULT_INTERNATIONAL_PHONE_TYPE;
        this.phoneNumber = firstPhoneNumber.getVendorPhoneNumber();
        this.phoneExtension = firstPhoneNumber.getVendorPhoneExtensionNumber();
        this.phoneDeviceType = CemiVendorConstants.DEFAULT_PHONE_DEVICE_TYPE;
        this.phonePrimary = determineIfFirstPhoneNumberForSettingPrimaryIndicator(phoneNumberCount);
        this.phoneUses = determinePhoneUseValuesBasedOnPhoneTypes(matchingVendorPhoneNumbers,
                firstPhoneNumber.getVendorHeaderGeneratedIdentifier(), firstPhoneNumber.getVendorDetailAssignedIdentifier());
        this.phoneTenantedUses = determinePhoneTenantedUseValuesBasedOnPhoneTypes(matchingVendorPhoneNumbers,
                firstPhoneNumber.getVendorHeaderGeneratedIdentifier(), firstPhoneNumber.getVendorDetailAssignedIdentifier());
        
        //columns not populated with this load
        this.comments = CemiVendorConstants.EMPTY_STRING;
    }
    
    private static String buildSupplierPhoneId(final VendorPhoneNumber vendorPhoneNumber, final String supplierId, int phoneNumberCount) {
        return MessageFormat.format(CemiVendorConstants.PHONE_ID_FORMAT,
                supplierId,
                Integer.toString(vendorPhoneNumber.getVendorPhoneGeneratedIdentifier()),
                Integer.toString(phoneNumberCount));
    }
    
    private static String determineIfFirstPhoneNumberForSettingPrimaryIndicator(int phoneNumberCount) {
        final boolean isPrimary = (phoneNumberCount == 1);
        return CemiUtils.convertToBooleanValueForFileExtract(isPrimary);
    }

    private static List<String> determinePhoneUseValuesBasedOnPhoneTypes(final List<VendorPhoneNumber> vendorPhoneNumbers,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingPhoneUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.PHONE_USES, vendorPhoneNumbers, VendorPhoneNumber::getVendorPhoneTypeCode);
        if (matchingPhoneUses.length > CemiVendorConstants.MAX_PHONE_USES) {
            LOG.warn("determinePhoneUseValuesBasedOnPhoneTypes, Found a total of {} phone uses across {} "
                    + "duplicate phones for Vendor {}-{}; only the first {} will be used in the output",
                    matchingPhoneUses.length, vendorPhoneNumbers.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_PHONE_USES);
        }
        return CemiUtils.createListWithElementsAndMinimumSize(
                CemiVendorConstants.MAX_PHONE_USES, matchingPhoneUses);
    }

    private static List<String> determinePhoneTenantedUseValuesBasedOnPhoneTypes(final List<VendorPhoneNumber> vendorPhoneNumbers,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        final String[] matchingPhoneTenantedUses = CemiUtils.getDistinctValuesFromMatchingSubLists(
                CemiVendorConstants.PHONE_TENANTED_USES, vendorPhoneNumbers, VendorPhoneNumber::getVendorPhoneTypeCode);
        if (matchingPhoneTenantedUses.length > CemiVendorConstants.MAX_PHONE_TENANTED_USES) {
            LOG.warn("determinePhoneUseTenantedValuesBasedOnPhoneTypes, Found a total of {} phone tenanted uses across {} "
                    + "duplicate phones for Vendor {}-{}; only the first {} will be used in the output",
                    matchingPhoneTenantedUses.length, vendorPhoneNumbers.size(), vendorHeaderGeneratedIdentifier,
                    vendorDetailAssignedIdentifier, CemiVendorConstants.MAX_PHONE_TENANTED_USES);
        }
        return CemiUtils.createListWithElementsAndMinimumSize(
                CemiVendorConstants.MAX_PHONE_TENANTED_USES, matchingPhoneTenantedUses);
    }

    public List<VendorPhoneNumber> getMatchingVendorPhoneNumbers() {
        return matchingVendorPhoneNumbers;
    }

    public void setMatchingVendorPhoneNumbers(List<VendorPhoneNumber> matchingVendorPhoneNumbers) {
        this.matchingVendorPhoneNumbers = matchingVendorPhoneNumbers;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getInternationalPhoneCode() {
        return internationalPhoneCode;
    }

    public void setInternationalPhoneCode(String internationalPhoneCode) {
        this.internationalPhoneCode = internationalPhoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneDeviceType() {
        return phoneDeviceType;
    }

    public void setPhoneDeviceType(String phoneDeviceType) {
        this.phoneDeviceType = phoneDeviceType;
    }

    public String getPhonePrimary() {
        return phonePrimary;
    }

    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }

    public List<String> getPhoneUses() {
        return phoneUses;
    }

    public void setPhoneUses(final List<String> phoneUses) {
        this.phoneUses = phoneUses;
    }

    public List<String> getPhoneTenantedUses() {
        return phoneTenantedUses;
    }

    public void setPhoneTenantedUses(final List<String> phoneTenantedUses) {
        this.phoneTenantedUses = phoneTenantedUses;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
