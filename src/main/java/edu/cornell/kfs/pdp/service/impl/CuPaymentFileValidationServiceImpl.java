package edu.cornell.kfs.pdp.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeType;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.impl.PaymentFileValidationServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.OriginationCode;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.service.CuPdpEmployeeService;

public class CuPaymentFileValidationServiceImpl extends PaymentFileValidationServiceImpl {
	private static final Logger LOG = LogManager.getLogger(CuPaymentFileValidationServiceImpl.class);

    protected PersonService personService;
    protected CuPdpEmployeeService cuPdpEmployeeService;
    protected List<String> paymentDetailPropertiesToCheckMaxLength;
    
    @Override
    protected void processGroupValidation(final PaymentFileLoad paymentFile, final MessageMap errorMap) {
        int groupCount = 0;
        for (final PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
            groupCount++;
            int noteLineCount = 0;
            int detailCount = 0;

            // We've encountered Payment Files that have address lines exceeding the column size in DB table;
            // so adding extra validation on payment group BO, especially the max length, based on DD definitions.
            // Check that PaymentGroup String properties don't exceed maximum allowed length
            checkPaymentGroupPropertyMaxLength(paymentGroup, errorMap);

            // verify payee id and owner code if customer requires them to be filled in
            if (paymentFile.getCustomer().getPayeeIdRequired() && StringUtils.isBlank(paymentGroup.getPayeeId())) {
                LOG.debug("processGroupValidation, No payee");
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_PAYEE_ID_REQUIRED, Integer.toString(groupCount));
            }

            if (paymentFile.getCustomer().getOwnershipCodeRequired() && StringUtils.isBlank(paymentGroup.getPayeeOwnerCd())) {
                LOG.debug("processGroupValidation, no ownership code");
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_PAYEE_OWNER_CODE, Integer.toString(groupCount));
            }

