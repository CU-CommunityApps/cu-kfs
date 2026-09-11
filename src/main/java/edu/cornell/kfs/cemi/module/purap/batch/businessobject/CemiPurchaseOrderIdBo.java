package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CemiPurchaseOrderIdBo extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 8461984766939682784L;

    private String documentNumber;
    private String documentTypeName;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(final String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

}
