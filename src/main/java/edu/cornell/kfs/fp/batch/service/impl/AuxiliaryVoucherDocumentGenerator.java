package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.AuxiliaryVoucherDocumentRuleConstants;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public class AuxiliaryVoucherDocumentGenerator
        extends AccountingDocumentGeneratorBase<AuxiliaryVoucherDocument> {

    protected AccountingPeriodService accountingPeriodService;
    protected UniversityDateService universityDateService;
    protected ParameterEvaluatorService parameterEvaluatorService;
    protected ParameterService parameterService;
    protected DateTimeService dateTimeService;

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
            throw new ValidationException(
                    "AV accounting line cannot use the 'amount' element to specify an amount; use the relevant debit/credit amount element instead");
        }
        
        KualiDecimal debitAmount = xmlLine.getDebitAmount();
        KualiDecimal creditAmount = xmlLine.getCreditAmount();
        if (hasAmountBeenSpecified(debitAmount)) {
            if (hasAmountBeenSpecified(creditAmount)) {
                throw new ValidationException("AV accounting line cannot specify both a debit amount and a credit amount");
            }
            accountingLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            accountingLine.setAmount(debitAmount);
        } else if (hasAmountBeenSpecified(creditAmount)) {
            accountingLine.setDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            accountingLine.setAmount(creditAmount);
        } else {
            throw new ValidationException("AV accounting line did not specify a debit or credit amount");
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
                documentEntry.getAccountingPeriod(), "AV document accounting period cannot be blank");
        Date currentDate = dateTimeService.getCurrentSqlDate();
        Integer currentFiscalYear = universityDateService.getCurrentFiscalYear();
        AccountingPeriod currentPeriod = accountingPeriodService.getByDate(currentDate);
        Collection<AccountingPeriod> openPeriods = accountingPeriodService.getOpenAccountingPeriods();
        return openPeriods.stream()
                .filter(this::periodIsNotRestrictedForAVs)
                .filter(period -> periodHasNotEnded(period, currentPeriod, currentFiscalYear)
                        || isInGracePeriod(period, currentDate, document))
                .filter(period -> periodMatchesXmlConfiguredPeriod(period, xmlPeriod))
                .findFirst()
                .orElseThrow(() -> new ValidationException(xmlPeriod + " is not an eligible open accounting period for AV documents"));
    }

    protected boolean periodIsNotRestrictedForAVs(AccountingPeriod period) {
        return parameterEvaluatorService.getParameterEvaluator(
                AuxiliaryVoucherDocument.class, AuxiliaryVoucherDocumentRuleConstants.RESTRICTED_PERIOD_CODES,
                        period.getUniversityFiscalPeriodCode())
                .evaluationSucceeds();
    }

    protected boolean periodHasNotEnded(AccountingPeriod period, AccountingPeriod currentPeriod, Integer currentFiscalYear) {
        return period.getUniversityFiscalYear().equals(currentFiscalYear)
                && accountingPeriodService.compareAccountingPeriodsByDate(period, currentPeriod) >= 0;
    }

    /*
     * NOTE: This is a duplicate of the logic from AuxiliaryVoucherDocument.calculateIfWithinGracePeriod,
     * but has been updated accordingly so that it can execute in micro-test situations.
     * If KualiCo updates the base method to be micro-test-friendly, then we can potentially remove this custom method.
     */
    protected boolean isInGracePeriod(AccountingPeriod period, Date currentDate, AuxiliaryVoucherDocument document) {
        Date periodEndDate = period.getUniversityFiscalPeriodEndDate();
        int today = document.comparableDateForm(currentDate);
        int periodBegin = document.comparableDateForm(
                document.calculateFirstDayOfMonth(periodEndDate));
        int periodClose = document.comparableDateForm(periodEndDate);
        int gracePeriodClose = periodClose + findAVGracePeriodInDays();
        return today >= periodBegin && today <= gracePeriodClose;
    }

    protected int findAVGracePeriodInDays() {
        String gracePeriodInDays = parameterService.getParameterValueAsString(
                AuxiliaryVoucherDocument.class, AuxiliaryVoucherDocumentRuleConstants.AUXILIARY_VOUCHER_ACCOUNTING_PERIOD_GRACE_PERIOD);
        return Integer.parseInt(gracePeriodInDays);
    }

    protected boolean periodMatchesXmlConfiguredPeriod(AccountingPeriod period, String xmlPeriod) {
        return StringUtils.equals(period.getUniversityFiscalPeriodName(), xmlPeriod);
    }

    protected String validateAndGetAuxiliaryVoucherType(AccountingXmlDocumentEntry documentEntry) {
        String avTypeCode = checkForNonBlankValue(
                documentEntry.getAuxiliaryVoucherType(), "AV type cannot be blank");
        switch (avTypeCode) {
            case KFSConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE :
            case KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE :
                return avTypeCode;
            case KFSConstants.AuxiliaryVoucher.RECODE_DOC_TYPE :
                throw new ValidationException("Cannot create XML-based AV documents of type " + avTypeCode);
            default :
                throw new ValidationException("Invalid AV document type: " + avTypeCode);
        }
    }

    protected Optional<Date> validateAndGetReversalDateIfApplicable(
            AccountingXmlDocumentEntry documentEntry, AccountingPeriod period, String avTypeCode, Date documentCreateDateWithoutTime) {
        switch (avTypeCode) {
            case KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE :
                return Optional.of(validateAndGetReversalDate(documentEntry, period, avTypeCode, documentCreateDateWithoutTime));
            default :
                if (documentEntry.getReversalDate() != null) {
                    throw new ValidationException("Cannot specify a reversal date for AV documents of type " + avTypeCode);
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

    protected String checkForNonBlankValue(String value, String errorMessage) throws ValidationException {
        if (StringUtils.isBlank(value)) {
            throw new ValidationException(errorMessage);
        }
        return value;
    }

    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
