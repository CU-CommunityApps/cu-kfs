package edu.cornell.kfs.tax.batch;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Validate;

public class TaxOutputConfig {

    private final String taxType;
    private final int reportYear;
    private final Date processingStartDate;
    private final List<String> taxOutputFileTypes;

    public TaxOutputConfig(final String taxType, final int reportYear,
            final Date processingStartDate, final String... taxOutputFileTypes) {
        Validate.notBlank(taxType, "taxType cannot be blank");
        Validate.notNull(processingStartDate, "processingStartDate cannot be null");
        Validate.notEmpty(taxOutputFileTypes, "taxOutputFileTypes cannot be null or empty");
        this.taxType = taxType;
        this.reportYear = reportYear;
        this.processingStartDate = processingStartDate;
        this.taxOutputFileTypes = List.of(taxOutputFileTypes);
    }

    public String getTaxType() {
        return taxType;
    }

    public int getReportYear() {
        return reportYear;
    }

    public Date getProcessingStartDate() {
        return new Date(processingStartDate.getTime());
    }

    public List<String> getTaxOutputFileTypes() {
        return taxOutputFileTypes;
    }

}
