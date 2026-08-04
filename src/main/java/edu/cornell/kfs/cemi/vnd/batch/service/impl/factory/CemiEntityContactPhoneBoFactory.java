package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.PhoneDeviceTypes;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;

public class CemiEntityContactPhoneBoFactory {

    private Optional<VendorContactPhoneNumber> vendorContactPhoneNumber;
    private int phoneIndex;

    public CemiEntityContactPhoneBoFactory(final Optional<VendorContactPhoneNumber> vendorContactPhoneNumber,
            final int phoneIndex) {
        this.vendorContactPhoneNumber = vendorContactPhoneNumber;
        this.phoneIndex = phoneIndex;
    }

    public static CemiEntityContactPhoneBo createCemiEntityContactPhoneBoFrom(
            final Optional<VendorContactPhoneNumber> vendorContactPhoneNumber, final int phoneIndex) {
        final CemiEntityContactPhoneBoFactory factory = new CemiEntityContactPhoneBoFactory(
                vendorContactPhoneNumber, phoneIndex);
        return factory.createCemiEntityContactPhoneBo();
    }

    public CemiEntityContactPhoneBo createCemiEntityContactPhoneBo() {
        final CemiEntityContactPhoneBo phoneBo = new CemiEntityContactPhoneBo();
        if (vendorContactPhoneNumber.isEmpty()) {
            return phoneBo;
        }

        final VendorContactPhoneNumber vendorPhoneInstance = vendorContactPhoneNumber.get();
        final String phoneRowId = Integer.toString(phoneIndex);
        final String basicPhoneNumber = StringUtils.defaultString(vendorPhoneInstance.getVendorPhoneNumber());
        final List<String> segmentedPhoneNumber = determinePhoneNumberSegments(basicPhoneNumber);

        phoneBo.setPhoneRowId(phoneRowId);
        phoneBo.setPhoneAreaCode(segmentedPhoneNumber.get(0));
        phoneBo.setTenantFormattedPhone(null);
        phoneBo.setInternationalFormattedPhone(null);
        phoneBo.setPhoneNumberWithoutAreaCode(segmentedPhoneNumber.get(1));
        phoneBo.setNationalFormattedPhone(null);
        phoneBo.setE164FormattedPhone(null);
        phoneBo.setWorkdayTraditionalFormattedPhone(null);
        phoneBo.setDeletePhone(null);
        phoneBo.setDoNotReplaceAllPhone(null);
        phoneBo.setPhoneCountryIsoCode(CemiVendorConstants.COUNTRY_CODE_UNITED_STATES);
        phoneBo.setInternationalPhoneCode(CemiEntityContactConstants.INTERNATIONAL_PHONE_CODE_UNITED_STATES);
        phoneBo.setPhoneNumber(basicPhoneNumber);
        phoneBo.setPhoneExtension(vendorPhoneInstance.getVendorPhoneExtensionNumber());
        phoneBo.setPhoneDeviceType(PhoneDeviceTypes.TELEPHONE);
        phoneBo.setPhoneUsageRowId(CemiEntityContactConstants.ROW_ID_1);
        phoneBo.setPhoneUsagePublic(CemiBaseConstants.YES);
        phoneBo.setPhoneUsageTypeRowId(CemiEntityContactConstants.ROW_ID_1);
        phoneBo.setPhoneUsageTypePrimary(CemiBaseConstants.YES);
        phoneBo.setPhoneUsageType(CommunicationUsageTypes.WORK);
        phoneBo.setPhoneUseFor(null);
        phoneBo.setPhoneUseForTenanted(null);
        phoneBo.setPhoneUsageComments(null);
        phoneBo.setExistingPhoneId(null);
        phoneBo.setNewPhoneId(null);

        return phoneBo;
    }

    private List<String> determinePhoneNumberSegments(final String phoneNumber) {
        if (StringUtils.isNotBlank(phoneNumber)
                && phoneNumber.length() == CemiEntityContactConstants.EXPECTED_PHONE_LENGTH) {
            return List.of(
                    phoneNumber.substring(0, CemiEntityContactConstants.AREA_CODE_LENGTH),
                    phoneNumber.substring(CemiEntityContactConstants.AREA_CODE_LENGTH)
            );
        } else {
            return List.of(StringUtils.defaultString(phoneNumber), KFSConstants.EMPTY_STRING);
        }
    }

}
