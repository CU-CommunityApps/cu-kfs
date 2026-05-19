package edu.cornell.kfs.cemi.sys.batch.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public abstract class CemiTempIndexedBusinessObjectBase extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 3833441688877327946L;

    private String jobRunDate;
    private Long rowIndex;

    public String getJobRunDate() {
        return jobRunDate;
    }

    public void setJobRunDate(final String jobRunDate) {
        this.jobRunDate = jobRunDate;
    }

    public Long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(final Long rowIndex) {
        this.rowIndex = rowIndex;
    }

}
