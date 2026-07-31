package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactFileEntityContactTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;

@SuppressWarnings("deprecation")
public class CemiEntityContactFileEntityContactTabRowBoFactory {

    private VendorContact vendorContact;
    private CemiEntityContactHeaderBo headerBo;
    private CemiEntityContactPhoneBo phoneBo;
    private CemiEntityContactEmailBo emailBo;
    @SuppressWarnings("unused")
    private boolean maskSensitiveData = true;

    public CemiEntityContactFileEntityContactTabRowBoFactory (final VendorContact vendorContact,
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo, final boolean maskSensitiveData) {
        this.vendorContact = vendorContact;
        this.headerBo = headerBo;
        this.phoneBo = phoneBo;
        this.emailBo = emailBo;
        this.maskSensitiveData = maskSensitiveData;
    }

    public static CemiEntityContactFileEntityContactTabRowBo createTabRowBoFrom(final VendorContact vendorContact,
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo, final boolean maskSensitiveData) {
        final CemiEntityContactFileEntityContactTabRowBoFactory factory = new CemiEntityContactFileEntityContactTabRowBoFactory(
                vendorContact, headerBo, phoneBo, emailBo, maskSensitiveData);
        return factory.createCemiEntityContactFileEntityContactTabRowBo();
    }

    public CemiEntityContactFileEntityContactTabRowBo createCemiEntityContactFileEntityContactTabRowBo() {
        Validate.validState(ObjectUtils.isNotNull(vendorContact), "Vendor Contact cannot be null");
        Validate.validState(ObjectUtils.isNotNull(headerBo), "Header BO cannot be null");
        Validate.validState(ObjectUtils.isNotNull(phoneBo),
                "Phone BO cannot be null; must provide an empty BO if the row has no phone number");
        Validate.validState(ObjectUtils.isNotNull(emailBo),
                "Email BO cannot be null; must provide an empty BO if the row has no email address");

        final CemiEntityContactFileEntityContactTabRowBo entityContactRow
                = new CemiEntityContactFileEntityContactTabRowBo();

        entityContactRow.setVendorContactGeneratedIdentifier(vendorContact.getVendorContactGeneratedIdentifier());

        entityContactRow.setSpreadsheetKey(headerBo.getSpreadsheetKey());
        entityContactRow.setAddOnly(headerBo.getAddOnly());
        entityContactRow.setExistingBusinessEntityContactId(headerBo.getExistingBusinessEntityContactId());
        entityContactRow.setPrimaryBillToContact(headerBo.getPrimaryBillToContact());
        entityContactRow.setDefaultBillToContact(headerBo.getDefaultBillToContact());
        entityContactRow.setNewBusinessEntityContactId(headerBo.getNewBusinessEntityContactId());
        entityContactRow.setSupplier(headerBo.getSupplier());
        entityContactRow.setBillableEntity(headerBo.getBillableEntity());
        entityContactRow.setFinancialInstitution(headerBo.getFinancialInstitution());
        entityContactRow.setTaxAuthority(headerBo.getTaxAuthority());
        entityContactRow.setNameRowId(headerBo.getNameRowId());
        entityContactRow.setFormattedName(headerBo.getFormattedName());
        entityContactRow.setReportingName(headerBo.getReportingName());
        entityContactRow.setCountry(headerBo.getCountry());
        entityContactRow.setTitle(headerBo.getTitle());
        entityContactRow.setTitleDescriptor(headerBo.getTitleDescriptor());
        entityContactRow.setSalutation(headerBo.getSalutation());
        entityContactRow.setFirstName(headerBo.getFirstName());
        entityContactRow.setMiddleName(headerBo.getMiddleName());
        entityContactRow.setLastName(headerBo.getLastName());
        entityContactRow.setSecondaryLastName(headerBo.getSecondaryLastName());
        entityContactRow.setTertiaryLastName(headerBo.getTertiaryLastName());
        entityContactRow.setLocalName(headerBo.getLocalName());
        entityContactRow.setLocalScript(headerBo.getLocalScript());
        entityContactRow.setLocalFirstName(headerBo.getLocalFirstName());
        entityContactRow.setLocalMiddleName(headerBo.getLocalMiddleName());
        entityContactRow.setLocalLastName(headerBo.getLocalLastName());
        entityContactRow.setLocalSecondaryLastName(headerBo.getLocalSecondaryLastName());
        entityContactRow.setLocalFirstName2(headerBo.getLocalFirstName2());
        entityContactRow.setLocalMiddleName2(headerBo.getLocalMiddleName2());
        entityContactRow.setLocalLastName2(headerBo.getLocalLastName2());
        entityContactRow.setLocalSecondaryLastName2(headerBo.getLocalSecondaryLastName2());
        entityContactRow.setSocialSuffix(headerBo.getSocialSuffix());
        entityContactRow.setSocialSuffixDescriptor(headerBo.getSocialSuffixDescriptor());
        entityContactRow.setAcademicSuffix(headerBo.getAcademicSuffix());
        entityContactRow.setHereditarySuffix(headerBo.getHereditarySuffix());
        entityContactRow.setHonorarySuffix(headerBo.getHonorarySuffix());
        entityContactRow.setProfessionalSuffix(headerBo.getProfessionalSuffix());
        entityContactRow.setReligiousSuffix(headerBo.getReligiousSuffix());
        entityContactRow.setRoyalSuffix(headerBo.getRoyalSuffix());
        entityContactRow.setFullNameForSingaporeAndMalaysia(headerBo.getFullNameForSingaporeAndMalaysia());
        entityContactRow.setAddressRowId(null);
        entityContactRow.setFormattedAddress(null);
        entityContactRow.setAddressFormatType(null);
        entityContactRow.setDefaultedBusinessSiteAddress(null);
        entityContactRow.setDeleteAddress(null);
        entityContactRow.setDoNotReplaceAllAddress(null);
        entityContactRow.setAddressEffectiveDate(null);
        entityContactRow.setAddressCountry(null);
        entityContactRow.setAddressLastModified(null);
        entityContactRow.setAddressLineRowId(null);
        entityContactRow.setAddressLineDescriptor(null);
        entityContactRow.setAddressLineType(null);
        entityContactRow.setAddressLineData(null);
        entityContactRow.setAddressMunicipality(null);
        entityContactRow.setAddressCountryCity(null);
        entityContactRow.setAddressIso31661Alpha2Code(null);
        entityContactRow.setSubmunicipalityRowId(null);
        entityContactRow.setSubmunicipalityAddressComponentName(null);
        entityContactRow.setSubmunicipalityType(null);
        entityContactRow.setSubmunicipalityData(null);
        entityContactRow.setCountryRegion(null);
        entityContactRow.setCountryRegionDescriptor(null);
        entityContactRow.setSubregionRowId(null);
        entityContactRow.setSubregionDescriptor(null);
        entityContactRow.setSubregionType(null);
        entityContactRow.setSubregionData(null);
        entityContactRow.setPostalCode(null);
        entityContactRow.setAddressUsageRowId(null);
        entityContactRow.setAddressUsagePublic(null);
        entityContactRow.setAddressUsageTypeRowId(null);
        entityContactRow.setAddressUsageTypePrimary(null);
        entityContactRow.setAddressUsageType(null);
        entityContactRow.setAddressUseFor(null);
        entityContactRow.setAddressUseForTenanted(null);
        entityContactRow.setAddressUsageComments(null);
        entityContactRow.setAddressNumberOfDays(null);
        entityContactRow.setAddressMunicipalityLocal(null);
        entityContactRow.setExistingAddressId(null);
        entityContactRow.setNewAddressId(null);
        entityContactRow.setPhoneRowId(phoneBo.getPhoneRowId());
        entityContactRow.setPhoneAreaCode(phoneBo.getPhoneAreaCode());
        entityContactRow.setTenantFormattedPhone(phoneBo.getTenantFormattedPhone());
        entityContactRow.setInternationalFormattedPhone(phoneBo.getInternationalFormattedPhone());
        entityContactRow.setPhoneNumberWithoutAreaCode(phoneBo.getPhoneNumberWithoutAreaCode());
        entityContactRow.setNationalFormattedPhone(phoneBo.getNationalFormattedPhone());
        entityContactRow.setE164FormattedPhone(phoneBo.getE164FormattedPhone());
        entityContactRow.setWorkdayTraditionalFormattedPhone(phoneBo.getWorkdayTraditionalFormattedPhone());
        entityContactRow.setDeletePhone(phoneBo.getDeletePhone());
        entityContactRow.setDoNotReplaceAllPhone(phoneBo.getDoNotReplaceAllPhone());
        entityContactRow.setPhoneCountryIsoCode(phoneBo.getPhoneCountryIsoCode());
        entityContactRow.setInternationalPhoneCode(phoneBo.getInternationalPhoneCode());
        entityContactRow.setPhoneNumber(phoneBo.getPhoneNumber());
        entityContactRow.setPhoneExtension(phoneBo.getPhoneExtension());
        entityContactRow.setPhoneDeviceType(phoneBo.getPhoneDeviceType());
        entityContactRow.setPhoneUsageRowId(phoneBo.getPhoneUsageRowId());
        entityContactRow.setPhoneUsagePublic(phoneBo.getPhoneUsagePublic());
        entityContactRow.setPhoneUsageTypeRowId(phoneBo.getPhoneUsageTypeRowId());
        entityContactRow.setPhoneUsageTypePrimary(phoneBo.getPhoneUsageTypePrimary());
        entityContactRow.setPhoneUsageType(phoneBo.getPhoneUsageType());
        entityContactRow.setPhoneUseFor(phoneBo.getPhoneUseFor());
        entityContactRow.setPhoneUseForTenanted(phoneBo.getPhoneUseForTenanted());
        entityContactRow.setPhoneUsageComments(phoneBo.getPhoneUsageComments());
        entityContactRow.setExistingPhoneId(phoneBo.getExistingPhoneId());
        entityContactRow.setNewPhoneId(phoneBo.getNewPhoneId());
        entityContactRow.setEmailRowId(emailBo.getEmailRowId());
        entityContactRow.setDeleteEmail(emailBo.getDeleteEmail());
        entityContactRow.setDoNotReplaceAllEmail(emailBo.getDoNotReplaceAllEmail());
        entityContactRow.setEmailAddress(emailBo.getEmailAddress());
        entityContactRow.setEmailComment(emailBo.getEmailComment());
        entityContactRow.setEmailUsageRowId(emailBo.getEmailUsageRowId());
        entityContactRow.setEmailUsagePublic(emailBo.getEmailUsagePublic());
        entityContactRow.setEmailUsageTypeRowId(emailBo.getEmailUsageTypeRowId());
        entityContactRow.setEmailUsageTypePrimary(emailBo.getEmailUsageTypePrimary());
        entityContactRow.setEmailUsageType(emailBo.getEmailUsageType());
        entityContactRow.setEmailUseFor(emailBo.getEmailUseFor());
        entityContactRow.setEmailUseForTenanted(emailBo.getEmailUseForTenanted());
        entityContactRow.setEmailUsageComments(emailBo.getEmailUsageComments());
        entityContactRow.setExistingEmailId(emailBo.getExistingEmailId());
        entityContactRow.setNewEmailId(emailBo.getNewEmailId());
        entityContactRow.setInstantMessengerRowId(null);
        entityContactRow.setDeleteInstantMessenger(null);
        entityContactRow.setDoNotReplaceAllInstantMessenger(null);
        entityContactRow.setInstantMessengerAddress(null);
        entityContactRow.setInstantMessengerType(null);
        entityContactRow.setInstantMessengerComment(null);
        entityContactRow.setInstantMessengerUsageRowId(null);
        entityContactRow.setInstantMessengerUsagePublic(null);
        entityContactRow.setInstantMessengerUsageTypeRowId(null);
        entityContactRow.setInstantMessengerUsageTypePrimary(null);
        entityContactRow.setInstantMessengerUsageType(null);
        entityContactRow.setInstantMessengerUseFor(null);
        entityContactRow.setInstantMessengerUseForTenanted(null);
        entityContactRow.setInstantMessengerUsageComments(null);
        entityContactRow.setExistingInstantMessengerId(null);
        entityContactRow.setNewInstantMessengerId(null);
        entityContactRow.setWebAddressRowId(null);
        entityContactRow.setDeleteWebAddress(null);
        entityContactRow.setDoNotReplaceAllWebAddress(null);
        entityContactRow.setWebAddress(null);
        entityContactRow.setWebAddressComment(null);
        entityContactRow.setWebAddressUsageRowId(null);
        entityContactRow.setWebAddressUsagePublic(null);
        entityContactRow.setWebAddressUsageTypeRowId(null);
        entityContactRow.setWebAddressUsageTypePrimary(null);
        entityContactRow.setWebAddressUsageType(null);
        entityContactRow.setWebAddressUseFor(null);
        entityContactRow.setWebAddressUseForTenanted(null);
        entityContactRow.setWebAddressUsageComments(null);
        entityContactRow.setExistingWebAddressId(null);
        entityContactRow.setNewWebAddressId(null);
        entityContactRow.setContactTypeRowId(CemiEntityContactConstants.ROW_ID_1);
        entityContactRow.setContactTypeTenanted(vendorContact.getVendorContactTypeCode());
        entityContactRow.setBusinessEntityContactType(null);
        entityContactRow.setExternalSystemId(null);
        entityContactRow.setExternalId(null);

        return entityContactRow;
    }

    /*
     * TODO: In future extracts, we may need more in-depth name-splitting logic to handle special cases
     *       (middle names, multiple concatenated names, company names, etc.)
     */
    private List<String> determineNameSegments(final String vendorContactName) {
        if (StringUtils.contains(vendorContactName, KFSConstants.BLANK_SPACE)) {
            return List.of(
                    StringUtils.substringBefore(vendorContactName, KFSConstants.BLANK_SPACE),
                    StringUtils.substringAfter(vendorContactName, KFSConstants.BLANK_SPACE)
            );
        } else {
            return List.of(StringUtils.defaultString(vendorContactName), KFSConstants.EMPTY_STRING);
        }
    }

}
