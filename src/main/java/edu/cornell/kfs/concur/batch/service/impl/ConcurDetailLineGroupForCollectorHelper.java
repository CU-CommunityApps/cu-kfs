package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;

/**
 * Helper class providing various utility methods and properties,
 * intended for use by the ConcurDetailLineGroupForCollector class.
 */
public class ConcurDetailLineGroupForCollectorHelper {

    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected DateTimeService dateTimeService;
    protected Function<String,String> dashPropertyValueGetter;
    protected String actualFinancialBalanceTypeCode;
    protected Date transmissionDate;
    protected String documentTypeCode;
    protected String systemOriginationCode;
    protected String chartCode;
    protected String prepaidOffsetChartCode;
    protected String prepaidOffsetAccountNumber;
    protected String prepaidOffsetObjectCode;
    protected String paymentOffsetObjectCode;
    protected String personalOffsetObjectCode;
    
    protected String atmFeeDebitChartCode;
    protected String atmFeeDebitAccountNumber;
    protected String atmFeeDebitObjectCode;
    protected String atmUnusedAmountOffsetObjectCode;

    public ConcurDetailLineGroupForCollectorHelper(String actualFinancialBalanceTypeCode, Date transmissionDate,
            ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService,
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService,
            ConfigurationService configurationService, ConcurBatchUtilityService concurBatchUtilityService, DateTimeService dateTimeService,
            Function<String,String> dashPropertyValueGetter, Function<String,String> concurParameterGetter) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
        this.configurationService = configurationService;
        this.concurBatchUtilityService = concurBatchUtilityService;
        this.dateTimeService = dateTimeService;
        this.dashPropertyValueGetter = dashPropertyValueGetter;
        this.actualFinancialBalanceTypeCode = actualFinancialBalanceTypeCode;
        this.transmissionDate = transmissionDate;
        
        this.documentTypeCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE);
        this.systemOriginationCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE);
        this.chartCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.prepaidOffsetChartCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_CHART_CODE);
        this.prepaidOffsetAccountNumber = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER);
        this.prepaidOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE);
        this.paymentOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PAYMENT_OFFSET_OBJECT_CODE);
        this.personalOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE);
        
        this.atmFeeDebitChartCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_ATM_FEE_DEBIT_CHART_CODE);
        this.atmFeeDebitAccountNumber = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_ATM_FEE_DEBIT_ACCOUNT_NUMBER);
        this.atmFeeDebitObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_ATM_FEE_DEBIT_OBJECT_CODE);
        this.atmUnusedAmountOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_UNUSED_ATM_OFFSET_OBJECT_CODE);
        
    }

    public String getActualFinancialBalanceTypeCode() {
        return actualFinancialBalanceTypeCode;
    }

    public Date getTransmissionDate() {
        return transmissionDate;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public String getSystemOriginationCode() {
        return systemOriginationCode;
    }

    public String getChartCode() {
        return chartCode;
    }

    public String getPrepaidOffsetChartCode() {
        return prepaidOffsetChartCode;
    }

    public String getPrepaidOffsetAccountNumber() {
        return prepaidOffsetAccountNumber;
    }

    public String getPrepaidOffsetObjectCode() {
        return prepaidOffsetObjectCode;
    }

    public String getPaymentOffsetObjectCode() {
        return paymentOffsetObjectCode;
    }

    public String getPersonalOffsetObjectCode() {
        return personalOffsetObjectCode;
    }

    public String getAtmFeeDebitChartCode() {
        return atmFeeDebitChartCode;
    }

    public String getAtmFeeDebitAccountNumber() {
        return atmFeeDebitAccountNumber;
    }

    public String getAtmFeeDebitObjectCode() {
        return atmFeeDebitObjectCode;
    }

    public String getAtmUnusedAmountOffsetObjectCode() {
        return atmUnusedAmountOffsetObjectCode;
    }

    public String getDashOnlyPropertyValue(String propertyName) {
        return dashPropertyValueGetter.apply(propertyName);
    }

    public String formatDate(Date value, String format) {
        return dateTimeService.toString(value, format);
    }

    public ConcurRequestedCashAdvance getRequestedCashAdvanceByCashAdvanceKey(String cashAdvanceKey) {
        Collection<ConcurRequestedCashAdvance> requestedCashAdvances = concurRequestedCashAdvanceService
                .findConcurRequestedCashAdvanceByCashAdvanceKey(cashAdvanceKey);
        if (CollectionUtils.size(requestedCashAdvances) == 1) {
            return requestedCashAdvances.iterator().next();
        } else {
            return null;
        }
    }

    public boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(detailLine);
    }

    public boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurStandardAccountingExtractCashAdvanceService.isAtmCashAdvanceLine(detailLine);
    }

    public boolean isAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurStandardAccountingExtractCashAdvanceService.isAtmFeeDebitLine(detailLine);
    }

    public boolean isAtmFeeCreditLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurStandardAccountingExtractCashAdvanceService.isAtmFeeCreditLine(detailLine);
    }

    public boolean isAtmCashAdvanceLineWithUnusedAmount(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurStandardAccountingExtractCashAdvanceService.isAtmCashAdvanceLineWithUnusedAmount(detailLine);
    }

    public String getValidationMessage(String messageKey) {
        return configurationService.getPropertyValueAsString(messageKey);
    }

    public String getFormattedValidationMessage(String messageKey, Object... messageArguments) {
        String validationMessagePattern = getValidationMessage(messageKey);
        return MessageFormat.format(validationMessagePattern, messageArguments);
    }

    public boolean lineRepresentsPersonalExpenseChargedToCorporateCard(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurBatchUtilityService.lineRepresentsPersonalExpenseChargedToCorporateCard(detailLine);
    }

    public boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurBatchUtilityService.lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(detailLine);
    }

    public boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(ConcurStandardAccountingExtractDetailLine detailLine) {
        return concurBatchUtilityService.lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(detailLine);
    }

}
