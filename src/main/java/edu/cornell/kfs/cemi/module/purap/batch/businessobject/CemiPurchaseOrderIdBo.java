package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.sql.Timestamp;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CemiPurchaseOrderIdBo extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 8461984766939682784L;

    private String documentNumber;
    private String documentTypeName;
    private String docRouteStatus;
    private Timestamp approvedDate;

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

    public String getDocRouteStatus() {
        return docRouteStatus;
    }

    public void setDocRouteStatus(final String docRouteStatus) {
        this.docRouteStatus = docRouteStatus;
    }

    public Timestamp getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(final Timestamp approvedDate) {
        this.approvedDate = approvedDate;
    }

}
