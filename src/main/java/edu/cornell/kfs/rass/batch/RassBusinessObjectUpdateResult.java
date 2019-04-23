package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;

public class RassBusinessObjectUpdateResult<R extends PersistableBusinessObject> {
    private final Class<R> businessObjectClass;
    private final String primaryKey;
    private final String documentId;
    private final RassObjectUpdateResultCode resultCode;

    public RassBusinessObjectUpdateResult(Class<R> businessObjectClass, String primaryKey,
            RassObjectUpdateResultCode resultCode) {
        this(businessObjectClass, primaryKey, null, resultCode);
    }

    public RassBusinessObjectUpdateResult(Class<R> businessObjectClass, String primaryKey, String documentId,
            RassObjectUpdateResultCode resultCode) {
        this.businessObjectClass = businessObjectClass;
        this.primaryKey = primaryKey;
        this.documentId = documentId;
        this.resultCode = resultCode;
    }

    public Class<R> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getDocumentId() {
        return documentId;
    }

    public RassObjectUpdateResultCode getResultCode() {
        return resultCode;
    }

}
