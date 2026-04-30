package edu.cornell.kfs.cemi.sys.util;

import org.apache.commons.lang3.Validate;

public final class CemiDtoIndexer {

    private final String jobRunDateString;
    private long rowIndex;

    public CemiDtoIndexer(final String jobRunDateString) {
        Validate.notBlank(jobRunDateString, "jobRunDateString cannot be blank");
        this.jobRunDateString = jobRunDateString;
    }

    public String getJobRunDateString() {
        return jobRunDateString;
    }

    public long getCurrentRowIndex() {
        return rowIndex;
    }

    public long getNextRowIndex() {
        rowIndex++;
        return rowIndex;
    }

}
