package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.util.function.Function;

import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.ConcurParameterConstants;

/**
 * Helper class providing various utility methods and properties,
 * intended for use by the ConcurDetailLineGroupForCollector class.
 */
public class ConcurDetailLineGroupForCollectorHelper {

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

    public ConcurDetailLineGroupForCollectorHelper(String actualFinancialBalanceTypeCode, Date transmissionDate,
            DateTimeService dateTimeService, Function<String,String> dashPropertyValueGetter, Function<String,String> parameterService) {
        this.dateTimeService = dateTimeService;
        this.dashPropertyValueGetter = dashPropertyValueGetter;
        this.actualFinancialBalanceTypeCode = actualFinancialBalanceTypeCode;
        this.transmissionDate = transmissionDate;
        
        this.documentTypeCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE);
        this.systemOriginationCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE);
        this.chartCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.prepaidOffsetAccountNumber = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER);
        this.prepaidOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE);
        this.cashOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CASH_OFFSET_OBJECT_CODE);
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

    public String getDashOnlyPropertyValue(String propertyName) {
        return dashPropertyValueGetter.apply(propertyName);
    }

    public String formatDate(Date value, String format) {
        return dateTimeService.toString(value, format);
    }

}
