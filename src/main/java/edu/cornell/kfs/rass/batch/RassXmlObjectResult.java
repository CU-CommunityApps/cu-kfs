package edu.cornell.kfs.rass.batch;

import edu.cornell.kfs.rass.RassConstants.RassResultCode;

public class RassXmlObjectResult {
    private final Class<?> businessObjectClass;
    private final String primaryKey;
    private final String documentId;
    private final RassResultCode resultCode;

    public RassXmlObjectResult(Class<?> businessObjectClass, String primaryKey, RassResultCode resultCode) {
        this(businessObjectClass, primaryKey, null, resultCode);
    }

    public RassXmlObjectResult(Class<?> businessObjectClass, String primaryKey, String documentId, RassResultCode resultCode) {
        this.businessObjectClass = businessObjectClass;
        this.primaryKey = primaryKey;
        this.documentId = documentId;
        this.resultCode = resultCode;
    }

    public Class<?> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getDocumentId() {
        return documentId;
    }

    public RassResultCode getResultCode() {
        return resultCode;
    }

}
