package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;

/**
 * Helper class providing various utility methods and properties,
 * intended for use by the ConcurDetailLineGroupForCollector class.
 */
public class ConcurDetailLineGroupForCollectorHelper {

    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected DateTimeService dateTimeService;
    protected Function<String,String> dashPropertyValueGetter;
    protected String actualFinancialBalanceTypeCode;
    protected Date transmissionDate;
    protected String documentTypeCode;
    protected String systemOriginationCode;
    protected String chartCode;
    protected String prepaidOffsetAccountNumber;
    protected String prepaidOffsetObjectCode;
    protected String cashOffsetObjectCode;
    protected String personalOffsetObjectCode;

    public ConcurDetailLineGroupForCollectorHelper(String actualFinancialBalanceTypeCode, Date transmissionDate,
            ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService,
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService, DateTimeService dateTimeService,
            Function<String,String> dashPropertyValueGetter, Function<String,String> concurParameterGetter) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
        this.dateTimeService = dateTimeService;
        this.dashPropertyValueGetter = dashPropertyValueGetter;
        this.actualFinancialBalanceTypeCode = actualFinancialBalanceTypeCode;
        this.transmissionDate = transmissionDate;
        
        this.documentTypeCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE);
        this.systemOriginationCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE);
        this.chartCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.prepaidOffsetAccountNumber = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER);
        this.prepaidOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE);
        this.cashOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CASH_OFFSET_OBJECT_CODE);
        this.personalOffsetObjectCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE);
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

    public String getPrepaidOffsetAccountNumber() {
        return prepaidOffsetAccountNumber;
    }

    public String getPrepaidOffsetObjectCode() {
        return prepaidOffsetObjectCode;
    }

    public String getCashOffsetObjectCode() {
        return cashOffsetObjectCode;
    }

    public String getPersonalOffsetObjectCode() {
        return personalOffsetObjectCode;
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

}
