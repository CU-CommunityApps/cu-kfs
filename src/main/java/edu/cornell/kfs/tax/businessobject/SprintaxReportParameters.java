package edu.cornell.kfs.tax.businessobject;

public class SprintaxReportParameters {

    private final java.sql.Date startDate;
    private final java.sql.Date endDate;
    private final int reportYear;
    private final java.util.Date jobRunDate;

    public SprintaxReportParameters(java.sql.Date startDate, java.sql.Date endDate, int reportYear, java.util.Date jobRunDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportYear = reportYear;
        this.jobRunDate = jobRunDate;
    }

    public java.sql.Date getStartDate() {
        return startDate;
    }

    public java.sql.Date getEndDate() {
        return endDate;
    }

    public int getReportYear() {
        return reportYear;
    }

    public java.util.Date getJobRunDate() {
        return jobRunDate;
    }

}
