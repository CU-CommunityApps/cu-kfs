package edu.cornell.kfs.tax.batch.dto;

import java.sql.Timestamp;

import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class RouteHeaderLite {

    private String documentNumber;
    private String initiatorPrincipalId;
    private String docRouteStatus;
    private Timestamp finalizedDate;
    private String docTitle;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

     public String getInitiatorPrincipalId() {
        return initiatorPrincipalId;
    }

    public void setInitiatorPrincipalId(final String initiatorPrincipalId) {
        this.initiatorPrincipalId = initiatorPrincipalId;
    }

    public String getDocRouteStatus() {
        return docRouteStatus;
    }

    public void setDocRouteStatus(final String docRouteStatus) {
        this.docRouteStatus = docRouteStatus;
    }

    public Timestamp getFinalizedDate() {
        return finalizedDate;
    }

    public java.sql.Date getFinalizedDateAsSqlDate() {
        return finalizedDate != null ? new java.sql.Date(finalizedDate.getTime()) : null;
    }

    public void setFinalizedDate(final Timestamp finalizedDate) {
        this.finalizedDate = finalizedDate;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(final String docTitle) {
        this.docTitle = docTitle;
    }

    public enum RouteHeaderField implements TaxDtoFieldEnum {
        documentNumber(CUKFSPropertyConstants.DOCUMENT_ID),
        initiatorPrincipalId(CUKFSPropertyConstants.INITIATOR_WORKFLOW_ID),
        docRouteStatus,
        finalizedDate,
        docTitle;

        private final String boFieldName;

        private RouteHeaderField() {
            this.boFieldName = name();
        }

        private RouteHeaderField(final String boFieldName) {
            this.boFieldName = boFieldName;
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return DocumentRouteHeaderValue.class;
        }

        @Override
        public String getBusinessObjectFieldName() {
            return boFieldName;
        }
    }

}
