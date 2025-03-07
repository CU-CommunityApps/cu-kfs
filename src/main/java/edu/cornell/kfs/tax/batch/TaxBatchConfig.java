package edu.cornell.kfs.tax.batch;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.util.TaxUtils;

public final class TaxBatchConfig {

    public enum Mode {
        CREATE_TAX_FILES,
        CREATE_TRANSACTION_LIST_FILE;
    }

    private final Mode mode;
    private final String taxType;
    private final int reportYear;
    private final java.util.Date processingStartDate;
    private final java.sql.Date startDate;
    private final java.sql.Date endDate;

    public TaxBatchConfig(final String taxType, final int reportYear,
            final java.util.Date processingStartDate, final java.sql.Date startDate, final java.sql.Date endDate) {
        this(Mode.CREATE_TAX_FILES, taxType, reportYear, processingStartDate, startDate, endDate);
    }

    public TaxBatchConfig(final Mode mode, final String taxType, final int reportYear,
            final java.util.Date processingStartDate, final java.sql.Date startDate, final java.sql.Date endDate) {
        Validate.notNull(mode, "mode cannot be null");
        Validate.notBlank(taxType, "taxType cannot be blank");
        Validate.notNull(processingStartDate, "processingStartDate cannot be null");
        Validate.notNull(startDate, "startDate cannot be null");
        Validate.notNull(endDate, "endDate cannot be null");
        this.mode = mode;
        this.taxType = taxType;
        this.reportYear = reportYear;
        this.processingStartDate = TaxUtils.copyDate(processingStartDate);
        this.startDate = TaxUtils.copyDate(startDate);
        this.endDate = TaxUtils.copyDate(endDate);
    }

    public TaxBatchConfig withMode(final Mode newMode) {
        return new TaxBatchConfig(newMode, taxType, reportYear, processingStartDate, startDate, endDate);
    }

    public Mode getMode() {
        return mode;
    }

    public String getTaxType() {
        return taxType;
    }

    public int getReportYear() {
        return reportYear;
    }

    public java.util.Date getProcessingStartDate() {
        return TaxUtils.copyDate(processingStartDate);
    }

    public java.sql.Date getStartDate() {
        return TaxUtils.copyDate(startDate);
    }

    public java.sql.Date getEndDate() {
        return TaxUtils.copyDate(endDate);
    }

}