            // validate payee id type
            if (StringUtils.isNotBlank(paymentGroup.getPayeeIdTypeCd())) {
                final PayeeType payeeType = businessObjectService.findBySinglePrimaryKey(PayeeType.class, paymentGroup.getPayeeIdTypeCd());
                if (payeeType == null) {
                    LOG.debug("processGroupValidation, no payee type");
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_PAYEE_ID_TYPE, Integer.toString(groupCount), paymentGroup.getPayeeIdTypeCd());
                }
            }
            
            // validate vendor id and customer institution number
            if (paymentGroup.getPayeeId().split("-").length > 1) {
                try {
                    paymentGroup.validateVendorIdAndCustomerInstitutionIdentifier(); 
                } catch(final RuntimeException e1) {
                    LOG.error("processGroupValidation, there was an error validating customer institution information", e1);
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING, new String[] { e1.getMessage() });
                }
            } else {
                LOG.debug("processGroupValidation, found a non vendor number payee ID: " + paymentGroup.getPayeeId());
                if (cuPdpEmployeeService.shouldPayeeBeProcessedAsEmployeeForThisCustomer(paymentFile)) {
                    final Person employee = findPerson(paymentGroup.getPayeeId());
                    if (ObjectUtils.isNull(employee)) {
                        LOG.error("processGroupValidation, unable to get a person from the employee id");
                        errorMap.putError(KFSConstants.GLOBAL_ERRORS, CUPdpKeyConstants.ERROR_PDP_PAYMENTLOAD_INVALID_EMPLOYEE_ID, paymentGroup.getPayeeId());
                    }
                }
            }
            
            // validate bank
            final String bankCode = paymentGroup.getBankCode();
            if (StringUtils.isNotBlank(bankCode)) {
                final Bank bank = bankService.getByPrimaryId(bankCode);
                if (bank == null) {
                    LOG.debug("processGroupValidation, no bank");
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_BANK_CODE, Integer.toString(groupCount), bankCode);
                }
                else if (!bank.isActive()) {
                    LOG.debug("processGroupValidation, bank isn't active");
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INACTIVE_BANK_CODE, Integer.toString(groupCount), bankCode);
                }
            }

            KualiDecimal groupTotal = KualiDecimal.ZERO;
            for (final PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
                detailCount++;

                // CU Customization: Check max lengths on Payment Detail properties.
                checkPaymentDetailPropertyMaxLength(paymentDetail, errorMap);

                // Add a line to print the invoice number
                noteLineCount++;
                
                noteLineCount = noteLineCount + paymentDetail.getNotes().size();

                if (paymentDetail.getNetPaymentAmount() == null && !paymentDetail.isDetailAmountProvided()) {
                    paymentDetail.setNetPaymentAmount(paymentDetail.getAccountTotal());
                }
                else if (paymentDetail.getNetPaymentAmount() == null && paymentDetail.isDetailAmountProvided()) {
                    paymentDetail.setNetPaymentAmount(paymentDetail.getCalculatedPaymentAmount());
                }

                // compare net to accounting segments
                if (paymentDetail.getAccountTotal().compareTo(paymentDetail.getNetPaymentAmount()) != 0) {
                    LOG.debug("processGroupValidation, account total (" + paymentDetail.getAccountTotal()  + ") not equal to net amount total (" + paymentDetail.getNetPaymentAmount() + ")");
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_DETAIL_TOTAL_MISMATCH, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getAccountTotal().toString(), paymentDetail.getNetPaymentAmount().toString());
                }

                // validate origin code if given
                if (StringUtils.isNotBlank(paymentDetail.getFinancialSystemOriginCode())) {
                    final OriginationCode originationCode = originationCodeService.getByPrimaryKey(paymentDetail.getFinancialSystemOriginCode());
                    if (originationCode == null) {
                        LOG.debug("processGroupValidation, origination code is null");
                        errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_ORIGIN_CODE, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getFinancialSystemOriginCode());
                    }
                }

                // validate doc type if given
                if (StringUtils.isNotBlank(paymentDetail.getFinancialDocumentTypeCode())) {
                    if ( !documentTypeService.isActiveByName(paymentDetail.getFinancialDocumentTypeCode()) ) {
                        LOG.debug("processGroupValidation, " + paymentDetail.getFinancialDocumentTypeCode() + " is not active.");
                        errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_DOC_TYPE, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getFinancialDocumentTypeCode());
                    }
                }

                groupTotal = groupTotal.add(paymentDetail.getNetPaymentAmount());
            }

            // verify total for group is not negative
            if (groupTotal.doubleValue() < 0) {
                LOG.debug("processGroupValidation, group total less than zero");
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_NEGATIVE_GROUP_TOTAL, Integer.toString(groupCount));
            }

            // check that the number of detail items and note lines will fit on a check stub
            if (noteLineCount > getMaxNoteLines()) {
                LOG.debug("processGroupValidation, too many notes");
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_MAX_NOTE_LINES, Integer.toString(groupCount), Integer.toString(noteLineCount), Integer.toString(getMaxNoteLines()));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("After processGroupValidation: " + printErrorMap(errorMap));
            }
        }
    }
    
    private Person findPerson(final String employeeId) {
        Person person = null;
        if (StringUtils.isNotBlank(employeeId)) {
            try {
                person = personService.getPersonByEmployeeId(employeeId);
            } catch (final Exception e) {
                LOG.error("findPerson, Unable to build a person from employee ID: " + employeeId, e);
            }
        }
        return person;
    }
    
    public String printErrorMap(final MessageMap errorMap) {
        final StringBuilder sb = new StringBuilder();
        final Set<String> keys = errorMap.getErrorMessages().keySet();
        if (keys.size() > 0) {
            for (final String key : keys) {
                final AutoPopulatingList<ErrorMessage> errors = errorMap.getErrorMessages().get(key);
                for (final ErrorMessage error : errors) {
                    sb.append("  Key: ").append(key).append("  error: ").append(error.toString());
                }
            }
        } else {
            sb.append("No errors");
        }
        return sb.toString();
    }
    
    @Override
    public void doHardEdits(final PaymentFileLoad paymentFile, final MessageMap errorMap) {
        super.doHardEdits(paymentFile, errorMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After doHardEdits: " + printErrorMap(errorMap));
        }
    }

    @Override
    protected void processHeaderValidation(final PaymentFileLoad paymentFile, final MessageMap errorMap) {
        super.processHeaderValidation(paymentFile, errorMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After processHeaderValidation: " + printErrorMap(errorMap));
        }
    }

    @Override
    protected void processTrailerValidation(final PaymentFileLoad paymentFile, final MessageMap errorMap) {
        super.processTrailerValidation(paymentFile, errorMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After processTrailerValidation: " + printErrorMap(errorMap));
        }
    }

    @Override
    protected void checkPaymentGroupPropertyMaxLength(final PaymentGroup paymentGroup, final MessageMap errorMap) {
        super.checkPaymentGroupPropertyMaxLength(paymentGroup, errorMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After checkPaymentGroupPropertyMaxLength: " + printErrorMap(errorMap));
        }
    }

    protected void checkPaymentDetailPropertyMaxLength(final PaymentDetail paymentDetail, final MessageMap errorMap) {
        for (final String propertyName : paymentDetailPropertiesToCheckMaxLength) {
            final String propertyValue = (String) ObjectUtils.getPropertyValue(paymentDetail, propertyName);
            if (StringUtils.isNotEmpty(propertyValue)) {
                final Integer maxLength = dataDictionaryService.getAttributeMaxLength(PaymentDetail.class, propertyName);
                if (maxLength == null) {
                    LOG.warn("checkPaymentDetailPropertyMaxLength, PaymentDetail field '" + propertyName
                            + "' does not have a max length specified in the data dictionary");
                } else if (maxLength < propertyValue.length()) {
                    String errorLabel = dataDictionaryService.getAttributeErrorLabel(PaymentDetail.class, propertyName);
                    errorLabel += " with the value '" + propertyValue + "'";
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_MAX_LENGTH,
                            errorLabel, maxLength.toString());
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("After checkPaymentDetailPropertyMaxLength: " + printErrorMap(errorMap));
        }
    }
    
    @Override
    protected void addWarningMessage(final List<String> warnings, final String messageKey, final String... arguments) {
        // Add to global warnings so they will show up on the Payment File Batch Upload screen if
        // the payment file was loaded via that screen
        GlobalVariables.getMessageMap().putWarning(KFSConstants.GLOBAL_MESSAGES, messageKey, arguments);

        final String message = kualiConfigurationService.getPropertyValueAsString(messageKey);
        /*
         * KFSPTS-35165 partial backport of FINP-10357
         */
        warnings.add(MessageFormat.format(message, (Object[]) arguments));
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setCuPdpEmployeeService(final CuPdpEmployeeService cuPdpEmployeeService) {
        this.cuPdpEmployeeService = cuPdpEmployeeService;
    }
    
    public void setPaymentDetailPropertiesToCheckMaxLength(final List<String> paymentDetailPropertiesToCheckMaxLength) {
        this.paymentDetailPropertiesToCheckMaxLength = paymentDetailPropertiesToCheckMaxLength;
    }
}
