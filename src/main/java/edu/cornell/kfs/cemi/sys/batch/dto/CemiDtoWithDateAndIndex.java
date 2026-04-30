package edu.cornell.kfs.cemi.sys.batch.dto;

import edu.cornell.kfs.cemi.sys.util.CemiDtoIndexer;

public abstract class CemiDtoWithDateAndIndex {

    protected final String jobRunDateString;
    protected final long rowIndex;

    protected CemiDtoWithDateAndIndex(final CemiDtoIndexer indexer) {
        this(indexer.getJobRunDateString(), indexer.getNextRowIndex());
    }

    protected CemiDtoWithDateAndIndex(final String jobRunDateString, final long rowIndex) {
        this.jobRunDateString = jobRunDateString;
        this.rowIndex = rowIndex;
    }

    public String getJobRunDateString() {
        return jobRunDateString;
    }

    public long getRowIndex() {
        return rowIndex;
    }

}
