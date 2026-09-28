package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierTaxIdBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String taxIdType;
    private String taxIdText;
    private String transactionTaxId;
    private String primaryTaxId;
    private String countryTaxId;

    public String getTaxIdType() {
        return taxIdType;
    }

    public void setTaxIdType(final String taxIdType) {
        this.taxIdType = taxIdType;
    }

    public String getTaxIdText() {
        return taxIdText;
    }

    public void setTaxIdText(final String taxIdText) {
        this.taxIdText = taxIdText;
    }

    public String getTransactionTaxId() {
        return transactionTaxId;
    }

    public void setTransactionTaxId(final String transactionTaxId) {
        this.transactionTaxId = transactionTaxId;
    }

    public String getPrimaryTaxId() {
        return primaryTaxId;
    }

    public void setPrimaryTaxId(final String primaryTaxId) {
        this.primaryTaxId = primaryTaxId;
    }

    public String getCountryTaxId() {
        return countryTaxId;
    }

    public void setCountryTaxId(final String countryTaxId) {
        this.countryTaxId = countryTaxId;
    }

}
