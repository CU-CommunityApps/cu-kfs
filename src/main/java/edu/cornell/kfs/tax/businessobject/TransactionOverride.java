package edu.cornell.kfs.tax.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/**
 * Business object that overrides the default 1099/1042S tax bucket mapping
 * for transaction detail lines matching the given date, doc number,
 * and line number.
 * 
 * universityDate, taxType, documentNumber, and financialDocumentLineNumber
 * form the primary key.
 */
public class TransactionOverride extends PersistableBusinessObjectBase implements MutableInactivatable {
    private static final long serialVersionUID = 7708933392082384455L;

    private java.sql.Date universityDate;
    private String taxType;
    private String documentNumber;
    private Integer financialDocumentLineNumber;
    private String boxNumber;
    private String formType;
    private boolean active;

    public TransactionOverride() {
        super();
    }



    /**
     * Returns the date that the overridden transaction was paid.
     */
    public java.sql.Date getUniversityDate() {
        return universityDate;
    }

    public void setUniversityDate(java.sql.Date universityDate) {
        this.universityDate = universityDate;
    }

    /**
     * Returns the type of tax reporting that this override applies to; typically "1099" or "1042S".
     */
    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    /**
     * Returns the document number of the transaction to override.
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Returns the line number of the transaction to override.
     */
    public Integer getFinancialDocumentLineNumber() {
        return financialDocumentLineNumber;
    }

    public void setFinancialDocumentLineNumber(Integer financialDocumentLineNumber) {
        this.financialDocumentLineNumber = financialDocumentLineNumber;
    }

    /**
     * Returns the box number to use as the override for the matching transaction.
     */
    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

}
