package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.service.SequenceAccessorService;

import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.options.StandardEntryClassValuesFinder;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsAchDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsPayeeAchAccountConversionService;

public class PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl implements PaymentWorksVendorToKfsPayeeAchAccountConversionService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected SequenceAccessorService sequenceAccessorService;

    @Override
    public KfsAchDataWrapper createKfsPayeeAchFromPmwVendor(PaymentWorksVendor pmwVendor) {
        KfsAchDataWrapper kfsAchDataWrapper = createPayeeAchForVendor(pmwVendor);
        return kfsAchDataWrapper;
    }
    
    private KfsAchDataWrapper createPayeeAchForVendor(PaymentWorksVendor pmwVendor) {
        KfsAchDataWrapper kfsAchDataWrapper = populatePayeeAchAccount(pmwVendor);
        kfsAchDataWrapper.setPayeeAchAccountExplanation(createPayeeAchExplanation(pmwVendor));
        kfsAchDataWrapper = buildPayeeAchNotes(pmwVendor, kfsAchDataWrapper);
        return kfsAchDataWrapper;
    }
    
    private KfsAchDataWrapper populatePayeeAchAccount(PaymentWorksVendor pmwVendor) {
        KfsAchDataWrapper kfsAchDataWrapper = new KfsAchDataWrapper();
        kfsAchDataWrapper.getPayeeAchAccount().setAchAccountGeneratedIdentifier(new KualiInteger(getSequenceAccessorService().getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME)));
        kfsAchDataWrapper.getPayeeAchAccount().setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
        kfsAchDataWrapper.getPayeeAchAccount().setPayeeIdNumber(formatPayeeNumberForVendor(pmwVendor));
        kfsAchDataWrapper.getPayeeAchAccount().setBankRoutingNumber(pmwVendor.getBankAcctRoutingNumber());
        kfsAchDataWrapper.getPayeeAchAccount().setBankAccountNumber(pmwVendor.getBankAcctBankAccountNumber());
        kfsAchDataWrapper.getPayeeAchAccount().setBankAccountTypeCode(convertBankAccountTypeFromPmwToKfs(pmwVendor.getBankAcctType()));
        kfsAchDataWrapper.getPayeeAchAccount().setStandardEntryClass(determineStandardEntryClass(kfsAchDataWrapper.getPayeeAchAccount().getBankAccountTypeCode()));
        kfsAchDataWrapper.getPayeeAchAccount().setPayeeName(pmwVendor.getRequestingCompanyLegalName());
        kfsAchDataWrapper.getPayeeAchAccount().setPayeeEmailAddress(pmwVendor.getBankAcctAchEmail());
        kfsAchDataWrapper.getPayeeAchAccount().setAchTransactionType(PaymentWorksConstants.KFSPayeeAchMaintenanceDocumentConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE);
        kfsAchDataWrapper.getPayeeAchAccount().setActive(true);
        return kfsAchDataWrapper;
    }
    
    private String convertBankAccountTypeFromPmwToKfs(String pmwBankAccountType) {
        List<PaymentWorksConstants.PaymentWorksBankAccountType> matchingPmwBanksAccountTypes = getPaymentWorksBatchUtilityService().findAllPmwBankAccountTypesMatching(pmwBankAccountType);
        return matchingPmwBanksAccountTypes.get(0).translationToKfsBankAccountTypeCode;
    }
    
    protected String determineStandardEntryClass(String kfsBankAccountTypeCode) {
        String valueToConvert = StringUtils.right(kfsBankAccountTypeCode, 3);
        if (StringUtils.isNotBlank(valueToConvert)) {
            valueToConvert = valueToConvert.toUpperCase(Locale.US);
            return StandardEntryClassValuesFinder.StandardEntryClass.valueOf(valueToConvert).toString();
        } else {
            throw new IllegalArgumentException("KFS bank account type code did not contain at least three characters : " + kfsBankAccountTypeCode);
        }
    }
    
    private String formatPayeeNumberForVendor(PaymentWorksVendor pmwVendor) {
        LOG.info("formatPayeeNumberForVendor: pmwVendor.kfsVendorHeaderGeneratedIdentifier = " + pmwVendor.getKfsVendorHeaderGeneratedIdentifier() + "  pmwVendor.kfsVendorDetailAssignedIdentifier = " + pmwVendor.getKfsVendorDetailAssignedIdentifier());
        StringBuilder sbText = new StringBuilder();
        sbText.append(pmwVendor.getKfsVendorHeaderGeneratedIdentifier());
        sbText.append(KFSConstants.DASH);
        sbText.append(pmwVendor.getKfsVendorDetailAssignedIdentifier());
        LOG.info("formatPayeeNumberForVendor: formatted vendor number = " + sbText.toString());
        return sbText.toString();
    }
    
    private String createPayeeAchExplanation(PaymentWorksVendor pmwVendor) {
        StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ACCOUNT_LABEL)).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_NAME_ON_ACCOUNT_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAcctNameOnAccount()).append(System.lineSeparator());
        sbText.append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_LABEL)).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_COUNTRY_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressCountry()).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STREET1_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressStreet1()).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STREET2_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressStreet2()).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_CITY_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressCity()).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STATE_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressState()).append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_ADDRESS_ZIP_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAddressZipCode()).append(System.lineSeparator());
        sbText.append(System.lineSeparator());
        sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.PAYEE_ACH_ACCOUNT_BANK_SWIFTCODE)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getBankAcctSwiftCode()).append(System.lineSeparator());
        
        return sbText.toString();
    }
    
    private KfsAchDataWrapper buildPayeeAchNotes(PaymentWorksVendor pmwVendor, KfsAchDataWrapper kfsAchDataWrapper) {
        StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PAAT_NOTES_PMW_VENDOR_NUMBER_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getPmwVendorRequestId()).append(System.lineSeparator());
        kfsAchDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsAchDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.PAAT_PMW_VENDOR_ID.getNoteDescriptionString());
        return kfsAchDataWrapper;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    
    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public SequenceAccessorService getSequenceAccessorService() {
        return sequenceAccessorService;
    }

    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
    }

}
