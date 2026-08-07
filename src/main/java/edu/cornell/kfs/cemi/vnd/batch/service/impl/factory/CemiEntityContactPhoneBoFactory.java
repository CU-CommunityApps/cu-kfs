package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.PhoneDeviceTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiEntityContactPhoneBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private Optional<VendorContactPhoneNumber> vendorContactPhoneNumber;
    private int phoneIndex;
    private PhoneNumberUtil phoneNumberUtil;
    private Optional<PhoneNumber> parsedPhoneNumber;

    public CemiEntityContactPhoneBoFactory(final Optional<VendorContactPhoneNumber> vendorContactPhoneNumber,
            final int phoneIndex) {
        Validate.notNull(vendorContactPhoneNumber, "vendorContactPhoneNumber wrapper object cannot be null");
        this.vendorContactPhoneNumber = vendorContactPhoneNumber;
        this.phoneIndex = phoneIndex;
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
        this.parsedPhoneNumber = vendorContactPhoneNumber.map(this::parsePhoneNumberIfPossible)
                .orElseGet(Optional::empty);
    }

    private Optional<PhoneNumber> parsePhoneNumberIfPossible(final VendorContactPhoneNumber vendorPhone) {
        final String rawPhoneNumber = vendorPhone.getVendorPhoneNumber();
        Validate.validState(StringUtils.isNotBlank(rawPhoneNumber),
                "Phone BO %s for Vendor Contact %s has a blank phone number; this should NEVER happen",
                vendorPhone.getVendorContactPhoneGeneratedIdentifier(),
                vendorPhone.getVendorContactGeneratedIdentifier());

        try {
            final String tentativeExplicitRegion = isPhoneNumberUsingInternationalFormat(rawPhoneNumber)
                    ? null : KFSConstants.COUNTRY_CODE_UNITED_STATES;
            final PhoneNumber parsedPhoneNumber = phoneNumberUtil.parseAndKeepRawInput(
                    rawPhoneNumber, tentativeExplicitRegion);
            return Optional.of(parsedPhoneNumber);
        } catch (final NumberParseException | RuntimeException e) {
            LOG.error("parsePhoneNumberIfPossible, Could not parse phone number from Phone BO {} for Vendor Contact {}; "
                    + "will mark as an error row and only print the raw phone number",
                    vendorPhone.getVendorContactPhoneGeneratedIdentifier(),
                    vendorPhone.getVendorContactGeneratedIdentifier(), e);
            return Optional.empty();
        }
    }

    public static CemiEntityContactPhoneBo createCemiEntityContactPhoneBoFrom(
            final Optional<VendorContactPhoneNumber> vendorContactPhoneNumber, final int phoneIndex) {
        final CemiEntityContactPhoneBoFactory factory = new CemiEntityContactPhoneBoFactory(
                vendorContactPhoneNumber, phoneIndex);
        return factory.createCemiEntityContactPhoneBo();
    }

    public CemiEntityContactPhoneBo createCemiEntityContactPhoneBo() {
        final CemiEntityContactPhoneBo phoneBo = new CemiEntityContactPhoneBo();
        final String phoneUsageComments = determinePhoneUsageComments();
        final CemiEntityContactGenericUsageBo phoneUsage = vendorContactPhoneNumber.isPresent()
                ? CemiEntityContactGenericUsageBoFactory.createUsageBoFrom(
                        CemiEntityContactConstants.ROW_ID_1, CommunicationUsageTypes.WORK, phoneUsageComments)
                : CemiEntityContactGenericUsageBoFactory.createEmptyUsageBo();

        phoneBo.setPhoneRowId(determinePhoneRowId());
        phoneBo.setPhoneAreaCode(determineAreaCode());
        phoneBo.setTenantFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setInternationalFormattedPhone(determineInternationalFormattedPhone());
        phoneBo.setPhoneNumberWithoutAreaCode(determinePhoneNumberWithoutAreaCode());
        phoneBo.setNationalFormattedPhone(determineNationalFormattedPhone());
        phoneBo.setE164FormattedPhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setWorkdayTraditionalFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setDeletePhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setDoNotReplaceAllPhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setPhoneCountryIsoCode(determinePhoneCountryIsoCode());
        phoneBo.setInternationalPhoneCode(determineInternationalPhoneCode());
        phoneBo.setPhoneNumber(determinePlainUnprefixedPhoneNumber());
        phoneBo.setPhoneExtension(determinePhoneExtension());
        phoneBo.setPhoneDeviceType(determinePhoneDeviceType());
        phoneBo.setExistingPhoneId(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setNewPhoneId(CemiBaseConstants.EMPTY_STRING);
        phoneBo.addPhoneUsage(phoneUsage);

        return phoneBo;
    }

    private String determinePhoneRowId() {
        return (vendorContactPhoneNumber.isPresent() && phoneIndex > 0)
                ? Integer.toString(phoneIndex) : CemiBaseConstants.EMPTY_STRING;
    }

    private String determineAreaCode() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        if (isUnitedStatesPhoneNumber(phoneNumber)) {
            final String usPhoneNumber = phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
            return StringUtils.substring(usPhoneNumber, 0, CemiEntityContactConstants.US_AREA_CODE_LENGTH);
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private String determineInternationalFormattedPhone() {
        return determineFormattedPhone(parsedPhoneNumber, true);
    }

    private String determineNationalFormattedPhone() {
        return determineFormattedPhone(parsedPhoneNumber, false);
    }

    private String determineFormattedPhone(final Optional<PhoneNumber> parsedPhoneNumber,
            final boolean internationalFormatStateToMatchOn) {
        if (vendorContactPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final VendorContactPhoneNumber vendorPhoneNumber = vendorContactPhoneNumber.get();
        final String rawPhoneNumber = vendorPhoneNumber.getVendorPhoneNumber();
        final boolean isUsingInternationalFormat = isPhoneNumberUsingInternationalFormat(rawPhoneNumber);
        return (isUsingInternationalFormat == internationalFormatStateToMatchOn)
                ? rawPhoneNumber : CemiBaseConstants.EMPTY_STRING;
    }

    private String determinePhoneNumberWithoutAreaCode() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        if (isUnitedStatesPhoneNumber(phoneNumber)) {
            final String usPhoneNumber = phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
            return StringUtils.substring(usPhoneNumber, CemiEntityContactConstants.US_AREA_CODE_LENGTH);
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private boolean isUnitedStatesPhoneNumber(final PhoneNumber phoneNumber) {
        final String regionCode = phoneNumberUtil.getRegionCodeForNumber(phoneNumber);
        return Strings.CI.equals(regionCode, KFSConstants.COUNTRY_CODE_UNITED_STATES);
    }

    private boolean isPhoneNumberUsingInternationalFormat(final String rawPhoneNumber) {
        return Strings.CI.startsWith(rawPhoneNumber, CUKFSConstants.PLUS_SIGN);
    }

    private String determinePhoneCountryIsoCode() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        final String regionCode = phoneNumberUtil.getRegionCodeForNumber(phoneNumber);
        return StringUtils.defaultIfBlank(regionCode, CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN);
    }

    private String determineInternationalPhoneCode() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        final String regionCode = phoneNumberUtil.getRegionCodeForNumber(phoneNumber);
        if (StringUtils.isBlank(regionCode)) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final int countryCallingCode = phoneNumberUtil.getCountryCodeForRegion(regionCode);
        return (countryCallingCode != 0) ? Integer.toString(countryCallingCode) : CemiBaseConstants.EMPTY_STRING;
    }

    private String determinePlainUnprefixedPhoneNumber() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        return phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
    }

    private String determinePhoneExtension() {
        if (vendorContactPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }

        final VendorContactPhoneNumber vendorPhoneNumber = vendorContactPhoneNumber.get();
        if (StringUtils.isNotBlank(vendorPhoneNumber.getVendorPhoneExtensionNumber())) {
            return vendorPhoneNumber.getVendorPhoneExtensionNumber();
        } else {
            final PhoneNumber phoneNumber = parsedPhoneNumber.get();
            return StringUtils.defaultIfBlank(phoneNumber.getExtension(), CemiBaseConstants.EMPTY_STRING);
        }
    }

    private String determinePhoneDeviceType() {
        return vendorContactPhoneNumber.isPresent() ? PhoneDeviceTypes.TELEPHONE : CemiBaseConstants.EMPTY_STRING;
    }

    private String determinePhoneUsageComments() {
        return didErrorOccurWhenParsingPhoneNumber()
                ? CemiEntityContactConstants.PHONE_PARSE_ERROR_MESSAGE : CemiBaseConstants.EMPTY_STRING;
    }

    private boolean didErrorOccurWhenParsingPhoneNumber() {
        return vendorContactPhoneNumber.isPresent() && parsedPhoneNumber.isEmpty();
    }

}
