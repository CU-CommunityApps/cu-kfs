package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;

import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public class AuxiliaryVoucherDocumentGenerator
        extends AccountingDocumentGeneratorBase<AuxiliaryVoucherDocument> {

    protected AccountingPeriodService accountingPeriodService;

    public AuxiliaryVoucherDocumentGenerator() {
        super();
    }

    public AuxiliaryVoucherDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends AuxiliaryVoucherDocument> getDocumentClass() {
        return AuxiliaryVoucherDocument.class;
    }

    @Override
    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A accountingLine = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        validateAndPopulateAccountingLineWithDebitCreditAmounts(accountingLine, xmlLine);
        return accountingLine;
    }

    protected <A extends AccountingLine> void validateAndPopulateAccountingLineWithDebitCreditAmounts(
            A accountingLine, AccountingXmlDocumentAccountingLine xmlLine) {
        if (hasAmountBeenSpecified(xmlLine.getAmount())) {
            throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_INVALID_AMOUNT_TYPE);
        }
        
        KualiDecimal debitAmount = xmlLine.getDebitAmount();
        KualiDecimal creditAmount = xmlLine.getCreditAmount();
        if (hasAmountBeenSpecified(debitAmount)) {
            if (hasAmountBeenSpecified(creditAmount)) {
                throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_DEBIT_AND_CREDIT_AMOUNT);
            }
            accountingLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            accountingLine.setAmount(debitAmount);
        } else if (hasAmountBeenSpecified(creditAmount)) {
            accountingLine.setDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            accountingLine.setAmount(creditAmount);
        } else {
            throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_AMOUNT_REQUIRED);
        }
    }

    protected boolean hasAmountBeenSpecified(KualiDecimal amount) {
        return amount != null && !KualiDecimal.ZERO.equals(amount);
    }

    @Override
    protected void populateCustomAccountingDocumentData(AuxiliaryVoucherDocument document, AccountingXmlDocumentEntry documentEntry) {
        DateTime documentCreateDate = getDocumentCreateDate(document);
        Date documentCreateDateWithoutTime = new Date(documentCreateDate.getMillis());
        
        AccountingPeriod period = findEligibleAccountingPeriod(document, documentEntry);
        document.setAccountingPeriod(period);
        
        String avTypeCode = validateAndGetAuxiliaryVoucherType(documentEntry);
        document.setTypeCode(avTypeCode);
        
        Optional<Date> reversalDate = validateAndGetReversalDateIfApplicable(
                documentEntry, period, avTypeCode, documentCreateDateWithoutTime);
        if (reversalDate.isPresent()) {
            document.setReversalDate(reversalDate.get());
        }
    }

    protected DateTime getDocumentCreateDate(AuxiliaryVoucherDocument document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return workflowDocument.getDateCreated();
    }

    /*
     * NOTE: Much of this period-searching logic has been duplicated and modified from AuxiliaryVoucherForm.
     * If KualiCo updates the related base code to be more micro-test-friendly, then we should update
     * the code below accordingly.
     */
    protected AccountingPeriod findEligibleAccountingPeriod(
            AuxiliaryVoucherDocument document, AccountingXmlDocumentEntry documentEntry) {
        String xmlPeriod = checkForNonBlankValue(
                documentEntry.getAccountingPeriod(), CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_PERIOD_REQUIRED);
        Collection<AccountingPeriod> openPeriods = accountingPeriodService.getOpenAccountingPeriods();
        return openPeriods.stream()
                .filter(period -> periodMatchesXmlConfiguredPeriod(period, xmlPeriod))
                .findFirst()
                .orElseThrow(() -> buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_PERIOD_NOT_FOUND, xmlPeriod));
    }

    protected boolean periodMatchesXmlConfiguredPeriod(AccountingPeriod period, String xmlPeriod) {
        return StringUtils.equals(period.getUniversityFiscalPeriodName(), xmlPeriod);
    }

    protected String validateAndGetAuxiliaryVoucherType(AccountingXmlDocumentEntry documentEntry) {
        String avTypeCode = checkForNonBlankValue(
                documentEntry.getAuxiliaryVoucherType(), CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_TYPE_REQUIRED);
        switch (avTypeCode) {
            case KFSConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE :
            case KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE :
                return avTypeCode;
            case KFSConstants.AuxiliaryVoucher.RECODE_DOC_TYPE :
                throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_INVALID_RECODE_TYPE, avTypeCode);
            default :
                throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_INVALID_TYPE, avTypeCode);
        }
    }

    protected Optional<Date> validateAndGetReversalDateIfApplicable(
            AccountingXmlDocumentEntry documentEntry, AccountingPeriod period, String avTypeCode, Date documentCreateDateWithoutTime) {
        switch (avTypeCode) {
            case KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE :
                return Optional.of(validateAndGetReversalDate(documentEntry, period, avTypeCode, documentCreateDateWithoutTime));
            default :
                if (documentEntry.getReversalDate() != null) {
                    throw buildNewValidationException(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_AV_INVALID_REVERSAL_FOR_TYPE, avTypeCode);
                }
                return Optional.empty();
        }
    }

    protected Date validateAndGetReversalDate(
            AccountingXmlDocumentEntry documentEntry, AccountingPeriod period, String avTypeCode, Date documentCreateDateWithoutTime) {
        java.util.Date xmlReversalDate = documentEntry.getReversalDate();
        if (xmlReversalDate != null) {
            return new java.sql.Date(xmlReversalDate.getTime());
        } else {
            return accountingPeriodService.getAccountingPeriodReversalDateByType(
                    avTypeCode, period.getUniversityFiscalPeriodCode(), period.getUniversityFiscalYear(), documentCreateDateWithoutTime);
        }
    }

    protected String checkForNonBlankValue(String value, String messageKey) throws ValidationException {
        if (StringUtils.isBlank(value)) {
            throw buildNewValidationException(messageKey);
        }
        return value;
    }

    protected ValidationException buildNewValidationException(String messageKey, Object... messageArguments) {
        String message = configurationService.getPropertyValueAsString(messageKey);
        String formattedMessage = MessageFormat.format(message, messageArguments);
        return new ValidationException(formattedMessage);
    }

    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

}
