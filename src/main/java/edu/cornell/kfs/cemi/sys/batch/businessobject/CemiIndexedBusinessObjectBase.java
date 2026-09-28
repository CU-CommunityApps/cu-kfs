package edu.cornell.kfs.cemi.sys.batch.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public abstract class CemiIndexedBusinessObjectBase extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 2446925945540509482L;

    private String jobRunDateString;
    private Long jobRunRowIndex;

    public String getJobRunDateString() {
        return jobRunDateString;
    }

    public void setJobRunDateString(final String jobRunDateString) {
        this.jobRunDateString = jobRunDateString;
    }

    public Long getJobRunRowIndex() {
        return jobRunRowIndex;
    }

    public void setJobRunRowIndex(final Long jobRunRowIndex) {
        this.jobRunRowIndex = jobRunRowIndex;
    }

}
