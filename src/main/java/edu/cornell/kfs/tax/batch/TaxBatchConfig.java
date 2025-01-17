package edu.cornell.kfs.tax.batch;

import org.apache.commons.lang3.Validate;

public final class TaxBatchConfig {

    private final String taxType;
    private final int reportYear;
    private final java.util.Date processingStartDate;
    private final java.sql.Date startDate;
    private final java.sql.Date endDate;

    public TaxBatchConfig(final String taxType, final int reportYear,
            final java.util.Date processingStartDate, final java.sql.Date startDate, final java.sql.Date endDate) {
        Validate.notBlank(taxType, "taxType cannot be blank");
        Validate.notNull(processingStartDate, "processingStartDate cannot be null");
        Validate.notNull(startDate, "startDate cannot be null");
        Validate.notNull(endDate, "endDate cannot be null");
        this.taxType = taxType;
        this.reportYear = reportYear;
        this.processingStartDate = processingStartDate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTaxType() {
        return taxType;
    }

    public int getReportYear() {
        return reportYear;
    }

    public java.util.Date getProcessingStartDate() {
        return new java.util.Date(processingStartDate.getTime());
    }

    public java.sql.Date getStartDate() {
        return new java.sql.Date(startDate.getTime());
    }

    public java.sql.Date getEndDate() {
        return new java.sql.Date(endDate.getTime());
    }

}
