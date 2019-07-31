package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;

public class RassBusinessObjectUpdateResult<R extends PersistableBusinessObject> {
    private final Class<R> businessObjectClass;
    private final String primaryKey;
    private final String documentId;
    private final RassObjectUpdateResultCode resultCode;
    private final String errorMessage;

    public RassBusinessObjectUpdateResult(Class<R> businessObjectClass, String primaryKey,
            RassObjectUpdateResultCode resultCode) {
        this(businessObjectClass, primaryKey, null, resultCode, null);
    }
    
    public RassBusinessObjectUpdateResult(Class<R> businessObjectClass, String primaryKey,
            RassObjectUpdateResultCode resultCode, String errorMessage) {
        this(businessObjectClass, primaryKey, null, resultCode, errorMessage);
    }

    public RassBusinessObjectUpdateResult(Class<R> businessObjectClass, String primaryKey, String documentId,
            RassObjectUpdateResultCode resultCode, String errorMessage) {
        this.businessObjectClass = businessObjectClass;
        this.primaryKey = primaryKey;
        this.documentId = documentId;
        this.resultCode = resultCode;
        this.errorMessage = errorMessage;
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

    public String getErrorMessage() {
        return errorMessage;
    }

}
