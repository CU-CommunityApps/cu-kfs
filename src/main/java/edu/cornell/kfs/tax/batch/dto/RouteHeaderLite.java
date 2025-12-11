package edu.cornell.kfs.tax.batch.dto;

import java.sql.Timestamp;

import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class RouteHeaderLite {

    private String documentNumber;
    private String initiatorPrincipalId;
    private String docRouteStatus;
    private Timestamp finalizedDate;
    private String title;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public enum RouteHeaderField implements TaxDtoFieldEnum {
        documentNumber,
        initiatorPrincipalId,
        docRouteStatus,
        finalizedDate,
        title;

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return DocumentRouteHeaderValue.class;
        }
    }

}
