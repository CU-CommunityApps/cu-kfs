package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactFileEntityContactTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactPhoneBo;

public class CemiEntityContactFileEntityContactTabRowBoFactory {

    private VendorContact vendorContact;
    private CemiEntityContactHeaderBo headerBo;
    private CemiEntityContactPhoneBo phoneBo;
    private CemiEntityContactEmailBo emailBo;
    private Map<Class<?>, CemiEntityContactGenericUsageBo> usages;
    @SuppressWarnings("unused")
    private boolean maskSensitiveData = true;

    public CemiEntityContactFileEntityContactTabRowBoFactory (final VendorContact vendorContact,
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo, final Map<Class<?>, CemiEntityContactGenericUsageBo> usages,
            final boolean maskSensitiveData) {
        this.vendorContact = vendorContact;
        this.headerBo = headerBo;
        this.phoneBo = phoneBo;
        this.emailBo = emailBo;
        this.usages = usages;
        this.maskSensitiveData = maskSensitiveData;
    }

    public static CemiEntityContactFileEntityContactTabRowBo createTabRowBoFrom(final VendorContact vendorContact,
            final CemiEntityContactHeaderBo headerBo, final CemiEntityContactPhoneBo phoneBo,
            final CemiEntityContactEmailBo emailBo, final Map<Class<?>, CemiEntityContactGenericUsageBo> usages,
            final boolean maskSensitiveData) {
        final CemiEntityContactFileEntityContactTabRowBoFactory factory = new CemiEntityContactFileEntityContactTabRowBoFactory(
                vendorContact, headerBo, phoneBo, emailBo, usages, maskSensitiveData);
        return factory.createCemiEntityContactFileEntityContactTabRowBo();
    }

    public CemiEntityContactFileEntityContactTabRowBo createCemiEntityContactFileEntityContactTabRowBo() {
        Validate.validState(ObjectUtils.isNotNull(vendorContact), "Vendor Contact cannot be null");
        Validate.validState(ObjectUtils.isNotNull(headerBo), "Header BO cannot be null");
        Validate.validState(ObjectUtils.isNotNull(phoneBo),
                "Phone BO cannot be null; must provide an empty BO if the row has no phone number");
        Validate.validState(ObjectUtils.isNotNull(emailBo),
                "Email BO cannot be null; must provide an empty BO if the row has no email address");

        final CemiEntityContactGenericUsageBo phoneUsage = usages.get(CemiEntityContactPhoneBo.class);
        final CemiEntityContactGenericUsageBo emailUsage = usages.get(CemiEntityContactEmailBo.class);
        Validate.validState(ObjectUtils.isNotNull(phoneUsage),
                "Phone usage data cannot be null; must provide an empty BO if the row has no phone number");
        Validate.validState(ObjectUtils.isNotNull(emailUsage),
                "Email usage data cannot be null; must provide an empty BO if the row has no email address");

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
        entityContactRow.setAddressRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setFormattedAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressFormatType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDefaultedBusinessSiteAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDeleteAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDoNotReplaceAllAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressEffectiveDate(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressCountry(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressLastModified(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressLineRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressLineDescriptor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressLineType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressLineData(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressMunicipality(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressCountryCity(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressIso31661Alpha2Code(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubmunicipalityRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubmunicipalityAddressComponentName(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubmunicipalityType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubmunicipalityData(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setCountryRegion(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setCountryRegionDescriptor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubregionRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubregionDescriptor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubregionType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setSubregionData(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setPostalCode(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsageRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsagePublic(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsageTypeRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsageTypePrimary(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsageType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUseFor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUseForTenanted(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressUsageComments(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressNumberOfDays(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setAddressMunicipalityLocal(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setExistingAddressId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setNewAddressId(CemiBaseConstants.EMPTY_STRING);
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
        entityContactRow.setPhoneUsageRowId(phoneUsage.getUsageRowId());
        entityContactRow.setPhoneUsagePublic(phoneUsage.getUsagePublic());
        entityContactRow.setPhoneUsageTypeRowId(phoneUsage.getUsageTypeRowId());
        entityContactRow.setPhoneUsageTypePrimary(phoneUsage.getUsageTypePrimary());
        entityContactRow.setPhoneUsageType(phoneUsage.getUsageType());
        entityContactRow.setPhoneUseFor(phoneUsage.getUseFor());
        entityContactRow.setPhoneUseForTenanted(phoneUsage.getUseForTenanted());
        entityContactRow.setPhoneUsageComments(phoneUsage.getUsageComments());
        entityContactRow.setExistingPhoneId(phoneBo.getExistingPhoneId());
        entityContactRow.setNewPhoneId(phoneBo.getNewPhoneId());
        entityContactRow.setEmailRowId(emailBo.getEmailRowId());
        entityContactRow.setDeleteEmail(emailBo.getDeleteEmail());
        entityContactRow.setDoNotReplaceAllEmail(emailBo.getDoNotReplaceAllEmail());
        entityContactRow.setEmailAddress(emailBo.getEmailAddress());
        entityContactRow.setEmailComment(emailBo.getEmailComment());
        entityContactRow.setEmailUsageRowId(emailUsage.getUsageRowId());
        entityContactRow.setEmailUsagePublic(emailUsage.getUsagePublic());
        entityContactRow.setEmailUsageTypeRowId(emailUsage.getUsageTypeRowId());
        entityContactRow.setEmailUsageTypePrimary(emailUsage.getUsageTypePrimary());
        entityContactRow.setEmailUsageType(emailUsage.getUsageType());
        entityContactRow.setEmailUseFor(emailUsage.getUseFor());
        entityContactRow.setEmailUseForTenanted(emailUsage.getUseForTenanted());
        entityContactRow.setEmailUsageComments(emailUsage.getUsageComments());
        entityContactRow.setExistingEmailId(emailBo.getExistingEmailId());
        entityContactRow.setNewEmailId(emailBo.getNewEmailId());
        entityContactRow.setInstantMessengerRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDeleteInstantMessenger(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDoNotReplaceAllInstantMessenger(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerComment(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsageRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsagePublic(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsageTypeRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsageTypePrimary(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsageType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUseFor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUseForTenanted(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setInstantMessengerUsageComments(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setExistingInstantMessengerId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setNewInstantMessengerId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDeleteWebAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setDoNotReplaceAllWebAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddress(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressComment(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsageRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsagePublic(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsageTypeRowId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsageTypePrimary(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsageType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUseFor(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUseForTenanted(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setWebAddressUsageComments(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setExistingWebAddressId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setNewWebAddressId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setContactTypeRowId(CemiEntityContactConstants.ROW_ID_1);
        entityContactRow.setContactTypeTenanted(vendorContact.getVendorContactTypeCode());
        entityContactRow.setBusinessEntityContactType(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setExternalSystemId(CemiBaseConstants.EMPTY_STRING);
        entityContactRow.setExternalId(CemiBaseConstants.EMPTY_STRING);

        return entityContactRow;
    }

}
