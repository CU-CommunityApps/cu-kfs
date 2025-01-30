package edu.cornell.kfs.tax.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class TestTaxObject extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private Long ID;
    private String eInvoiceId;
    private KualiDecimal amount;
    private String secretValue;
    private String valueNotNeededByDto;
    private int count;
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

    public String getValueNotNeededByDto() {
        return valueNotNeededByDto;
    }

    public void setValueNotNeededByDto(final String valueNotNeededByDto) {
        this.valueNotNeededByDto = valueNotNeededByDto;
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
