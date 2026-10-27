package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierClassificationBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String workdayRefId;
    private String expirationDate;

    public String getWorkdayRefId() {
        return workdayRefId;
    }

    public void setWorkdayRefId(final String workdayRefId) {
        this.workdayRefId = workdayRefId;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final String expirationDate) {
        this.expirationDate = expirationDate;
    }

}
