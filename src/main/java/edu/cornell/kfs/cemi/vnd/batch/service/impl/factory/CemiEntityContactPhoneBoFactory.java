package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.service.CemiIsoCountryService;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.PhoneDeviceTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;

public class CemiEntityContactPhoneBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorContactPhoneNumber> mergedPhoneNumbers;
    private Optional<VendorContactPhoneNumber> firstPhoneNumber;
    private int phoneIndex;
    private CemiIsoCountryService cemiIsoCountryService;
    private PhoneNumberUtil phoneNumberUtil;
    private Optional<PhoneNumber> parsedPhoneNumber;

    public CemiEntityContactPhoneBoFactory(final List<VendorContactPhoneNumber> mergedPhoneNumbers,
            final int phoneIndex, final CemiIsoCountryService cemiIsoCountryService) {
        Validate.notNull(mergedPhoneNumbers, "mergedPhoneNumbers list cannot be null");
        Validate.isTrue(mergedPhoneNumbers.isEmpty() == (phoneIndex <= 0),
                "phoneIndex must be a positive value if, and only if, mergedPhoneNumbers is non-empty");
        Validate.notNull(cemiIsoCountryService, "cemiIsoCountryService cannot be null");
        this.mergedPhoneNumbers = mergedPhoneNumbers;
        this.firstPhoneNumber = !mergedPhoneNumbers.isEmpty() ? Optional.of(mergedPhoneNumbers.get(0)) : Optional.empty();
        this.phoneIndex = phoneIndex;
        this.cemiIsoCountryService = cemiIsoCountryService;
        this.phoneNumberUtil = CemiVendorUtils.getPhoneNumberUtil();
        this.parsedPhoneNumber = firstPhoneNumber.map(CemiVendorUtils::parsePhoneNumberIfPossible)
                .orElseGet(Optional::empty);
    }

    public static CemiEntityContactPhoneBo createCemiEntityContactPhoneBoFrom(
            final List<VendorContactPhoneNumber> mergedPhoneNumbers, final int phoneIndex,
            final CemiIsoCountryService cemiIsoCountryService) {
        final CemiEntityContactPhoneBoFactory factory = new CemiEntityContactPhoneBoFactory(
                mergedPhoneNumbers, phoneIndex, cemiIsoCountryService);
        return factory.createCemiEntityContactPhoneBo();
    }

    public CemiEntityContactPhoneBo createCemiEntityContactPhoneBo() {
        final CemiEntityContactPhoneBo phoneBo = new CemiEntityContactPhoneBo();
        if (mergedPhoneNumbers.size() > 1) {
            LOG.debug("createCemiEntityContactPhoneBo, Creating Entity Contact Phone for Vendor Contact {} using {} "
                    + "merged phone numbers", firstPhoneNumber.get().getVendorContactGeneratedIdentifier(),
                    mergedPhoneNumbers.size());
        }

        if (firstPhoneNumber.isPresent()) {
            final VendorContactPhoneNumber vendorContactPhone = firstPhoneNumber.get();
            phoneBo.setVendorContactGeneratedIdentifier(vendorContactPhone.getVendorContactGeneratedIdentifier());
            phoneBo.setVendorContactPhoneGeneratedIdentifier(
                    vendorContactPhone.getVendorContactPhoneGeneratedIdentifier());
        }

        final Pair<String, String> areaCodeRelatedSegments = determineAreaCodeRelatedPhoneSegments();

        final CemiEntityContactGenericUsageBo phoneUsage = firstPhoneNumber.isPresent()
                ? CemiEntityContactGenericUsageBoFactory.createUsageBoFrom(
                        CemiEntityContactConstants.ROW_ID_1, CommunicationUsageTypes.WORK, CemiBaseConstants.EMPTY_STRING)
                : CemiEntityContactGenericUsageBoFactory.createEmptyUsageBo();

        phoneBo.setPhoneRowId(determinePhoneRowId());
        phoneBo.setPhoneAreaCode(areaCodeRelatedSegments.getLeft());
        phoneBo.setTenantFormattedPhone(CemiBaseConstants.EMPTY_STRING);
        phoneBo.setInternationalFormattedPhone(determineInternationalFormattedPhone());
        phoneBo.setPhoneNumberWithoutAreaCode(areaCodeRelatedSegments.getRight());
        phoneBo.setNationalFormattedPhone(CemiBaseConstants.EMPTY_STRING);
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
        return (firstPhoneNumber.isPresent() && phoneIndex > 0)
                ? Integer.toString(phoneIndex) : CemiBaseConstants.EMPTY_STRING;
    }

    private Pair<String, String> determineAreaCodeRelatedPhoneSegments() {
        if (firstPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return Pair.of(CemiBaseConstants.EMPTY_STRING, CemiBaseConstants.EMPTY_STRING);
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        final String nationalSignificantNumber = phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
        final int areaCodeLength = phoneNumberUtil.getLengthOfGeographicalAreaCode(phoneNumber);
        if (areaCodeLength > 0 && StringUtils.length(nationalSignificantNumber) > areaCodeLength) {
            final String areaCode = nationalSignificantNumber.substring(0, areaCodeLength);
            final String nonAreaCodeSegment = nationalSignificantNumber.substring(areaCodeLength);
            return Pair.of(areaCode, nonAreaCodeSegment);
        } else if (determineInternationalPhoneCodeAsInteger() == CemiEntityContactConstants.USA_CANADA_PHONE_CODE
                && StringUtils.length(nationalSignificantNumber) == CemiEntityContactConstants.USA_CANADA_PHONE_LENGTH) {
            final String areaCode = nationalSignificantNumber.substring(
                    0, CemiEntityContactConstants.USA_CANADA_AREA_CODE_LENGTH);
            final String nonAreaCodeSegment = nationalSignificantNumber.substring(
                    CemiEntityContactConstants.USA_CANADA_AREA_CODE_LENGTH);
            return Pair.of(areaCode, nonAreaCodeSegment);
        } else {
            return Pair.of(CemiBaseConstants.EMPTY_STRING, CemiBaseConstants.EMPTY_STRING);
        }
    }

    private String determineInternationalFormattedPhone() {
        if (firstPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final VendorContactPhoneNumber vendorContactPhone = firstPhoneNumber.get();
        final String phoneNumberString = vendorContactPhone.getVendorPhoneNumber();
        return CemiVendorUtils.isPhoneNumberUsingInternationalFormat(phoneNumberString)
                ? phoneNumberString : CemiBaseConstants.EMPTY_STRING;
    }

    private String determinePhoneCountryIsoCode() {
        if (firstPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        final String regionCode = CemiVendorUtils.getRegionCode(phoneNumber);
        String iso3CharCountryCode = null;
        if (StringUtils.isNotBlank(regionCode)) {
            iso3CharCountryCode = cemiIsoCountryService.getIso3CharCountryCode(regionCode);
        }
        return StringUtils.defaultIfBlank(iso3CharCountryCode, CemiBaseConstants.ISO_3_CHAR_COUNTRY_CODE_UNKNOWN);
    }

    private String determineInternationalPhoneCode() {
        final int internationalPhoneCode = determineInternationalPhoneCodeAsInteger();
        return (internationalPhoneCode > 0) ? Integer.toString(internationalPhoneCode) : CemiBaseConstants.EMPTY_STRING;
    }

    private int determineInternationalPhoneCodeAsInteger() {
        if (firstPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return -1;
        }
        final PhoneNumber phoneNumber = parsedPhoneNumber.get();
        final String regionCode = CemiVendorUtils.getRegionCode(phoneNumber);
        if (StringUtils.isBlank(regionCode)) {
            return -1;
        }
        return phoneNumberUtil.getCountryCodeForRegion(regionCode);
    }

    private String determinePlainUnprefixedPhoneNumber() {
        if (didErrorOccurWhenParsingPhoneNumber()) {
            return CemiEntityContactConstants.PHONE_PARSE_ERROR_MESSAGE;
        } else if (firstPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            final PhoneNumber phoneNumber = parsedPhoneNumber.get();
            return phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
        }
    }

    private boolean didErrorOccurWhenParsingPhoneNumber() {
        return firstPhoneNumber.isPresent() && parsedPhoneNumber.isEmpty();
    }

    private String determinePhoneExtension() {
        if (firstPhoneNumber.isEmpty() || parsedPhoneNumber.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }

        final VendorContactPhoneNumber vendorContactPhone = firstPhoneNumber.get();
        if (StringUtils.isNotBlank(vendorContactPhone.getVendorPhoneExtensionNumber())) {
            return vendorContactPhone.getVendorPhoneExtensionNumber();
        } else {
            final PhoneNumber phoneNumber = parsedPhoneNumber.get();
            return StringUtils.defaultIfBlank(phoneNumber.getExtension(), CemiBaseConstants.EMPTY_STRING);
        }
    }

    private String determinePhoneDeviceType() {
        return firstPhoneNumber.isPresent() ? PhoneDeviceTypes.LANDLINE : CemiBaseConstants.EMPTY_STRING;
    }

}
