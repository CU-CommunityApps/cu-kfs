package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

// REMOVE THIS vnd SPECIFIC PACKAGE CLASS AFTER ALL OF THESE CLASSES AT LOCATIONS LISTED HAVE BEEN REFACTORED: 
// HERE edu/cornell/kfs/cemi/vnd/batch/businessobject:
//      CemiOrderFromSupplierBo
//      CemiRemitToSupplierBo
//      CemiSupplierAddressBo
//      CemiSupplierBo
//      CemiSupplierEmailBo
// AND HERE edu.cornell.kfs.cemi.vnd.batch.service.impl
//      CemiOrmDataBuilderBase

public abstract class CemiIndexedBusinessObjectBase extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 2446925945540509482L;

    private String jobRunDate;
    private Long jobRunRowIndex;

    public String getJobRunDate() {
        return jobRunDate;
    }

    public void setJobRunDate(final String jobRunDate) {
        this.jobRunDate = jobRunDate;
    }

    public Long getJobRunRowIndex() {
        return jobRunRowIndex;
    }

    public void setJobRunRowIndex(final Long jobRunRowIndex) {
        this.jobRunRowIndex = jobRunRowIndex;
    }

}
