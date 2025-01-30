package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;
import edu.cornell.kfs.tax.businessobject.TestTaxObject;

@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = TestTaxObject.class)
})
public class TestTaxObjectLite {

    @TaxDtoField
    private Long ID;

    @TaxDtoField
    private String eInvoiceId;

    @TaxDtoField
    private KualiDecimal amount;

    @TaxDtoField
    private String secretValue;

    @TaxDtoField
    private int count;

    @TaxDtoField
    private boolean active;

    public Long getID() {
        return ID;
    }

    public void setID(final Long iD) {
        this.ID = iD;
    }

    public String geteInvoiceId() {
        return eInvoiceId;
    }

    public void seteInvoiceId(final String eInvoiceId) {
        this.eInvoiceId = eInvoiceId;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(final KualiDecimal amount) {
        this.amount = amount;
    }

    public String getSecretValue() {
        return secretValue;
    }

    public void setSecretValue(final String secretValue) {
        this.secretValue = secretValue;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
